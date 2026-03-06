package com.oriole.wisepen.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.system.api.domain.dto.MailSendDTO;
import com.oriole.wisepen.system.api.feign.RemoteMailService;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.domain.dto.req.AuthRegisterRequest;
import com.oriole.wisepen.user.api.domain.dto.req.AuthPwdResetRequest;
import com.oriole.wisepen.user.api.domain.dto.req.AuthPwdResetVerifyRequest;
import com.oriole.wisepen.user.api.domain.dto.UserInfoDTO;
import com.oriole.wisepen.user.api.enums.Status;
import com.oriole.wisepen.user.domain.entity.UserEntity;
import com.oriole.wisepen.user.domain.entity.UserProfileEntity;
import com.oriole.wisepen.user.domain.entity.UserTokenPoolEntity;
import com.oriole.wisepen.user.exception.UserErrorCode;
import com.oriole.wisepen.user.mapper.UserWalletsMapper;
import com.oriole.wisepen.user.service.UserService;
import com.oriole.wisepen.user.mapper.UserMapper;
import com.oriole.wisepen.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserWalletsMapper userWalletsMapper;

    @Autowired
    StringRedisTemplate redisTemplate;

    private final TemplateEngine templateEngine;

    private final RemoteMailService remoteMailService;

    @Override
    public UserEntity getUserCoreInfoByAccount(String account) {
        return userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
                .and(w -> w.eq(UserEntity::getUsername, account).or().eq(UserEntity::getCampusNo, account))
                .last("LIMIT 1"));
    }

    @Override
    public UserDisplayBase getUserDisplayInfoById(Long userId) {
        if (userId == null) {
            throw new ServiceException(UserErrorCode.USERNAME_EXISTED);
        }
        UserEntity userEntity = userMapper.selectById(userId);
        return BeanUtil.copyProperties(userEntity, UserDisplayBase.class);
    }

    @Override
    public Map<Long, UserDisplayBase> getUserDisplayInfoByIds(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> userList = userMapper.selectBatchIds(userIds);

        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyMap();
        }

        return userList.stream().filter(Objects::nonNull).collect(Collectors.toMap(
                UserEntity::getUserId,
                user -> BeanUtil.copyProperties(user, UserDisplayBase.class),
                (existing, replacement) -> existing
        ));
    }


    /**
     * 注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(AuthRegisterRequest registerRequest) {
        // 校验用户名是否存在
        if (userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getUsername, registerRequest.getUsername())) > 0) {
            throw new ServiceException(UserErrorCode.USERNAME_EXISTED);
        }

        // 新建未验证的学生用户
        UserEntity user = UserEntity.builder()
                .username(registerRequest.getUsername())
                .identityType(IdentityType.STUDENT)
                .status(Status.UNIDENTIFIED)
                .build();

        // 加密用户密码
        user.setPassword(BCrypt.hashpw(registerRequest.getPassword()));
        userMapper.insert(user);

        // 新建档案
        UserProfileEntity userProfile = UserProfileEntity.builder()
                .userId(user.getUserId())
                .university("复旦大学")
                .college("复旦大学")
                .build();
        userProfileMapper.insert(userProfile);

        UserTokenPoolEntity userWallets = new UserTokenPoolEntity();
        userWallets.setUserId(user.getUserId());
        userWallets.setTokenLimit(0);
        userWallets.setTokenUsed(0);
        userWalletsMapper.insert(userWallets);
    }

    /**
     * 发送重置邮件
     */
    @Override
    public void sendResetMail(AuthPwdResetVerifyRequest resetRequest) {
        // 查询学号对应用户
        String campusNo = resetRequest.getCampusNo();
        UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getCampusNo, campusNo).last("LIMIT 1"));

        if(user==null){
            log.warn("重置密码申请：学号 {} 不存在，流程静默终止", campusNo);
            return; // 处于安全考虑，不存在也不报错，防止撞库
        }

        String token = IdUtil.fastSimpleUUID();

        String redisKey = "auth:reset:token:" + token;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(user.getUserId()), 15, TimeUnit.MINUTES);

        // 构建重置链接
        String resetLink = "https://wisepen.fudan.edu.cn/reset-pwd?token=" + token;

        // 构建重置邮件
        Context context = new Context();
        context.setVariable("student_id", campusNo);
        context.setVariable("reset_link", resetLink);
        context.setVariable("current_date", DateUtil.now());
        // Thymeleaf 渲染
        String emailContent = templateEngine.process("resetMailTemplate", context);

        // 构造邮件 DTO 并发送
        MailSendDTO mailDTO = MailSendDTO.builder()
                .toEmail(user.getEmail())
                .subject("密码重置申请")
                .content(emailContent) // 传递渲染后的 HTML 字符串
                .build();

        try {
            remoteMailService.sendMail(mailDTO);
            log.info("Email sent. campusNo={}, email={}", campusNo, user.getEmail());
        } catch (Exception e) {
            log.error("Email sending failed.", e);
            throw new ServiceException(UserErrorCode.EMAIL_SEND_ERROR);
        }
    }

    @Override
    public UserInfoDTO getUserInfoById(Long userId) {
        // 查核心账号
        UserEntity user = userMapper.selectById(userId);

        if (user == null) {
            return null;
        }

        // 查档案详情
        UserProfileEntity profile = userProfileMapper.selectById(user.getUserId());

        // 组装 DTO
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtil.copyProperties(user, dto);

        if (profile != null) {
            BeanUtil.copyProperties(profile, dto);
        }

        return dto;
    }

    /**
     * 执行重置密码（通过token）
     */
    @Override
    public void resetPassword(AuthPwdResetRequest resetExecuteRequest){
        String redisKey = "auth:reset:token:" + resetExecuteRequest.getToken();
        String userId = redisTemplate.opsForValue().get(redisKey);

        if(userId == null){
            throw new ServiceException(UserErrorCode.PASSWORD_RESET_FAILED);
        }

        updatePasswordByUserId(userId, resetExecuteRequest.getNewPassword());
        // 成功后立即清理 Token
        redisTemplate.delete(redisKey);
        log.info("用户 {} 密码重置成功", userId);
    }

    boolean updatePasswordByUserId(String userId, String newPassword) {
        UserEntity user = UserEntity.builder()
                .userId(Long.valueOf(userId))
                .password(BCrypt.hashpw(newPassword))
                .updateTime(java.time.LocalDateTime.now())
                .build();
        Integer result = userMapper.updateById(user);
        return result > 0;
    }
}
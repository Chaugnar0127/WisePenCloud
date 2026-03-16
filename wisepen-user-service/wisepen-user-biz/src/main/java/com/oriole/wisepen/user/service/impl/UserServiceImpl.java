package com.oriole.wisepen.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.wisepen.common.core.domain.PageResult;
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
import com.oriole.wisepen.user.cache.RedisCacheManager;
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
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserWalletsMapper userWalletsMapper;
    private final RedisCacheManager redisCacheManager;

    private final TemplateEngine templateEngine;
    private final RemoteMailService remoteMailService;

    @Value("${wisepen.user.default-password:WisePen@123456}")
    private String defaultAdminResetPassword;

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

    @Override
    public void register(AuthRegisterRequest req) {
        // 校验用户名是否存在
        if (userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getUsername, req.getUsername())) > 0) {
            throw new ServiceException(UserErrorCode.USERNAME_EXISTED);
        }

        // 新建未验证的学生用户
        UserEntity user = UserEntity.builder()
                .username(req.getUsername())
                .identityType(IdentityType.STUDENT)
                .status(Status.UNIDENTIFIED)
                .build();

        // 加密用户密码
        user.setPassword(BCrypt.hashpw(req.getPassword()));
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
        userWallets.setTokenBalance(0);
        userWallets.setTokenUsed(0);
        userWalletsMapper.insert(userWallets);
    }

    @Override
    public void sendResetMail(AuthPwdResetVerifyRequest req) {
        // 查询学号对应用户
        String campusNo = req.getCampusNo();
        UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getCampusNo, campusNo).last("LIMIT 1"));

        if(user==null){
            log.warn("重置密码申请：学号 {} 不存在，流程静默终止", campusNo);
            return; // 处于安全考虑，不存在也不报错，防止撞库
        }

        // uid存入Redis
        String token = redisCacheManager.setPwdResetToken(user.getUserId());
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

        // 密码置为空
        dto.setPassword(null);

        return dto;
    }

    // 重置密码
    @Override
    public void resetPassword(AuthPwdResetRequest req){
        Long userId = redisCacheManager.getPwdResetUser(req.getToken());
        if(userId == null){
            throw new ServiceException(UserErrorCode.PASSWORD_RESET_FAILED);
        }

        updatePasswordByUserId(userId, req.getNewPassword());
        log.info("用户 {} 密码重置成功", userId);
    }

    // 修改密码
    public boolean updatePasswordByUserId(Long userId, String newPassword) {
        UserEntity user = UserEntity.builder()
                .userId(userId)
                .password(BCrypt.hashpw(newPassword))
                .updateTime(java.time.LocalDateTime.now())
                .build();
        return userMapper.updateById(user) > 0;
    }

    /**
     * 更新用户资料
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserInfoDTO profileDto) {
        // 加载现有实体
        UserEntity existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new ServiceException(UserErrorCode.USER_NOT_EXIST);
        }
        UserProfileEntity existingProfile = userProfileMapper.selectById(userId);
        if (existingProfile == null) {
            // 若档案不存在则新建一个基础档案
            existingProfile = UserProfileEntity.builder().userId(userId).build();
        }

        IdentityType identity = existingUser.getIdentityType();

        // 复制非空字段到现有实体
        BeanUtil.copyProperties(profileDto, existingUser, CopyOptions.create().setIgnoreNullValue(true));
        BeanUtil.copyProperties(profileDto, existingProfile, CopyOptions.create().setIgnoreNullValue(true));

        // 按身份过滤字段
        if (identity == IdentityType.STUDENT) {
            existingProfile.setAcademicTitle(existingProfile.getAcademicTitle()); // 保持原值（无操作）
        } else if (identity == IdentityType.TEACHER) {
            existingProfile.setMajor(existingProfile.getMajor());
            existingProfile.setClassName(existingProfile.getClassName());
        }

        // 更新两张表
        int r1 = userMapper.updateById(existingUser);
        int r2;
        if (userProfileMapper.selectById(userId) == null) {
            r2 = userProfileMapper.insert(existingProfile);
        } else {
            r2 = userProfileMapper.updateById(existingProfile);
        }

        if (r1 == 0 || r2 == 0) {
            throw new ServiceException(UserErrorCode.UPDATE_FAILED);
        }
    }

    /**
     * 发起邮箱验证
     */
    @Override
    public void initiateEmailVerify(Long userId, int suffixType) {
        // 获取邮箱
        UserEntity user = userMapper.selectById(userId);
        String email = getEmail(suffixType, user);
        String token = redisCacheManager.setEmailVerificationCode(email, userId);
        String content = "请点击该链接进行验证: https://wisepen.fudan.edu.cn/verify-email?token=" + token + "\n(该链接15分钟内有效)";

        MailSendDTO mailDTO = MailSendDTO.builder()
                .toEmail(email)
                .subject("邮箱验证验证码")
                .content(content)
                .build();

        try {
            remoteMailService.sendMail(mailDTO);
            log.info("Verify email sent. userId={}, email={}", userId, email);
        } catch (Exception e) {
            log.error("Verify email sending failed.", e);
            throw new ServiceException(UserErrorCode.EMAIL_SEND_ERROR);
        }
    }

    private static String getEmail(int suffixType, UserEntity user) {
        if (user == null) {
            throw new ServiceException(UserErrorCode.USER_NOT_EXIST);
        }

        if (user.getStatus() != Status.UNIDENTIFIED) {
            throw new ServiceException(UserErrorCode.USER_STATUS_ERROR);
        }

        String campusNo = user.getCampusNo();
        if (campusNo == null) {
            throw new ServiceException(UserErrorCode.USER_CAMPUS_NO_ERROR);
        }

        // 简单后缀映射：0 -> @m.fudan.edu.cn, 1 -> @fudan.edu.cn
        String suffix = suffixType == 1 ? "@fudan.edu.cn" : "@m.fudan.edu.cn";
        return campusNo + suffix;
    }

    /**
     * 验证 token 并更新用户状态和邮箱
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkVerifyToken(String token) {
        String val = redisCacheManager.getEmailVerificationUser(token);
        if (val == null) {
            return false;
        }

        String[] parts = val.split(":", 2);
        if (parts.length < 2) {
            return false;
        }

        Long userId = Long.valueOf(parts[0]);
        String email = parts[1];

        // 在将当前用户设为已验证之前，检查是否已有其他已验证用户使用相同的 campusNo
        String campusNo = null;
        if (email != null && email.contains("@")) {
            campusNo = email.split("@", 2)[0];
        }

        if (campusNo != null) {
            long existed = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
                    .eq(UserEntity::getCampusNo, campusNo)
                    .eq(UserEntity::getStatus, Status.NORMAL)
                    .ne(UserEntity::getUserId, userId));
            if (existed > 0) {
                // 已有其他已验证账号使用相同学号
                log.warn("邮箱验证失败：学号 {} 已被其他已验证账号占用，userId={}", campusNo, userId);
                throw new ServiceException(UserErrorCode.CAMPUS_NO_EXISTED);
            }
        }

        UserEntity updateUser = new UserEntity();
        updateUser.setUserId(userId);
        updateUser.setEmail(email);
        updateUser.setStatus(Status.NORMAL);
        updateUser.setUpdateTime(java.time.LocalDateTime.now());

        int r = userMapper.updateById(updateUser);
        return r > 0;
    }

    /**
     * 管理后台分页检索
     */
    @Override
    public PageResult<UserInfoDTO> adminList(int page, int size, String keyword, Integer status, Integer identityType) {
        // 默认分页参数
        int p = Math.max(1, page);
        int s = Math.max(1, size);

        Page<UserEntity> pager = new Page<>(p, s);

        // 构建查询条件
        LambdaQueryWrapper<UserEntity> qw = Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getDelFlag, 0);

        if (status != null) {
            qw.eq(UserEntity::getStatus, status);
        }
        if (identityType != null) {
            qw.eq(UserEntity::getIdentityType, identityType);
        }

        if (StrUtil.isNotBlank(keyword)) {
            // 关键词模糊匹配 realName，或精确匹配 campusNo、username，或尝试作为 id 精确匹配
            String kw = keyword.trim();
            qw.and(w -> {
                w.like(UserEntity::getRealName, kw)
                 .or().eq(UserEntity::getCampusNo, kw)
                 .or().eq(UserEntity::getUsername, kw);
                // 尝试作为 id 精确匹配
                try {
                    Long id = Long.valueOf(kw);
                    w.or().eq(UserEntity::getUserId, id);
                } catch (Exception ignored) {
                }
            });
        }

        qw.orderByDesc(UserEntity::getCreateTime);

        // 分页查询用户核心信息
        Page<UserEntity> result = userMapper.selectPage(pager, qw);

        PageResult<UserInfoDTO> pageResult = new PageResult<>(result.getTotal(), p, s);

        List<UserEntity> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            // 批量查询 profile
            List<Long> ids = records.stream().map(UserEntity::getUserId).collect(Collectors.toList());
            List<UserProfileEntity> profiles = userProfileMapper.selectBatchIds(ids);
            Map<Long, UserProfileEntity> profileMap = Collections.emptyMap();
            if (profiles != null) {
                profileMap = profiles.stream().collect(Collectors.toMap(UserProfileEntity::getUserId, p2 -> p2));
            }

            List<UserInfoDTO> dtos = new ArrayList<>();
            for (UserEntity u : records) {
                UserInfoDTO dto = new UserInfoDTO();
                BeanUtil.copyProperties(u, dto);
                UserProfileEntity pf = profileMap.get(u.getUserId());
                if (pf != null) BeanUtil.copyProperties(pf, dto);
                // 管理端返回密码字段置空
                dto.setPassword(null);
                dtos.add(dto);
            }
            pageResult.addAll(dtos);
        }

        return pageResult;
    }

    /**
     * 管理员更新用户信息（全字段）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(Long operatorUserId, UserInfoDTO dto) {
        if (dto == null || dto.getId() == null) throw new ServiceException(UserErrorCode.USER_NOT_EXIST);
        Long targetId = dto.getId();

        UserEntity oldUser = userMapper.selectById(targetId);
        if (oldUser == null) throw new ServiceException(UserErrorCode.USER_NOT_EXIST);

        // 唯一性校验 username
        if (dto.getUsername() != null && !dto.getUsername().equals(oldUser.getUsername())) {
            if (userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
                    .eq(UserEntity::getUsername, dto.getUsername())
                    .ne(UserEntity::getUserId, targetId)) > 0) {
                throw new ServiceException(UserErrorCode.USERNAME_EXISTED);
            }
        }
        // 唯一性校验 campusNo
        if (dto.getCampusNo() != null && !dto.getCampusNo().equals(oldUser.getCampusNo())) {
            if (userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
                    .eq(UserEntity::getCampusNo, dto.getCampusNo())
                    .eq(UserEntity::getStatus, Status.NORMAL)
                    .ne(UserEntity::getUserId, targetId)) > 0) {
                throw new ServiceException(UserErrorCode.CAMPUS_NO_EXISTED);
            }
        }

        // 载入或新建 profile
        UserProfileEntity profile = userProfileMapper.selectById(targetId);
        if (profile == null) profile = UserProfileEntity.builder().userId(targetId).build();

        // 处理身份变更副作用
        if (dto.getIdentityType() != null && !dto.getIdentityType().equals(oldUser.getIdentityType())) {
            if (dto.getIdentityType() == IdentityType.STUDENT) {
                profile.setAcademicTitle(null);
            } else if (dto.getIdentityType() == IdentityType.TEACHER) {
                profile.setMajor(null);
                profile.setClassName(null);
                profile.setEnrollmentYear(null);
                profile.setDegreeLevel(null);
            }
        }

        // 学号变更副作用：若原 email 为学号邮箱，清空 email 并置为未验证
        if (dto.getCampusNo() != null && !Objects.equals(dto.getCampusNo(), oldUser.getCampusNo())) {
            String oldEmail = oldUser.getEmail();
            if (oldEmail != null && (oldEmail.endsWith("@m.fudan.edu.cn") || oldEmail.endsWith("@fudan.edu.cn"))) {
                oldUser.setEmail(null);
                oldUser.setStatus(Status.UNIDENTIFIED);
            }
        }

        // 复制字段
        BeanUtil.copyProperties(dto, oldUser, CopyOptions.create().setIgnoreNullValue(true));
        BeanUtil.copyProperties(dto, profile, CopyOptions.create().setIgnoreNullValue(true));

        int r1 = userMapper.updateById(oldUser);
        int r2;
        if (userProfileMapper.selectById(targetId) == null) {
            r2 = userProfileMapper.insert(profile);
        } else {
            r2 = userProfileMapper.updateById(profile);
        }

        if (r1 == 0 || r2 == 0) {
            throw new ServiceException(UserErrorCode.UPDATE_FAILED);
        }
    }

    /**
     * 管理员重置密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(Long targetUserId) {
        UserEntity user = userMapper.selectById(targetUserId);
        if (user == null) throw new ServiceException(UserErrorCode.USER_NOT_EXIST);

        String pwd = defaultAdminResetPassword;
        String hashed = BCrypt.hashpw(pwd);

        UserEntity updateUser = new UserEntity();
        updateUser.setUserId(targetUserId);
        updateUser.setPassword(hashed);
        updateUser.setUpdateTime(java.time.LocalDateTime.now());

        int r = userMapper.updateById(updateUser);
        if (r == 0) throw new ServiceException(UserErrorCode.UPDATE_FAILED);

        // 删除该用户的 session（强制下线）
        try {
            redisCacheManager.deleteSessionsByUserId(targetUserId);
        } catch (Exception e) {
            log.warn("删除用户会话失败 userId={}", targetUserId, e);
        }
    }
}

package com.oriole.wisepen.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.market.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.market.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.market.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.market.api.enums.ExchangeDirection;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import com.oriole.wisepen.market.domain.entity.InfoPointTransactionRecordEntity;
import com.oriole.wisepen.market.domain.entity.UserInfoPointEntity;
import com.oriole.wisepen.market.exception.MarketErrorCode;
import com.oriole.wisepen.market.mapper.InfoPointMapper;
import com.oriole.wisepen.market.mapper.InfoPointTransactionRecordMapper;
import com.oriole.wisepen.market.service.IInfoPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InfoPointServiceImpl implements IInfoPointService {

    private final InfoPointMapper infoPointMapper;
    private final InfoPointTransactionRecordMapper recordMapper;

    //信息点与 Token 的兑换比率：1 Token = rate 信息点
    @Value("${wisepen.market.exchange-rate:10}")
    private int rate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeBalance(InfoPointChangeRequest req) {
        Long userId = req.getUserId();
        Integer changeAmount = req.getChangeAmount();

        // 校验用户账户是否存在
        if (!infoPointMapper.exists(Wrappers.<UserInfoPointEntity>lambdaQuery().eq(UserInfoPointEntity::getUserId, userId))) {
            // 新建账户
            UserInfoPointEntity userInfoPointEntity = UserInfoPointEntity.builder()
                    .userId(userId)
                    .infoPointBalance(0)
                    .build();
            infoPointMapper.insert(userInfoPointEntity);
        }

        LambdaUpdateWrapper<UserInfoPointEntity> updateWrapper = Wrappers.<UserInfoPointEntity>lambdaUpdate()
                .eq(UserInfoPointEntity::getUserId, userId);

        // 扣款 校验余额是否足够
        if (changeAmount < 0) {
            updateWrapper.ge(UserInfoPointEntity::getInfoPointBalance, -changeAmount);
        }
        updateWrapper.setSql("info_point_balance = info_point_balance + {0}", changeAmount);

        // 检验交易是否成功
        int affectedRows = infoPointMapper.update(null, updateWrapper);
        if (affectedRows == 0) {
            if (changeAmount < 0) {
                throw new ServiceException(MarketErrorCode.INFO_POINT_INSUFFICIENT);
            }
            throw new ServiceException(MarketErrorCode.INFO_POINT_CHANGE_FAILED);
        }

        // 新建交易记录
        InfoPointTransactionRecordEntity record = BeanUtil.copyProperties(req, InfoPointTransactionRecordEntity.class);
        record.setBalanceAfter(infoPointMapper.selectById(userId).getInfoPointBalance());
        recordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTransaction(Long buyerId, Long sellerId, Integer price, Long relatedId) {
        if (buyerId.equals(sellerId)) {
            throw new ServiceException(MarketErrorCode.SELF_TRANSACTION_NOT_ALLOWED);
        }
        if (price == null || price <= 0) {
            throw new ServiceException(MarketErrorCode.INVALID_PRICE);
        }

        // 买方
        changeBalance(InfoPointChangeRequest.builder()
                .userId(buyerId)
                .changeAmount(-price)
                .changeType(InfoPointChangeType.MARKET_PURCHASE)
                .relatedId(relatedId)
                .build());

        // 卖方
        changeBalance(InfoPointChangeRequest.builder()
                .userId(sellerId)
                .changeAmount(price)
                .changeType(InfoPointChangeType.MARKET_INCOME)
                .relatedId(relatedId)
                .build());

        log.info("集市交易完成: buyer={}, seller={}, price={}, relatedId={}", buyerId, sellerId, price, relatedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeCurrency(CurrencyExchangeRequest req) {
        Long userId = req.getUserId();
        Integer amount = req.getChangeAmount();
        ExchangeDirection direction = req.getExchangeDirection();

        if (amount == null || amount <= 0) {
            throw new ServiceException(MarketErrorCode.EXCHANGE_AMOUNT_INVALID);
        }

        if (direction == ExchangeDirection.INFOPOINT_TO_TOKEN) {
            // 信息点 → Token：扣 amount 信息点，换 amount/rate 个 Token
            if (amount % rate != 0) {
                throw new ServiceException(MarketErrorCode.EXCHANGE_AMOUNT_INVALID);
            }
            int tokenAmount = amount / rate;
            changeBalance(InfoPointChangeRequest.builder()
                    .userId(userId)
                    .changeAmount(-amount)
                    .changeType(InfoPointChangeType.EXCHANGE_TO_TOKEN)
                    .meta(JSONUtil.toJsonStr(Map.of("exchangeRate", rate, "tokenAmount", tokenAmount)))
                    .build());
            // TODO: remoteWalletService.changeUserTokenBalance(userId, tokenAmount, ...)
            log.info("换汇完成: userId={}, infoPoint=-{}, token=+{}", userId, amount, tokenAmount);

        } else if (direction == ExchangeDirection.TOKEN_TO_INFOPOINT) {
            // Token → 信息点：扣 amount 个 Token，换 amount*rate 信息点
            int infoPointAmount = amount * rate;
            // TODO: remoteWalletService.changeUserTokenBalance(userId, -amount, ...)
            changeBalance(InfoPointChangeRequest.builder()
                    .userId(userId)
                    .changeAmount(infoPointAmount)
                    .changeType(InfoPointChangeType.EXCHANGE_FROM_TOKEN)
                    .meta(JSONUtil.toJsonStr(Map.of("exchangeRate", rate, "tokenAmount", amount)))
                    .build());

            log.info("换汇完成: userId={}, token=-{}, infoPoint=+{}", userId, amount, infoPointAmount);
        }
    }

    @Override
    public Integer getBalance(Long userId) {
        UserInfoPointEntity userPoint = infoPointMapper.selectById(userId);
        return userPoint!=null?userPoint.getInfoPointBalance():0;
    }

    @Override
    public PageResult<InfoPointTransactionRecordResponse> getRecordlist(Long userId, InfoPointChangeType changeType, Integer page, Integer size) {
        Page<InfoPointTransactionRecordEntity> recordPage = new Page<>(page, size);

        LambdaQueryWrapper<InfoPointTransactionRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InfoPointTransactionRecordEntity::getUserId, userId);
        wrapper.orderByDesc(InfoPointTransactionRecordEntity::getCreateTime);

        if (changeType != null) {
            wrapper.eq(InfoPointTransactionRecordEntity::getChangeType, changeType);
        }

        // 分页查询
        Page<InfoPointTransactionRecordEntity> resultPage = recordMapper.selectPage(recordPage, wrapper);
        PageResult<InfoPointTransactionRecordResponse> pageResult = new PageResult<>(resultPage.getTotal(), page, size);

        List<InfoPointTransactionRecordEntity> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return pageResult;
        }

        List<InfoPointTransactionRecordResponse> responses = records.stream()
                .map(record -> BeanUtil.copyProperties(record, InfoPointTransactionRecordResponse.class))
                .collect(Collectors.toList());

        pageResult.addAll(responses);
        return pageResult;
    }


}


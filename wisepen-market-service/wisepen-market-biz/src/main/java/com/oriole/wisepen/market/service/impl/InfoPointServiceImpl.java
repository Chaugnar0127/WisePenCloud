package com.oriole.wisepen.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.market.api.domain.dto.req.InfoPointChangeRequest;
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

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class InfoPointServiceImpl implements IInfoPointService {

    private final InfoPointMapper infoPointMapper;
    private final InfoPointTransactionRecordMapper recordMapper;

    /** 信息点与 Token 的兑换比率：1 Token = rate 信息点 */
    @Value("${wisepen.market.exchange-rate:10}")
    private int rate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeBalance(InfoPointChangeRequest infoPointChangeReq) {
        Long userId = infoPointChangeReq.getUserId();
        Integer amount = infoPointChangeReq.getAmount();
        ensureAccountExists(userId);
        LambdaUpdateWrapper<UserInfoPointEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInfoPointEntity::getUserId, userId);
        if (amount < 0) {
            updateWrapper.ge(UserInfoPointEntity::getInfoPointBalance, -amount);
        }
        updateWrapper.setSql("info_point_balance = info_point_balance + {0}", amount);
        int rows = infoPointMapper.update(null, updateWrapper);
        if (rows == 0) {
            if (amount < 0) {
                throw new ServiceException(MarketErrorCode.INFO_POINT_INSUFFICIENT);
            }
            throw new ServiceException(MarketErrorCode.INFO_POINT_CHANGE_FAILED);
        }

        InfoPointTransactionRecordEntity record = BeanUtil.copyProperties(infoPointChangeReq, InfoPointTransactionRecordEntity.class);
        record.setBalanceAfter(infoPointMapper.selectById(userId).getInfoPointBalance());
        insertRecord(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void marketTransaction(Long buyerId, Long sellerId, Integer price, String relatedId) {
        if (buyerId.equals(sellerId)) {
            throw new ServiceException(MarketErrorCode.SELF_TRANSACTION_NOT_ALLOWED);
        }
        if (price == null || price <= 0) {
            throw new ServiceException(MarketErrorCode.INVALID_PRICE);
        }

        changeBalance(InfoPointChangeRequest.builder()
                .userId(buyerId).amount(-price)
                .changeType(InfoPointChangeType.MARKET_PURCHASE)
                .relatedId(relatedId).build());

        changeBalance(InfoPointChangeRequest.builder()
                .userId(sellerId).amount(price)
                .changeType(InfoPointChangeType.MARKET_INCOME)
                .relatedId(relatedId).build());

        log.info("集市交易完成: buyer={}, seller={}, price={}, relatedId={}", buyerId, sellerId, price, relatedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeCurrency(Long userId, Integer amount, ExchangeDirection direction) {
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
                    .userId(userId).amount(-amount)
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
                    .userId(userId).amount(infoPointAmount)
                    .changeType(InfoPointChangeType.EXCHANGE_FROM_TOKEN)
                    .meta(JSONUtil.toJsonStr(Map.of("exchangeRate", rate, "tokenAmount", amount)))
                    .build());

            log.info("换汇完成: userId={}, token=-{}, infoPoint=+{}", userId, amount, infoPointAmount);
        }
    }

    private void ensureAccountExists(Long userId) {
        if (infoPointMapper.selectById(userId) == null) {
            UserInfoPointEntity entity = new UserInfoPointEntity();
            entity.setUserId(userId);
            entity.setInfoPointBalance(0);
            infoPointMapper.insert(entity);
        }
    }

    private void insertRecord(InfoPointTransactionRecordEntity record) {
        recordMapper.insert(record);
    }
}


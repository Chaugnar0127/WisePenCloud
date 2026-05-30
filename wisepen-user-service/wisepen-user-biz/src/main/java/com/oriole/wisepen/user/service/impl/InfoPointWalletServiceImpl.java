package com.oriole.wisepen.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeReverseRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeSettleRequest;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import com.oriole.wisepen.user.api.enums.InfoPointTradeReverseReason;
import com.oriole.wisepen.user.api.enums.InfoPointTradeStatus;
import com.oriole.wisepen.user.domain.entity.InfoPointTransactionRecordEntity;
import com.oriole.wisepen.user.domain.entity.UserInfoPointEntity;
import com.oriole.wisepen.user.exception.UserError;
import com.oriole.wisepen.user.mapper.InfoPointMapper;
import com.oriole.wisepen.user.mapper.InfoPointTransactionRecordMapper;
import com.oriole.wisepen.user.service.IInfoPointWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoPointWalletServiceImpl implements IInfoPointWalletService {

    private final InfoPointMapper infoPointMapper;
    private final InfoPointTransactionRecordMapper infoPointTransactionRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInfoPointBalance(InfoPointChangeRequest req) {
        applyInfoPointChange(req);
    }

    @Override
    public Integer getInfoPointBalance(Long userId) {
        UserInfoPointEntity userPoint = infoPointMapper.selectById(userId);
        return userPoint != null ? userPoint.getInfoPointBalance() : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleInfoPointTrade(InfoPointTradeSettleRequest req) {
        Long buyerId = req.getBuyerId();
        Long sellerId = req.getSellerId();
        Integer price = req.getPrice();
        Long relatedId = req.getRelatedId();

        // relatedId 是市场订单的幂等键，已有流水需要按当前状态补偿或跳过。
        MarketTradeRecords existing = analyzeMarketTradeRecords(listMarketTradeRecords(relatedId));
        if (existing.hasAny()) {
            settleFromExistingRecords(existing, buyerId, sellerId, price, relatedId);
            return;
        }

        try {
            InfoPointChangeRequest purchase = InfoPointChangeRequest.builder()
                    .userId(buyerId)
                    .changeAmount(-price)
                    .changeType(InfoPointChangeType.MARKET_PURCHASE)
                    .tradeStatus(InfoPointTradeStatus.PAID)
                    .relatedId(relatedId)
                    .operatorId(buyerId)
                    .build();

            InfoPointChangeRequest income = InfoPointChangeRequest.builder()
                    .userId(sellerId)
                    .changeAmount(price)
                    .changeType(InfoPointChangeType.MARKET_INCOME)
                    .tradeStatus(InfoPointTradeStatus.PAID)
                    .relatedId(relatedId)
                    .operatorId(buyerId)
                    .build();

            applyInfoPointChange(purchase);
            applyInfoPointChange(income);
        } catch (DuplicateKeyException ex) {
            if (analyzeMarketTradeRecords(listMarketTradeRecords(relatedId)).fullySettled()) {
                log.info("信息点交易结算幂等跳过(唯一索引): relatedId={}", relatedId);
                return;
            }
            existing = analyzeMarketTradeRecords(listMarketTradeRecords(relatedId));
            if (existing.hasAny()) {
                settleFromExistingRecords(existing, buyerId, sellerId, price, relatedId);
                return;
            }
            throw new ServiceException(UserError.WALLET_INFO_POINT_MARKET_TRADE_EXISTS);
        }

        log.info("信息点交易已扣款: buyer={}, seller={}, price={}, relatedId={}", buyerId, sellerId, price, relatedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseInfoPointTrade(InfoPointTradeReverseRequest req) {
        Long relatedId = req.getRelatedId();
        MarketTradeRecords records = analyzeMarketTradeRecords(listMarketTradeRecords(relatedId));
        if (!records.hasAny()) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_NOT_FOUND);
        }
        // 冲正可能由投递失败重试触发，已撤回流水直接幂等返回。
        if (records.hasRevoked()) {
            log.info("信息点交易冲正幂等跳过: relatedId={}", relatedId);
            return;
        }
        if (!records.hasPurchase() && records.hasIncome()) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_SETTLE_INCONSISTENT);
        }
        if (records.hasNonPaid()) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_REVOKE_NOT_ALLOWED);
        }

        Long operatorId = req.getOperatorId();
        String meta = buildReverseMeta(req.getReason(), req.getDetail());
        InfoPointTransactionRecordEntity purchase = records.requirePurchase();

        if (records.fullySettled()) {
            InfoPointTransactionRecordEntity income = records.requireIncome();
            InfoPointChangeRequest purchaseRevoke = InfoPointChangeRequest.builder()
                    .userId(purchase.getUserId())
                    .changeAmount(-purchase.getChangeAmount())
                    .changeType(InfoPointChangeType.MARKET_TRADE_REVOKE)
                    .tradeStatus(InfoPointTradeStatus.ADMIN_REVOKED)
                    .relatedId(relatedId)
                    .meta(meta)
                    .operatorId(operatorId)
                    .build();

            InfoPointChangeRequest incomeRevoke = InfoPointChangeRequest.builder()
                    .userId(income.getUserId())
                    .changeAmount(-income.getChangeAmount())
                    .changeType(InfoPointChangeType.MARKET_TRADE_REVOKE)
                    .tradeStatus(InfoPointTradeStatus.ADMIN_REVOKED)
                    .relatedId(relatedId)
                    .meta(meta)
                    .operatorId(operatorId)
                    .build();

            applyInfoPointChange(purchaseRevoke);
            applyInfoPointChange(incomeRevoke);

            infoPointTransactionRecordMapper.update(null,
                    Wrappers.<InfoPointTransactionRecordEntity>lambdaUpdate()
                            .eq(InfoPointTransactionRecordEntity::getRelatedId, relatedId)
                            .in(InfoPointTransactionRecordEntity::getChangeType,
                                    InfoPointChangeType.MARKET_PURCHASE, InfoPointChangeType.MARKET_INCOME)
                            .set(InfoPointTransactionRecordEntity::getTradeStatus, InfoPointTradeStatus.ADMIN_REVOKED));
        } else {
            InfoPointChangeRequest purchaseRevoke = InfoPointChangeRequest.builder()
                    .userId(purchase.getUserId())
                    .changeAmount(-purchase.getChangeAmount())
                    .changeType(InfoPointChangeType.MARKET_TRADE_REVOKE)
                    .tradeStatus(InfoPointTradeStatus.ADMIN_REVOKED)
                    .relatedId(relatedId)
                    .meta(meta)
                    .operatorId(operatorId)
                    .build();

            applyInfoPointChange(purchaseRevoke);

            infoPointTransactionRecordMapper.update(null,
                    Wrappers.<InfoPointTransactionRecordEntity>lambdaUpdate()
                            .eq(InfoPointTransactionRecordEntity::getRelatedId, relatedId)
                            .eq(InfoPointTransactionRecordEntity::getChangeType, InfoPointChangeType.MARKET_PURCHASE)
                            .set(InfoPointTransactionRecordEntity::getTradeStatus, InfoPointTradeStatus.ADMIN_REVOKED));
            log.warn("信息点集市冲正(仅买家扣款流水): relatedId={} buyerId={}", relatedId, purchase.getUserId());
        }
        log.info("信息点交易已冲正: relatedId={} operatorId={} reason={}", relatedId, operatorId, req.getReason());
    }

    private void applyInfoPointChange(InfoPointChangeRequest req) {
        Long userId = req.getUserId();
        Integer changeAmount = req.getChangeAmount();

        // 账户不存在时先创建空账户，保证后续原子增减余额能命中记录。
        if (!infoPointMapper.exists(Wrappers.<UserInfoPointEntity>lambdaQuery().eq(UserInfoPointEntity::getUserId, userId))) {
            infoPointMapper.insert(UserInfoPointEntity.builder()
                    .userId(userId)
                    .infoPointBalance(0)
                    .build());
        }

        LambdaUpdateWrapper<UserInfoPointEntity> updateWrapper = Wrappers.<UserInfoPointEntity>lambdaUpdate()
                .eq(UserInfoPointEntity::getUserId, userId);
        if (changeAmount < 0) {
            // 扣减余额必须在 SQL 条件中完成，避免并发下余额被扣成负数。
            updateWrapper.ge(UserInfoPointEntity::getInfoPointBalance, -changeAmount);
        }
        updateWrapper.setSql("info_point_balance = info_point_balance + {0}", changeAmount);

        int affectedRows = infoPointMapper.update(null, updateWrapper);
        if (affectedRows == 0) {
            if (changeAmount < 0) {
                throw new ServiceException(UserError.WALLET_INFO_POINT_INSUFFICIENT);
            }
            throw new ServiceException(UserError.WALLET_INFO_POINT_CHANGE_FAILED);
        }

        InfoPointTransactionRecordEntity record = BeanUtil.copyProperties(req, InfoPointTransactionRecordEntity.class);
        UserInfoPointEntity updated = infoPointMapper.selectById(userId);
        record.setBalanceAfter(updated == null ? 0 : updated.getInfoPointBalance());
        infoPointTransactionRecordMapper.insert(record);
    }

    /**
     * 已有集市流水时的结算：幂等跳过、补卖方入账或报错。
     */
    private void settleFromExistingRecords(MarketTradeRecords existing, Long buyerId,
                                           Long sellerId, Integer price, Long relatedId) {
        if (existing.hasRevoked()) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_ALREADY_REVOKED);
        }
        if (existing.fullySettled()) {
            log.info("信息点交易结算幂等跳过: relatedId={}", relatedId);
            return;
        }
        if (!existing.hasPurchase()) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_SETTLE_INCONSISTENT);
        }

        InfoPointTransactionRecordEntity purchase = existing.requirePurchase();
        if (!buyerId.equals(purchase.getUserId())
                || !Objects.equals(purchase.getChangeAmount(), -price)
                || purchase.getTradeStatus() != InfoPointTradeStatus.PAID) {
            throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_SETTLE_MISMATCH);
        }

        try {
            InfoPointChangeRequest income = InfoPointChangeRequest.builder()
                    .userId(sellerId)
                    .changeAmount(price)
                    .changeType(InfoPointChangeType.MARKET_INCOME)
                    .tradeStatus(InfoPointTradeStatus.PAID)
                    .relatedId(relatedId)
                    .operatorId(buyerId)
                    .build();

            applyInfoPointChange(income);
        } catch (DuplicateKeyException ex) {
            if (!analyzeMarketTradeRecords(listMarketTradeRecords(relatedId)).fullySettled()) {
                throw new ServiceException(UserError.WALLET_INFO_POINT_MARKET_TRADE_EXISTS);
            }
        }
        log.warn("信息点集市结算已补全卖方入账: relatedId={} buyerId={} sellerId={} price={}",
                relatedId, buyerId, sellerId, price);
    }

    private List<InfoPointTransactionRecordEntity> listMarketTradeRecords(Long relatedId) {
        return infoPointTransactionRecordMapper.selectList(
                Wrappers.<InfoPointTransactionRecordEntity>lambdaQuery()
                        .eq(InfoPointTransactionRecordEntity::getRelatedId, relatedId)
                        .in(InfoPointTransactionRecordEntity::getChangeType,
                                InfoPointChangeType.MARKET_PURCHASE, InfoPointChangeType.MARKET_INCOME)
        );
    }

    private MarketTradeRecords analyzeMarketTradeRecords(List<InfoPointTransactionRecordEntity> records) {
        InfoPointTransactionRecordEntity purchase = null;
        InfoPointTransactionRecordEntity income = null;
        boolean hasRevoked = false;
        boolean hasNonPaid = false;

        if (records != null) {
            for (InfoPointTransactionRecordEntity record : records) {
                if (record.getChangeType() == InfoPointChangeType.MARKET_PURCHASE && purchase == null) {
                    purchase = record;
                } else if (record.getChangeType() == InfoPointChangeType.MARKET_INCOME && income == null) {
                    income = record;
                }
                if (record.getTradeStatus() == InfoPointTradeStatus.ADMIN_REVOKED) {
                    hasRevoked = true;
                }
                if (record.getTradeStatus() != InfoPointTradeStatus.PAID) {
                    hasNonPaid = true;
                }
            }
        }
        return new MarketTradeRecords(purchase, income, hasRevoked, hasNonPaid);
    }

    private static String buildReverseMeta(InfoPointTradeReverseReason reason, String detail) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", reason == null ? InfoPointTradeReverseReason.ADMIN.name() : reason.name());
        if (detail != null && !detail.isBlank()) {
            payload.put("detail", detail);
        }
        return JSONUtil.toJsonStr(payload);
    }

    private record MarketTradeRecords(
            InfoPointTransactionRecordEntity purchase,
            InfoPointTransactionRecordEntity income,
            boolean hasRevoked,
            boolean hasNonPaid
    ) {
        boolean hasAny() {
            return purchase != null || income != null;
        }

        boolean hasPurchase() {
            return purchase != null;
        }

        boolean hasIncome() {
            return income != null;
        }

        boolean fullySettled() {
            return hasPurchase() && hasIncome();
        }

        InfoPointTransactionRecordEntity requirePurchase() {
            if (purchase == null) {
                throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_SETTLE_INCONSISTENT);
            }
            return purchase;
        }

        InfoPointTransactionRecordEntity requireIncome() {
            if (income == null) {
                throw new ServiceException(UserError.WALLET_INFO_POINT_TRADE_SETTLE_INCONSISTENT);
            }
            return income;
        }
    }
}

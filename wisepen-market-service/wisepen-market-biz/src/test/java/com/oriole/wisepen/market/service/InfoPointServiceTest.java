package com.oriole.wisepen.market.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.oriole.wisepen.market.service.impl.InfoPointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InfoPointService 测试")
class InfoPointServiceTest {

    @Mock
    private InfoPointMapper infoPointMapper;

    @Mock
    private InfoPointTransactionRecordMapper recordMapper;

    @InjectMocks
    private InfoPointServiceImpl infoPointService;

    @Captor
    private ArgumentCaptor<UserInfoPointEntity> userInfoPointCaptor;

    @Captor
    private ArgumentCaptor<InfoPointTransactionRecordEntity> recordCaptor;

    @Captor
    private ArgumentCaptor<LambdaUpdateWrapper<UserInfoPointEntity>> updateWrapperCaptor;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_SELLER_ID = 2L;
    private static final Long TEST_BUYER_ID = 3L;
    private static final Long TEST_RELATED_ID = 100L;
    private static final int TEST_RATE = 10;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(infoPointService, "rate", TEST_RATE);
    }

    @Test
    @DisplayName("changeBalance - 新用户创建账户并增加余额")
    void changeBalance_NewUser_Success() {
        InfoPointChangeRequest req = InfoPointChangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(100)
                .changeType(InfoPointChangeType.ADMIN_GRANT)
                .relatedId(TEST_RELATED_ID)
                .build();

        when(infoPointMapper.exists(any())).thenReturn(false);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserInfoPointEntity mockUser = UserInfoPointEntity.builder()
                .userId(TEST_USER_ID)
                .infoPointBalance(100)
                .build();
        when(infoPointMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

        assertDoesNotThrow(() -> infoPointService.changeBalance(req));

        verify(infoPointMapper).insert(userInfoPointCaptor.capture());
        UserInfoPointEntity savedUser = userInfoPointCaptor.getValue();
        assertEquals(TEST_USER_ID, savedUser.getUserId());
        assertEquals(0, savedUser.getInfoPointBalance());

        verify(recordMapper).insert(recordCaptor.capture());
        InfoPointTransactionRecordEntity savedRecord = recordCaptor.getValue();
        assertEquals(TEST_USER_ID, savedRecord.getUserId());
        assertEquals(100, savedRecord.getChangeAmount());
        assertEquals(InfoPointChangeType.ADMIN_GRANT, savedRecord.getChangeType());
    }

    @Test
    @DisplayName("changeBalance - 现有用户增加余额")
    void changeBalance_ExistingUser_AddBalance_Success() {
        InfoPointChangeRequest req = InfoPointChangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(50)
                .changeType(InfoPointChangeType.MARKET_INCOME)
                .relatedId(TEST_RELATED_ID)
                .build();

        when(infoPointMapper.exists(any())).thenReturn(true);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserInfoPointEntity mockUser = UserInfoPointEntity.builder()
                .userId(TEST_USER_ID)
                .infoPointBalance(150)
                .build();
        when(infoPointMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

        assertDoesNotThrow(() -> infoPointService.changeBalance(req));

        verify(infoPointMapper, never()).insert(any());
        verify(recordMapper).insert(recordCaptor.capture());
        assertEquals(50, recordCaptor.getValue().getChangeAmount());
    }

    @Test
    @DisplayName("changeBalance - 扣款时余额不足")
    void changeBalance_InsufficientBalance() {
        InfoPointChangeRequest req = InfoPointChangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(-100)
                .changeType(InfoPointChangeType.MARKET_PURCHASE)
                .relatedId(TEST_RELATED_ID)
                .build();

        when(infoPointMapper.exists(any())).thenReturn(true);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.changeBalance(req));

        assertEquals(MarketErrorCode.INFO_POINT_INSUFFICIENT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("changeBalance - 更新失败抛出异常")
    void changeBalance_UpdateFailed() {
        InfoPointChangeRequest req = InfoPointChangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(50)
                .changeType(InfoPointChangeType.ADMIN_GRANT)
                .relatedId(TEST_RELATED_ID)
                .build();

        when(infoPointMapper.exists(any())).thenReturn(true);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.changeBalance(req));

        assertEquals(MarketErrorCode.INFO_POINT_CHANGE_FAILED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("handleTransaction - 正常交易")
    void handleTransaction_Success() {
        when(infoPointMapper.exists(any())).thenReturn(true);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserInfoPointEntity mockUser = UserInfoPointEntity.builder()
                .userId(TEST_BUYER_ID)
                .infoPointBalance(100)
                .build();
        when(infoPointMapper.selectById(anyLong())).thenReturn(mockUser);

        assertDoesNotThrow(() -> infoPointService.handleTransaction(TEST_BUYER_ID, TEST_SELLER_ID, 100, TEST_RELATED_ID));

        verify(recordMapper, times(2)).insert(any(InfoPointTransactionRecordEntity.class));
    }

    @Test
    @DisplayName("handleTransaction - 自买自卖被拒绝")
    void handleTransaction_SelfTransaction() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.handleTransaction(TEST_BUYER_ID, TEST_BUYER_ID, 100, TEST_RELATED_ID));

        assertEquals(MarketErrorCode.SELF_TRANSACTION_NOT_ALLOWED.getCode(), exception.getCode());
        verify(infoPointMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("handleTransaction - 无效价格")
    void handleTransaction_InvalidPrice() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.handleTransaction(TEST_BUYER_ID, TEST_SELLER_ID, 0, TEST_RELATED_ID));

        assertEquals(MarketErrorCode.INVALID_PRICE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("handleTransaction - 负数价格")
    void handleTransaction_NegativePrice() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.handleTransaction(TEST_BUYER_ID, TEST_SELLER_ID, -50, TEST_RELATED_ID));

        assertEquals(MarketErrorCode.INVALID_PRICE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("handleTransaction - null价格")
    void handleTransaction_NullPrice() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.handleTransaction(TEST_BUYER_ID, TEST_SELLER_ID, null, TEST_RELATED_ID));

        assertEquals(MarketErrorCode.INVALID_PRICE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("exchangeCurrency - 信息点兑换Token成功")
    void exchangeCurrency_InfoPointToToken_Success() {
        CurrencyExchangeRequest req = CurrencyExchangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(100)
                .exchangeDirection(ExchangeDirection.INFOPOINT_TO_TOKEN)
                .build();

        when(infoPointMapper.exists(any())).thenReturn(true);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserInfoPointEntity mockUser = UserInfoPointEntity.builder()
                .userId(TEST_USER_ID)
                .infoPointBalance(0)
                .build();
        when(infoPointMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

        assertDoesNotThrow(() -> infoPointService.exchangeCurrency(req));

        verify(recordMapper).insert(recordCaptor.capture());
        InfoPointTransactionRecordEntity record = recordCaptor.getValue();
        assertEquals(-100, record.getChangeAmount());
        assertEquals(InfoPointChangeType.EXCHANGE_TO_TOKEN, record.getChangeType());
        assertTrue(record.getMeta().contains("\"tokenAmount\":10"));
    }

    @Test
    @DisplayName("exchangeCurrency - 信息点兑换Token金额不合法（非倍数）")
    void exchangeCurrency_InfoPointToToken_InvalidAmount() {
        CurrencyExchangeRequest req = CurrencyExchangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(15)
                .exchangeDirection(ExchangeDirection.INFOPOINT_TO_TOKEN)
                .build();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.exchangeCurrency(req));

        assertEquals(MarketErrorCode.EXCHANGE_AMOUNT_INVALID.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("exchangeCurrency - Token兑换信息点成功")
    void exchangeCurrency_TokenToInfoPoint_Success() {
        CurrencyExchangeRequest req = CurrencyExchangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(5)
                .exchangeDirection(ExchangeDirection.TOKEN_TO_INFOPOINT)
                .build();

        when(infoPointMapper.exists(any())).thenReturn(true);
        when(infoPointMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserInfoPointEntity mockUser = UserInfoPointEntity.builder()
                .userId(TEST_USER_ID)
                .infoPointBalance(150)
                .build();
        when(infoPointMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

        assertDoesNotThrow(() -> infoPointService.exchangeCurrency(req));

        verify(recordMapper).insert(recordCaptor.capture());
        InfoPointTransactionRecordEntity record = recordCaptor.getValue();
        assertEquals(50, record.getChangeAmount());
        assertEquals(InfoPointChangeType.EXCHANGE_FROM_TOKEN, record.getChangeType());
        assertTrue(record.getMeta().contains("\"tokenAmount\":5"));
    }

    @Test
    @DisplayName("exchangeCurrency - 非法兑换金额（零）")
    void exchangeCurrency_ZeroAmount() {
        CurrencyExchangeRequest req = CurrencyExchangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(0)
                .exchangeDirection(ExchangeDirection.INFOPOINT_TO_TOKEN)
                .build();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.exchangeCurrency(req));

        assertEquals(MarketErrorCode.EXCHANGE_AMOUNT_INVALID.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("exchangeCurrency - 非法兑换金额（负数）")
    void exchangeCurrency_NegativeAmount() {
        CurrencyExchangeRequest req = CurrencyExchangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(-10)
                .exchangeDirection(ExchangeDirection.TOKEN_TO_INFOPOINT)
                .build();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.exchangeCurrency(req));

        assertEquals(MarketErrorCode.EXCHANGE_AMOUNT_INVALID.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("exchangeCurrency - null金额")
    void exchangeCurrency_NullAmount() {
        CurrencyExchangeRequest req = CurrencyExchangeRequest.builder()
                .userId(TEST_USER_ID)
                .changeAmount(null)
                .exchangeDirection(ExchangeDirection.INFOPOINT_TO_TOKEN)
                .build();

        ServiceException exception = assertThrows(ServiceException.class,
                () -> infoPointService.exchangeCurrency(req));

        assertEquals(MarketErrorCode.EXCHANGE_AMOUNT_INVALID.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("getBalance - 用户存在")
    void getBalance_UserExists() {
        UserInfoPointEntity mockUser = UserInfoPointEntity.builder()
                .userId(TEST_USER_ID)
                .infoPointBalance(500)
                .build();
        when(infoPointMapper.selectById(TEST_USER_ID)).thenReturn(mockUser);

        Integer balance = infoPointService.getBalance(TEST_USER_ID);

        assertEquals(500, balance);
    }

    @Test
    @DisplayName("getBalance - 用户不存在")
    void getBalance_UserNotExists() {
        when(infoPointMapper.selectById(TEST_USER_ID)).thenReturn(null);

        Integer balance = infoPointService.getBalance(TEST_USER_ID);

        assertEquals(0, balance);
    }

    @Test
    @DisplayName("getRecordlist - 正常分页查询")
    void getRecordlist_Success() {
        Page<InfoPointTransactionRecordEntity> mockPage = new Page<>(1, 10);
        mockPage.setTotal(20);

        InfoPointTransactionRecordEntity record = InfoPointTransactionRecordEntity.builder()
                .recordId(1L)
                .userId(TEST_USER_ID)
                .changeAmount(100)
                .changeType(InfoPointChangeType.MARKET_INCOME)
                .balanceAfter(200)
                .createTime(LocalDateTime.now())
                .build();
        mockPage.setRecords(Collections.singletonList(record));

        when(recordMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        PageResult<InfoPointTransactionRecordResponse> result = infoPointService.getRecordlist(TEST_USER_ID, null, 1, 10);

        assertNotNull(result);
        assertEquals(20, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(100, result.getList().getFirst().getChangeAmount());
    }

    @Test
    @DisplayName("getRecordlist - 按类型过滤")
    void getRecordlist_FilterByType() {
        Page<InfoPointTransactionRecordEntity> mockPage = new Page<>(1, 10);
        mockPage.setTotal(5);

        InfoPointTransactionRecordEntity record = InfoPointTransactionRecordEntity.builder()
                .recordId(1L)
                .userId(TEST_USER_ID)
                .changeAmount(-50)
                .changeType(InfoPointChangeType.MARKET_PURCHASE)
                .balanceAfter(150)
                .createTime(LocalDateTime.now())
                .build();
        mockPage.setRecords(Collections.singletonList(record));

        when(recordMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        PageResult<InfoPointTransactionRecordResponse> result = infoPointService.getRecordlist(TEST_USER_ID,
                InfoPointChangeType.MARKET_PURCHASE, 1, 10);

        assertNotNull(result);
        assertEquals(5, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(InfoPointChangeType.MARKET_PURCHASE, result.getList().getFirst().getChangeType());
    }

    @Test
    @DisplayName("getRecordlist - 空结果")
    void getRecordlist_EmptyResult() {
        Page<InfoPointTransactionRecordEntity> mockPage = new Page<>(1, 10);
        mockPage.setTotal(0);
        mockPage.setRecords(Collections.emptyList());

        when(recordMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        PageResult<InfoPointTransactionRecordResponse> result = infoPointService.getRecordlist(TEST_USER_ID, null, 1, 10);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
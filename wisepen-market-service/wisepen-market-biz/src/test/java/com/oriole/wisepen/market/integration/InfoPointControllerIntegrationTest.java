package com.oriole.wisepen.market.integration;

import com.oriole.wisepen.market.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.market.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import com.oriole.wisepen.market.service.IInfoPointService;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.market.exception.MarketErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("InfoPoint Controller Integration Tests")
class InfoPointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IInfoPointService infoPointService;

    @Value("${security.internal-token}")
    private String internalToken;

    private static final Long TEST_USER_ID = 1001L;

    @Test
    @DisplayName("GET /market/wallet/getBalance - 必传参数userId缺失")
    void getBalance_MissingUserId() throws Exception {
        mockMvc.perform(get("/market/wallet/getBalance")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getBalance - 参数格式错误（非数字）")
    void getBalance_InvalidUserIdFormat() throws Exception {
        mockMvc.perform(get("/market/wallet/getBalance")
                        .param("userId", "invalid_id")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getBalance - 正常请求响应格式")
    void getBalance_Success_ResponseFormat() throws Exception {
        when(infoPointService.getBalance(TEST_USER_ID)).thenReturn(500);

        mockMvc.perform(get("/market/wallet/getBalance")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").isNumber())
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 必传参数exchangeDirection缺失")
    void exchangeCurrency_MissingDirection() throws Exception {
        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": 100
                }
                """;

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 必传参数userId缺失")
    void exchangeCurrency_MissingUserId() throws Exception {
        String requestBody = """
                {
                    "changeAmount": 100,
                    "exchangeDirection": 0
                }
                """;

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 参数格式错误（changeAmount非数字）")
    void exchangeCurrency_InvalidAmountFormat() throws Exception {
        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": "invalid",
                    "exchangeDirection": 0
                }
                """;

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 参数格式错误（exchangeDirection无效值）")
    void exchangeCurrency_InvalidDirection() throws Exception {
        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": 100,
                    "exchangeDirection": 999
                }
                """;

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 全局异常处理（业务异常）")
    void exchangeCurrency_ServiceException() throws Exception {
        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": 100,
                    "changeType": 1,
                    "exchangeDirection": 0
                }
                """;

        doThrow(new ServiceException(MarketErrorCode.EXCHANGE_AMOUNT_INVALID))
                .when(infoPointService).exchangeCurrency(any(CurrencyExchangeRequest.class));

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.EXCHANGE_AMOUNT_INVALID.getCode()))
                .andExpect(jsonPath("$.msg")
                        .value(MarketErrorCode.EXCHANGE_AMOUNT_INVALID.getMsg()));
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 全局异常处理（运行时异常）")
    void exchangeCurrency_RuntimeException() throws Exception {
        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": 100,
                    "exchangeDirection": 0
                }
                """;

        doThrow(new RuntimeException("数据库连接异常"))
                .when(infoPointService).exchangeCurrency(any(CurrencyExchangeRequest.class));

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /market/wallet/exchangeCurrency - 正常请求响应格式")
    void exchangeCurrency_Success_ResponseFormat() throws Exception {
        doNothing().when(infoPointService).exchangeCurrency(any(CurrencyExchangeRequest.class));

        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": 100,
                    "exchangeDirection": 0
                }
                """;

        mockMvc.perform(post("/market/wallet/exchangeCurrency")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 必传参数userId缺失")
    void getRecordList_MissingUserId() throws Exception {
        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("page", "1")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 必传参数page缺失")
    void getRecordList_MissingPage() throws Exception {
        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 必传参数size缺失")
    void getRecordList_MissingSize() throws Exception {
        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .param("page", "1")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 参数格式错误（page非数字）")
    void getRecordList_InvalidPageFormat() throws Exception {
        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .param("page", "invalid")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 参数格式错误（changeType无效值）")
    void getRecordList_InvalidChangeType() throws Exception {
        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .param("changeType", "INVALID_TYPE")
                        .param("page", "1")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 全局异常处理（业务异常）")
    void getRecordList_ServiceException() throws Exception {
        when(infoPointService.getRecordlist(anyLong(), any(), anyInt(), anyInt()))
                .thenThrow(new ServiceException(MarketErrorCode.INFO_POINT_CHANGE_FAILED));

        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .param("page", "1")
                        .param("size", "10")
                        .param("changeType", "MARKET_PURCHASE")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code")
                        .value(MarketErrorCode.INFO_POINT_CHANGE_FAILED.getCode()));
    }

    @Test
    @DisplayName("GET /market/wallet/getRecordList - 正常请求响应格式")
    void getRecordList_Success_ResponseFormat() throws Exception {
        InfoPointTransactionRecordResponse record = new InfoPointTransactionRecordResponse();
        record.setRecordId(1L);
        record.setUserId(TEST_USER_ID);
        record.setChangeAmount(100);
        record.setChangeType(InfoPointChangeType.MARKET_INCOME);
        record.setBalanceAfter(200);

        PageResult<InfoPointTransactionRecordResponse> mockResult = new PageResult<>(10L, 1, 10);
        mockResult.addAll(Collections.singletonList(record));

        when(infoPointService.getRecordlist(eq(TEST_USER_ID), eq(InfoPointChangeType.MARKET_INCOME), eq(1), eq(10)))
                .thenReturn(mockResult);

        mockMvc.perform(get("/market/wallet/getRecordList")
                        .param("userId", String.valueOf(TEST_USER_ID))
                        .param("page", "1")
                        .param("size", "10")
                        .param("changeType", "MARKET_INCOME")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.page").isNumber())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.list").isArray());
    }
}
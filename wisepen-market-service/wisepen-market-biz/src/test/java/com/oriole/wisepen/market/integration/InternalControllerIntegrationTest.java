package com.oriole.wisepen.market.integration;

import com.oriole.wisepen.market.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.market.service.IInfoPointService;
import com.oriole.wisepen.market.service.IMarketService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Internal Controller Integration Tests")
class InternalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IInfoPointService infoPointService;

    @MockBean
    private IMarketService marketService;

    @Value("${security.internal-token}")
    private String internalToken;

    private static final Long TEST_PRODUCT_ID = 2001L;

    @Test
    @DisplayName("POST /internal/infoPoint/changeBalance - 必传参数缺失")
    void changeBalance_MissingParams() throws Exception {
        mockMvc.perform(post("/internal/infoPoint/changeBalance")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /internal/infoPoint/changeBalance - 参数格式错误")
    void changeBalance_InvalidParams() throws Exception {
        mockMvc.perform(post("/internal/infoPoint/changeBalance")
                        .param("userId", "invalid")
                        .param("changeAmount", "100")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /internal/infoPoint/changeBalance - 全局异常处理（余额不足）")
    void changeBalance_InsufficientBalance() throws Exception {
        doThrow(new ServiceException(MarketErrorCode.INFO_POINT_INSUFFICIENT))
                .when(infoPointService).changeBalance(any(InfoPointChangeRequest.class));

        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": -100,
                    "changeType": 5
                }
                """;

        mockMvc.perform(post("/internal/infoPoint/changeBalance")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.INFO_POINT_INSUFFICIENT.getCode()));
    }

    @Test
    @DisplayName("POST /internal/infoPoint/changeBalance - 正常请求响应格式")
    void changeBalance_Success_ResponseFormat() throws Exception {
        doNothing().when(infoPointService).changeBalance(any(InfoPointChangeRequest.class));

        String requestBody = """
                {
                    "userId": 1001,
                    "changeAmount": 100,
                    "changeType": 5
                }
                """;

        mockMvc.perform(post("/internal/infoPoint/changeBalance")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("POST /internal/infoPoint/deleteProduct - 必传参数productId缺失")
    void deleteProduct_MissingProductId() throws Exception {
        mockMvc.perform(post("/internal/infoPoint/deleteProduct")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /internal/infoPoint/deleteProduct - 参数格式错误（非数字）")
    void deleteProduct_InvalidProductIdFormat() throws Exception {
        mockMvc.perform(post("/internal/infoPoint/deleteProduct")
                        .param("productId", "invalid")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /internal/infoPoint/deleteProduct - 全局异常处理（商品不存在）")
    void deleteProduct_ProductNotFound() throws Exception {
        doThrow(new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND))
                .when(marketService).deleteProduct(TEST_PRODUCT_ID);

        mockMvc.perform(post("/internal/infoPoint/deleteProduct")
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.PRODUCT_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("POST /internal/infoPoint/deleteProduct - 正常请求响应格式")
    void deleteProduct_Success_ResponseFormat() throws Exception {
        doNothing().when(marketService).deleteProduct(TEST_PRODUCT_ID);

        mockMvc.perform(post("/internal/infoPoint/deleteProduct")
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists());
    }
}
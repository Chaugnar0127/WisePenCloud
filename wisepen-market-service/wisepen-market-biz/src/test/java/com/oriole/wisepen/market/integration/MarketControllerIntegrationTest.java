package com.oriole.wisepen.market.integration;

import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;
import com.oriole.wisepen.market.service.IMarketService;
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
@DisplayName("Market Controller Integration Tests")
class MarketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IMarketService marketService;

    @Value("${security.internal-token}")
    private String internalToken;

    private static final Long TEST_USER_ID = 114514L;

    private static final Long TEST_PRODUCT_ID = 1001L;

    @Test
    @DisplayName("GET /market/shop/getProductList - 必传参数page缺失")
    void getProductList_MissingPage() throws Exception {
        mockMvc.perform(get("/market/shop/getProductList")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/shop/getProductList - 必传参数size缺失")
    void getProductList_MissingSize() throws Exception {
        mockMvc.perform(get("/market/shop/getProductList")
                        .param("page", "1")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/shop/getProductList - 参数格式错误（page非数字）")
    void getProductList_InvalidPageFormat() throws Exception {
        mockMvc.perform(get("/market/shop/getProductList")
                        .param("page", "invalid")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/shop/getProductList - 正常请求响应格式")
    void getProductList_Success_ResponseFormat() throws Exception {
        ProductInfoResponse product = new ProductInfoResponse();
        product.setProductId(TEST_PRODUCT_ID);

        PageResult<ProductInfoResponse> mockResult = new PageResult<>(10L, 1, 10);
        mockResult.addAll(Collections.singletonList(product));

        when(marketService.getProductList(any(), eq(1), eq(10)))
                .thenReturn(mockResult);

        mockMvc.perform(get("/market/shop/getProductList")
                        .param("page", "1")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.total").isString())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("GET /market/shop/getProductDetail - 必传参数productId缺失")
    void getProductDetail_MissingProductId() throws Exception {
        mockMvc.perform(get("/market/shop/getProductDetail")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/shop/getProductDetail - 参数格式错误（非数字）")
    void getProductDetail_InvalidProductIdFormat() throws Exception {
        mockMvc.perform(get("/market/shop/getProductDetail")
                        .param("productId", "invalid")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/shop/getProductDetail - 全局异常处理（商品不存在）")
    void getProductDetail_ProductNotFound() throws Exception {
        when(marketService.getProductDetail(TEST_PRODUCT_ID))
                .thenThrow(new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/market/shop/getProductDetail")
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.PRODUCT_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("GET /market/shop/getProductDetail - 正常请求响应格式")
    void getProductDetail_Success_ResponseFormat() throws Exception {
        ProductInfoResponse product = new ProductInfoResponse();
        product.setProductId(TEST_PRODUCT_ID);
        product.setProductName("测试商品");

        when(marketService.getProductDetail(TEST_PRODUCT_ID)).thenReturn(product);

        mockMvc.perform(get("/market/shop/getProductDetail")
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.productId").value(TEST_PRODUCT_ID));
    }

    @Test
    @DisplayName("POST /market/shop/addProduct - 请求体为空")
    void addProduct_EmptyBody() throws Exception {
        mockMvc.perform(post("/market/shop/addProduct")
                        .content("{}")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/shop/addProduct - 全局异常处理（非资源所有者）")
    void addProduct_NotResourceOwner() throws Exception {
        String requestBody = """
                {
                    "groupId": 100,
                    "resourceId": 2000,
                    "productName": "测试商品",
                    "tradeContentType": 1,
                    "tagId": 100,
                    "price": 100
                }
                """;

        doThrow(new ServiceException(MarketErrorCode.NOT_RESOURCE_OWNER))
                .when(marketService).addProduct(any(ProductCreateRequest.class));

        mockMvc.perform(post("/market/shop/addProduct")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.NOT_RESOURCE_OWNER.getCode()));
    }

    @Test
    @DisplayName("POST /market/shop/addProduct - 正常请求响应格式")
    void addProduct_Success_ResponseFormat() throws Exception {
        String requestBody = """
                {
                    "groupId": 100,
                    "resourceId": 2000,
                    "productName": "测试商品",
                    "tradeContentType": 1,
                    "tagId": 100,
                    "price": 100
                }
                """;

        doNothing().when(marketService).addProduct(any(ProductCreateRequest.class));

        mockMvc.perform(post("/market/shop/addProduct")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("POST /market/shop/updateProduct - 请求体为空")
    void updateProduct_EmptyBody() throws Exception {
        mockMvc.perform(post("/market/shop/updateProduct")
                        .content("{}")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/shop/updateProduct - 全局异常处理（商品不存在）")
    void updateProduct_ProductNotFound() throws Exception {
        String requestBody = """
                {
                    "productId": 1001
                }
                """;

        doThrow(new ServiceException(MarketErrorCode.PRODUCT_NOT_FOUND))
                .when(marketService).updateProduct(any(ProductCreateRequest.class));

        mockMvc.perform(post("/market/shop/updateProduct")
                        .content(requestBody)
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.PRODUCT_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("POST /market/shop/purchase - 必传参数productId缺失")
    void purchase_MissingProductId() throws Exception {
        mockMvc.perform(post("/market/shop/purchase")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/shop/purchase - 参数格式错误（非数字）")
    void purchase_InvalidProductIdFormat() throws Exception {
        mockMvc.perform(post("/market/shop/purchase")
                        .param("productId", "invalid")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/shop/purchase - 全局异常处理（余额不足）")
    void purchase_InfoPointInsufficient() throws Exception {
        doThrow(new ServiceException(MarketErrorCode.INFO_POINT_INSUFFICIENT))
                .when(marketService).purchase(TEST_PRODUCT_ID);

        mockMvc.perform(post("/market/shop/purchase")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.INFO_POINT_INSUFFICIENT.getCode()));
    }

    @Test
    @DisplayName("POST /market/shop/purchase - 正常请求响应格式")
    void purchase_Success_ResponseFormat() throws Exception {
        doNothing().when(marketService).purchase(TEST_PRODUCT_ID);

        mockMvc.perform(post("/market/shop/purchase")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("POST /market/shop/deleteProduct - 必传参数productId缺失")
    void deleteProduct_MissingProductId() throws Exception {
        mockMvc.perform(post("/market/shop/deleteProduct")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /market/shop/deleteProduct - 全局异常处理（无权限）")
    void deleteProduct_PermissionDenied() throws Exception {
        doThrow(new ServiceException(MarketErrorCode.PRODUCT_PERMISSION_DENIED))
                .when(marketService).deleteProduct(TEST_PRODUCT_ID);

        mockMvc.perform(post("/market/shop/deleteProduct")
                        .param("productId", String.valueOf(TEST_PRODUCT_ID))
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MarketErrorCode.PRODUCT_PERMISSION_DENIED.getCode()));
    }

    @Test
    @DisplayName("GET /market/shop/getMyList - 必传参数page缺失")
    void getMyList_MissingPage() throws Exception {
        mockMvc.perform(get("/market/shop/getMyList")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /market/shop/getMyList - 正常请求响应格式")
    void getMyList_Success_ResponseFormat() throws Exception {
        PageResult<ProductInfoResponse> mockResult = new PageResult<>(5L, 1, 10);

        when(marketService.getMyList(1, 10)).thenReturn(mockResult);

        mockMvc.perform(get("/market/shop/getMyList")
                        .param("page", "1")
                        .param("size", "10")
                        .header("X-From-Source", internalToken)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.total").value(5));
    }
}
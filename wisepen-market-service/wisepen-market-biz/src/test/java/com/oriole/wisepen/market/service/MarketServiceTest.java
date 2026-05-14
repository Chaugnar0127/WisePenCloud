package com.oriole.wisepen.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.market.api.domain.dto.req.ProductCreateRequest;
import com.oriole.wisepen.market.api.domain.dto.req.ProductSearchRequest;
import com.oriole.wisepen.market.api.domain.dto.res.ProductInfoResponse;
import com.oriole.wisepen.market.api.enums.OrderStatus;
import com.oriole.wisepen.market.api.enums.ProductStatus;
import com.oriole.wisepen.market.api.enums.SortType;
import com.oriole.wisepen.market.api.enums.TradeType;
import com.oriole.wisepen.market.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.market.domain.entity.MarketProductEntity;
import com.oriole.wisepen.market.exception.MarketErrorCode;
import com.oriole.wisepen.market.mapper.MarketOrderMapper;
import com.oriole.wisepen.market.mapper.MarketProductMapper;
import com.oriole.wisepen.market.service.impl.MarketServiceImpl;
import com.oriole.wisepen.resource.domain.dto.ResourceCheckPermissionResDTO;
import com.oriole.wisepen.resource.enums.ResourceAccessRole;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.feign.RemoteUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import cn.hutool.json.JSONUtil;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketService 测试")
class MarketServiceTest {

    @Mock
    private MarketProductMapper marketProductMapper;

    @Mock
    private MarketOrderMapper marketOrderMapper;

    @Mock
    private RemoteResourceService remoteResourceService;

    @Mock
    private RemoteUserService remoteUserService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private com.oriole.wisepen.market.service.IInfoPointService infoPointService;

    @InjectMocks
    private MarketServiceImpl marketService;

    @Captor
    private ArgumentCaptor<MarketProductEntity> productCaptor;

    @Captor
    private ArgumentCaptor<MarketOrderEntity> orderCaptor;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_SELLER_ID = 2L;
    private static final Long TEST_GROUP_ID = 100L;
    private static final Long TEST_ORDER_ID = 999L;
    private static final Long TEST_PRODUCT_ID = 1000L;
    private static final Long TEST_RESOURCE_ID = 2000L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setUserId(TEST_USER_ID);
        Map<String, Integer> groupRoles = new HashMap<>();
        groupRoles.put(TEST_GROUP_ID.toString(), 1);
        String groupRoleJson = JSONUtil.toJsonStr(groupRoles);
        SecurityContextHolder.setGroupRoleMap(groupRoleJson);
    }

    @Test
    @DisplayName("getProductList - 正常分页查询")
    void getProductList_Success() {
        ProductSearchRequest searchReq = new ProductSearchRequest();
        searchReq.setGroupId(TEST_GROUP_ID);
        searchReq.setSortBy(SortType.TIME_DESC);

        Page<MarketProductEntity> mockPage = new Page<>(1, 10);
        mockPage.setTotal(20);
        mockPage.setRecords(Collections.singletonList(createMockProduct()));

        when(marketProductMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        PageResult<ProductInfoResponse> result = marketService.getProductList(searchReq, 1, 10);

        assertNotNull(result);
        assertEquals(20, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(TEST_PRODUCT_ID, result.getList().get(0).getProductId());
    }

    @Test
    @DisplayName("getProductDetail - 商品存在")
    void getProductDetail_Success() {
        MarketProductEntity mockProduct = createMockProduct();
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(mockProduct);

        Map<Long, UserDisplayBase> userMap = new HashMap<>();
        userMap.put(TEST_USER_ID, new UserDisplayBase("testUser", "测试用户", null, null));
        when(remoteUserService.getUserDisplayInfo(anyList()))
                .thenReturn(mock(R.class));
        when(remoteUserService.getUserDisplayInfo(anyList()).getData()).thenReturn(userMap);

        ProductInfoResponse result = marketService.getProductDetail(TEST_PRODUCT_ID);

        assertNotNull(result);
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        assertEquals("测试商品", result.getProductName());
        assertEquals("testUser", result.getSellerName());
    }

    @Test
    @DisplayName("getProductDetail - 商品不存在")
    void getProductDetail_NotFound() {
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.getProductDetail(TEST_PRODUCT_ID));

        assertEquals(MarketErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("addProduct - 正常创建商品")
    void addProduct_Success() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setGroupId(TEST_GROUP_ID);
        createReq.setResourceId(TEST_RESOURCE_ID);
        createReq.setTagId(1L);
        createReq.setProductName("测试商品");
        createReq.setPrice(100);
        createReq.setTradeContentType(TradeType.OWNERSHIP);

        ResourceCheckPermissionResDTO permRes = new ResourceCheckPermissionResDTO(ResourceAccessRole.OWNER);
        when(remoteResourceService.checkResPermission(any()))
                .thenReturn(mock(com.oriole.wisepen.common.core.domain.R.class));
        when(remoteResourceService.checkResPermission(any()).getData()).thenReturn(permRes);

        when(remoteResourceService.updateResourceTags(any())).thenReturn(R.ok());

        assertDoesNotThrow(() -> marketService.addProduct(createReq));

        verify(marketProductMapper).insert(productCaptor.capture());
        MarketProductEntity savedProduct = productCaptor.getValue();
        assertEquals(TEST_USER_ID, savedProduct.getSellerId());
        assertEquals(ProductStatus.ON_SHELF, savedProduct.getStatus());
        assertEquals("测试商品", savedProduct.getProductName());
    }

    @Test
    @DisplayName("addProduct - 非资源所有者")
    void addProduct_NotOwner() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setGroupId(TEST_GROUP_ID);
        createReq.setResourceId(TEST_RESOURCE_ID);

        ResourceCheckPermissionResDTO permRes = new ResourceCheckPermissionResDTO(ResourceAccessRole.GROUP_MEMBER);
        when(remoteResourceService.checkResPermission(any())).thenReturn(mock(R.class));
        when(remoteResourceService.checkResPermission(any()).getData()).thenReturn(permRes);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.addProduct(createReq));

        assertEquals(MarketErrorCode.NOT_RESOURCE_OWNER.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("addProduct - 商品已上架")
    void addProduct_Duplicate() {
        ProductCreateRequest createReq = new ProductCreateRequest();
        createReq.setGroupId(TEST_GROUP_ID);
        createReq.setResourceId(TEST_RESOURCE_ID);

        ResourceCheckPermissionResDTO permRes = new ResourceCheckPermissionResDTO(ResourceAccessRole.OWNER);
        when(remoteResourceService.checkResPermission(any()))
                .thenReturn(mock(com.oriole.wisepen.common.core.domain.R.class));
        when(remoteResourceService.checkResPermission(any()).getData()).thenReturn(permRes);

        doThrow(DuplicateKeyException.class).when(marketProductMapper).insert(any());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.addProduct(createReq));

        assertEquals(MarketErrorCode.PRODUCT_ALREADY_LISTED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("updateProduct - 正常更新")
    void updateProduct_Success() {
        MarketProductEntity existingProduct = createMockProduct();
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(existingProduct);

        ProductCreateRequest updateReq = new ProductCreateRequest();
        updateReq.setProductId(TEST_PRODUCT_ID);
        updateReq.setProductName("更新后的商品名");
        updateReq.setPrice(200);

        assertDoesNotThrow(() -> marketService.updateProduct(updateReq));

        verify(marketProductMapper).updateById(productCaptor.capture());
        MarketProductEntity updatedProduct = productCaptor.getValue();
        assertEquals("更新后的商品名", updatedProduct.getProductName());
        assertEquals(200, updatedProduct.getPrice());
        assertEquals(TEST_RESOURCE_ID, updatedProduct.getResourceId());
    }

    @Test
    @DisplayName("updateProduct - 商品不存在")
    void updateProduct_NotFound() {
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(null);

        ProductCreateRequest updateReq = new ProductCreateRequest();
        updateReq.setProductId(TEST_PRODUCT_ID);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.updateProduct(updateReq));

        assertEquals(MarketErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("updateProduct - 无权限")
    void updateProduct_NoPermission() {
        MarketProductEntity existingProduct = createMockProduct();
        existingProduct.setSellerId(999L);
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(existingProduct);

        ProductCreateRequest updateReq = new ProductCreateRequest();
        updateReq.setProductId(TEST_PRODUCT_ID);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.updateProduct(updateReq));

        assertEquals(MarketErrorCode.PRODUCT_PERMISSION_DENIED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("purchase - 所有权交易")
    void purchase_Owner() {
        MarketProductEntity product = createMockProduct();
        product.setPrice(100);
        product.setSellerId(TEST_SELLER_ID);
        product.setTradeContentType(TradeType.OWNERSHIP.getCode());
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(product);

        doAnswer(invocation -> {
            MarketOrderEntity order = invocation.getArgument(0);
            order.setOrderId(TEST_ORDER_ID);
            return null;
        }).when(marketOrderMapper).insert(any(MarketOrderEntity.class));

        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<MarketOrderEntity> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        doNothing().when(infoPointService).handleTransaction(anyLong(), anyLong(), anyInt(), eq(TEST_ORDER_ID));

        assertDoesNotThrow(() -> marketService.purchase(TEST_PRODUCT_ID));

        verify(marketOrderMapper).insert(orderCaptor.capture());
        MarketOrderEntity order = orderCaptor.getValue();
        assertEquals(TEST_PRODUCT_ID, order.getProductId());
        assertEquals(TEST_USER_ID, order.getBuyerId());
        assertEquals(product.getSellerId(), order.getSellerId());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("purchase - 使用权交易")
    void purchase_Use() {
        MarketProductEntity product = createMockProduct();
        product.setPrice(100);
        product.setSellerId(TEST_SELLER_ID);
        product.setTradeContentType(TradeType.USE_RIGHT.getCode());
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(product);

        doAnswer(invocation -> {
            MarketOrderEntity order = invocation.getArgument(0);
            order.setOrderId(TEST_ORDER_ID);
            return null;
        }).when(marketOrderMapper).insert(any(MarketOrderEntity.class));

        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<MarketOrderEntity> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        doNothing().when(infoPointService).handleTransaction(anyLong(), anyLong(), anyInt(), eq(TEST_ORDER_ID));

        when(remoteResourceService.updateResourceActionPermission(any())).thenReturn(R.ok());

        assertDoesNotThrow(() -> marketService.purchase(TEST_PRODUCT_ID));

        verify(marketOrderMapper).insert(orderCaptor.capture());
        MarketOrderEntity order = orderCaptor.getValue();
        assertEquals(TEST_PRODUCT_ID, order.getProductId());
        assertEquals(TEST_USER_ID, order.getBuyerId());
        assertEquals(product.getSellerId(), order.getSellerId());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("purchase - 商品不存在")
    void purchase_ProductNotFound() {
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.purchase(TEST_PRODUCT_ID));

        assertEquals(MarketErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("purchase - 商品已下架")
    void purchase_ProductOffShelf() {
        MarketProductEntity product = createMockProduct();
        product.setStatus(ProductStatus.OFF_SHELF);
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(product);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.purchase(TEST_PRODUCT_ID));

        assertEquals(MarketErrorCode.PRODUCT_OFF_SHELF.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("purchase - 购买自己的商品")
    void purchase_OwnProduct() {
        MarketProductEntity product = createMockProduct();
        product.setSellerId(TEST_USER_ID);
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(product);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.purchase(TEST_PRODUCT_ID));

        assertEquals(MarketErrorCode.CANNOT_BUY_OWN_PRODUCT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("deleteProduct - 正常下架")
    void deleteProduct_Success() {
        MarketProductEntity product = createMockProduct();
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(product);

        when(remoteResourceService.updateResourceTags(any())).thenReturn(R.ok());

        assertDoesNotThrow(() -> marketService.deleteProduct(TEST_PRODUCT_ID));

        verify(marketProductMapper).updateById(productCaptor.capture());
        assertEquals(ProductStatus.OFF_SHELF, productCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("deleteProduct - 商品不存在")
    void deleteProduct_NotFound() {
        when(marketProductMapper.selectById(TEST_PRODUCT_ID)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> marketService.deleteProduct(TEST_PRODUCT_ID));

        assertEquals(MarketErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("getMyList - 查询我的商品列表")
    void getMyList_Success() {
        Page<MarketProductEntity> mockPage = new Page<>(1, 10);
        mockPage.setTotal(5);
        mockPage.setRecords(Collections.singletonList(createMockProduct()));

        when(marketProductMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        PageResult<ProductInfoResponse> result = marketService.getMyList(1, 10);

        assertNotNull(result);
        assertEquals(5, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    private MarketProductEntity createMockProduct() {
        return MarketProductEntity.builder()
                .productId(TEST_PRODUCT_ID)
                .productName("测试商品")
                .sellerId(TEST_USER_ID)
                .groupId(TEST_GROUP_ID)
                .resourceId(TEST_RESOURCE_ID)
                .price(100)
                .status(ProductStatus.ON_SHELF)
                .buyerCount(0)
                .createTime(LocalDateTime.now())
                .build();
    }
}
package com.oriole.wisepen.market.api.domain.dto.req;

import com.oriole.wisepen.market.api.enums.SortType;
import com.oriole.wisepen.market.api.enums.TradeType;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProductSearchRequest implements Serializable {
    private Long groupId;
    private String tagIds;
    private String keyword;
    private TradeType tradeContentType;
    private Long resourceType;
    private SortType sortBy;
}
package com.oriole.wisepen.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_mkt_order")
public class MarketOrderEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId;

    private Long productId;
    private Long sellerId;
    private Long buyerId;

    // JSON 格式的元数据
    private String meta;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
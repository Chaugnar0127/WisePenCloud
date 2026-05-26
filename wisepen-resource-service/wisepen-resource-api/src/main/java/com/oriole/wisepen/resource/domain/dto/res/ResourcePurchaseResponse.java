package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.SellInfoBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "购买结果；仅表示信息点扣款已成功")
public class ResourcePurchaseResponse extends SellInfoBase {

    @Schema(description = "资源 ID")
    private String resourceId;

    @Schema(description = "集市购买订单号，与钱包流水 relatedId 一致")
    private Long orderId;
}

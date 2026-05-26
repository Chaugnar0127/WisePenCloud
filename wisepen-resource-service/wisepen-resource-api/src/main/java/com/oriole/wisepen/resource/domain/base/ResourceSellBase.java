package com.oriole.wisepen.resource.domain.base;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 市场售卖项定位：{@code resourceId} + {@code sellId}。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSellBase {

    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    @Schema(description = "资源 ID")
    private String resourceId;

    @NotBlank(message = ResourceValidationMsg.SELL_ID_NOT_BLANK)
    @Schema(description = "售卖项 ID")
    private String sellId;
}

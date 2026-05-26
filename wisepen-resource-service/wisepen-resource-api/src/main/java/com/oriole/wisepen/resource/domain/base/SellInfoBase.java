package com.oriole.wisepen.resource.domain.base;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.enums.SaleMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SellInfoBase {

    /** 服务端生成，提审请求勿传 */
    private String sellId;

    @NotBlank(message = ResourceValidationMsg.GROUP_ID_NOT_BLANK)
    private String groupId;

    @NotBlank(message = ResourceValidationMsg.TAG_ID_NOT_BLANK)
    private String tagId;

    @NotNull(message = ResourceValidationMsg.PRICE_NOT_NULL)
    @Positive(message = ResourceValidationMsg.PRICE_POSITIVE)
    private Integer price;

    @NotNull(message = ResourceValidationMsg.SALE_METHOD_NOT_NULL)
    private SaleMethod saleMethod;

    /** 服务端生成，提审请求勿传 */
    private Long version;

    /** 服务端生成，提审请求勿传 */
    private LocalDateTime listedAt;
}

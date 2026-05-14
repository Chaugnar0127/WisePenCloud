package com.oriole.wisepen.market.api.domain.base;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InfoPointBase implements Serializable {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private Integer changeAmount;
}


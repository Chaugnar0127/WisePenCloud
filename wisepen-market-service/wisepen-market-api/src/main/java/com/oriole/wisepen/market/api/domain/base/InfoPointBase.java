package com.oriole.wisepen.market.api.domain.base;

import lombok.AllArgsConstructor;import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InfoPointBase implements Serializable {
    private Long userId;
    private Integer changeAmount;
}


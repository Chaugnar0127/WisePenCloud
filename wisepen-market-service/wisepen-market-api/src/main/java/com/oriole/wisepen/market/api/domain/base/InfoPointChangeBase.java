package com.oriole.wisepen.market.api.domain.base;

import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import lombok.AllArgsConstructor;import lombok.Data;
import lombok.NoArgsConstructor;import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class InfoPointChangeBase implements Serializable {
    private Long userId;
    private Integer amount;
    private InfoPointChangeType changeType;
}


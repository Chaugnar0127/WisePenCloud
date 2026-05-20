package com.oriole.wisepen.user.api.domain.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InfoPointTradeRevokeRequest {
    @NotNull
    private Long relatedId;

    private String reason;
}

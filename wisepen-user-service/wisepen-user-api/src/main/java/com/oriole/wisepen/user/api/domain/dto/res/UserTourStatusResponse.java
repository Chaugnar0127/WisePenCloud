package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.enums.TourCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTourStatusResponse {
    private TourCode tourCode;
    private Boolean completed;
}

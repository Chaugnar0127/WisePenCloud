package com.oriole.wisepen.user.api.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckinResponse {

    private Long checkinId;
    private LocalDate checkinDate;
    private Integer rewardAmount;
    private Integer infoPointBalance;
}

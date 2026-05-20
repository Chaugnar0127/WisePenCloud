package com.oriole.wisepen.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellReviewInfo {
    private Boolean approved;
    private String comment;
    private String reviewerId;
    private LocalDateTime reviewedAt;
}

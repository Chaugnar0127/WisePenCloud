package com.oriole.wisepen.web.app.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageVersionInfoResponse {
    private String resourceId;
    private Integer version;
    private Long size;
    private String contentHash;
    private Long creatorUserId;
    private LocalDateTime createTime;
}

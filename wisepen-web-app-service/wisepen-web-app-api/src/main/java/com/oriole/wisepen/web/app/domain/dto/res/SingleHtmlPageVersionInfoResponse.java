package com.oriole.wisepen.web.app.domain.dto.res;

import com.oriole.wisepen.web.app.domain.base.SingleHtmlPageVersionBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SingleHtmlPageVersionInfoResponse extends SingleHtmlPageVersionBase {
    private String singleHtmlPageId;
    private String resourceId;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

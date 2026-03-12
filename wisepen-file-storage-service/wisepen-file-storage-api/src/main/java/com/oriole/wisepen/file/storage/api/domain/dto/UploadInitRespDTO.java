package com.oriole.wisepen.file.storage.api.domain.dto;

import com.oriole.wisepen.file.storage.api.domain.base.UploadUrlBase;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UploadInitRespDTO extends UploadUrlBase implements Serializable {
    /** 是否触发秒传 */
    private Boolean flashUploaded;
    private String domain;
    private String objectKey;
}
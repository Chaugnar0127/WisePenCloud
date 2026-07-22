package com.oriole.wisepen.media.api.domain.base;

import com.oriole.wisepen.media.api.enums.MediaStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaStatus {
    private MediaStatusEnum status;
    private String errorMessage;

    public MediaStatus(MediaStatusEnum status) {
        this.status = status;
    }

    public MediaStatus(String errorMessage) {
        this.status = MediaStatusEnum.FAILED;
        this.errorMessage = errorMessage;
    }
}

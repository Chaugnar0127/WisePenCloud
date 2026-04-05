package com.oriole.wisepen.document.api.domain.base;

import com.oriole.wisepen.document.api.enums.DocumentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatus {
    private DocumentStatusEnum status;
    private String errorMessage;

    public DocumentStatus(DocumentStatusEnum status) {
        this.status = status;
    }

    public DocumentStatus(String errorMessage) {
        this.status = DocumentStatusEnum.FAILED;
        this.errorMessage = errorMessage;
    }
}

package com.oriole.wisepen.document.api.domain.dto.req;

import com.oriole.wisepen.document.api.constant.DocumentValidationMsg;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档上传初始化请求
 */
@Data
public class DocumentUploadInitRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = DocumentValidationMsg.FILENAME_EMPTY)
    private String filename;

    @NotBlank(message = DocumentValidationMsg.EXTENSION_EMPTY)
    private String extension;

    @NotBlank(message = DocumentValidationMsg.MD5_EMPTY)
    private String md5;

    @NotNull(message = DocumentValidationMsg.FILE_SIZE_NULL)
    private Long size;
}

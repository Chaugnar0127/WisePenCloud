package com.oriole.wisepen.system.api.domain.dto.req;

import com.oriole.wisepen.system.api.enums.FeedbackType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xiong Heng
 */
@Data
public class FeedbackRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String content;
    private String contact;
    @Size(max = 1024, message = "图片地址长度不能超过1024字符")
    private String imageUrl;
    private String browser;
    private FeedbackType type;
}

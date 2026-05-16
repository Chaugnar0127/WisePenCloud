package com.oriole.wisepen.resource.domain.dto;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阅读事件上报请求体。
 * <p>
 * 由 note/document 等业务服务在用户查看详情时调用，
 * 资源服务据此执行去重判定并更新 readCount。
 * 预览接口不应调用此接口。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceReadRecordReqDTO {

    /** 被阅读的资源ID，必填 */
    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;

    /** 阅读者的用户ID，必填，用于去重判定 */
    @NotNull(message = ResourceValidationMsg.USER_ID_NOT_NULL)
    private Long userId;

    /**
     * 阅读来源标识，必填。
     * 建议值：NOTE_INFO、DOC_INFO、SKILL_INFO
     */
    @NotBlank(message = ResourceValidationMsg.READ_SOURCE_NOT_BLANK)
    private String source;

    /**
     * 可选：调用方的业务关联ID，用于日志排查与跨服务问题定位。
     * 不替代 OTel 自动生成的 trace，允许为空。
     */
    private String requestId;
}

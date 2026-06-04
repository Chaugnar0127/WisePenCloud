package com.oriole.wisepen.ai.asset.exception;

import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.ResultKey;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.common.core.exception.ErrorReason;
import com.oriole.wisepen.ai.asset.constant.AIAssetSubject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Skill 微服务(9)专属业务错误
 */
@Getter
@AllArgsConstructor
public enum SkillError implements IResult {

    SKILL_NOT_FOUND(9111, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.NOT_FOUND), "Skill 不存在"),
    SKILL_OWNER_MISMATCH(9121, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.PERMISSION_DENIED), "当前用户不是 Skill 所有者"),
    SKILL_VERSION_INVALID(9131, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.INVALID), "Skill 版本不合法"),
    SKILL_VERSION_IS_NOT_DRAFT(9131, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.INVALID), "Skill 版本不是草稿版本"),
    SKILL_CORE_ASSET_NOT_FOUND(9131, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.INVALID), "Skill 关键资源丢失"),
    SKILL_CORE_ASSET_NOT_READY(9131, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.INVALID), "Skill 资源未就绪"),
    SKILL_RELATIVE_PATH_INVALID(9132, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.INVALID), "Skill 相对路径不合法"),
    SKILL_ASSET_UPLOAD_URL_APPLY_FAILED(9141, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.FAILED), "初始化 Skill 文件上传失败"),
    SKILL_RESOURCE_REGISTER_FAILED(9144, new ResultKey(BusinessDomain.SKILL, AIAssetSubject.SKILL, ErrorReason.FAILED), "注册 Skill 资源失败");

    private final Integer code;
    private final ResultKey key;
    private final String msg;
}

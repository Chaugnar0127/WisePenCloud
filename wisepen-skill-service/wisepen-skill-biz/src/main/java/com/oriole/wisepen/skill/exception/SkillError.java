package com.oriole.wisepen.skill.exception;

import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.ResultKey;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.common.core.exception.ErrorReason;
import com.oriole.wisepen.skill.constant.SkillSubject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Skill 微服务(9)专属业务错误
 */
@Getter
@AllArgsConstructor
public enum SkillError implements IResult {

    SKILL_NOT_FOUND(9111, new ResultKey(BusinessDomain.SKILL, SkillSubject.SKILL, ErrorReason.NOT_FOUND), "Skill 不存在"),
    SKILL_OWNER_MISMATCH(9121, new ResultKey(BusinessDomain.SKILL, SkillSubject.SKILL, ErrorReason.PERMISSION_DENIED), "当前用户不是 Skill 所有者"),
    SKILL_VERSION_INVALID(9131, new ResultKey(BusinessDomain.SKILL, SkillSubject.SKILL, ErrorReason.INVALID), "Skill 版本不合法"),
    SKILL_RELATIVE_PATH_INVALID(9132, new ResultKey(BusinessDomain.SKILL, SkillSubject.SKILL, ErrorReason.INVALID), "Skill 相对路径不合法"),
    SKILL_UPLOAD_INIT_FAILED(9141, new ResultKey(BusinessDomain.SKILL, SkillSubject.SKILL, ErrorReason.FAILED), "初始化 Skill 文件上传失败"),
    SKILL_RESOURCE_REGISTER_FAILED(9144, new ResultKey(BusinessDomain.SKILL, SkillSubject.SKILL, ErrorReason.FAILED), "注册 Skill 资源失败");

    private final Integer code;
    private final ResultKey key;
    private final String msg;
}

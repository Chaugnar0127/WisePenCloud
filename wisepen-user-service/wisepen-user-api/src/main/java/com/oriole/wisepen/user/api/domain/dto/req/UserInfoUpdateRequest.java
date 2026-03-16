package com.oriole.wisepen.user.api.domain.dto.req;

import com.oriole.wisepen.user.api.enums.DegreeLevel;
import com.oriole.wisepen.user.api.enums.GenderType;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户更新个人信息 DTO（仅允许修改的字段）
 */
@Data
public class UserInfoUpdateRequest implements Serializable {
    private String nickname;
    private String avatar; // 预留 URL

    private String realName;
    private GenderType sex;

    // 教育信息
    private String college;
    private String major; // 学生可修改
    private String className; // 学生可修改
    private Integer enrollmentYear; // 只读，后端解析为只读但仍可包含
    private DegreeLevel degreeLevel; // 只读/不可改

    // 老师字段
    private String academicTitle; // 仅老师可修改
}

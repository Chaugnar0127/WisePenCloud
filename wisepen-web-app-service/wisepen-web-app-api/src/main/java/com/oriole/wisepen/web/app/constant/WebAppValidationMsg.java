package com.oriole.wisepen.web.app.constant;

public interface WebAppValidationMsg {
    String RESOURCE_ID_NOT_BLANK = "资源 ID 不能为空";
    String OWNER_USER_ID_NOT_NULL = "资源所有者用户 ID 不能为空";
    String OPERATOR_USER_ID_NOT_NULL = "操作者用户 ID 不能为空";
    String OPERATOR_GROUP_ROLES_NOT_NULL = "操作者小组角色不能为空";
    String TITLE_NOT_BLANK = "页面标题不能为空";
    String FILENAME_NOT_BLANK = "文件名不能为空";
    String MD5_NOT_BLANK = "文件 MD5 不能为空";
    String EXPECTED_SIZE_NOT_NULL = "预期文件大小必须大于 0";
}

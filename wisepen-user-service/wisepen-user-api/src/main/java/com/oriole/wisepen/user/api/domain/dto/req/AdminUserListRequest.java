package com.oriole.wisepen.user.api.domain.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理后台用户列表请求 DTO
 */
@Data
public class AdminUserListRequest implements Serializable {
    private int page = 1;
    private int size = 20;
    private String keyword;
    private Integer status;
    private Integer identityType;
}

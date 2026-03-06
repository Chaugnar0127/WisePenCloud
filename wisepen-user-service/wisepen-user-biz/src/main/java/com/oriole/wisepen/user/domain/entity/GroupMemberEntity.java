package com.oriole.wisepen.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oriole.wisepen.user.api.domain.base.GroupMemberBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@TableName("sys_group_member")
public class GroupMemberEntity extends GroupMemberBase {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long groupId; // 组ID
    private Long userId; // 用户ID
}
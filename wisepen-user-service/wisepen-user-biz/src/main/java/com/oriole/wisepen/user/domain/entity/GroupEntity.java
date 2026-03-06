package com.oriole.wisepen.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oriole.wisepen.common.core.domain.enums.GroupType;
import com.oriole.wisepen.user.api.domain.base.GroupInfoBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@TableName("sys_group")
public class GroupEntity extends GroupInfoBase {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long groupId;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    private Date createTime;

    private Date updateTime;
}
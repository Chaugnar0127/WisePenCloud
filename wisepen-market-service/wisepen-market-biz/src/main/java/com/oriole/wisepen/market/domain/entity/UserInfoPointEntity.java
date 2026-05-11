package com.oriole.wisepen.market.domain.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_mkt_user_info_point")
public class UserInfoPointEntity implements Serializable {

    @TableId(type = IdType.INPUT)
    private Long userId;

    @TableField(value = "info_point_balance")
    private Integer infoPointBalance;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

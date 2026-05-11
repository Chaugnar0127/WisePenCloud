package com.oriole.wisepen.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_mkt_info_point_record")
public class InfoPointTransactionRecordEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;


    private Long userId;
    private Integer amount;
    private InfoPointChangeType changeType;

    //关联业务ID（如订单ID）
    private String relatedId;

    private Integer balanceAfter;

    // JSON 格式的元数据
    private String meta;

    // 操作人ID（管理员操作时记录）
    private Long operatorId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

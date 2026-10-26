package com.oriole.wisepen.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oriole.wisepen.system.api.enums.FeedbackStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class FeedbackEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String contact;
    @TableField("image_url")
    private String imageUrl;
    private String browser;
    private boolean bugReport;
    private boolean suggestion;
    private boolean consultation;
    private boolean complaint;
    private boolean other;
    private FeedbackStatus status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

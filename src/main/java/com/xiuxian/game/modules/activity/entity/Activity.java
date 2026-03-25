package com.xiuxian.game.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动配置实体类
 */
@Data
@TableName("activities")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("activity_name")
    private String name;

    private String description;

    private String activityType; // DAILY_LOGIN/RECHARGE/COMBAT/CULTIVATION

    private String status; // DRAFT/ACTIVE/ENDED

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String rules; // JSON格式

    private String rewards; // JSON格式

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

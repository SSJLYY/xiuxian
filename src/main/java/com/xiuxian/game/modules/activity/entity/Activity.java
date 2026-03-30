package com.xiuxian.game.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动配置实体类
 *
 * <p>存储活动的基本信息，包括活动名称、类型、时间范围、规则和奖励等。</p>
 *
 * <p>活动状态：</p>
 * <ul>
 *   <li>DRAFT - 草稿状态，活动尚未开始</li>
 *   <li>ACTIVE - 进行中，玩家可以参与</li>
 *   <li>ENDED - 已结束，不再接受参与</li>
 * </ul>
 *
 * <p>活动类型：</p>
 * <ul>
 *   <li>DAILY_LOGIN - 每日登录活动</li>
 *   <li>RECHARGE - 充值活动</li>
 *   <li>COMBAT - 战斗活动</li>
 *   <li>CULTIVATION - 修炼活动</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
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

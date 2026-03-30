package com.xiuxian.game.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家活动进度实体类
 *
 * <p>记录玩家参与活动的进度信息，包括：</p>
 * <ul>
 *   <li>活动ID和玩家ID</li>
 *   <li>进度数据（JSON格式）</li>
 *   <li>完成状态</li>
 *   <li>奖励领取状态</li>
 *   <li>完成时间和奖励领取时间</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Data
@TableName("player_activities")
public class PlayerActivityProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("activity_id")
    private Integer activityId;

    @TableField("player_id")
    private Integer playerId;

    private String progress; // JSON格式

    @TableField("is_completed")
    private Boolean completed;

    @TableField("is_rewarded")
    private Boolean rewarded;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("rewarded_at")
    private LocalDateTime rewardedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

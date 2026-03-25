package com.xiuxian.game.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家活动进度实体�?
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

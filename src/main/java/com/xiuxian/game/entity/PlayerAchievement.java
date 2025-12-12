package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 玩家成就实体类
 */
@Data
@TableName("player_achievements")
public class PlayerAchievement {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "player_id")
    private Integer playerId;
    
    @TableField(value = "achievement_id")
    private Integer achievementId;
    
    private Integer progress;
    
    @TableField(value = "is_completed")
    private Boolean isCompleted;
    
    @TableField(value = "is_claimed")
    private Boolean isClaimed;
    
    @TableField(value = "completed_at")
    private LocalDateTime completedAt;
    
    @TableField(value = "claimed_at")
    private LocalDateTime claimedAt;
}

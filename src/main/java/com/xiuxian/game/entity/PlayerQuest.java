package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("player_quests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerQuest {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "quest_id")
    private Integer questId;

    @TableField(value = "current_progress")
    @Builder.Default
    private Integer currentProgress = 0;

    @TableField(value = "completed")
    @Builder.Default
    private Boolean completed = false;

    @TableField(value = "reward_claimed")
    @Builder.Default
    private Boolean rewardClaimed = false;

    @TableField(value = "completed_at")
    private LocalDateTime completedAt;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
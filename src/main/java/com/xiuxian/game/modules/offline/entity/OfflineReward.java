package com.xiuxian.game.modules.offline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("offline_rewards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineReward {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "offline_minutes")
    @Builder.Default
    private Integer offlineMinutes = 0;

    @TableField(value = "exp_gained")
    @Builder.Default
    private Integer expGained = 0;

    @TableField(value = "spirit_stones_gained")
    @Builder.Default
    private Integer spiritStonesGained = 0;

    @TableField(value = "claimed")
    @Builder.Default
    private Boolean claimed = false;

    @TableField(value = "claimed_at")
    private LocalDateTime claimedAt;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;
}


package com.xiuxian.game.modules.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家宠物实体�?
 * 对应数据�?player_pets �?
 */
@TableName("player_pets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPet {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "pet_id")
    private Integer petId;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "level")
    @Builder.Default
    private Integer level = 1;

    @TableField(value = "exp")
    @Builder.Default
    private Long exp = 0L;

    @TableField(value = "exp_to_next")
    @Builder.Default
    private Long expToNext = 100L;

    @TableField(value = "attack")
    @Builder.Default
    private Integer attack = 0;

    @TableField(value = "defense")
    @Builder.Default
    private Integer defense = 0;

    @TableField(value = "health")
    @Builder.Default
    private Integer health = 0;

    @TableField(value = "max_health")
    @Builder.Default
    private Integer maxHealth = 0;

    @TableField(value = "speed")
    @Builder.Default
    private Integer speed = 0;

    @TableField(value = "loyalty")
    @Builder.Default
    private Integer loyalty = 50; // 忠诚�?0-100)

    @TableField(value = "hunger")
    @Builder.Default
    private Integer hunger = 100; // 饱食�?0-100)

    @TableField(value = "is_active")
    @Builder.Default
    private Boolean isActive = false; // 是否出战

    @TableField(value = "is_locked")
    @Builder.Default
    private Boolean isLocked = false; // 是否锁定

    @TableField(value = "total_battles")
    @Builder.Default
    private Integer totalBattles = 0;

    @TableField(value = "total_wins")
    @Builder.Default
    private Integer totalWins = 0;

    @TableField(value = "last_feed_time")
    private LocalDateTime lastFeedTime;

    @TableField(value = "last_train_time")
    private LocalDateTime lastTrainTime;

    @TableField(value = "captured_at")
    private LocalDateTime capturedAt;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 计算胜率
     */
    public double getWinRate() {
        if (totalBattles == 0) {
            return 0.0;
        }
        return (double) totalWins / totalBattles * 100;
    }

    /**
     * 检查是否需要喂�?
     */
    public boolean needsFeeding() {
        return hunger < 30;
    }

    /**
     * 检查忠诚度状�?
     */
    public String getLoyaltyStatus() {
        if (loyalty >= 80) return "忠诚";
        if (loyalty >= 50) return "友好";
        if (loyalty >= 20) return "一�?;
        return "疏远";
    }
}


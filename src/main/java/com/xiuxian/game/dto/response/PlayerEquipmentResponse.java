package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEquipmentResponse {
    private Integer id;
    private Integer playerId;
    private Integer equipmentId;
    private String slot;
    private Boolean equipped;
    private Integer durability;
    private Integer maxDurability;
    private Integer enhanceLevel;
    private Integer enhanceAttackBonus;
    private Integer enhanceDefenseBonus;
    private Integer enhanceHealthBonus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 装备信息
    private String name;
    private String description;
    private String type;
    private Integer level;
    private Integer quality;
    private Integer attackBonus;
    private Integer defenseBonus;
    private Integer healthBonus;
    private Integer manaBonus;
    private Integer speedBonus;
    private Integer requiredLevel;
    private Integer price;

    // 计算总属性
    public int getTotalAttackBonus() {
        return attackBonus + enhanceAttackBonus;
    }

    public int getTotalDefenseBonus() {
        return defenseBonus + enhanceDefenseBonus;
    }

    public int getTotalHealthBonus() {
        return healthBonus + enhanceHealthBonus;
    }
}
package com.xiuxian.game.modules.equipment.entity;

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
 * 玩家装备实体类
 *
 * <p>记录玩家拥有的装备信息，包括装备状态、耐久度、强化等级等。</p>
 *
 * <p>装备槽位：</p>
 * <ul>
 *   <li>weapon - 武器槽位</li>
 *   <li>armor - 防具槽位</li>
 *   <li>accessory - 饰品槽位</li>
 * </ul>
 *
 * <p>装备状态：</p>
 * <ul>
 *   <li>equipped - 是否已装备</li>
 *   <li>durability - 当前耐久度</li>
 *   <li>maxDurability - 最大耐久度</li>
 *   <li>enhanceLevel - 强化等级</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
 */
@TableName("player_equipment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEquipment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "player_id")
    private Integer playerId;

    @TableField(value = "equipment_id")
    private Integer equipmentId;

    @TableField(value = "slot")
    private String slot;

    @TableField(value = "is_equipped")
    @Builder.Default
    private Boolean equipped = false;

    @TableField(value = "durability")
    @Builder.Default
    private Integer durability = 100;

    @TableField(value = "max_durability")
    @Builder.Default
    private Integer maxDurability = 100;

    @TableField(value = "enhance_level")
    @Builder.Default
    private Integer enhanceLevel = 0; // 强化等级

    @TableField(value = "enhance_attack_bonus")
    @Builder.Default
    private Integer enhanceAttackBonus = 0;

    @TableField(value = "enhance_defense_bonus")
    @Builder.Default
    private Integer enhanceDefenseBonus = 0;

    @TableField(value = "enhance_health_bonus")
    @Builder.Default
    private Integer enhanceHealthBonus = 0;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    // Getter and Setter methods for Lombok compatibility
    public Boolean getEquipped() {
        return equipped;
    }

    public void setEquipped(Boolean equipped) {
        this.equipped = equipped;
    }

    // 业务方法
    public int getTotalAttackBonus(Equipment equipment) {
        return defaultInt(equipment == null ? null : equipment.getAttackBonus()) + defaultInt(enhanceAttackBonus);
    }

    public int getTotalDefenseBonus(Equipment equipment) {
        return defaultInt(equipment == null ? null : equipment.getDefenseBonus()) + defaultInt(enhanceDefenseBonus);
    }

    public int getTotalHealthBonus(Equipment equipment) {
        return defaultInt(equipment == null ? null : equipment.getHealthBonus()) + defaultInt(enhanceHealthBonus);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}

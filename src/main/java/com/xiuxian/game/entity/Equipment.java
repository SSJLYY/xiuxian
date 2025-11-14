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

@TableName("equipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "type")
    private String type; // 武器、防具、饰品等

    @TableField(value = "level")
    @Builder.Default
    private Integer level = 1;

    @TableField(value = "quality")
    @Builder.Default
    private Integer quality = 1; // 1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说

    @TableField(value = "attack_bonus")
    @Builder.Default
    private Integer attackBonus = 0;

    @TableField(value = "defense_bonus")
    @Builder.Default
    private Integer defenseBonus = 0;

    @TableField(value = "health_bonus")
    @Builder.Default
    private Integer healthBonus = 0;

    @TableField(value = "mana_bonus")
    @Builder.Default
    private Integer manaBonus = 0;

    @TableField(value = "speed_bonus")
    @Builder.Default
    private Integer speedBonus = 0;

    @TableField(value = "required_level")
    @Builder.Default
    private Integer requiredLevel = 1;

    @TableField(value = "price")
    @Builder.Default
    private Integer price = 0;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    // 业务方法
    public boolean canEquip(int playerLevel) {
        return playerLevel >= requiredLevel;
    }

    public int getTotalStats() {
        return attackBonus + defenseBonus + healthBonus + manaBonus + speedBonus;
    }

    public String getQualityName() {
        switch (quality) {
            case 1: return "普通";
            case 2: return "精良";
            case 3: return "稀有";
            case 4: return "史诗";
            case 5: return "传说";
            default: return "未知";
        }
    }

    public boolean isWeapon() {
        return "武器".equals(type);
    }

    public boolean isArmor() {
        return "防具".equals(type);
    }

    public boolean isAccessory() {
        return "饰品".equals(type);
    }
}
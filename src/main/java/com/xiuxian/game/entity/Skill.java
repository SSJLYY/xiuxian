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

@TableName("skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "level")
    private Integer level;

    @TableField(value = "max_level")
    private Integer maxLevel;

    @TableField(value = "base_damage")
    private Double baseDamage;

    @TableField(value = "damage_per_level")
    private Double damagePerLevel;

    @TableField(value = "cooldown")
    private Integer cooldown;

    @TableField(value = "mana_cost")
    private Integer manaCost;

    @TableField(value = "skill_type")
    private String skillType; // 攻击、防御、辅助等

    @TableField(value = "element")
    private String element; // 金、木、水、火、土

    @TableField(value = "unlock_level")
    private Integer unlockLevel; // 角色等级解锁要求

    @TableField(value = "required_spirit_stones")
    private Integer requiredSpiritStones;

    @TableField(value = "icon")
    private String icon;

    @TableField(value = "animation")
    private String animation;

    @TableField(value = "active")
    @Builder.Default
    private Boolean active = true;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
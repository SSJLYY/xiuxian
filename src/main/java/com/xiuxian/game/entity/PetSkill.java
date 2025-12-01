package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物技能实体类
 * 对应数据库 pet_skills 表
 */
@TableName("pet_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetSkill {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "description")
    private String description;

    @TableField(value = "skill_type")
    private String skillType; // 攻击、防御、辅助

    @TableField(value = "base_damage")
    @Builder.Default
    private Double baseDamage = 0.0;

    @TableField(value = "damage_multiplier")
    @Builder.Default
    private BigDecimal damageMultiplier = BigDecimal.ONE;

    @TableField(value = "cooldown")
    @Builder.Default
    private Integer cooldown = 0;

    @TableField(value = "energy_cost")
    @Builder.Default
    private Integer energyCost = 0;

    @TableField(value = "unlock_pet_level")
    @Builder.Default
    private Integer unlockPetLevel = 1;

    @TableField(value = "active")
    @Builder.Default
    private Boolean active = true;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}

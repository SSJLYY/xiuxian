package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("player_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "nickname")
    private String nickname;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Long exp = 0L;

    @TableField(value = "exp_to_next")
    @Builder.Default
    private Long expToNext = 100L;

    @Builder.Default
    private String realm = "练气期";

    @Builder.Default
    private BigDecimal cultivationSpeed = BigDecimal.ONE;

    @TableField(value = "spirit_stones")
    @Builder.Default
    private Long spiritStones = 1000L;

    @TableField(value = "cultivation_points")
    @Builder.Default
    private Long cultivationPoints = 0L;

    @TableField(value = "contribution_points")
    @Builder.Default
    private Long contributionPoints = 0L;

    @TableField(value = "last_online_time")
    @Builder.Default
    private LocalDateTime lastOnlineTime = LocalDateTime.now();

    @TableField(value = "total_cultivation_time")
    @Builder.Default
    private Long totalCultivationTime = 0L;

    // 修炼状态
    @TableField(value = "is_cultivating")
    @JsonProperty("isCultivating")
    @Builder.Default
    private Boolean isCultivating = false;

    @TableField(value = "last_cultivation_start")
    private LocalDateTime lastCultivationStart;

    @TableField(value = "last_cultivation_end")
    private LocalDateTime lastCultivationEnd;

    // 基础属性
    @Builder.Default
    private Integer attack = 10;

    @Builder.Default
    private Integer defense = 5;

    @Builder.Default
    private Integer health = 100;

    @Builder.Default
    private Integer mana = 50;

    @Builder.Default
    private Integer speed = 10;

    // 装备加成属性
    @TableField(value = "equipment_attack_bonus")
    @Builder.Default
    private Integer equipmentAttackBonus = 0;

    @TableField(value = "equipment_defense_bonus")
    @Builder.Default
    private Integer equipmentDefenseBonus = 0;

    @TableField(value = "equipment_health_bonus")
    @Builder.Default
    private Integer equipmentHealthBonus = 0;

    @TableField(value = "equipment_mana_bonus")
    @Builder.Default
    private Integer equipmentManaBonus = 0;

    @TableField(value = "equipment_speed_bonus")
    @Builder.Default
    private Integer equipmentSpeedBonus = 0;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    // 装备加成计算方法
    public Integer getTotalHealth() {
        return this.health + getEquipmentHealthBonus();
    }

    public Integer getEquipmentAttackBonus() {
        return this.equipmentAttackBonus;
    }

    public void setEquipmentAttackBonus(Integer equipmentAttackBonus) {
        this.equipmentAttackBonus = equipmentAttackBonus;
    }

    public Integer getEquipmentDefenseBonus() {
        return this.equipmentDefenseBonus;
    }

    public void setEquipmentDefenseBonus(Integer equipmentDefenseBonus) {
        this.equipmentDefenseBonus = equipmentDefenseBonus;
    }

    public Integer getEquipmentHealthBonus() {
        return this.equipmentHealthBonus;
    }

    public void setEquipmentHealthBonus(Integer equipmentHealthBonus) {
        this.equipmentHealthBonus = equipmentHealthBonus;
    }

    public Integer getEquipmentManaBonus() {
        return this.equipmentManaBonus;
    }

    public void setEquipmentManaBonus(Integer equipmentManaBonus) {
        this.equipmentManaBonus = equipmentManaBonus;
    }

    public Integer getEquipmentSpeedBonus() {
        return this.equipmentSpeedBonus;
    }

    public void setEquipmentSpeedBonus(Integer equipmentSpeedBonus) {
        this.equipmentSpeedBonus = equipmentSpeedBonus;
    }
}
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

    @TableField(value = "exp")
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

    @TableField(value = "attribute_points")
    @Builder.Default
    private Integer attributePoints = 0;

    @TableField(value = "skill_points")
    @Builder.Default
    private Integer skillPoints = 0;

    @TableField(value = "last_online_time")
    @Builder.Default
    private LocalDateTime lastOnlineTime = LocalDateTime.now();

    @TableField(value = "last_login_at")
    private LocalDateTime lastLoginAt;

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
    
    // 新增：技能加成属性
    @TableField(value = "skill_attack_bonus")
    @Builder.Default
    private Integer skillAttackBonus = 0;

    @TableField(value = "skill_defense_bonus")
    @Builder.Default
    private Integer skillDefenseBonus = 0;

    @TableField(value = "skill_health_bonus")
    @Builder.Default
    private Integer skillHealthBonus = 0;

    @TableField(value = "skill_mana_bonus")
    @Builder.Default
    private Integer skillManaBonus = 0;

    @TableField(value = "skill_speed_bonus")
    @Builder.Default
    private Integer skillSpeedBonus = 0;

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
    
    // 新增：技能加成getter/setter方法
    public Integer getSkillAttackBonus() {
        return this.skillAttackBonus;
    }

    public void setSkillAttackBonus(Integer skillAttackBonus) {
        this.skillAttackBonus = skillAttackBonus;
    }

    public Integer getSkillDefenseBonus() {
        return this.skillDefenseBonus;
    }

    public void setSkillDefenseBonus(Integer skillDefenseBonus) {
        this.skillDefenseBonus = skillDefenseBonus;
    }

    public Integer getSkillHealthBonus() {
        return this.skillHealthBonus;
    }

    public void setSkillHealthBonus(Integer skillHealthBonus) {
        this.skillHealthBonus = skillHealthBonus;
    }

    public Integer getSkillManaBonus() {
        return this.skillManaBonus;
    }

    public void setSkillManaBonus(Integer skillManaBonus) {
        this.skillManaBonus = skillManaBonus;
    }

    public Integer getSkillSpeedBonus() {
        return this.skillSpeedBonus;
    }

    public void setSkillSpeedBonus(Integer skillSpeedBonus) {
        this.skillSpeedBonus = skillSpeedBonus;
    }
    
    // 新增：获取总属性的方法
    public Integer getTotalAttack() {
        return this.attack + getEquipmentAttackBonus() + getSkillAttackBonus();
    }
    
    public Integer getTotalDefense() {
        return this.defense + getEquipmentDefenseBonus() + getSkillDefenseBonus();
    }
    
    public Integer getTotalHealth() {
        return this.health + getEquipmentHealthBonus() + getSkillHealthBonus();
    }
    
    public Integer getTotalMana() {
        return this.mana + getEquipmentManaBonus() + getSkillManaBonus();
    }
    
    public Integer getTotalSpeed() {
        return this.speed + getEquipmentSpeedBonus() + getSkillSpeedBonus();
    }
}
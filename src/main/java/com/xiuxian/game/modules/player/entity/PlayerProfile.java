package com.xiuxian.game.modules.player.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 玩家档案实体类
 * 对应数据库 player_profiles 表，存储玩家的游戏数据和状态
 * 
 * @author xiuxian
 * @version 1.0
 */
@TableName("player_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {

    /**
     * 玩家档案ID，主键，自增长
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的用户ID，对应 users 表的 id
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 玩家昵称，游戏内显示的名称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 玩家等级，默认1级
     */
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

    @TableField(value = "cultivation_speed")
    @Builder.Default
    private BigDecimal cultivationSpeed = BigDecimal.ONE;

    @TableField(value = "cultivation_type")
    @Builder.Default
    private String cultivationType = "normal";

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

    @TableField(value = "total_battles")
    @JsonProperty("totalBattles")
    @Builder.Default
    private Integer totalBattles = 0;

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
    @TableField(exist = false)
    @Builder.Default
    private Integer skillHealthBonus = 0;
    
    @TableField(exist = false)
    @Builder.Default
    private Integer skillManaBonus = 0;
    
    @TableField(exist = false)
    @Builder.Default
    private Integer skillAttackBonus = 0;
    
    @TableField(exist = false)
    @Builder.Default
    private Integer skillDefenseBonus = 0;
    
    @TableField(exist = false)
    @Builder.Default
    private Integer skillSpeedBonus = 0;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 乐观锁版本号（MyBatis-Plus @Version）
     * 每次更新自动 +1，并发冲突时抛出 OptimisticLockerInnerInterceptor 异常
     */
    @Version
    @TableField(value = "version")
    private Integer version;

    /**
     * 获取总攻击力（基础 + 装备加成）
     */
    public Integer getTotalAttack() {
        return this.attack + (this.equipmentAttackBonus != null ? this.equipmentAttackBonus : 0);
    }
    
    /**
     * 获取总防御力（基础 + 装备加成）
     */
    public Integer getTotalDefense() {
        return this.defense + (this.equipmentDefenseBonus != null ? this.equipmentDefenseBonus : 0);
    }
    
    /**
     * 获取总生命值（基础 + 装备加成）
     */
    public Integer getTotalHealth() {
        return this.health + (this.equipmentHealthBonus != null ? this.equipmentHealthBonus : 0);
    }
    
    /**
     * 获取总法力值（基础 + 装备加成）
     */
    public Integer getTotalMana() {
        return this.mana + (this.equipmentManaBonus != null ? this.equipmentManaBonus : 0);
    }
    
    /**
     * 获取总速度（基础 + 装备加成）
     */
    public Integer getTotalSpeed() {
        return this.speed + (this.equipmentSpeedBonus != null ? this.equipmentSpeedBonus : 0);
    }
    
    // 新增：技能加成的setter和getter方法
    public Integer getSkillHealthBonus() {
        return skillHealthBonus;
    }

    public void setSkillHealthBonus(Integer skillHealthBonus) {
        this.skillHealthBonus = skillHealthBonus;
    }

    public Integer getSkillManaBonus() {
        return skillManaBonus;
    }

    public void setSkillManaBonus(Integer skillManaBonus) {
        this.skillManaBonus = skillManaBonus;
    }

    public Integer getSkillAttackBonus() {
        return skillAttackBonus;
    }

    public void setSkillAttackBonus(Integer skillAttackBonus) {
        this.skillAttackBonus = skillAttackBonus;
    }

    public Integer getSkillDefenseBonus() {
        return skillDefenseBonus;
    }

    public void setSkillDefenseBonus(Integer skillDefenseBonus) {
        this.skillDefenseBonus = skillDefenseBonus;
    }

    public Integer getSkillSpeedBonus() {
        return skillSpeedBonus;
    }

    public void setSkillSpeedBonus(Integer skillSpeedBonus) {
        this.skillSpeedBonus = skillSpeedBonus;
    }
}

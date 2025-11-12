package com.xiuxian.game.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Long exp = 0L;

    @Column(name = "exp_to_next", nullable = false)
    @Builder.Default
    private Long expToNext = 100L;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String realm = "练气期";

    @Column(name = "cultivation_speed", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cultivationSpeed = BigDecimal.ONE;

    @Column(name = "spirit_stones", nullable = false)
    @Builder.Default
    private Long spiritStones = 1000L;

    @Column(name = "cultivation_points", nullable = false)
    @Builder.Default
    private Long cultivationPoints = 0L;

    @Column(name = "contribution_points", nullable = false)
    @Builder.Default
    private Long contributionPoints = 0L;

    @Column(name = "last_online_time", nullable = false)
    @Builder.Default
    private LocalDateTime lastOnlineTime = LocalDateTime.now();

    @Column(name = "total_cultivation_time", nullable = false)
    @Builder.Default
    private Long totalCultivationTime = 0L;

    // 修炼状态
    @Column(name = "is_cultivating")
    @JsonProperty("isCultivating")
    @Builder.Default
    private Boolean isCultivating = false;

    @Column(name = "last_cultivation_start")
    private LocalDateTime lastCultivationStart;

    @Column(name = "last_cultivation_end")
    private LocalDateTime lastCultivationEnd;

    // 基础属性
    @Column(nullable = false)
    @Builder.Default
    private Integer attack = 10;

    @Column(nullable = false)
    @Builder.Default
    private Integer defense = 5;

    @Column(nullable = false)
    @Builder.Default
    private Integer health = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer mana = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer speed = 10;

    // 装备加成属性
    @Column(name = "equipment_attack_bonus", nullable = false)
    @Builder.Default
    private Integer equipmentAttackBonus = 0;

    @Column(name = "equipment_defense_bonus", nullable = false)
    @Builder.Default
    private Integer equipmentDefenseBonus = 0;

    @Column(name = "equipment_health_bonus", nullable = false)
    @Builder.Default
    private Integer equipmentHealthBonus = 0;

    @Column(name = "equipment_mana_bonus", nullable = false)
    @Builder.Default
    private Integer equipmentManaBonus = 0;

    @Column(name = "equipment_speed_bonus", nullable = false)
    @Builder.Default
    private Integer equipmentSpeedBonus = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
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
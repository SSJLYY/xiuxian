package com.xiuxian.game.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    // 战斗属性
    @Column(nullable = false)
    @Builder.Default
    private Integer attack = 10;

    @Column(nullable = false)
    @Builder.Default
    private Integer defense = 10;

    @Column(nullable = false)
    @Builder.Default
    private Integer health = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer mana = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer speed = 10;

    // 装备加成属性（临时计算，不持久化）
    @Transient
    private Integer equipmentAttackBonus = 0;

    @Transient
    private Integer equipmentDefenseBonus = 0;

    @Transient
    private Integer equipmentHealthBonus = 0;

    @Transient
    private Integer equipmentManaBonus = 0;

    @Transient
    private Integer equipmentSpeedBonus = 0;

    /**
     * 获取总攻击力（基础攻击 + 装备加成）
     */
    public Integer getTotalAttack() {
        return attack + equipmentAttackBonus;
    }

    /**
     * 获取总防御力（基础防御 + 装备加成）
     */
    public Integer getTotalDefense() {
        return defense + equipmentDefenseBonus;
    }

    /**
     * 获取总生命值（基础生命 + 装备加成）
     */
    public Integer getTotalHealth() {
        return health + equipmentHealthBonus;
    }

    /**
     * 获取总灵力值（基础灵力 + 装备加成）
     */
    public Integer getTotalMana() {
        return mana + equipmentManaBonus;
    }

    /**
     * 获取总速度（基础速度 + 装备加成）
     */
    public Integer getTotalSpeed() {
        return speed + equipmentSpeedBonus;
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
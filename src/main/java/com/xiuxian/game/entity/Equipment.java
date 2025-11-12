package com.xiuxian.game.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "equipments")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String type; // 武器、防具、饰品等

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer quality = 1; // 1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说

    @Column(name = "attack_bonus", nullable = false)
    @Builder.Default
    private Integer attackBonus = 0;

    @Column(name = "defense_bonus", nullable = false)
    @Builder.Default
    private Integer defenseBonus = 0;

    @Column(name = "health_bonus", nullable = false)
    @Builder.Default
    private Integer healthBonus = 0;

    @Column(name = "mana_bonus", nullable = false)
    @Builder.Default
    private Integer manaBonus = 0;

    @Column(name = "speed_bonus", nullable = false)
    @Builder.Default
    private Integer speedBonus = 0;

    @Column(name = "required_level", nullable = false)
    @Builder.Default
    private Integer requiredLevel = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer price = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        // 设置默认值
        if (level == null) level = 1;
        if (quality == null) quality = 1;
        if (attackBonus == null) attackBonus = 0;
        if (defenseBonus == null) defenseBonus = 0;
        if (healthBonus == null) healthBonus = 0;
        if (manaBonus == null) manaBonus = 0;
        if (speedBonus == null) speedBonus = 0;
        if (requiredLevel == null) requiredLevel = 1;
        if (price == null) price = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
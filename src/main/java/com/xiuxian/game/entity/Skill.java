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
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer maxLevel;

    @Column(nullable = false)
    private Double baseDamage;

    @Column(nullable = false)
    private Double damagePerLevel;

    @Column(nullable = false)
    private Integer cooldown;

    @Column(nullable = false)
    private Integer manaCost;

    @Column(nullable = false)
    private String skillType; // 攻击、防御、辅助等

    @Column(nullable = false)
    private String element; // 金、木、水、火、土

    @Column(nullable = false)
    private Integer unlockLevel; // 角色等级解锁要求

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
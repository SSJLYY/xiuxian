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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String type; // 武器、防具、饰品等

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer quality; // 1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说

    @Column(nullable = false)
    private Integer attackBonus;

    @Column(nullable = false)
    private Integer defenseBonus;

    @Column(nullable = false)
    private Integer healthBonus;

    @Column(nullable = false)
    private Integer manaBonus;

    @Column(nullable = false)
    private Integer speedBonus;

    @Column(nullable = false)
    private Integer requiredLevel;

    @Column(nullable = false)
    private Integer price;

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
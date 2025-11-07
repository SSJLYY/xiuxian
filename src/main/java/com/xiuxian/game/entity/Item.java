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
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String type; // 消耗品、材料、任务物品等

    @Column(nullable = false)
    private Integer quality; // 1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说

    @Column(nullable = false)
    private Boolean stackable; // 是否可堆叠

    @Column(nullable = false)
    private Integer maxStack; // 最大堆叠数量

    @Column(nullable = false)
    private Integer price; // 价格（灵石）

    @Column(nullable = false)
    private Boolean sellable; // 是否可出售

    @Column(nullable = false)
    private Boolean usable; // 是否可使用

    @Column(nullable = false)
    private String effect; // 使用效果描述

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
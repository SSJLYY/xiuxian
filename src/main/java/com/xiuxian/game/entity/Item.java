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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String type; // 消耗品、材料、任务物品等

    @Column(nullable = false)
    @Builder.Default
    private Integer quality = 1; // 1-普通, 2-精良, 3-稀有, 4-史诗, 5-传说

    @Column(nullable = false)
    @Builder.Default
    private Boolean stackable = true; // 是否可堆叠

    @Column(name = "max_stack", nullable = false)
    @Builder.Default
    private Integer maxStack = 99; // 最大堆叠数量

    @Column(nullable = false)
    @Builder.Default
    private Integer price = 0; // 价格（灵石）

    @Column(nullable = false)
    @Builder.Default
    private Boolean sellable = true; // 是否可出售

    @Column(nullable = false)
    @Builder.Default
    private Boolean usable = true; // 是否可使用

    @Column(columnDefinition = "TEXT")
    private String effect; // 使用效果描述

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
        if (quality == null) quality = 1;
        if (stackable == null) stackable = true;
        if (maxStack == null) maxStack = 99;
        if (price == null) price = 0;
        if (sellable == null) sellable = true;
        if (usable == null) usable = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 业务方法
    public boolean canStackWith(Item other) {
        return this.stackable && other.stackable &&
                this.id.equals(other.id) &&
                this.maxStack > 1;
    }

    public int getRemainingStackSpace() {
        return maxStack;
    }
}
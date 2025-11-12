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
@Table(name = "shop_items")
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(nullable = false, length = 50)
    private String shopType; // general, equipment, materials, special

    @Column(name = "price_spirit_stones", nullable = false)
    @Builder.Default
    private Integer priceSpiritStones = 0;

    @Column(name = "price_contribution_points", nullable = false)
    @Builder.Default
    private Integer priceContributionPoints = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = -1; // -1表示无限库存

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

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
        // 确保默认值
        if (priceSpiritStones == null) priceSpiritStones = 0;
        if (priceContributionPoints == null) priceContributionPoints = 0;
        if (stock == null) stock = -1;
        if (isAvailable == null) isAvailable = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 添加业务逻辑方法
    public boolean isUnlimitedStock() {
        return stock == -1;
    }

    public boolean hasStock(int quantity) {
        return isUnlimitedStock() || (stock >= quantity);
    }

    public void decreaseStock(int quantity) {
        if (!isUnlimitedStock()) {
            this.stock = Math.max(0, this.stock - quantity);
        }
    }

    public void increaseStock(int quantity) {
        if (!isUnlimitedStock()) {
            this.stock += quantity;
        }
    }

    public long getTotalPrice(int quantity) {
        return (long) priceSpiritStones * quantity;
    }
}
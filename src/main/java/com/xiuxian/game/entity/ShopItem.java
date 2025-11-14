package com.xiuxian.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("shop_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItem {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "item_id")
    private Integer itemId;

    @TableField(value = "equipment_id")
    private Integer equipmentId;

    @TableField(value = "shop_type")
    private String shopType; // general, equipment, materials, special

    @TableField(value = "price_spirit_stones")
    @Builder.Default
    private Integer priceSpiritStones = 0;

    @TableField(value = "price_contribution_points")
    @Builder.Default
    private Integer priceContributionPoints = 0;

    @TableField(value = "stock")
    @Builder.Default
    private Integer stock = -1; // -1表示无限库存

    @TableField(value = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

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
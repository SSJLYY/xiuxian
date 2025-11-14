package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemResponse {
    private Long id;
    private Long itemId;
    private Long equipmentId;
    private String itemName;
    private String itemDescription;
    private String itemType;
    private Integer itemQuality;
    private String equipmentName;
    private String equipmentDescription;
    private String equipmentType;
    private Integer equipmentQuality;
    private Integer requiredLevel;
    private String shopType;
    private Integer priceSpiritStones;
    private Integer priceContributionPoints;
    private Integer stock;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 添加便捷方法
    public boolean isUnlimitedStock() {
        return stock == null || stock == -1;
    }

    public boolean canAffordWithSpiritStones(long playerSpiritStones) {
        return playerSpiritStones >= priceSpiritStones;
    }

    public boolean isInStock() {
        return isUnlimitedStock() || (stock != null && stock > 0);
    }
}
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
public class PlayerItemResponse {
    private Integer id;
    private Integer itemId;
    private String itemName;
    private String itemDescription;
    private String itemType;
    private Integer itemQuality;
    private Integer quantity;
    private Boolean locked;
    private Integer maxStack;
    private Boolean stackable;
    private Boolean usable;
    private String effect;
    private Integer price;
    private Boolean sellable;
    private Boolean canUseMore;
    private Boolean stackFull;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
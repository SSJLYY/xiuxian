package com.xiuxian.game.modules.auction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 拍卖物品实体�?
 */
@Data
@TableName("auction_items")
public class AuctionItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer sellerId;
    
    private String itemType; // EQUIPMENT/ITEM/PET
    
    private Integer itemId;
    
    private Long playerItemId;
    
    private Integer quantity;
    
    private Integer price;
    
    private String status; // ON_SALE/SOLD/CANCELLED/EXPIRED
    
    private Integer buyerId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private LocalDateTime expireAt;
    
    private LocalDateTime soldAt;
}


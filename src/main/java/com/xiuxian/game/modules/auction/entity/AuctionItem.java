package com.xiuxian.game.modules.auction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 拍卖物品实体类
 *
 * <p>存储拍卖行中的物品信息，包括卖家、买家、价格、状态等。</p>
 *
 * <p>物品类型：</p>
 * <ul>
 *   <li>EQUIPMENT - 装备</li>
 *   <li>ITEM - 物品</li>
 *   <li>PET - 宠物</li>
 * </ul>
 *
 * <p>拍卖状态：</p>
 * <ul>
 *   <li>ON_SALE - 出售中</li>
 *   <li>SOLD - 已售出</li>
 *   <li>CANCELLED - 已取消</li>
 *   <li>EXPIRED - 已过期</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
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

package com.xiuxian.game.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 拍卖上架请求
 * 封装 AuctionService.listItem 的参数（不含 playerId，由 Controller 从认证上下文获取）
 *
 * @author xiuxian
 */
@Data
public class ListAuctionRequest {

    @NotBlank(message = "物品类型不能为空")
    private String itemType; // ITEM/EQUIPMENT/PET

    @NotNull(message = "物品ID不能为空")
    private Integer itemId;

    @NotNull(message = "玩家物品ID不能为空")
    private Long playerItemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @NotNull(message = "价格不能为空")
    @Min(value = 1, message = "价格必须大于0")
    private Integer price;

    @NotNull(message = "持续时间不能为空")
    @Min(value = 1, message = "持续时间必须大于0")
    private Integer duration; // 小时
}

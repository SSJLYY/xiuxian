package com.xiuxian.game.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopBuyRequest {
    @NotNull(message = "商店物品ID不能为空")
    @Min(value = 1, message = "商店物品ID必须大于0")
    private Long shopItemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}

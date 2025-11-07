package com.xiuxian.game.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemAddRequest {
    @NotNull(message = "物品ID不能为空")
    @Min(value = 1, message = "物品ID必须大于0")
    private Long itemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}

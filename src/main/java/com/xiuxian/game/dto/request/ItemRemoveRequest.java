package com.xiuxian.game.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemRemoveRequest {
    @NotNull(message = "玩家物品ID不能为空")
    @Min(value = 1, message = "玩家物品ID必须大于0")
    private Long playerItemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}

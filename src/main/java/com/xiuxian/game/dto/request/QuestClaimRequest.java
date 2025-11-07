package com.xiuxian.game.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestClaimRequest {
    @NotNull(message = "玩家任务ID不能为空")
    @Min(value = 1, message = "玩家任务ID必须大于0")
    private Long playerQuestId;
}

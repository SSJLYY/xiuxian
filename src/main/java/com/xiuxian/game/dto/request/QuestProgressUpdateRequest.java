package com.xiuxian.game.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestProgressUpdateRequest {
    @NotNull(message = "任务ID不能为空")
    @Min(value = 1, message = "任务ID必须大于0")
    private Long questId;

    @NotNull(message = "进度不能为空")
    @Min(value = 1, message = "进度必须大于0")
    private Integer progress;
}

package com.xiuxian.game.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillLearnRequest {
    @NotNull(message = "技能ID不能为空")
    @Min(value = 1, message = "技能ID必须大于0")
    private Long skillId;
}

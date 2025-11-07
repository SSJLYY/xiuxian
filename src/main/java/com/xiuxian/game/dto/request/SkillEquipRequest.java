package com.xiuxian.game.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillEquipRequest {
    @NotNull(message = "技能ID不能为空")
    @Min(value = 1, message = "技能ID必须大于0")
    private Long playerSkillId;

    @NotNull(message = "槽位号不能为空")
    @Min(value = 1, message = "槽位号必须大于0")
    @Max(value = 10, message = "槽位号不能超过10")
    private Integer slotNumber;
}

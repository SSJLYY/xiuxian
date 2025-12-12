package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {
    private Integer id;
    private Integer level;
    private Boolean equipped;
    private Integer slotNumber;
    private Integer cooldown;
    private Integer manaCost;
    private SkillSummary skill;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillSummary {
        private Integer id;
        private String name;
        private String description;
        private String type;
        private Integer unlockLevel;
        private Integer maxLevel;
    }
}


package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSkillResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private String skillDescription;
    private Integer level;
    private Integer experience;
    private Boolean equipped;
    private Integer slotNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

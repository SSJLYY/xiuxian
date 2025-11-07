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
public class SkillResponse {
    private Long id;
    private String name;
    private String description;
    private Integer level;
    private Integer maxLevel;
    private Double baseDamage;
    private Double damagePerLevel;
    private Integer cooldown;
    private Integer manaCost;
    private String skillType;
    private String element;
    private Integer unlockLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

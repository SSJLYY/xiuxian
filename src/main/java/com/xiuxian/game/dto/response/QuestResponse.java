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
public class QuestResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Integer requiredAmount;
    private Integer rewardExp;
    private Integer rewardSpiritStones;
    private Integer rewardContributionPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

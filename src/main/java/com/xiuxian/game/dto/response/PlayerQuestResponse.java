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
public class PlayerQuestResponse {
    private Long id;
    private Long questId;
    private String questTitle;
    private String questDescription;
    private String questType;
    private Integer currentProgress;
    private Integer requiredAmount;
    private Boolean completed;
    private Boolean rewardClaimed;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerQuestDetailResponse {
    private Long id;
    private Integer currentProgress;
    private Boolean completed;
    private Boolean rewardClaimed;
    private QuestResponse quest;
}


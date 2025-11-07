package com.xiuxian.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineRewardResponse {
    private long offlineMinutes;
    private long expGained;
    private long spiritStonesGained;
    private long cultivationPointsGained;
}
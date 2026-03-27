package com.xiuxian.game.modules.narrative.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.narrative.service.NarrativeService;
import com.xiuxian.game.modules.narrative.service.OfflineNarrativeService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 叙事控制器
 * 负责离线事件、Flag查询等叙事相关功能
 */
@Slf4j
@RestController
@RequestMapping("/api/narrative")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NarrativeController {

    private final NarrativeService narrativeService;
    private final OfflineNarrativeService offlineNarrativeService;
    private final PlayerService playerService;

    /**
     * 检查离线事件（登录时调用）
     */
    @GetMapping("/offline-events")
    public ResponseEntity<ApiResponse<List<OfflineNarrativeService.OfflineEventResult>>> checkOfflineEvents() {
        Integer playerId = playerService.getCurrentPlayerId();
        log.info("检查离线事件: playerId={}", playerId);
        List<OfflineNarrativeService.OfflineEventResult> events = offlineNarrativeService.checkOfflineEvents(playerId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * 获取玩家的叙事flag列表
     */
    @GetMapping("/flags")
    public ResponseEntity<ApiResponse<java.util.Set<String>>> getPlayerFlags() {
        Integer playerId = playerService.getCurrentPlayerId();
        log.info("获取玩家flags: playerId={}", playerId);
        java.util.Set<String> flags = narrativeService.getPlayerFlags(playerId);
        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    /**
     * 检查玩家是否拥有指定flag
     */
    @GetMapping("/flags/{flagKey}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hasFlag(@PathVariable String flagKey) {
        Integer playerId = playerService.getCurrentPlayerId();
        log.info("检查flag: playerId={}, flagKey={}", playerId, flagKey);
        boolean has = narrativeService.hasFlag(playerId, flagKey);
        Map<String, Object> result = new HashMap<>();
        result.put("flagKey", flagKey);
        result.put("has", has);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

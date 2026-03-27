package com.xiuxian.game.modules.narrative.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.narrative.service.LoreService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 传说图鉴控制器
 * 负责传说条目的查询和收集进度
 */
@Slf4j
@RestController
@RequestMapping("/api/lore")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LoreController {

    private final LoreService loreService;
    private final PlayerService playerService;

    /**
     * 获取传说收集进度
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<LoreService.LoreProgressVo>> getLoreProgress() {
        Integer playerId = playerService.getCurrentPlayerId();
        log.info("获取传说进度: playerId={}", playerId);
        LoreService.LoreProgressVo progress = loreService.getLoreProgress(playerId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * 获取所有传说条目（未发现的内容会被隐藏）
     */
    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<List<LoreService.LoreVo>>> getAllEntries() {
        Integer playerId = playerService.getCurrentPlayerId();
        log.info("获取传说条目: playerId={}", playerId);
        List<LoreService.LoreVo> entries = loreService.getAllLoreEntries(playerId);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    /**
     * 获取已发现的传说条目
     */
    @GetMapping("/discovered")
    public ResponseEntity<ApiResponse<List<LoreService.LoreVo>>> getDiscoveredLore() {
        Integer playerId = playerService.getCurrentPlayerId();
        log.info("获取已发现传说: playerId={}", playerId);
        List<LoreService.LoreVo> entries = loreService.getDiscoveredLore(playerId);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }
}


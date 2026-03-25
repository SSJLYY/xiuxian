package com.xiuxian.game.modules.narrative.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.narrative.entity.Npc;
import com.xiuxian.game.modules.narrative.service.NarrativeService;
import com.xiuxian.game.modules.narrative.service.NpcService;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NPC控制器
 * 负责NPC列表、NPC详情、NPC关系查询
 */
@Slf4j
@RestController
@RequestMapping("/api/npc")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NpcController {

    private final NpcService npcService;
    private final PlayerService playerService;

    /**
     * 获取所有NPC列表（按玩家等级过滤）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Npc>>> getNpcList() {
        Integer playerId = playerService.getCurrentPlayerId();
        LogUtils.info(log, "获取NPC列表", "playerId", playerId);
        List<Npc> npcs = npcService.getAllNpcs(null);
        return ResponseEntity.ok(ApiResponse.success(npcs));
    }

    /**
     * 获取NPC详情（含关系信息和日常对话）
     */
    @GetMapping("/{npcId}")
    public ResponseEntity<ApiResponse<NpcService.NpcDetailVo>> getNpcDetail(@PathVariable Integer npcId) {
        Integer playerId = playerService.getCurrentPlayerId();
        LogUtils.info(log, "获取NPC详情", "playerId", playerId, "npcId", npcId);
        NpcService.NpcDetailVo detail = npcService.getNpcDetail(npcId, playerId);
        if (detail == null) {
            return ResponseEntity.ok(ApiResponse.error("NPC不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 获取玩家与所有NPC的关系摘要
     */
    @GetMapping("/relations")
    public ResponseEntity<ApiResponse<List<NpcService.NpcRelationSummary>>> getNpcRelations() {
        Integer playerId = playerService.getCurrentPlayerId();
        LogUtils.info(log, "获取NPC关系列表", "playerId", playerId);
        List<NpcService.NpcRelationSummary> relations = npcService.getNpcRelationSummaries(playerId);
        return ResponseEntity.ok(ApiResponse.success(relations));
    }
}

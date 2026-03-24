package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.service.NarrativeService;
import com.xiuxian.game.service.PlayerService;
import com.xiuxian.game.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 对话控制器
 * 负责对话树的开始、推进和状态查询
 */
@Slf4j
@RestController
@RequestMapping("/api/dialogue")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DialogueController {

    private final NarrativeService narrativeService;
    private final PlayerService playerService;

    /**
     * 获取NPC可用的对话树列表
     */
    @GetMapping("/available/{npcId}")
    public ResponseEntity<ApiResponse<?>> getAvailableDialogues(@PathVariable Integer npcId) {
        Integer playerId = playerService.getCurrentPlayerId();
        LogUtils.info(log, "获取NPC可用对话", "playerId", playerId, "npcId", npcId);
        var dialogues = narrativeService.getAvailableDialogues(playerId, npcId);
        return ResponseEntity.ok(ApiResponse.success(dialogues));
    }

    /**
     * 开始或继续一个对话
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<NarrativeService.DialogueSceneData>> startDialogue(
            @RequestBody Map<String, String> request) {
        Integer playerId = playerService.getCurrentPlayerId();
        String dialogueKey = request.get("dialogueKey");
        if (dialogueKey == null || dialogueKey.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("dialogueKey不能为空"));
        }
        LogUtils.info(log, "开始对话", "playerId", playerId, "dialogueKey", dialogueKey);
        NarrativeService.DialogueSceneData scene = narrativeService.startOrContinueDialogue(playerId, dialogueKey);
        return ResponseEntity.ok(ApiResponse.success(scene));
    }

    /**
     * 做出选择 / 推进对话
     */
    @PostMapping("/choice")
    public ResponseEntity<ApiResponse<NarrativeService.DialogueSceneData>> makeChoice(
            @RequestBody Map<String, String> request) {
        Integer playerId = playerService.getCurrentPlayerId();
        String dialogueKey = request.get("dialogueKey");
        String choiceNodeKey = request.get("choiceNodeKey");
        if (dialogueKey == null || choiceNodeKey == null) {
            return ResponseEntity.ok(ApiResponse.error("参数不完整"));
        }
        LogUtils.info(log, "对话选择", "playerId", playerId, "dialogueKey", dialogueKey, "choice", choiceNodeKey);
        NarrativeService.DialogueSceneData scene = narrativeService.makeChoice(playerId, dialogueKey, choiceNodeKey);
        return ResponseEntity.ok(ApiResponse.success(scene));
    }
}

package com.xiuxian.game.modules.narrative.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.narrative.entity.DialogueTree;
import com.xiuxian.game.modules.narrative.service.NarrativeService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<ApiResponse<List<DialogueTree>>> getAvailableDialogues(@PathVariable Integer npcId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取NPC可用对话: playerId={}, npcId={}", playerId, npcId);
            List<DialogueTree> dialogues = narrativeService.getAvailableDialogues(playerId, npcId);
            return ResponseEntity.ok(ApiResponse.success(dialogues));
        } catch (Exception e) {
            return narrativeError(e);
        }
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
            return ResponseEntity.badRequest().body(ApiResponse.error("dialogueKey不能为空"));
        }
        try {
            log.info("开始对话: playerId={}, dialogueKey={}", playerId, dialogueKey);
            NarrativeService.DialogueSceneData scene = narrativeService.startOrContinueDialogue(playerId, dialogueKey);
            return ResponseEntity.ok(ApiResponse.success(scene));
        } catch (Exception e) {
            return narrativeError(e);
        }
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
            return ResponseEntity.badRequest().body(ApiResponse.error("参数不完整"));
        }
        try {
            log.info("对话选择: playerId={}, dialogueKey={}, choice={}", playerId, dialogueKey, choiceNodeKey);
            NarrativeService.DialogueSceneData scene = narrativeService.makeChoice(playerId, dialogueKey, choiceNodeKey);
            return ResponseEntity.ok(ApiResponse.success(scene));
        } catch (Exception e) {
            return narrativeError(e);
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> narrativeError(Exception e) {
        if (e instanceof com.xiuxian.game.common.exception.BusinessException) {
            com.xiuxian.game.common.exception.BusinessException be = (com.xiuxian.game.common.exception.BusinessException) e;
            int code = be.getCode();
            if (code == 3001) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(be.getMessage()));
            }
            if (code == 3002) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(be.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(be.getMessage()));
        }
        log.error("叙事接口异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("系统繁忙，请稍后再试"));
    }
}


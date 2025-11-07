package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.PlayerQuest;
import com.xiuxian.game.entity.Quest;
import com.xiuxian.game.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerQuest>>> getQuests() {
        try {
            List<PlayerQuest> quests = questService.getPlayerAllQuests();
            return ResponseEntity.ok(ApiResponse.success("获取成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<PlayerQuest>>> getDailyQuests() {
        try {
            List<PlayerQuest> quests = questService.getPlayerDailyQuests();
            return ResponseEntity.ok(ApiResponse.success("获取日常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/daily/refresh")
    public ResponseEntity<ApiResponse<List<PlayerQuest>>> refreshDailyQuests() {
        try {
            List<PlayerQuest> quests = questService.refreshDailyQuests();
            return ResponseEntity.ok(ApiResponse.success("刷新日常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{questId}/progress")
    public ResponseEntity<ApiResponse<PlayerQuest>> updateQuestProgress(
            @PathVariable Integer questId,
            @RequestParam int progress) {
        try {
            PlayerQuest quest = questService.updateQuestProgress(questId, progress);
            return ResponseEntity.ok(ApiResponse.success("更新任务进度成功", quest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{playerQuestId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimQuestReward(@PathVariable Integer playerQuestId) {
        try {
            questService.claimQuestReward(playerQuestId);
            return ResponseEntity.ok(ApiResponse.success("领取任务奖励成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取玩家所有任务
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PlayerQuest>>> getAllQuests() {
        try {
            List<PlayerQuest> quests = questService.getPlayerAllQuests();
            return ResponseEntity.ok(ApiResponse.success("获取所有任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 根据类型获取任务
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<PlayerQuest>>> getQuestsByType(@PathVariable Quest.QuestType type) {
        try {
            List<PlayerQuest> quests = questService.getPlayerQuestsByType(type);
            return ResponseEntity.ok(ApiResponse.success("获取" + type + "任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 批量领取奖励
    @PostMapping("/claim-all")
    public ResponseEntity<ApiResponse<Integer>> claimAllCompletedQuestRewards() {
        try {
            int claimedCount = questService.claimAllCompletedQuestRewards();
            return ResponseEntity.ok(ApiResponse.success("批量领取奖励成功，共领取" + claimedCount + "个任务奖励", claimedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 检查是否有未完成的任务
    @GetMapping("/has-incomplete")
    public ResponseEntity<ApiResponse<Boolean>> hasIncompleteQuests() {
        try {
            boolean hasIncomplete = questService.hasIncompleteQuests();
            return ResponseEntity.ok(ApiResponse.success("检查完成", hasIncomplete));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取未领取奖励的已完成任务数量
    @GetMapping("/unclaimed-count")
    public ResponseEntity<ApiResponse<Long>> getUnclaimedCompletedQuestsCount() {
        try {
            long count = questService.getUnclaimedCompletedQuestsCount();
            return ResponseEntity.ok(ApiResponse.success("获取未领取奖励任务数量成功", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 根据任务类型更新进度
    @PostMapping("/progress/by-type")
    public ResponseEntity<ApiResponse<Void>> updateQuestProgressByType(
            @RequestParam Quest.QuestType questType,
            @RequestParam int progress) {
        try {
            questService.updateQuestProgressByType(questType, progress);
            return ResponseEntity.ok(ApiResponse.success("更新任务进度成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
package com.xiuxian.game.modules.quest.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerQuestDetailResponse;
import com.xiuxian.game.modules.quest.entity.PlayerQuest;
import com.xiuxian.game.modules.quest.entity.Quest;
import com.xiuxian.game.modules.quest.service.QuestService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.features.quests.enabled", havingValue = "true")
public class QuestController {

    private final QuestService questService;
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            List<PlayerQuestDetailResponse> quests = questService.getPlayerAllQuestsDetail(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getDailyQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), Quest.QuestType.DAILY);
            return ResponseEntity.ok(ApiResponse.success("获取日常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getWeeklyQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), Quest.QuestType.WEEKLY);
            return ResponseEntity.ok(ApiResponse.success("获取周常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getMonthlyQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), Quest.QuestType.MONTHLY);
            return ResponseEntity.ok(ApiResponse.success("获取月常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/daily/refresh")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> refreshDailyQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            questService.refreshDailyQuests(player.getId());
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), Quest.QuestType.DAILY);
            return ResponseEntity.ok(ApiResponse.success("刷新日常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/weekly/refresh")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> refreshWeeklyQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            questService.refreshWeeklyQuests(player.getId());
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), Quest.QuestType.WEEKLY);
            return ResponseEntity.ok(ApiResponse.success("刷新周常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/monthly/refresh")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> refreshMonthlyQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            questService.refreshMonthlyQuests(player.getId());
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), Quest.QuestType.MONTHLY);
            return ResponseEntity.ok(ApiResponse.success("刷新月常任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{questId}/progress")
    public ResponseEntity<ApiResponse<PlayerQuest>> updateQuestProgress(
            @PathVariable Integer questId,
            @RequestParam int progress) {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            PlayerQuest quest = questService.updateQuestProgress(player.getId(), questId, progress);
            return ResponseEntity.ok(ApiResponse.success("更新任务进度成功", quest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{playerQuestId}/claim")
    public ResponseEntity<ApiResponse<Void>> claimQuestReward(@PathVariable Integer playerQuestId) {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            questService.claimQuestRewardByPlayerQuestId(player.getId(), playerQuestId);
            return ResponseEntity.ok(ApiResponse.success("领取任务奖励成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取玩家所有任务
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getAllQuests() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            List<PlayerQuestDetailResponse> quests = questService.getPlayerAllQuestsDetail(player.getId());
            return ResponseEntity.ok(ApiResponse.success("获取所有任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 根据类型获取任务
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getQuestsByType(@PathVariable Quest.QuestType type) {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            List<PlayerQuestDetailResponse> quests = questService.getPlayerQuestsDetailByType(player.getId(), type);
            return ResponseEntity.ok(ApiResponse.success("获取" + type + "任务成功", quests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 批量领取奖励
    @PostMapping("/claim-all")
    public ResponseEntity<ApiResponse<Integer>> claimAllCompletedQuestRewards() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            int claimedCount = questService.claimAllCompletedQuestRewards(player.getId());
            return ResponseEntity.ok(ApiResponse.success("批量领取奖励成功，共领取" + claimedCount + "个任务奖励", claimedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

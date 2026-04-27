package com.xiuxian.game.modules.quest.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.PlayerQuestDetailResponse;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.quest.entity.PlayerQuest;
import com.xiuxian.game.modules.quest.entity.Quest;
import com.xiuxian.game.modules.quest.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getMyQuests() {
        return getQuests();
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

    @PostMapping("/accept/{questId}")
    public ResponseEntity<ApiResponse<PlayerQuest>> acceptQuest(@PathVariable Integer questId) {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            PlayerQuest quest = questService.acceptQuest(player.getId(), questId);
            return ResponseEntity.ok(ApiResponse.success("接受任务成功", quest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/complete/{playerQuestId}")
    public ResponseEntity<ApiResponse<Void>> completeQuest(@PathVariable Integer playerQuestId) {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            questService.completeQuest(player.getId(), playerQuestId);
            return ResponseEntity.ok(ApiResponse.success("完成任务成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{questId}/progress")
    public ResponseEntity<ApiResponse<PlayerQuest>> updateQuestProgress(
            @PathVariable Integer questId,
            @RequestParam(required = false) Integer progress,
            @RequestBody(required = false) Map<String, Integer> request) {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            Integer resolvedProgress = progress;
            if (resolvedProgress == null && request != null) {
                resolvedProgress = request.get("progress");
            }
            if (resolvedProgress == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("progress不能为空"));
            }
            PlayerQuest quest = questService.updateQuestProgress(player.getId(), questId, resolvedProgress);
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PlayerQuestDetailResponse>>> getAllQuests() {
        return getQuests();
    }

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

    @PostMapping("/claim-all")
    public ResponseEntity<ApiResponse<Integer>> claimAllCompletedQuestRewards() {
        try {
            PlayerProfile player = playerService.getCurrentPlayerProfile();
            int claimedCount = questService.claimAllCompletedQuestRewards(player.getId());
            return ResponseEntity.ok(ApiResponse.success("批量领取奖励成功", claimedCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

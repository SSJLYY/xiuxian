package com.xiuxian.game.modules.achievement.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.achievement.entity.Achievement;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import com.xiuxian.game.modules.achievement.service.AchievementService;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成就控制器
 *
 * <p>处理玩家成就相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>获取成就列表</li>
 *   <li>获取成就详情</li>
 *   <li>领取成就奖励</li>
 *   <li>获取成就进度</li>
 * </ul>
 *
 * <p>所有接口都需要JWT Token认证，确保只有登录用户才能访问。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final PlayerService playerService;

    /**
     * 获取成就列表
     *
     * <p>返回所有成就及当前玩家的完成进度，按成就类型和排序字段排序。</p>
     *
     * <p>成就列表包含：</p>
     * <ul>
     *   <li>成就ID和名称</li>
     *   <li>成就描述</li>
     *   <li>成就类型（等级/战斗/修炼/收集）</li>
     *   <li>完成条件</li>
     *   <li>奖励信息</li>
     *   <li>当前进度</li>
     *   <li>是否已完成</li>
     *   <li>是否已领取奖励</li>
     * </ul>
     *
     * <p>成就类型：</p>
     * <ul>
     *   <li>LEVEL - 等级成就</li>
     *   <li>COMBAT - 战斗成就</li>
     *   <li>CULTIVATION - 修炼成就</li>
     *   <li>COLLECTION - 收集成就</li>
     * </ul>
     *
     * @return 成就列表（含进度信息）
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AchievementWithProgress>>> getAchievementList() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();

            log.debug("获取成就列表: playerId={}", playerId);

            // 获取所有成就
            List<Achievement> achievements = achievementService.getAllAchievements();

            // 获取玩家成就进度
            List<PlayerAchievement> playerAchievements = achievementService.getPlayerAchievements(playerId);
            Map<Integer, PlayerAchievement> progressMap = playerAchievements.stream()
                    .collect(Collectors.toMap(PlayerAchievement::getAchievementId, pa -> pa, (a, b) -> a));

            // 组合结果
            List<AchievementWithProgress> result = achievements.stream()
                    .map(achievement -> {
                        AchievementWithProgress awp = new AchievementWithProgress();
                        awp.setId(achievement.getId());
                        awp.setName(achievement.getName());
                        awp.setDescription(achievement.getDescription());
                        awp.setAchievementType(achievement.getAchievementType());
                        awp.setConditionType(achievement.getConditionType());
                        awp.setConditionValue(achievement.getConditionValue());
                        awp.setRewardExp(achievement.getRewardExp());
                        awp.setRewardSpiritStones(achievement.getRewardSpiritStones());
                        awp.setRewardTitle(achievement.getRewardTitle());
                        awp.setIcon(achievement.getIcon());
                        awp.setSortOrder(achievement.getSortOrder());

                        PlayerAchievement pa = progressMap.get(achievement.getId());
                        if (pa != null) {
                            awp.setProgress(pa.getProgress());
                            awp.setIsCompleted(pa.getIsCompleted());
                            awp.setIsClaimed(pa.getIsClaimed());
                            awp.setCompletedAt(pa.getCompletedAt());
                            awp.setClaimedAt(pa.getClaimedAt());
                        } else {
                            awp.setProgress(0);
                            awp.setIsCompleted(false);
                            awp.setIsClaimed(false);
                        }

                        return awp;
                    })
                    .collect(Collectors.toList());

            LogUtils.logUserAction(null, playerId, "GET_ACHIEVEMENT_LIST",
                    "获取成就列表");

            log.debug("获取成就列表成功: playerId={}, count={}", playerId, result.size());

            return ResponseEntity.ok(ApiResponse.success("获取成功", result));

        } catch (Exception e) {
            log.error("获取成就列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取成就详情
     *
     * <p>返回指定成就的详细信息，包括当前玩家的完成进度和奖励领取状态。</p>
     *
     * <p>成就详情包含：</p>
     * <ul>
     *   <li>成就基本信息（名称、描述、类型）</li>
     *   <li>完成条件详情</li>
     *   <li>奖励信息（经验值、灵石、称号）</li>
     *   <li>当前进度</li>
     *   <li>完成时间</li>
     *   <li>奖励领取时间</li>
     * </ul>
     *
     * @param id 成就ID
     * @return 成就详情（含进度信息）
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AchievementWithProgress>> getAchievementDetail(@PathVariable Integer id) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();

            log.debug("获取成就详情: playerId={}, achievementId={}", playerId, id);

            // 获取成就信息
            Achievement achievement = achievementService.getAllAchievements().stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (achievement == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("成就不存在"));
            }

            // 获取玩家进度
            List<PlayerAchievement> playerAchievements = achievementService.getPlayerAchievements(playerId);
            PlayerAchievement pa = playerAchievements.stream()
                    .filter(p -> p.getAchievementId().equals(id))
                    .findFirst()
                    .orElse(null);

            // 组合返回数据
            AchievementWithProgress result = new AchievementWithProgress();
            result.setId(achievement.getId());
            result.setName(achievement.getName());
            result.setDescription(achievement.getDescription());
            result.setAchievementType(achievement.getAchievementType());
            result.setConditionType(achievement.getConditionType());
            result.setConditionValue(achievement.getConditionValue());
            result.setRewardExp(achievement.getRewardExp());
            result.setRewardSpiritStones(achievement.getRewardSpiritStones());
            result.setRewardTitle(achievement.getRewardTitle());
            result.setIcon(achievement.getIcon());
            result.setSortOrder(achievement.getSortOrder());

            if (pa != null) {
                result.setProgress(pa.getProgress());
                result.setIsCompleted(pa.getIsCompleted());
                result.setIsClaimed(pa.getIsClaimed());
                result.setCompletedAt(pa.getCompletedAt());
                result.setClaimedAt(pa.getClaimedAt());
            } else {
                result.setProgress(0);
                result.setIsCompleted(false);
                result.setIsClaimed(false);
            }

            LogUtils.logUserAction(null, playerId, "GET_ACHIEVEMENT_DETAIL",
                    "查看成就详情: achievementId=" + id);

            log.debug("获取成就详情成功: playerId={}, achievementId={}", playerId, id);

            return ResponseEntity.ok(ApiResponse.success("获取成功", result));

        } catch (Exception e) {
            log.error("获取成就详情失败: achievementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 领取成就奖励
     *
     * <p>领取已完成成就的奖励，奖励将直接发放到玩家账户。</p>
     *
     * <p>领取条件：</p>
     * <ul>
     *   <li>成就必须已完成</li>
     *   <li>奖励尚未领取</li>
     * </ul>
     *
     * <p>领取流程：</p>
     * <ul>
     *   <li>验证成就完成状态和领取状态</li>
     *   <li>发放经验值奖励</li>
     *   <li>发放灵石奖励（如有）</li>
     *   <li>发放称号奖励（如有）</li>
     *   <li>更新领取状态和领取时间</li>
     * </ul>
     *
     * <p>错误情况：</p>
     * <ul>
     *   <li>成就不存在 - 返回错误信息</li>
     *   <li>成就未完成 - 返回"成就未完成"错误</li>
     *   <li>奖励已领取 - 返回"成就奖励已领取"错误</li>
     * </ul>
     *
     * @param id 成就ID
     * @return 领取结果
     */
    @PostMapping("/{id}/claim")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> claimAchievementReward(@PathVariable Integer id) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();

            log.info("领取成就奖励: playerId={}, achievementId={}", playerId, id);

            achievementService.claimAchievementReward(playerId, id.longValue());

            LogUtils.logUserAction(null, playerId, "CLAIM_ACHIEVEMENT_REWARD",
                    "领取成就奖励: achievementId=" + id);
            LogUtils.logBusiness("ACHIEVEMENT", "领取成就奖励",
                    "playerId", playerId, "achievementId", id);

            log.info("领取成就奖励成功: playerId={}, achievementId={}", playerId, id);

            return ResponseEntity.ok(ApiResponse.success("领取成功", null));

        } catch (Exception e) {
            log.error("领取成就奖励失败: playerId={}, achievementId={}, error={}",
                    playerService.getCurrentPlayerId(), id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取成就进度统计
     *
     * <p>返回玩家的成就完成情况统计信息。</p>
     *
     * <p>统计信息包含：</p>
     * <ul>
     *   <li>总成就数量</li>
     *   <li>已完成数量</li>
     *   <li>已领取奖励数量</li>
     *   <li>完成率</li>
     *   <li>各类型成就数量和完成数量</li>
     * </ul>
     *
     * @return 成就进度统计信息
     */
    @GetMapping("/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAchievementProgress() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();

            log.debug("获取成就进度统计: playerId={}", playerId);

            List<Achievement> allAchievements = achievementService.getAllAchievements();
            List<PlayerAchievement> playerAchievements = achievementService.getPlayerAchievements(playerId);

            long totalCount = allAchievements.size();
            long completedCount = playerAchievements.stream()
                    .filter(PlayerAchievement::getIsCompleted)
                    .count();
            long claimedCount = playerAchievements.stream()
                    .filter(PlayerAchievement::getIsClaimed)
                    .count();

            double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;

            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            for (Achievement achievement : allAchievements) {
                String type = achievement.getAchievementType();
                typeStats.put(type, typeStats.getOrDefault(type, 0L) + 1);
            }

            Map<String, Long> typeCompletedStats = new HashMap<>();
            Map<Integer, PlayerAchievement> progressMap = playerAchievements.stream()
                    .collect(Collectors.toMap(PlayerAchievement::getAchievementId, pa -> pa, (a, b) -> a));

            for (Achievement achievement : allAchievements) {
                PlayerAchievement pa = progressMap.get(achievement.getId());
                if (pa != null && pa.getIsCompleted()) {
                    String type = achievement.getAchievementType();
                    typeCompletedStats.put(type, typeCompletedStats.getOrDefault(type, 0L) + 1);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("completedCount", completedCount);
            result.put("claimedCount", claimedCount);
            result.put("completionRate", String.format("%.2f", completionRate));
            result.put("typeStats", typeStats);
            result.put("typeCompletedStats", typeCompletedStats);

            log.debug("获取成就进度统计成功: playerId={}, completedCount={}/{}",
                    playerId, completedCount, totalCount);

            return ResponseEntity.ok(ApiResponse.success("获取成功", result));

        } catch (Exception e) {
            log.error("获取成就进度统计失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 成就含进度DTO
     */
    @Data
    public static class AchievementWithProgress {
        private Integer id;
        private String name;
        private String description;
        private String achievementType;
        private String conditionType;
        private Integer conditionValue;
        private Integer rewardExp;
        private Integer rewardSpiritStones;
        private String rewardTitle;
        private String icon;
        private Integer sortOrder;

        // 玩家进度信息
        private Integer progress;
        private Boolean isCompleted;
        private Boolean isClaimed;
        private java.time.LocalDateTime completedAt;
        private java.time.LocalDateTime claimedAt;
    }
}

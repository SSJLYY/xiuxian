package com.xiuxian.game.modules.achievement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.achievement.entity.Achievement;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import com.xiuxian.game.modules.achievement.mapper.AchievementMapper;
import com.xiuxian.game.modules.achievement.mapper.PlayerAchievementMapper;
import com.xiuxian.game.modules.achievement.service.AchievementService;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/achievement")
@RequiredArgsConstructor
@Validated
public class AdminAchievementController {

    private final AchievementService achievementService;
    private final AchievementMapper achievementMapper;
    private final PlayerAchievementMapper playerAchievementMapper;
    private final AdminAuthService adminAuthService;

    private Integer getCurrentAdminId() {
        return adminAuthService.getCurrentAdminId();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Achievement>> createAchievement(
            @Valid @RequestBody CreateAchievementRequest request) {
        try {
            Integer adminId = getCurrentAdminId();

            Achievement achievement = new Achievement();
            achievement.setName(request.getName());
            achievement.setDescription(request.getDescription());
            achievement.setAchievementType(request.getAchievementType());
            achievement.setConditionType(request.getConditionType());
            achievement.setConditionValue(request.getConditionValue());
            achievement.setRewardExp(request.getRewardExp());
            achievement.setRewardSpiritStones(request.getRewardSpiritStones());
            achievement.setRewardTitle(request.getRewardTitle());
            achievement.setIcon(request.getIcon());
            achievement.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
            achievementMapper.insert(achievement);

            LogUtils.logUserAction(null, adminId, "ADMIN_CREATE_ACHIEVEMENT",
                    "创建成就: name=" + achievement.getName());
            LogUtils.logBusiness("ADMIN_ACHIEVEMENT", "创建成就",
                    "adminId", adminId,
                    "achievementId", achievement.getId(),
                    "type", achievement.getAchievementType());

            return ResponseEntity.ok(ApiResponse.success("创建成功", achievement));
        } catch (Exception e) {
            log.error("创建成就失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Achievement>> updateAchievement(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAchievementRequest request) {
        try {
            Integer adminId = getCurrentAdminId();
            Achievement achievement = achievementMapper.selectById(id);
            if (achievement == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            if (request.getName() != null) {
                achievement.setName(request.getName());
            }
            if (request.getDescription() != null) {
                achievement.setDescription(request.getDescription());
            }
            if (request.getAchievementType() != null) {
                achievement.setAchievementType(request.getAchievementType());
            }
            if (request.getConditionType() != null) {
                achievement.setConditionType(request.getConditionType());
            }
            if (request.getConditionValue() != null) {
                achievement.setConditionValue(request.getConditionValue());
            }
            if (request.getRewardExp() != null) {
                achievement.setRewardExp(request.getRewardExp());
            }
            if (request.getRewardSpiritStones() != null) {
                achievement.setRewardSpiritStones(request.getRewardSpiritStones());
            }
            if (request.getRewardTitle() != null) {
                achievement.setRewardTitle(request.getRewardTitle());
            }
            if (request.getIcon() != null) {
                achievement.setIcon(request.getIcon());
            }
            if (request.getSortOrder() != null) {
                achievement.setSortOrder(request.getSortOrder());
            }

            int updatedRows = achievementMapper.updateById(achievement);
            if (updatedRows == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            LogUtils.logUserAction(null, adminId, "ADMIN_UPDATE_ACHIEVEMENT",
                    "更新成就: achievementId=" + id);
            LogUtils.logBusiness("ADMIN_ACHIEVEMENT", "更新成就",
                    "adminId", adminId,
                    "achievementId", id);

            return ResponseEntity.ok(ApiResponse.success("更新成功", achievement));
        } catch (Exception e) {
            log.error("更新成就失败: achievementId={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAchievement(@PathVariable Integer id) {
        try {
            Integer adminId = getCurrentAdminId();
            Achievement achievement = achievementMapper.selectById(id);
            if (achievement == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            boolean deleted = achievementService.deleteAchievementAndProgress(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            LogUtils.logUserAction(null, adminId, "ADMIN_DELETE_ACHIEVEMENT",
                    "删除成就: achievementId=" + id);
            LogUtils.logBusiness("ADMIN_ACHIEVEMENT", "删除成就",
                    "adminId", adminId,
                    "achievementId", id);

            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            log.error("删除成就失败: achievementId={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IPage<Achievement>>> getAllAchievements(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String type) {
        try {
            Integer adminId = getCurrentAdminId();
            IPage<Achievement> pageParam = PageUtil.createPage(page, size);
            QueryWrapper<Achievement> queryWrapper = new QueryWrapper<>();
            if (type != null && !type.trim().isEmpty()) {
                queryWrapper.eq("achievement_type", type.trim());
            }
            queryWrapper.orderByAsc("sort_order", "id");

            IPage<Achievement> achievements = achievementMapper.selectPage(pageParam, queryWrapper);
            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ACHIEVEMENT_LIST",
                    "获取成就列表: page=" + page + ", size=" + size);

            return ResponseEntity.ok(ApiResponse.success("获取成功", achievements));
        } catch (Exception e) {
            log.error("获取成就列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Achievement>> getAchievementDetail(@PathVariable Integer id) {
        try {
            Integer adminId = getCurrentAdminId();
            Achievement achievement = achievementMapper.selectById(id);
            if (achievement == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ACHIEVEMENT_DETAIL",
                    "查看成就详情: achievementId=" + id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", achievement));
        } catch (Exception e) {
            log.error("获取成就详情失败: achievementId={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAchievementStats() {
        try {
            Long totalCount = achievementMapper.selectCount(null);
            List<Achievement> allAchievements = achievementMapper.selectList(
                    new QueryWrapper<Achievement>().orderByAsc("achievement_type", "sort_order", "id"));
            Map<String, Long> typeCount = new HashMap<>();
            for (Achievement achievement : allAchievements) {
                typeCount.put(achievement.getAchievementType(),
                        typeCount.getOrDefault(achievement.getAchievementType(), 0L) + 1);
            }

            Long totalCompletions = playerAchievementMapper.selectCount(
                    new QueryWrapper<PlayerAchievement>().eq("is_completed", true));
            Long totalClaims = playerAchievementMapper.selectCount(
                    new QueryWrapper<PlayerAchievement>().eq("is_claimed", true));
            Long totalPlayerAchievements = playerAchievementMapper.selectCount(null);
            double avgCompletionRate = totalPlayerAchievements > 0
                    ? (double) totalCompletions / totalPlayerAchievements * 100
                    : 0;

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("typeCount", typeCount);
            result.put("totalCompletions", totalCompletions);
            result.put("totalClaims", totalClaims);
            result.put("avgCompletionRate", String.format("%.2f", avgCompletionRate));

            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            log.error("获取成就统计失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PlayerAchievement>>> getPlayerAchievements(
            @PathVariable Integer playerId) {
        try {
            Integer adminId = getCurrentAdminId();
            List<PlayerAchievement> playerAchievements = achievementService.getPlayerAchievements(playerId);
            LogUtils.logUserAction(null, adminId, "ADMIN_GET_PLAYER_ACHIEVEMENTS",
                    "查看玩家成就: playerId=" + playerId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", playerAchievements));
        } catch (Exception e) {
            log.error("获取玩家成就失败: playerId={}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @Data
    public static class CreateAchievementRequest {
        @NotBlank(message = "成就名称不能为空")
        private String name;

        @NotBlank(message = "成就描述不能为空")
        private String description;

        @NotBlank(message = "成就类型不能为空")
        private String achievementType;

        @NotBlank(message = "条件类型不能为空")
        private String conditionType;

        @NotNull(message = "条件值不能为空")
        @Min(value = 1, message = "条件值必须大于0")
        private Integer conditionValue;

        private Integer rewardExp;
        private Integer rewardSpiritStones;
        private String rewardTitle;
        private String icon;
        private Integer sortOrder;
    }

    @Data
    public static class UpdateAchievementRequest {
        private String name;
        private String description;
        private String achievementType;
        private String conditionType;

        @Min(value = 1, message = "条件值必须大于0")
        private Integer conditionValue;

        private Integer rewardExp;
        private Integer rewardSpiritStones;
        private String rewardTitle;
        private String icon;
        private Integer sortOrder;
    }
}

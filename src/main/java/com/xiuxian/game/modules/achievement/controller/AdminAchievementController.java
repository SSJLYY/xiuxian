package com.xiuxian.game.modules.achievement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.achievement.entity.Achievement;
import com.xiuxian.game.modules.achievement.entity.PlayerAchievement;
import com.xiuxian.game.modules.achievement.mapper.AchievementMapper;
import com.xiuxian.game.modules.achievement.mapper.PlayerAchievementMapper;
import com.xiuxian.game.modules.achievement.service.AchievementService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员成就控制器
 *
 * <p>处理管理员成就管理相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>创建成就</li>
 *   <li>更新成就</li>
 *   <li>删除成就</li>
 *   <li>获取所有成就列表</li>
 *   <li>获取成就统计信息</li>
 *   <li>查看玩家成就完成情况</li>
 * </ul>
 *
 * <p>所有接口都需要ADMIN角色权限。</p>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
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

    /**
     * 创建成就
     *
     * <p>管理员创建新的成就模板。</p>
     *
     * <p>成就类型：</p>
     * <ul>
     *   <li>LEVEL - 等级成就（达到指定等级）</li>
     *   <li>COMBAT - 战斗成就（击败怪物数量）</li>
     *   <li>CULTIVATION - 修炼成就（修炼时长）</li>
     *   <li>COLLECTION - 收集成就（收集物品/宠物）</li>
     * </ul>
     *
     * <p>条件类型：</p>
     * <ul>
     *   <li>REACH_LEVEL - 达到等级</li>
     *   <li>KILL_MONSTER - 击败怪物</li>
     *   <li>CULTIVATE_TIME - 修炼时长</li>
     *   <li>COLLECT_ITEM - 收集物品</li>
     *   <li>COLLECT_PET - 收集宠物</li>
     * </ul>
     *
     * <p>奖励配置：</p>
     * <ul>
     *   <li>经验奖励（可选）</li>
     *   <li>灵石奖励（可选）</li>
     *   <li>称号奖励（可选）</li>
     * </ul>
     *
     * @param request 成就创建请求
     * @return 创建的成就信息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Achievement>> createAchievement(
            @Valid @RequestBody CreateAchievementRequest request) {
        try {
            Integer adminId = getCurrentAdminId();

            log.info("管理员创建成就: adminId={}, name={}", adminId, request.getName());

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
                    "管理员创建成就: name=" + request.getName());
            LogUtils.logBusiness("ADMIN_ACHIEVEMENT", "管理员创建成就",
                    "adminId", adminId, "achievementId", achievement.getId(),
                    "type", request.getAchievementType());

            log.info("管理员创建成就成功: adminId={}, achievementId={}", adminId, achievement.getId());

            return ResponseEntity.ok(ApiResponse.success("创建成功", achievement));

        } catch (Exception e) {
            log.error("管理员创建成就失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新成就
     *
     * <p>管理员更新成就模板信息。</p>
     *
     * <p>注意事项：</p>
     * <ul>
     *   <li>更新成就不会影响玩家已获得的进度</li>
     *   <li>修改完成条件可能导致已完成的成就变为未完成</li>
     *   <li>建议谨慎修改已有成就的条件值</li>
     * </ul>
     *
     * @param id 成就ID
     * @param request 成就更新请求
     * @return 更新后的成就信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Achievement>> updateAchievement(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAchievementRequest request) {
        try {
            Integer adminId = getCurrentAdminId();

            log.info("管理员更新成就: adminId={}, achievementId={}", adminId, id);

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

            achievementMapper.updateById(achievement);

            LogUtils.logUserAction(null, adminId, "ADMIN_UPDATE_ACHIEVEMENT",
                    "管理员更新成就: achievementId=" + id);
            LogUtils.logBusiness("ADMIN_ACHIEVEMENT", "管理员更新成就",
                    "adminId", adminId, "achievementId", id);

            log.info("管理员更新成就成功: adminId={}, achievementId={}", adminId, id);

            return ResponseEntity.ok(ApiResponse.success("更新成功", achievement));

        } catch (Exception e) {
            log.error("管理员更新成就失败: achievementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除成就
     *
     * <p>管理员删除成就模板。</p>
     *
     * <p>注意事项：</p>
     * <ul>
     *   <li>删除后无法恢复</li>
     *   <li>会同时删除所有玩家的该成就进度记录</li>
     *   <li>建议只删除测试或错误创建的成就</li>
     *   <li>已有玩家完成的成就不建议删除</li>
     * </ul>
     *
     * @param id 成就ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAchievement(@PathVariable Integer id) {
        try {
            Integer adminId = getCurrentAdminId();

            log.info("管理员删除成就: adminId={}, achievementId={}", adminId, id);

            Achievement achievement = achievementMapper.selectById(id);
            if (achievement == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            // 删除成就
            achievementMapper.deleteById(id);

            // 删除所有玩家的该成就进度
            playerAchievementMapper.delete(
                    new QueryWrapper<PlayerAchievement>().eq("achievement_id", id));

            LogUtils.logUserAction(null, adminId, "ADMIN_DELETE_ACHIEVEMENT",
                    "管理员删除成就: achievementId=" + id);
            LogUtils.logBusiness("ADMIN_ACHIEVEMENT", "管理员删除成就",
                    "adminId", adminId, "achievementId", id);

            log.info("管理员删除成就成功: adminId={}, achievementId={}", adminId, id);

            return ResponseEntity.ok(ApiResponse.success("删除成功", null));

        } catch (Exception e) {
            log.error("管理员删除成就失败: achievementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有成就列表（管理端）
     *
     * <p>管理员查看所有成就模板。</p>
     *
     * <p>支持功能：</p>
     * <ul>
     *   <li>分页查询</li>
     *   <li>按类型筛选</li>
     *   <li>按排序字段排列</li>
     * </ul>
     *
     * @param page 页码，默认1
     * @param size 每页数量，默认20
     * @param type 成就类型筛选（可选）
     * @return 成就列表分页数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IPage<Achievement>>> getAllAchievements(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String type) {
        try {
            Integer adminId = getCurrentAdminId();

            log.debug("管理员获取成就列表: adminId={}, page={}, size={}, type={}",
                    adminId, page, size, type);

            Page<Achievement> pageParam = new Page<>(page, size);
            QueryWrapper<Achievement> queryWrapper = new QueryWrapper<>();

            if (type != null && !type.isEmpty()) {
                queryWrapper.eq("achievement_type", type);
            }

            queryWrapper.orderByAsc("sort_order", "id");

            IPage<Achievement> achievements = achievementMapper.selectPage(pageParam, queryWrapper);

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ACHIEVEMENT_LIST",
                    "管理员获取成就列表: page=" + page + ", size=" + size);

            log.debug("管理员获取成就列表成功: adminId={}, total={}", adminId, achievements.getTotal());

            return ResponseEntity.ok(ApiResponse.success("获取成功", achievements));

        } catch (Exception e) {
            log.error("管理员获取成就列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取成就详情（管理端）
     *
     * <p>管理员查看成就详情。</p>
     *
     * @param id 成就ID
     * @return 成就详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Achievement>> getAchievementDetail(@PathVariable Integer id) {
        try {
            Integer adminId = getCurrentAdminId();

            log.debug("管理员获取成就详情: adminId={}, achievementId={}", adminId, id);

            Achievement achievement = achievementMapper.selectById(id);
            if (achievement == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("成就不存在"));
            }

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ACHIEVEMENT_DETAIL",
                    "管理员查看成就详情: achievementId=" + id);

            log.debug("管理员获取成就详情成功: adminId={}, achievementId={}", adminId, id);

            return ResponseEntity.ok(ApiResponse.success("获取成功", achievement));

        } catch (Exception e) {
            log.error("管理员获取成就详情失败: achievementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取成就统计信息
     *
     * <p>返回成就系统的统计数据，用于管理后台仪表板展示。</p>
     *
     * <p>统计信息包括：</p>
     * <ul>
     *   <li>总成就数</li>
     *   <li>各类型成就数量</li>
     *   <li>玩家总完成次数</li>
     *   <li>玩家总领取次数</li>
     *   <li>平均完成率</li>
     * </ul>
     *
     * @return 成就统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAchievementStats() {
        try {
            Integer adminId = getCurrentAdminId();

            log.debug("管理员获取成就统计: adminId={}", adminId);

            // 总成就数
            Long totalCount = achievementMapper.selectCount(null);

            // 各类型成就数量
            List<Achievement> allAchievements = achievementMapper.selectList(
                    new QueryWrapper<Achievement>().orderByAsc("achievement_type", "sort_order", "id")
            );
            Map<String, Long> typeCount = new HashMap<>();
            for (Achievement achievement : allAchievements) {
                String type = achievement.getAchievementType();
                typeCount.put(type, typeCount.getOrDefault(type, 0L) + 1);
            }

            // 玩家完成统计
            Long totalCompletions = playerAchievementMapper.selectCount(
                    new QueryWrapper<PlayerAchievement>().eq("is_completed", true));
            Long totalClaims = playerAchievementMapper.selectCount(
                    new QueryWrapper<PlayerAchievement>().eq("is_claimed", true));

            // 计算平均完成率
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

            log.debug("管理员获取成就统计成功: adminId={}, totalCount={}", adminId, totalCount);

            return ResponseEntity.ok(ApiResponse.success("获取成功", result));

        } catch (Exception e) {
            log.error("管理员获取成就统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 查看玩家成就完成情况
     *
     * <p>管理员查看指定玩家的成就完成情况。</p>
     *
     * <p>返回信息包括：</p>
     * <ul>
     *   <li>玩家所有成就进度</li>
     *   <li>完成状态和领取状态</li>
     *   <li>完成时间和领取时间</li>
     * </ul>
     *
     * @param playerId 玩家ID
     * @return 玩家成就列表
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PlayerAchievement>>> getPlayerAchievements(
            @PathVariable Integer playerId) {
        try {
            Integer adminId = getCurrentAdminId();

            log.debug("管理员查看玩家成就: adminId={}, playerId={}", adminId, playerId);

            List<PlayerAchievement> playerAchievements = achievementService.getPlayerAchievements(playerId);

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_PLAYER_ACHIEVEMENTS",
                    "管理员查看玩家成就: playerId=" + playerId);

            log.debug("管理员查看玩家成就成功: adminId={}, playerId={}, count={}",
                    adminId, playerId, playerAchievements.size());

            return ResponseEntity.ok(ApiResponse.success("获取成功", playerAchievements));

        } catch (Exception e) {
            log.error("管理员查看玩家成就失败: playerId={}, error={}", playerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建成就请求DTO
     */
    @Data
    public static class CreateAchievementRequest {

        @NotBlank(message = "成就名称不能为空")
        private String name;

        @NotBlank(message = "成就描述不能为空")
        private String description;

        @NotBlank(message = "成就类型不能为空")
        private String achievementType; // LEVEL/COMBAT/CULTIVATION/COLLECTION

        @NotBlank(message = "条件类型不能为空")
        private String conditionType; // REACH_LEVEL/KILL_MONSTER/CULTIVATE_TIME

        @NotNull(message = "条件数值不能为空")
        @Min(value = 1, message = "条件数值必须大于0")
        private Integer conditionValue;

        private Integer rewardExp;

        private Integer rewardSpiritStones;

        private String rewardTitle;

        private String icon;

        private Integer sortOrder;
    }

    /**
     * 更新成就请求DTO
     */
    @Data
    public static class UpdateAchievementRequest {

        private String name;

        private String description;

        private String achievementType;

        private String conditionType;

        @Min(value = 1, message = "条件数值必须大于0")
        private Integer conditionValue;

        private Integer rewardExp;

        private Integer rewardSpiritStones;

        private String rewardTitle;

        private String icon;

        private Integer sortOrder;
    }
}

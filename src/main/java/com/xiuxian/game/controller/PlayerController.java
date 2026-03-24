package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.service.PlayerService;
import com.xiuxian.game.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 玩家控制器
 * 
 * <p>处理玩家相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>获取玩家档案信息</li>
 *   <li>修炼系统操作（开始/停止修炼）</li>
 *   <li>属性点分配</li>
 *   <li>修炼状态重置</li>
 * </ul>
 * 
 * <p>所有接口都需要JWT Token认证，确保只有登录用户才能访问。</p>
 * 
 * <p>核心功能：</p>
 * <ul>
 *   <li>修炼系统 - 玩家可以挂机修炼获得经验</li>
 *   <li>属性系统 - 玩家可以分配属性点提升能力</li>
 *   <li>等级系统 - 经验满足条件时自动升级</li>
 *   <li>境界系统 - 达到特定等级时境界突破</li>
 * </ul>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */
@Slf4j
@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    /**
     * 玩家服务
     */
    private final PlayerService playerService;

    /**
     * 获取玩家档案信息
     * 
     * <p>返回当前登录玩家的完整档案信息，包括：</p>
     * <ul>
     *   <li>基础属性（等级、经验、境界等）</li>
     *   <li>战斗属性（攻击、防御、生命、法力、速度）</li>
     *   <li>装备加成属性</li>
     *   <li>技能加成属性</li>
     *   <li>资源信息（灵石、修炼点等）</li>
     *   <li>修炼状态</li>
     * </ul>
     * 
     * @return 玩家档案信息
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> getProfile() {
        try {
            log.debug("获取玩家档案信息");
            
            // 获取当前玩家基础信息
            PlayerProfile currentProfile = playerService.getCurrentPlayerProfile();
            
            // 获取包含所有加成的完整玩家信息
            PlayerProfile profile = playerService.getPlayerProfileWithBonuses(currentProfile.getId());
            
            // 记录操作日志
            LogUtils.logUserAction(null, profile.getId(), "GET_PROFILE", 
                    "获取玩家档案信息");
            
            log.debug("获取玩家档案信息成功: 玩家ID={}, 昵称={}, 等级={}", 
                    profile.getId(), profile.getNickname(), profile.getLevel());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", profile));
            
        } catch (Exception e) {
            log.error("获取玩家档案信息失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 开始修炼
     * 
     * <p>玩家进入修炼状态，开始挂机修炼获得经验。</p>
     * 
     * <p>修炼机制：</p>
     * <ul>
     *   <li>玩家每秒获得基础经验 × 修炼速度</li>
     *   <li>经验满足条件时自动升级</li>
     *   <li>升级时提升基础属性</li>
     *   <li>达到特定等级时境界突破</li>
     *   <li>境界突破获得额外属性点和技能点</li>
     * </ul>
     * 
     * @return 修炼开始结果
     */
    @PostMapping("/cultivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cultivate() {
        try {
            // 获取当前玩家信息
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            
            log.info("玩家开始修炼: 玩家ID={}, 昵称={}, 当前等级={}", 
                    profile.getId(), profile.getNickname(), profile.getLevel());
            
            // 执行修炼逻辑
            playerService.cultivate();
            
            // 记录操作日志
            LogUtils.logUserAction(null, profile.getId(), "START_CULTIVATION", 
                    "玩家开始修炼");
            LogUtils.logBusiness("CULTIVATION", "玩家开始修炼", 
                    "playerId", profile.getId(), "level", profile.getLevel());
            
            log.info("玩家修炼开始成功: 玩家ID={}", profile.getId());
            return ResponseEntity.ok(ApiResponse.success("修炼成功", null));
            
        } catch (Exception e) {
            log.error("玩家开始修炼失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 停止修炼
     * 
     * <p>玩家结束修炼状态，计算修炼收益。</p>
     * 
     * <p>收益计算：</p>
     * <ul>
     *   <li>根据修炼时长计算经验收益</li>
     *   <li>检查是否升级或境界突破</li>
     *   <li>更新任务进度（每日修炼任务等）</li>
     *   <li>记录修炼日志</li>
     * </ul>
     * 
     * @return 修炼停止结果
     */
    @PostMapping("/cultivate/stop")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> stopCultivate() {
        try {
            // 获取当前玩家信息
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            
            log.info("玩家停止修炼: 玩家ID={}, 昵称={}, 当前等级={}", 
                    profile.getId(), profile.getNickname(), profile.getLevel());
            
            // 执行停止修炼逻辑
            playerService.stopCultivate();
            
            // 记录操作日志
            LogUtils.logUserAction(null, profile.getId(), "STOP_CULTIVATION", 
                    "玩家停止修炼");
            LogUtils.logBusiness("CULTIVATION", "玩家停止修炼", 
                    "playerId", profile.getId(), "level", profile.getLevel());
            
            log.info("玩家修炼停止成功: 玩家ID={}", profile.getId());
            return ResponseEntity.ok(ApiResponse.success("停止修炼成功", null));
            
        } catch (Exception e) {
            log.error("玩家停止修炼失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 分配属性点
     * 
     * <p>玩家使用可用的属性点来提升基础属性。</p>
     * 
     * <p>属性点来源：</p>
     * <ul>
     *   <li>境界突破时获得</li>
     *   <li>完成特殊任务奖励</li>
     *   <li>使用特殊道具</li>
     * </ul>
     * 
     * <p>可分配的属性：</p>
     * <ul>
     *   <li>攻击力 - 影响战斗伤害</li>
     *   <li>防御力 - 减少受到的伤害</li>
     *   <li>生命值 - 影响生存能力</li>
     *   <li>法力值 - 影响技能使用</li>
     *   <li>速度 - 影响战斗先手和闪避</li>
     * </ul>
     * 
     * @param payload 属性分配方案，包含各属性的加点数量
     * @return 更新后的玩家档案
     */
    @PostMapping("/attributes/allocate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PlayerProfile>> allocateAttributes(@RequestBody Map<String, Integer> payload) {
        try {
            // 获取当前玩家信息
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            int availablePoints = profile.getAttributePoints() == null ? 0 : profile.getAttributePoints();
            
            log.info("玩家分配属性点: 玩家ID={}, 可用点数={}, 分配方案={}", 
                    profile.getId(), availablePoints, payload);
            
            // 计算总消耗点数
            int totalSpend = 0;
            String[] attributes = {"attack", "defense", "health", "mana", "speed"};
            for (String attr : attributes) {
                Integer points = payload.get(attr);
                if (points != null && points > 0) {
                    totalSpend += points;
                }
            }
            
            // 验证分配方案
            if (totalSpend <= 0) {
                throw new IllegalArgumentException("未提供有效的加点方案");
            }
            if (totalSpend > availablePoints) {
                throw new IllegalArgumentException("属性点不足，可用点数: " + availablePoints + "，需要点数: " + totalSpend);
            }
            
            // 应用属性点分配
            profile.setAttack(profile.getAttack() + payload.getOrDefault("attack", 0));
            profile.setDefense(profile.getDefense() + payload.getOrDefault("defense", 0));
            profile.setHealth(profile.getHealth() + payload.getOrDefault("health", 0));
            profile.setMana(profile.getMana() + payload.getOrDefault("mana", 0));
            profile.setSpeed(profile.getSpeed() + payload.getOrDefault("speed", 0));
            profile.setAttributePoints(availablePoints - totalSpend);
            
            // 保存更新
            playerService.savePlayerProfile(profile);
            
            // 记录操作日志
            LogUtils.logUserAction(null, profile.getId(), "ALLOCATE_ATTRIBUTES", 
                    "分配属性点: " + payload.toString());
            LogUtils.logBusiness("ATTRIBUTE_ALLOCATION", "玩家分配属性点", 
                    "playerId", profile.getId(), "spend", totalSpend, "remaining", profile.getAttributePoints());
            
            log.info("属性点分配成功: 玩家ID={}, 消耗点数={}, 剩余点数={}", 
                    profile.getId(), totalSpend, profile.getAttributePoints());
            
            return ResponseEntity.ok(ApiResponse.success("加点成功", profile));
            
        } catch (Exception e) {
            log.error("属性点分配失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 领取离线奖励（已废弃）
     * 
     * <p>此接口已迁移到 /api/offline-reward，请使用新接口。</p>
     * 
     * @return 提示信息
     * @deprecated 请使用 /api/offline-reward 接口
     */
    @PostMapping("/claim-offline-rewards")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<ApiResponse<?>> claimOfflineRewards() {
        try {
            log.warn("使用了已废弃的离线奖励接口: /api/player/claim-offline-rewards");
            return ResponseEntity.ok(ApiResponse.success("请使用 /api/offline-reward 接口", null));
        } catch (Exception e) {
            log.error("离线奖励接口调用失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 重置修炼状态
     * 
     * <p>强制重置玩家的修炼状态为未修炼。</p>
     * <p>主要用于解决前后端状态不一致的问题。</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>页面刷新导致的状态不一致</li>
     *   <li>网络异常导致的状态异常</li>
     *   <li>客户端异常退出后的状态恢复</li>
     * </ul>
     * 
     * @return 重置结果
     */
    @PostMapping("/reset-cultivation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> resetCultivation() {
        try {
            // 获取当前玩家信息
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            
            log.info("重置修炼状态: 玩家ID={}, 昵称={}, 当前状态={}", 
                    profile.getId(), profile.getNickname(), profile.getIsCultivating());
            
            // 重置修炼状态
            profile.setIsCultivating(false);
            playerService.savePlayerProfile(profile);
            
            // 记录操作日志
            LogUtils.logUserAction(null, profile.getId(), "RESET_CULTIVATION", 
                    "重置修炼状态");
            LogUtils.logBusiness("CULTIVATION", "重置修炼状态", 
                    "playerId", profile.getId());
            
            log.info("修炼状态重置成功: 玩家ID={}", profile.getId());
            return ResponseEntity.ok(ApiResponse.success("修炼状态已重置", null));
            
        } catch (Exception e) {
            log.error("重置修炼状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * GDD：检查是否可以进行境界突破
     * 
     * @return 是否可以突破
     */
    @GetMapping("/breakthrough/can")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> canBreakthrough() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            boolean can = playerService.canBreakthrough(profile.getId());
            return ResponseEntity.ok(ApiResponse.success(can));
        } catch (Exception e) {
            log.error("检查突破状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * GDD：执行境界突破
     * 需要达到当前境界最高等级，并消耗破境丹（或5000灵石）
     * 触发心魔战斗，70%胜率
     * 
     * @return 突破结果
     */
    @PostMapping("/breakthrough")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> attemptBreakthrough() {
        try {
            PlayerProfile profile = playerService.getCurrentPlayerProfile();
            String result = playerService.attemptBreakthrough(profile.getId());
            
            // 记录操作日志
            LogUtils.logUserAction(null, profile.getId(), "BREAKTHROUGH", result);
            LogUtils.logBusiness("BREAKTHROUGH", "境界突破", 
                    "playerId", profile.getId(), "result", result);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("境界突破失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
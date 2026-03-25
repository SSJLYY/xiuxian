package com.xiuxian.game.modules.vip.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.vip.entity.PlayerVip;
import com.xiuxian.game.modules.vip.entity.RechargeRecord;
import com.xiuxian.game.modules.vip.entity.VipLevel;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.vip.service.RechargeService;
import com.xiuxian.game.modules.vip.service.VipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {
    
    private final VipService vipService;
    private final RechargeService rechargeService;
    private final PlayerService playerService;
    
    /**
     * 获取玩家VIP信息
     */
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PlayerVip> getVipInfo() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            PlayerVip playerVip = vipService.getPlayerVip(playerId);
            return ApiResponse.success("获取成功", playerVip);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有VIP等级配置
     */
    @GetMapping("/levels")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<VipLevel>> getVipLevels() {
        try {
            List<VipLevel> vipLevels = vipService.getAllVipLevels();
            return ApiResponse.success("获取成功", vipLevels);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 领取VIP每日奖励
     */
    @PostMapping("/daily-reward")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> claimDailyReward() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            boolean success = vipService.claimDailyReward(playerId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            
            if (success) {
                return ApiResponse.success("领取成功", result);
            } else {
                return ApiResponse.success("今日已领�?, result);
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取玩家充值记�?
     */
    @GetMapping("/recharge-records")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RechargeRecord>> getRechargeRecords(@RequestParam(defaultValue = "10") int limit) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            List<RechargeRecord> records = rechargeService.getPlayerRechargeRecords(playerId, limit);
            return ApiResponse.success("获取成功", records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 模拟充值接口（仅用于测试）
     */
    @PostMapping("/recharge/{amount}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> simulateRecharge(@PathVariable Integer amount) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            // 创建充值订�?
            RechargeRecord record = rechargeService.createRechargeOrder(playerId, amount);
            
            // 直接处理充值成�?
            rechargeService.processRechargeSuccess(record.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", record.getId());
            result.put("amount", amount);
            
            return ApiResponse.success("充值成�?, result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 检查VIP特权
     */
    @GetMapping("/privilege/{requiredLevel}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> checkPrivilege(@PathVariable Integer requiredLevel) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            boolean hasPrivilege = vipService.hasVipPrivilege(playerId, requiredLevel);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasPrivilege", hasPrivilege);
            result.put("requiredLevel", requiredLevel);
            
            PlayerVip playerVip = vipService.getPlayerVip(playerId);
            result.put("currentLevel", playerVip.getVipLevel());
            
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

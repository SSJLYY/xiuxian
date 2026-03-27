package com.xiuxian.game.modules.guild.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.guild.service.GuildBossService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 宗门BOSS控制器
 * 提供协作击杀BOSS、伤害查询、奖励领取接口
 */
@Slf4j
@RestController
@RequestMapping("/api/guild/boss")
@RequiredArgsConstructor
public class GuildBossController {

    private final GuildBossService guildBossService;
    private final PlayerService playerService;

    /**
     * 获取当前宗门BOSS信息
     * GET /api/guild/boss/current
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GuildBossService.GuildBossVO> getCurrentBoss() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            GuildBossService.GuildBossVO vo = guildBossService.getCurrentBoss(playerId);
            return ApiResponse.success("获取成功", vo);
        } catch (Exception e) {
            log.error("获取宗门BOSS失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 挑战宗门BOSS
     * POST /api/guild/boss/challenge
     */
    @PostMapping("/challenge")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GuildBossService.ChallengeResult> challengeBoss() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            GuildBossService.ChallengeResult result = guildBossService.challengeBoss(playerId);
            String msg = result.getBossDefeated() ? "BOSS已被击败！全宗门胜利！" : "攻击成功！造成" + result.getDamage() + "点伤害";
            return ApiResponse.success(msg, result);
        } catch (Exception e) {
            log.error("挑战宗门BOSS失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 领取BOSS击败奖励
     * POST /api/guild/boss/claim-reward
     */
    @PostMapping("/claim-reward")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> claimReward() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            Map<String, Object> result = guildBossService.claimReward(playerId);
            return ApiResponse.success("奖励领取成功", result);
        } catch (Exception e) {
            log.error("领取BOSS奖励失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}


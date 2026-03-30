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
 *
 * <p>提供宗门BOSS相关的REST API接口，包括BOSS信息查询、挑战和奖励领取</p>
 *
 * <p>主要接口：</p>
 * <ul>
 *   <li>GET /api/guild/boss/current - 获取当前宗门BOSS信息</li>
 *   <li>POST /api/guild/boss/challenge - 挑战宗门BOSS</li>
 *   <li>POST /api/guild/boss/claim-reward - 领取BOSS击败奖励</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-01-01
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
     *
     * <p>获取当前玩家所在宗门的BOSS信息，包括BOSS状态、血量等</p>
     *
     * @return BOSS信息
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GuildBossService.GuildBossVO> getCurrentBoss() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取宗门BOSS信息, playerId={}", playerId);
            GuildBossService.GuildBossVO vo = guildBossService.getCurrentBoss(playerId);
            log.info("获取宗门BOSS信息成功, playerId={}, bossId={}", playerId, vo.getBossId());
            return ApiResponse.success("获取成功", vo);
        } catch (Exception e) {
            log.error("获取宗门BOSS失败, playerId={}, error={}",
                    playerService.getCurrentPlayerId(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 挑战宗门BOSS
     *
     * <p>玩家挑战宗门BOSS，造成伤害并可能击败BOSS</p>
     *
     * @return 挑战结果，包括伤害值和是否击败BOSS
     */
    @PostMapping("/challenge")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GuildBossService.ChallengeResult> challengeBoss() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("玩家挑战宗门BOSS, playerId={}", playerId);
            GuildBossService.ChallengeResult result = guildBossService.challengeBoss(playerId);
            String msg = result.getBossDefeated() ? "BOSS已被击败！全宗门胜利！" : "攻击成功！造成" + result.getDamage() + "点伤害";
            log.info("宗门BOSS挑战完成, playerId={}, damage={}, bossDefeated={}",
                    playerId, result.getDamage(), result.getBossDefeated());
            return ApiResponse.success(msg, result);
        } catch (Exception e) {
            log.error("挑战宗门BOSS失败, playerId={}, error={}",
                    playerService.getCurrentPlayerId(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 领取BOSS击败奖励
     *
     * <p>当宗门BOSS被击败后，玩家领取击败奖励</p>
     *
     * @return 奖励内容
     */
    @PostMapping("/claim-reward")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> claimReward() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("领取宗门BOSS奖励, playerId={}", playerId);
            Map<String, Object> result = guildBossService.claimReward(playerId);
            log.info("宗门BOSS奖励领取成功, playerId={}", playerId);
            return ApiResponse.success("奖励领取成功", result);
        } catch (Exception e) {
            log.error("领取BOSS奖励失败, playerId={}, error={}",
                    playerService.getCurrentPlayerId(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}


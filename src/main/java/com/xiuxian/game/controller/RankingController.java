package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.entity.Ranking;
import com.xiuxian.game.service.PlayerService;
import com.xiuxian.game.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行榜控制器
 * 提供各类排行榜查询接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final PlayerService playerService;

    /**
     * 获取等级排行榜
     * GET /api/ranking/level?page=1&size=100
     */
    @GetMapping("/level")
    public ApiResponse<List<Ranking>> getLevelRanking(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            log.info("获取等级排行榜: page={}, size={}", page, size);
            
            // 限制最大返回数量
            if (size > 100) {
                size = 100;
            }
            
            List<Ranking> rankings = rankingService.getRankingList("LEVEL", size);
            return ApiResponse.success("获取成功", rankings);
        } catch (Exception e) {
            log.error("获取等级排行榜失败", e);
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取战力排行榜
     * GET /api/ranking/power
     */
    @GetMapping("/power")
    public ApiResponse<List<Ranking>> getPowerRanking(
            @RequestParam(defaultValue = "100") int size) {
        try {
            log.info("获取战力排行榜: size={}", size);
            
            if (size > 100) {
                size = 100;
            }
            
            List<Ranking> rankings = rankingService.getRankingList("COMBAT_POWER", size);
            return ApiResponse.success("获取成功", rankings);
        } catch (Exception e) {
            log.error("获取战力排行榜失败", e);
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取财富排行榜（灵石）
     * GET /api/ranking/wealth
     */
    @GetMapping("/wealth")
    public ApiResponse<List<Ranking>> getWealthRanking(
            @RequestParam(defaultValue = "100") int size) {
        try {
            log.info("获取财富排行榜: size={}", size);
            
            if (size > 100) {
                size = 100;
            }
            
            List<Ranking> rankings = rankingService.getRankingList("SPIRIT_STONES", size);
            return ApiResponse.success("获取成功", rankings);
        } catch (Exception e) {
            log.error("获取财富排行榜失败", e);
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取宠物排行榜
     * GET /api/ranking/pet
     */
    @GetMapping("/pet")
    public ApiResponse<List<Ranking>> getPetRanking(
            @RequestParam(defaultValue = "100") int size) {
        try {
            log.info("获取宠物排行榜: size={}", size);
            
            if (size > 100) {
                size = 100;
            }
            
            List<Ranking> rankings = rankingService.getRankingList("PET", size);
            return ApiResponse.success("获取成功", rankings);
        } catch (Exception e) {
            log.error("获取宠物排行榜失败", e);
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取玩家排名
     * GET /api/ranking/my-rank?type=level
     */
    @GetMapping("/my-rank")
    public ApiResponse<Map<String, Object>> getMyRank(
            @RequestParam String type) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            log.info("获取玩家排名: playerId={}, type={}", playerId, type);
            
            String rankingType = convertTypeToRankingType(type);
            Integer rank = rankingService.getPlayerRank(playerId, rankingType);
            
            // 获取玩家信息
            PlayerProfile player = playerService.getPlayerProfileById(playerId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("rank", rank != null ? rank : "未上榜");
            result.put("type", type);
            result.put("playerId", playerId);
            result.put("playerName", player != null ? player.getNickname() : "");
            result.put("realm", player != null ? player.getRealm() : "");
            
            // 根据类型添加对应的分数
            if (player != null && rank != null) {
                switch (rankingType) {
                    case "LEVEL":
                        result.put("score", player.getLevel());
                        break;
                    case "COMBAT_POWER":
                        long combatPower = player.getAttack() + player.getDefense() + 
                                          player.getHealth() + player.getMana() + player.getSpeed();
                        result.put("score", combatPower);
                        break;
                    case "SPIRIT_STONES":
                        result.put("score", player.getSpiritStones());
                        break;
                }
            }
            
            return ApiResponse.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取玩家排名失败: type={}", type, e);
            return ApiResponse.error("获取排名失败: " + e.getMessage());
        }
    }

    /**
     * 通用排行榜查询接口（兼容旧接口）
     * GET /api/ranking/{type}
     */
    @GetMapping("/{type}")
    public ApiResponse<List<Ranking>> getRankingList(
            @PathVariable String type,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            log.info("获取排行榜: type={}, limit={}", type, limit);
            
            if (limit > 100) {
                limit = 100;
            }
            
            String rankingType = convertTypeToRankingType(type);
            List<Ranking> rankings = rankingService.getRankingList(rankingType, limit);
            return ApiResponse.success("获取成功", rankings);
        } catch (Exception e) {
            log.error("获取排行榜失败: type={}", type, e);
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 转换前端类型到数据库排行榜类型
     */
    private String convertTypeToRankingType(String type) {
        switch (type.toLowerCase()) {
            case "level":
                return "LEVEL";
            case "power":
            case "combat_power":
                return "COMBAT_POWER";
            case "wealth":
            case "spirit_stones":
                return "SPIRIT_STONES";
            case "pet":
                return "PET";
            default:
                return type.toUpperCase();
        }
    }
}

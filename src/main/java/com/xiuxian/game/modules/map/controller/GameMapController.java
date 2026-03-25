package com.xiuxian.game.modules.map.controller;

import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.modules.map.entity.GameMap;
import com.xiuxian.game.modules.map.entity.PlayerMapProgress;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.response.ApiResponse;
import com.xiuxian.game.modules.map.service.GameMapService;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏地图控制器
 *
 * @author LevelDesigner
 * @since 2026-03-23
 */
@Slf4j
@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class GameMapController {

    private final GameMapService gameMapService;
    private final PlayerService playerService;

    /**
     * 获取所有地图
     */
    @GetMapping
    public ApiResponse<List<GameMap>> getAllMaps(Authentication authentication) {
        Integer playerId = getPlayerId(authentication);
        PlayerProfile profile = playerService.getPlayerProfile(playerId);

        List<GameMap> maps = gameMapService.getMapsForPlayer(
            playerId,
            profile.getLevel(),
            profile.getRealm()
        );

        return ApiResponse.success(maps);
    }

    /**
     * 获取地图详情
     */
    @GetMapping("/{mapId}")
    public ApiResponse<GameMap> getMapDetail(@PathVariable Integer mapId) {
        GameMap map = gameMapService.getMapById(mapId);
        if (map == null) {
            return ApiResponse.error(404, "地图不存在");
        }
        return ApiResponse.success(map);
    }

    /**
     * 获取当前所在地图
     */
    @GetMapping("/current")
    public ApiResponse<GameMap> getCurrentMap(Authentication authentication) {
        Integer playerId = getPlayerId(authentication);
        GameMap map = gameMapService.getCurrentMap(playerId);

        if (map == null) {
            // 如果没有当前地图，返回起始地图
            map = gameMapService.getMapById(1);
        }

        return ApiResponse.success(map);
    }

    /**
     * 进入地图
     */
    @PostMapping("/enter/{mapId}")
    public ApiResponse<PlayerMapProgress> enterMap(
            @PathVariable Integer mapId,
            Authentication authentication) {

        Integer playerId = getPlayerId(authentication);
        PlayerProfile profile = playerService.getPlayerProfile(playerId);

        try {
            PlayerMapProgress progress = gameMapService.enterMap(
                playerId,
                mapId,
                profile.getLevel(),
                profile.getRealm()
            );

            return ApiResponse.success(progress, "进入地图成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 离开当前地图
     */
    @PostMapping("/leave")
    public ApiResponse<Void> leaveMap(Authentication authentication) {
        Integer playerId = getPlayerId(authentication);
        gameMapService.leaveMap(playerId);
        return ApiResponse.success(null, "离开地图成功");
    }

    /**
     * 探索当前地图（触发遭遇战）
     */
    @GetMapping("/explore")
    public ApiResponse<GameMapService.MapEncounter> explore(Authentication authentication) {
        Integer playerId = getPlayerId(authentication);
        PlayerProfile profile = playerService.getPlayerProfile(playerId);

        GameMap currentMap = gameMapService.getCurrentMap(playerId);
        if (currentMap == null) {
            return ApiResponse.error(400, "你不在任何地图中");
        }

        // 安全区不能探索
        if ("SAFE".equals(currentMap.getMapType())) {
            return ApiResponse.error(400, "安全区不能探索");
        }

        GameMapService.MapEncounter encounter = gameMapService.generateEncounter(
            playerId,
            currentMap.getId(),
            profile.getLevel()
        );

        if (encounter == null) {
            return ApiResponse.error(500, "生成遭遇战失败");
        }

        return ApiResponse.success(encounter);
    }

    /**
     * 领取离线收益
     */
    @GetMapping("/offline-reward")
    public ApiResponse<GameMapService.OfflineReward> getOfflineReward(Authentication authentication) {
        Integer playerId = getPlayerId(authentication);

        PlayerMapProgress progress = gameMapService.getCurrentMap(playerId) != null
            ? new PlayerMapProgress() // 简化处理，实际需要查询
            : null;

        if (progress == null) {
            return ApiResponse.error(400, "无法计算离线收益");
        }

        GameMap map = gameMapService.getMapById(progress.getMapId());
        GameMapService.OfflineReward reward = gameMapService.calculateOfflineReward(
            playerId,
            progress,
            map
        );

        if (reward == null) {
            return ApiResponse.success(null, "没有离线收益");
        }

        return ApiResponse.success(reward);
    }

    /**
     * 从认证信息获取玩家ID
     */
    private Integer getPlayerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        return Integer.valueOf(authentication.getName());
    }
}

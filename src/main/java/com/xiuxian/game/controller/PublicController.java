package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.PlayerProfile;
import com.xiuxian.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PlayerService playerService;

    /**
     * 获取玩家公开信息
     */
    @GetMapping("/players/{playerId}")
    public ResponseEntity<ApiResponse<PlayerProfile>> getPlayerPublicInfo(@PathVariable Integer playerId) {
        try {
            PlayerProfile player = playerService.getPlayerProfileById(playerId);

            // 创建公开信息副本，不包含敏感信息
            PlayerProfile publicInfo = PlayerProfile.builder()
                    .id(player.getId())
                    .nickname(player.getNickname())
                    .level(player.getLevel())
                    .realm(player.getRealm())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("获取成功", publicInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取排行榜
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<?>> getLeaderboard() {
        try {
            // 实现获取排行榜逻辑
            // 这里只是示例，实际应从数据库查询
            return ResponseEntity.ok(ApiResponse.success("获取排行榜成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
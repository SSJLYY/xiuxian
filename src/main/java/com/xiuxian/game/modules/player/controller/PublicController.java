package com.xiuxian.game.modules.player.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PlayerService playerService;

    @GetMapping("/players/{playerId}")
    public ResponseEntity<ApiResponse<PlayerProfile>> getPlayerPublicInfo(@PathVariable Integer playerId) {
        try {
            PlayerProfile player = playerService.getPlayerProfileById(playerId);
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

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<?>> getLeaderboard() {
        try {
            return ResponseEntity.ok(ApiResponse.success("获取排行榜成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

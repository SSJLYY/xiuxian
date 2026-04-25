package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.AdminApiResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.modules.admin.service.AdminPlayerService;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台玩家管理 Controller
 * 提供玩家列表查询等管理功能
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin/players")
@CrossOrigin(origins = "${admin.cors.allowed-origins:localhost,127.0.0.1}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlayerController {

    private final AdminPlayerService adminPlayerService;

    /**
     * 查询玩家列表（分页）
     *
     * @param page     页码（默认 1）
     * @param size     每页大小（默认 10）
     * @param nickname 昵称关键词（可选）
     * @param userId   用户 ID（可选）
     * @return 玩家列表分页数据
     */
    @GetMapping
    public ResponseEntity<AdminApiResponse> getPlayers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Long userId) {
        try {
            Page<PlayerProfile> pageData = adminPlayerService.getPlayerList(page, size, nickname,
                    userId != null ? userId.intValue() : null);
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("records", pageData.getRecords());
            data.put("current", pageData.getCurrent());
            data.put("pages", pageData.getPages());
            data.put("total", pageData.getTotal());
            data.put("size", pageData.getSize());
            
            return ResponseEntity.ok(AdminApiResponse.success("查询玩家列表成功", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdminApiResponse.error("查询玩家列表失败: " + e.getMessage()));
        }
    }
}

package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.AdminApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台玩家管理 Controller
 * 提供玩家列表查询等管理功能
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin/players")
@CrossOrigin(origins = "*")
public class AdminPlayerController {

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
            Map<String, Object> data = new HashMap<>();
            data.put("records", new ArrayList<>());
            data.put("current", page);
            data.put("pages", 1);
            data.put("total", 0);
            
            return ResponseEntity.ok(AdminApiResponse.success("查询玩家列表成功", data));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminApiResponse.error("查询玩家列表失败: " + e.getMessage()));
        }
    }
}

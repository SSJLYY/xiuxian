package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.AdminApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员玩家管理控制器
 */
@RestController
@RequestMapping("/api/admin/players")
@CrossOrigin(origins = "*")
public class AdminPlayerController {

    /**
     * 获取玩家列表
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
            
            return ResponseEntity.ok(AdminApiResponse.success("获取玩家列表成功", data));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminApiResponse.error("获取玩家列表失败: " + e.getMessage()));
        }
    }
}
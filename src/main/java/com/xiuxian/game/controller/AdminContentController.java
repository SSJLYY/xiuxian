package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.AdminApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员内容管理控制器
 */
@RestController
@RequestMapping("/api/admin/content")
@CrossOrigin(origins = "*")
public class AdminContentController {

    /**
     * 获取内容统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminApiResponse> getContentStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("items", 0);
            stats.put("equipments", 0);
            stats.put("skills", 0);
            stats.put("pets", 0);
            stats.put("monsters", 0);
            
            return ResponseEntity.ok(AdminApiResponse.success("获取内容统计数据成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminApiResponse.error("获取内容统计数据失败: " + e.getMessage()));
        }
    }
}
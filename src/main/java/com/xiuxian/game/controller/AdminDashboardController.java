package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.AdminApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员仪表板控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminApiResponse> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("onlinePlayers", 0);
            stats.put("newUsersToday", 0);
            stats.put("activeToday", 0);
            stats.put("todayIncome", 0);
            
            return ResponseEntity.ok(AdminApiResponse.success("获取仪表板数据成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(AdminApiResponse.error("获取仪表板数据失败: " + e.getMessage()));
        }
    }
}
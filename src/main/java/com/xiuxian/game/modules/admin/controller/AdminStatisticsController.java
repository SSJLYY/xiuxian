package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.entity.DailyStatistics;
import com.xiuxian.game.modules.admin.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台统计数据 Controller
 * 提供整体统计、最近每日统计、收入统计、玩家增长统计等接口
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 获取整体统计数据（累计玩家数、累计收入等）
     *
     * @return 整体统计 Map
     */
    @GetMapping("/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverallStats() {
        try {
            Map<String, Object> stats = adminStatisticsService.getOverallStats();
            return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取最近 N 天的每日统计数据
     *
     * @param days 查询天数（默认 7 天）
     * @return 每日统计列表
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DailyStatistics>>> getRecentStats(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<DailyStatistics> stats = adminStatisticsService.getRecentStats(days);
            return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取收入统计数据
     *
     * @param days 查询天数（默认 7 天）
     * @return 收入统计 Map
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueStats(
            @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> stats = adminStatisticsService.getRevenueStats(days);
            return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取玩家增长统计数据
     *
     * @param days 查询天数（默认 7 天）
     * @return 玩家增长统计 Map
     */
    @GetMapping("/player-growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlayerGrowthStats(
            @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> stats = adminStatisticsService.getPlayerGrowthStats(days);
            return ResponseEntity.ok(ApiResponse.success("获取成功", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}

package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.DailyStatistics;
import com.xiuxian.game.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 获取综合统计数据
     */
    @GetMapping("/overall")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getOverallStats() {
        try {
            Map<String, Object> stats = adminStatisticsService.getOverallStats();
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取最近的统计数据
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<DailyStatistics>> getRecentStats(
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<DailyStatistics> stats = adminStatisticsService.getRecentStats(days);
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取收入统计
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getRevenueStats(
            @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> stats = adminStatisticsService.getRevenueStats(days);
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取玩家增长统计
     */
    @GetMapping("/player-growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getPlayerGrowthStats(
            @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> stats = adminStatisticsService.getPlayerGrowthStats(days);
            return ApiResponse.success("获取成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统监控 Controller
 * 提供 CPU、内存、磁盘及系统综合信息的查询接口
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin/monitoring")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    /**
     * 获取操作系统基础信息
     */
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> info = adminMonitoringService.getSystemInfo();
            return ApiResponse.success("获取成功", info);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取 CPU 使用率信息
     */
    @GetMapping("/cpu")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getCpuInfo() {
        try {
            Map<String, Object> info = adminMonitoringService.getCpuInfo();
            return ApiResponse.success("获取成功", info);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取内存使用情况
     */
    @GetMapping("/memory")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getMemoryInfo() {
        try {
            Map<String, Object> info = adminMonitoringService.getMemoryInfo();
            return ApiResponse.success("获取成功", info);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取磁盘使用情况
     */
    @GetMapping("/disk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getDiskInfo() {
        try {
            Map<String, Object> info = adminMonitoringService.getDiskInfo();
            return ApiResponse.success("获取成功", info);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取全部监控信息（系统 + CPU + 内存 + 磁盘）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getAllMonitoringInfo() {
        try {
            Map<String, Object> allInfo = new java.util.HashMap<>();

            allInfo.put("system", adminMonitoringService.getSystemInfo());
            allInfo.put("cpu", adminMonitoringService.getCpuInfo());
            allInfo.put("memory", adminMonitoringService.getMemoryInfo());
            allInfo.put("disk", adminMonitoringService.getDiskInfo());

            return ApiResponse.success("获取成功", allInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

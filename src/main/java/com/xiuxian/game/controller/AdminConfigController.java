package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.GameConfig;
import com.xiuxian.game.service.AdminOperationLogService;
import com.xiuxian.game.service.GameConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {
    
    private final GameConfigService gameConfigService;
    private final AdminOperationLogService adminOperationLogService;
    
    /**
     * 获取所有配置
     */
    @GetMapping("/list")
    public ApiResponse<List<GameConfig>> getAllConfigs() {
        try {
            List<GameConfig> configs = gameConfigService.getAllConfigs();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return ApiResponse.error("获取失败");
        }
    }
    
    /**
     * 按分类获取配置
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<GameConfig>> getConfigsByCategory(@PathVariable String category) {
        try {
            List<GameConfig> configs = gameConfigService.getConfigsByCategory(category);
            return ApiResponse.success(configs);
        } catch (Exception e) {
            log.error("获取分类配置失败: category={}", category, e);
            return ApiResponse.error("获取失败");
        }
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/update")
    public ApiResponse<Void> updateConfig(@RequestParam String key,
                                        @RequestParam String value,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false) String category,
                                        HttpServletRequest request) {
        try {
            String oldValue = gameConfigService.getString(key, null);
            gameConfigService.setConfig(key, value, description, category);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "UPDATE_CONFIG", "CONFIG", 
                    key, String.format("更新配置: %s 从 %s 改为 %s", key, oldValue, value), request);
            
            return ApiResponse.success("更新成功", null);
        } catch (Exception e) {
            log.error("更新配置失败: key={}, value={}", key, value, e);
            return ApiResponse.error("更新失败");
        }
    }
    
    /**
     * 创建配置
     */
    @PostMapping("/create")
    public ApiResponse<Void> createConfig(@RequestParam String key,
                                        @RequestParam String value,
                                        @RequestParam String description,
                                        @RequestParam String category,
                                        HttpServletRequest request) {
        try {
            gameConfigService.setConfig(key, value, description, category);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "CREATE_CONFIG", "CONFIG", 
                    key, String.format("创建配置: %s = %s", key, value), request);
            
            return ApiResponse.success("创建成功", null);
        } catch (Exception e) {
            log.error("创建配置失败: key={}, value={}", key, value, e);
            return ApiResponse.error("创建失败");
        }
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteConfig(@RequestParam String key,
                                        HttpServletRequest request) {
        try {
            String oldValue = gameConfigService.getString(key, null);
            gameConfigService.deleteConfig(key);
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "DELETE_CONFIG", "CONFIG", 
                    key, String.format("删除配置: %s (原值: %s)", key, oldValue), request);
            
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除配置失败: key={}", key, e);
            return ApiResponse.error("删除失败");
        }
    }
    
    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshCache(HttpServletRequest request) {
        try {
            gameConfigService.refreshCache();
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "REFRESH_CONFIG_CACHE", null, 
                    null, "刷新配置缓存", request);
            
            return ApiResponse.success("刷新成功", null);
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            return ApiResponse.error("刷新失败");
        }
    }
    
    /**
     * 批量更新配置
     */
    @PostMapping("/batch-update")
    public ApiResponse<Void> batchUpdateConfigs(@RequestBody Map<String, String> configs,
                                              HttpServletRequest request) {
        try {
            StringBuilder logDetail = new StringBuilder("批量更新配置: ");
            
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                String key = entry.getKey();
                String newValue = entry.getValue();
                String oldValue = gameConfigService.getString(key, null);
                
                gameConfigService.setConfig(key, newValue, null, null);
                logDetail.append(String.format("%s: %s->%s; ", key, oldValue, newValue));
            }
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "BATCH_UPDATE_CONFIG", "CONFIG", 
                    null, logDetail.toString(), request);
            
            return ApiResponse.success("批量更新成功", null);
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return ApiResponse.error("批量更新失败");
        }
    }
    
    /**
     * 开启/关闭双倍经验活动
     */
    @PostMapping("/toggle-double-exp")
    public ApiResponse<Void> toggleDoubleExp(@RequestParam boolean enabled,
                                           HttpServletRequest request) {
        try {
            gameConfigService.setConfig(GameConfigService.ConfigKeys.DOUBLE_EXP_ENABLED, 
                    String.valueOf(enabled), "双倍经验活动开启", "ACTIVITY");
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "TOGGLE_DOUBLE_EXP", "ACTIVITY", 
                    null, "双倍经验活动: " + (enabled ? "开启" : "关闭"), request);
            
            return ApiResponse.success(enabled ? "双倍经验活动已开启" : "双倍经验活动已关闭", null);
        } catch (Exception e) {
            log.error("切换双倍经验活动失败: enabled={}", enabled, e);
            return ApiResponse.error("操作失败");
        }
    }
    
    /**
     * 开启/关闭双倍掉落活动
     */
    @PostMapping("/toggle-double-drop")
    public ApiResponse<Void> toggleDoubleDrop(@RequestParam boolean enabled,
                                            HttpServletRequest request) {
        try {
            gameConfigService.setConfig(GameConfigService.ConfigKeys.DOUBLE_DROP_ENABLED, 
                    String.valueOf(enabled), "双倍掉落活动开启", "ACTIVITY");
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "TOGGLE_DOUBLE_DROP", "ACTIVITY", 
                    null, "双倍掉落活动: " + (enabled ? "开启" : "关闭"), request);
            
            return ApiResponse.success(enabled ? "双倍掉落活动已开启" : "双倍掉落活动已关闭", null);
        } catch (Exception e) {
            log.error("切换双倍掉落活动失败: enabled={}", enabled, e);
            return ApiResponse.error("操作失败");
        }
    }
    
    /**
     * 开启/关闭维护模式
     */
    @PostMapping("/toggle-maintenance")
    public ApiResponse<Void> toggleMaintenance(@RequestParam boolean enabled,
                                             HttpServletRequest request) {
        try {
            gameConfigService.setConfig(GameConfigService.ConfigKeys.MAINTENANCE_MODE, 
                    String.valueOf(enabled), "维护模式", "SYSTEM");
            
            // 记录管理员操作日志
            adminOperationLogService.recordOperation(1, "TOGGLE_MAINTENANCE", "SYSTEM", 
                    null, "维护模式: " + (enabled ? "开启" : "关闭"), request);
            
            return ApiResponse.success(enabled ? "维护模式已开启" : "维护模式已关闭", null);
        } catch (Exception e) {
            log.error("切换维护模式失败: enabled={}", enabled, e);
            return ApiResponse.error("操作失败");
        }
    }
}
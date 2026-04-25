package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.entity.GameConfig;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.admin.service.AdminOperationLogService;
import com.xiuxian.game.modules.admin.service.GameConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 游戏配置管理控制器
 * 提供游戏参数的增删改查和缓存刷新功能
 *
 * @author shaun.sheng
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    private final GameConfigService gameConfigService;
    private final AdminOperationLogService adminOperationLogService;
    private final AdminAuthService adminAuthService;

    private Integer getAdminId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0;
        }
        String token = authHeader.substring(7);
        return adminAuthService.isValidAdminToken(token) ? 1 : 0;
    }

    /**
     * 获取所有游戏配置
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<GameConfig>>> getAllConfigs() {
        try {
            List<GameConfig> configs = gameConfigService.getAllConfigs();
            return ResponseEntity.ok(ApiResponse.success(configs));
        } catch (Exception e) {
            log.error("获取所有配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取配置失败"));
        }
    }

    /**
     * 按分类获取配置
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<GameConfig>>> getConfigsByCategory(@PathVariable String category) {
        try {
            List<GameConfig> configs = gameConfigService.getConfigsByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(configs));
        } catch (Exception e) {
            log.error("按分类获取配置失败, category={}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("获取配置失败"));
        }
    }

    /**
     * 更新配置项
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateConfig(@RequestParam String key,
                                          @RequestParam String value,
                                          @RequestParam(required = false) String description,
                                          @RequestParam(required = false) String category,
                                          HttpServletRequest request) {
        try {
            String oldValue = gameConfigService.getString(key, null);
            gameConfigService.setConfig(key, value, description, category);

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "UPDATE_CONFIG", "CONFIG",
                    key, String.format("更新配置: %s 旧值=%s 新值=%s", key, oldValue, value), request);

            return ResponseEntity.ok(ApiResponse.success("配置更新成功", null));
        } catch (Exception e) {
            log.error("更新配置失败, key={}, value={}", key, value, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("更新配置失败"));
        }
    }

    /**
     * 新增配置项
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createConfig(@RequestParam String key,
                                          @RequestParam String value,
                                          @RequestParam String description,
                                          @RequestParam String category,
                                          HttpServletRequest request) {
        try {
            gameConfigService.setConfig(key, value, description, category);

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "CREATE_CONFIG", "CONFIG",
                    key, String.format("新增配置: %s = %s", key, value), request);

            return ResponseEntity.ok(ApiResponse.success("配置新增成功", null));
        } catch (Exception e) {
            log.error("新增配置失败, key={}, value={}", key, value, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("新增配置失败"));
        }
    }

    /**
     * 删除配置项
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@RequestParam String key,
                                          HttpServletRequest request) {
        try {
            String oldValue = gameConfigService.getString(key, null);
            gameConfigService.deleteConfig(key);

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "DELETE_CONFIG", "CONFIG",
                    key, String.format("删除配置: %s (原值=%s)", key, oldValue), request);

            return ResponseEntity.ok(ApiResponse.success("配置删除成功", null));
        } catch (Exception e) {
            log.error("删除配置失败, key={}", key, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("删除配置失败"));
        }
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshCache(HttpServletRequest request) {
        try {
            gameConfigService.refreshCache();

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "REFRESH_CONFIG_CACHE", null,
                    null, "刷新游戏配置缓存", request);

            return ResponseEntity.ok(ApiResponse.success("配置缓存刷新成功", null));
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("刷新缓存失败"));
        }
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/batch-update")
    public ResponseEntity<ApiResponse<Void>> batchUpdateConfigs(@RequestBody Map<String, String> configs,
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

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "BATCH_UPDATE_CONFIG", "CONFIG",
                    null, logDetail.toString(), request);

            return ResponseEntity.ok(ApiResponse.success("批量更新成功", null));
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("批量更新失败"));
        }
    }

    /**
     * 开启/关闭双倍经验活动
     */
    @PostMapping("/toggle-double-exp")
    public ResponseEntity<ApiResponse<Void>> toggleDoubleExp(@RequestParam boolean enabled,
                                              HttpServletRequest request) {
        try {
            gameConfigService.setConfig(GameConfigService.ConfigKeys.DOUBLE_EXP_ENABLED,
                    String.valueOf(enabled), "双倍经验活动开关", "ACTIVITY");

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "TOGGLE_DOUBLE_EXP", "ACTIVITY",
                    null, "双倍经验活动: " + (enabled ? "已开启" : "已关闭"), request);

            return ResponseEntity.ok(ApiResponse.success(enabled ? "双倍经验已开启" : "双倍经验已关闭", null));
        } catch (Exception e) {
            log.error("切换双倍经验状态失败, enabled={}", enabled, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("操作失败"));
        }
    }

    /**
     * 开启/关闭双倍掉落活动
     */
    @PostMapping("/toggle-double-drop")
    public ResponseEntity<ApiResponse<Void>> toggleDoubleDrop(@RequestParam boolean enabled,
                                               HttpServletRequest request) {
        try {
            gameConfigService.setConfig(GameConfigService.ConfigKeys.DOUBLE_DROP_ENABLED,
                    String.valueOf(enabled), "双倍掉落活动开关", "ACTIVITY");

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "TOGGLE_DOUBLE_DROP", "ACTIVITY",
                    null, "双倍掉落活动: " + (enabled ? "已开启" : "已关闭"), request);

            return ResponseEntity.ok(ApiResponse.success(enabled ? "双倍掉落已开启" : "双倍掉落已关闭", null));
        } catch (Exception e) {
            log.error("切换双倍掉落状态失败, enabled={}", enabled, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("操作失败"));
        }
    }

    /**
     * 开启/关闭维护模式
     */
    @PostMapping("/toggle-maintenance")
    public ResponseEntity<ApiResponse<Void>> toggleMaintenance(@RequestParam boolean enabled,
                                                HttpServletRequest request) {
        try {
            gameConfigService.setConfig(GameConfigService.ConfigKeys.MAINTENANCE_MODE,
                    String.valueOf(enabled), "维护模式开关", "SYSTEM");

            // 记录操作日志
            adminOperationLogService.recordOperation(getAdminId(request), "TOGGLE_MAINTENANCE", "SYSTEM",
                    null, "维护模式: " + (enabled ? "已开启" : "已关闭"), request);

            return ResponseEntity.ok(ApiResponse.success(enabled ? "维护模式已开启" : "维护模式已关闭", null));
        } catch (Exception e) {
            log.error("切换维护模式失败, enabled={}", enabled, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("操作失败"));
        }
    }
}

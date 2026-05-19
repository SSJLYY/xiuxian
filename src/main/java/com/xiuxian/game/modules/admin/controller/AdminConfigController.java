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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminConfigController {

    private final GameConfigService gameConfigService;
    private final AdminOperationLogService adminOperationLogService;
    private final AdminAuthService adminAuthService;

    private Integer getAdminId() {
        return adminAuthService.getCurrentAdminId();
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<GameConfig>>> getAllConfigs() {
        try {
            return ResponseEntity.ok(ApiResponse.success(gameConfigService.getAllConfigs()));
        } catch (Exception e) {
            log.error("获取所有配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取配置失败"));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<GameConfig>>> getConfigsByCategory(
            @PathVariable @NotBlank(message = "分类不能为空") String category) {
        try {
            return ResponseEntity.ok(ApiResponse.success(gameConfigService.getConfigsByCategory(category.trim())));
        } catch (Exception e) {
            log.error("按分类获取配置失败, category={}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取配置失败"));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateConfig(
            @RequestParam @NotBlank(message = "key不能为空") String key,
            @RequestParam @NotBlank(message = "value不能为空") String value,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        try {
            String normalizedKey = key.trim();
            String oldValue = gameConfigService.getString(normalizedKey, null);
            gameConfigService.updateConfig(normalizedKey, value, description, category);

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "UPDATE_CONFIG",
                    "CONFIG",
                    normalizedKey,
                    String.format("更新配置: %s old=%s new=%s", normalizedKey, oldValue, value),
                    request);

            return ResponseEntity.ok(ApiResponse.success("配置更新成功", null));
        } catch (Exception e) {
            log.error("更新配置失败, key={}, value={}", key, value, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createConfig(
            @RequestParam @NotBlank(message = "key不能为空") String key,
            @RequestParam @NotBlank(message = "value不能为空") String value,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        try {
            String normalizedKey = key.trim();
            gameConfigService.createConfig(normalizedKey, value, description, category);

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "CREATE_CONFIG",
                    "CONFIG",
                    normalizedKey,
                    String.format("新增配置: %s = %s", normalizedKey, value),
                    request);

            return ResponseEntity.ok(ApiResponse.success("配置新增成功", null));
        } catch (Exception e) {
            log.error("新增配置失败, key={}, value={}", key, value, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(
            @RequestParam @NotBlank(message = "key不能为空") String key,
            HttpServletRequest request) {
        try {
            String normalizedKey = key.trim();
            String oldValue = gameConfigService.getString(normalizedKey, null);
            gameConfigService.deleteConfig(normalizedKey);

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "DELETE_CONFIG",
                    "CONFIG",
                    normalizedKey,
                    String.format("删除配置: %s (old=%s)", normalizedKey, oldValue),
                    request);

            return ResponseEntity.ok(ApiResponse.success("配置删除成功", null));
        } catch (Exception e) {
            log.error("删除配置失败, key={}", key, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshCache(HttpServletRequest request) {
        try {
            gameConfigService.refreshCache();
            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "REFRESH_CONFIG_CACHE",
                    null,
                    null,
                    "刷新游戏配置缓存",
                    request);
            return ResponseEntity.ok(ApiResponse.success("配置缓存刷新成功", null));
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("刷新缓存失败"));
        }
    }

    @PostMapping("/batch-update")
    public ResponseEntity<ApiResponse<Void>> batchUpdateConfigs(
            @RequestBody Map<String, String> configs,
            HttpServletRequest request) {
        try {
            StringBuilder logDetail = new StringBuilder("批量更新配置: ");
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                String normalizedKey = entry.getKey() == null ? null : entry.getKey().trim();
                String newValue = entry.getValue();
                String oldValue = gameConfigService.getString(normalizedKey, null);

                gameConfigService.updateConfig(normalizedKey, newValue, null, null);
                logDetail.append(String.format("%s: %s->%s; ", normalizedKey, oldValue, newValue));
            }

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "BATCH_UPDATE_CONFIG",
                    "CONFIG",
                    null,
                    logDetail.toString(),
                    request);

            return ResponseEntity.ok(ApiResponse.success("批量更新成功", null));
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/toggle-double-exp")
    public ResponseEntity<ApiResponse<Void>> toggleDoubleExp(
            @RequestParam boolean enabled,
            HttpServletRequest request) {
        try {
            gameConfigService.setConfig(
                    GameConfigService.ConfigKeys.DOUBLE_EXP_ENABLED,
                    String.valueOf(enabled),
                    "双倍经验活动开关",
                    "ACTIVITY");

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "TOGGLE_DOUBLE_EXP",
                    "ACTIVITY",
                    null,
                    "双倍经验活动: " + (enabled ? "已开启" : "已关闭"),
                    request);

            return ResponseEntity.ok(ApiResponse.success(enabled ? "双倍经验已开启" : "双倍经验已关闭", null));
        } catch (Exception e) {
            log.error("切换双倍经验状态失败, enabled={}", enabled, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("操作失败"));
        }
    }

    @PostMapping("/toggle-double-drop")
    public ResponseEntity<ApiResponse<Void>> toggleDoubleDrop(
            @RequestParam boolean enabled,
            HttpServletRequest request) {
        try {
            gameConfigService.setConfig(
                    GameConfigService.ConfigKeys.DOUBLE_DROP_ENABLED,
                    String.valueOf(enabled),
                    "双倍掉落活动开关",
                    "ACTIVITY");

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "TOGGLE_DOUBLE_DROP",
                    "ACTIVITY",
                    null,
                    "双倍掉落活动: " + (enabled ? "已开启" : "已关闭"),
                    request);

            return ResponseEntity.ok(ApiResponse.success(enabled ? "双倍掉落已开启" : "双倍掉落已关闭", null));
        } catch (Exception e) {
            log.error("切换双倍掉落状态失败, enabled={}", enabled, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("操作失败"));
        }
    }

    @PostMapping("/toggle-maintenance")
    public ResponseEntity<ApiResponse<Void>> toggleMaintenance(
            @RequestParam boolean enabled,
            HttpServletRequest request) {
        try {
            gameConfigService.setConfig(
                    GameConfigService.ConfigKeys.MAINTENANCE_MODE,
                    String.valueOf(enabled),
                    "维护模式开关",
                    "SYSTEM");

            adminOperationLogService.recordOperation(
                    getAdminId(),
                    "TOGGLE_MAINTENANCE",
                    "SYSTEM",
                    null,
                    "维护模式: " + (enabled ? "已开启" : "已关闭"),
                    request);

            return ResponseEntity.ok(ApiResponse.success(enabled ? "维护模式已开启" : "维护模式已关闭", null));
        } catch (Exception e) {
            log.error("切换维护模式失败, enabled={}", enabled, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("操作失败"));
        }
    }
}

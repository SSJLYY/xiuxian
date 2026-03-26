package com.xiuxian.game.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.entity.AdminOperationLog;
import com.xiuxian.game.modules.player.entity.PlayerLoginLog;
import com.xiuxian.game.modules.admin.service.AdminOperationLogService;
import com.xiuxian.game.modules.player.service.PlayerLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 日志管理控制器
 * 提供玩家登录日志和管理员操作日志的查询功能
 *
 * @author shaun.sheng
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    private final PlayerLoginLogService playerLoginLogService;
    private final AdminOperationLogService adminOperationLogService;

    /**
     * 查询玩家登录日志（分页）
     */
    @GetMapping("/player-login")
    public ApiResponse<Page<PlayerLoginLog>> getPlayerLoginLogs(
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<PlayerLoginLog> result = playerLoginLogService.getLoginLogs(
                    playerId, startTime, endTime, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询玩家登录日志失败", e);
            return ApiResponse.error("查询失败");
        }
    }

    /**
     * 统计玩家登录次数
     */
    @GetMapping("/player-login/count")
    public ApiResponse<Long> countPlayerLogins(
            @RequestParam Integer playerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            Long count = playerLoginLogService.countPlayerLogins(playerId, startTime, endTime);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("统计玩家登录次数失败", e);
            return ApiResponse.error("查询失败");
        }
    }

    /**
     * 查询管理员操作日志（分页）
     */
    @GetMapping("/admin-operation")
    public ApiResponse<Page<AdminOperationLog>> getAdminOperationLogs(
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Page<AdminOperationLog> result = adminOperationLogService.getOperationLogs(
                    adminId, operationType, targetType, startTime, endTime, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询管理员操作日志失败", e);
            return ApiResponse.error("查询失败");
        }
    }

    /**
     * 统计管理员操作次数
     */
    @GetMapping("/admin-operation/count")
    public ApiResponse<Long> countAdminOperations(
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        try {
            Long count = adminOperationLogService.countOperations(adminId, operationType, startTime, endTime);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("统计管理员操作次数失败", e);
            return ApiResponse.error("查询失败");
        }
    }

    /**
     * 获取所有操作类型列表
     */
    @GetMapping("/operation-types")
    public ApiResponse<String[]> getOperationTypes() {
        String[] operationTypes = {
            AdminOperationLogService.OperationType.PLAYER_BAN,
            AdminOperationLogService.OperationType.PLAYER_UNBAN,
            AdminOperationLogService.OperationType.PLAYER_DELETE,
            AdminOperationLogService.OperationType.PLAYER_REWARD,
            AdminOperationLogService.OperationType.PLAYER_MODIFY,
            AdminOperationLogService.OperationType.ANNOUNCEMENT_CREATE,
            AdminOperationLogService.OperationType.ANNOUNCEMENT_UPDATE,
            AdminOperationLogService.OperationType.ANNOUNCEMENT_DELETE,
            AdminOperationLogService.OperationType.MAIL_SEND,
            AdminOperationLogService.OperationType.MAIL_BATCH_SEND,
            AdminOperationLogService.OperationType.GIFT_CODE_CREATE,
            AdminOperationLogService.OperationType.GIFT_CODE_DISABLE,
            AdminOperationLogService.OperationType.ACTIVITY_CREATE,
            AdminOperationLogService.OperationType.ACTIVITY_UPDATE,
            AdminOperationLogService.OperationType.CONFIG_UPDATE
        };
        return ApiResponse.success(operationTypes);
    }
}

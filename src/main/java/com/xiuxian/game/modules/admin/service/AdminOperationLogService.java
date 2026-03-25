package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.modules.admin.entity.AdminOperationLog;
import com.xiuxian.game.modules.admin.mapper.AdminOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 管理员操作日志服�?
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationLogService {
    
    private final AdminOperationLogMapper operationLogMapper;
    
    /**
     * 记录管理员操作日�?
     */
    @Async
    public void recordOperation(Integer adminId, String operationType, String targetType, 
                              String targetId, String operationDetail, HttpServletRequest request) {
        try {
            AdminOperationLog operationLog = new AdminOperationLog();
            operationLog.setAdminId(adminId);
            operationLog.setOperationType(operationType);
            operationLog.setTargetType(targetType);
            operationLog.setTargetId(targetId);
            operationLog.setOperationDetail(operationDetail);
            operationLog.setIpAddress(getClientIpAddress(request));
            operationLog.setCreatedAt(LocalDateTime.now());
            
            operationLogMapper.insert(operationLog);
            log.info("记录管理员操作日�? adminId={}, operation={}, target={}:{}", 
                    adminId, operationType, targetType, targetId);
        } catch (Exception e) {
            log.error("记录管理员操作日志失�? adminId={}, operation={}", adminId, operationType, e);
        }
    }
    
    /**
     * 记录管理员操作日志（简化版本）
     */
    @Async
    public void recordOperation(Integer adminId, String operationType, String operationDetail, 
                              HttpServletRequest request) {
        recordOperation(adminId, operationType, null, null, operationDetail, request);
    }
    
    /**
     * 分页查询操作日志
     */
    public Page<AdminOperationLog> getOperationLogs(Integer adminId, String operationType, 
                                                   String targetType, LocalDateTime startTime, 
                                                   LocalDateTime endTime, int page, int size) {
        Page<AdminOperationLog> pageParam = new Page<>(page, size);
        QueryWrapper<AdminOperationLog> queryWrapper = new QueryWrapper<>();
        
        if (adminId != null) {
            queryWrapper.eq("admin_id", adminId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (targetType != null && !targetType.isEmpty()) {
            queryWrapper.eq("target_type", targetType);
        }
        if (startTime != null) {
            queryWrapper.ge("created_at", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("created_at", endTime);
        }
        
        queryWrapper.orderByDesc("created_at");
        return operationLogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 统计操作次数
     */
    public Long countOperations(Integer adminId, String operationType, LocalDateTime startTime, 
                               LocalDateTime endTime) {
        QueryWrapper<AdminOperationLog> queryWrapper = new QueryWrapper<>();
        
        if (adminId != null) {
            queryWrapper.eq("admin_id", adminId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (startTime != null) {
            queryWrapper.ge("created_at", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("created_at", endTime);
        }
        
        return operationLogMapper.selectCount(queryWrapper);
    }
    
    /**
     * 清理过期日志（保�?80天）
     */
    @Async
    public void cleanExpiredLogs() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(180);
            QueryWrapper<AdminOperationLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.lt("created_at", expireTime);
            
            int deletedCount = operationLogMapper.delete(queryWrapper);
            log.info("清理过期管理员操作日�? {} �?, deletedCount);
        } catch (Exception e) {
            log.error("清理过期管理员操作日志失�?, e);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 操作类型常量
     */
    public static class OperationType {
        public static final String PLAYER_BAN = "PLAYER_BAN";
        public static final String PLAYER_UNBAN = "PLAYER_UNBAN";
        public static final String PLAYER_DELETE = "PLAYER_DELETE";
        public static final String PLAYER_REWARD = "PLAYER_REWARD";
        public static final String PLAYER_MODIFY = "PLAYER_MODIFY";
        public static final String ANNOUNCEMENT_CREATE = "ANNOUNCEMENT_CREATE";
        public static final String ANNOUNCEMENT_UPDATE = "ANNOUNCEMENT_UPDATE";
        public static final String ANNOUNCEMENT_DELETE = "ANNOUNCEMENT_DELETE";
        public static final String MAIL_SEND = "MAIL_SEND";
        public static final String MAIL_BATCH_SEND = "MAIL_BATCH_SEND";
        public static final String GIFT_CODE_CREATE = "GIFT_CODE_CREATE";
        public static final String GIFT_CODE_DISABLE = "GIFT_CODE_DISABLE";
        public static final String ACTIVITY_CREATE = "ACTIVITY_CREATE";
        public static final String ACTIVITY_UPDATE = "ACTIVITY_UPDATE";
        public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    }
}

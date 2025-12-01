package com.xiuxian.game.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 日志工具类
 * 
 * <p>提供统一的日志记录工具和方法，支持链路追踪、结构化日志等功能。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>链路追踪ID生成和管理</li>
 *   <li>结构化日志记录</li>
 *   <li>用户操作日志记录</li>
 *   <li>业务日志记录</li>
 *   <li>安全日志记录</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 开始链路追踪
 * LogUtils.startTrace();
 * 
 * // 记录用户操作
 * LogUtils.logUserAction("user123", "LOGIN", "用户登录成功");
 * 
 * // 记录业务操作
 * LogUtils.logBusiness("CULTIVATION", "玩家开始修炼", "playerId", 123);
 * 
 * // 结束链路追踪
 * LogUtils.endTrace();
 * }</pre>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */
@Slf4j
public class LogUtils {
    
    /**
     * 链路追踪ID的MDC键名
     */
    private static final String TRACE_ID_KEY = "traceId";
    
    /**
     * 用户ID的MDC键名
     */
    private static final String USER_ID_KEY = "userId";
    
    /**
     * 玩家ID的MDC键名
     */
    private static final String PLAYER_ID_KEY = "playerId";
    
    /**
     * 操作类型的MDC键名
     */
    private static final String OPERATION_KEY = "operation";
    
    /**
     * 开始链路追踪
     * 生成唯一的追踪ID并放入MDC中
     * 
     * @return 生成的追踪ID
     */
    public static String startTrace() {
        String traceId = generateTraceId();
        MDC.put(TRACE_ID_KEY, traceId);
        log.debug("开始链路追踪: {}", traceId);
        return traceId;
    }
    
    /**
     * 开始链路追踪（使用指定的追踪ID）
     * 
     * @param traceId 指定的追踪ID
     */
    public static void startTrace(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
        log.debug("开始链路追踪: {}", traceId);
    }
    
    /**
     * 结束链路追踪
     * 清除MDC中的追踪信息
     */
    public static void endTrace() {
        String traceId = MDC.get(TRACE_ID_KEY);
        log.debug("结束链路追踪: {}", traceId);
        MDC.clear();
    }
    
    /**
     * 设置用户上下文
     * 
     * @param userId 用户ID
     * @param playerId 玩家ID（可选）
     */
    public static void setUserContext(String userId, Integer playerId) {
        MDC.put(USER_ID_KEY, userId);
        if (playerId != null) {
            MDC.put(PLAYER_ID_KEY, playerId.toString());
        }
        log.debug("设置用户上下文: userId={}, playerId={}", userId, playerId);
    }
    
    /**
     * 设置操作上下文
     * 
     * @param operation 操作类型
     */
    public static void setOperationContext(String operation) {
        MDC.put(OPERATION_KEY, operation);
        log.debug("设置操作上下文: {}", operation);
    }
    
    /**
     * 记录用户操作日志
     * 
     * @param userId 用户ID
     * @param action 操作动作
     * @param description 操作描述
     */
    public static void logUserAction(String userId, String action, String description) {
        setUserContext(userId, null);
        setOperationContext(action);
        log.info("用户操作 - 用户: {}, 动作: {}, 描述: {}", userId, action, description);
    }
    
    /**
     * 记录用户操作日志（包含玩家ID）
     * 
     * @param userId 用户ID
     * @param playerId 玩家ID
     * @param action 操作动作
     * @param description 操作描述
     */
    public static void logUserAction(String userId, Integer playerId, String action, String description) {
        setUserContext(userId, playerId);
        setOperationContext(action);
        log.info("用户操作 - 用户: {}, 玩家: {}, 动作: {}, 描述: {}", userId, playerId, action, description);
    }
    
    /**
     * 记录业务操作日志
     * 
     * @param businessType 业务类型
     * @param description 操作描述
     * @param params 附加参数
     */
    public static void logBusiness(String businessType, String description, Object... params) {
        setOperationContext(businessType);
        
        StringBuilder sb = new StringBuilder();
        sb.append("业务操作 - 类型: ").append(businessType)
          .append(", 描述: ").append(description);
        
        if (params != null && params.length > 0) {
            sb.append(", 参数: ");
            for (int i = 0; i < params.length; i += 2) {
                if (i + 1 < params.length) {
                    sb.append(params[i]).append("=").append(params[i + 1]);
                    if (i + 2 < params.length) {
                        sb.append(", ");
                    }
                }
            }
        }
        
        log.info(sb.toString());
    }
    
    /**
     * 记录安全相关日志
     * 
     * @param securityEvent 安全事件类型
     * @param userId 用户ID
     * @param description 事件描述
     * @param ipAddress IP地址
     */
    public static void logSecurity(String securityEvent, String userId, String description, String ipAddress) {
        setUserContext(userId, null);
        setOperationContext(securityEvent);
        log.warn("安全事件 - 事件: {}, 用户: {}, IP: {}, 描述: {}", securityEvent, userId, ipAddress, description);
    }
    
    /**
     * 记录错误日志
     * 
     * @param operation 操作名称
     * @param errorMessage 错误信息
     * @param exception 异常对象
     */
    public static void logError(String operation, String errorMessage, Throwable exception) {
        setOperationContext(operation);
        log.error("操作失败 - 操作: {}, 错误: {}", operation, errorMessage, exception);
    }
    
    /**
     * 记录性能日志
     * 
     * @param operation 操作名称
     * @param duration 执行时间（毫秒）
     * @param params 附加参数
     */
    public static void logPerformance(String operation, long duration, Object... params) {
        setOperationContext(operation);
        
        StringBuilder sb = new StringBuilder();
        sb.append("性能监控 - 操作: ").append(operation)
          .append(", 耗时: ").append(duration).append("ms");
        
        if (params != null && params.length > 0) {
            sb.append(", 参数: ");
            for (int i = 0; i < params.length; i += 2) {
                if (i + 1 < params.length) {
                    sb.append(params[i]).append("=").append(params[i + 1]);
                    if (i + 2 < params.length) {
                        sb.append(", ");
                    }
                }
            }
        }
        
        if (duration > 1000) {
            log.warn(sb.toString() + " [慢操作]");
        } else {
            log.info(sb.toString());
        }
    }
    
    /**
     * 记录数据库操作日志
     * 
     * @param operation 操作类型（SELECT/INSERT/UPDATE/DELETE）
     * @param table 表名
     * @param duration 执行时间（毫秒）
     * @param rowCount 影响行数
     */
    public static void logDatabase(String operation, String table, long duration, int rowCount) {
        setOperationContext("DATABASE_" + operation);
        
        String message = String.format("数据库操作 - 操作: %s, 表: %s, 耗时: %dms, 影响行数: %d", 
                operation, table, duration, rowCount);
        
        if (duration > 1000) {
            log.warn(message + " [慢查询]");
        } else {
            log.debug(message);
        }
    }
    
    /**
     * 记录API调用日志
     * 
     * @param method HTTP方法
     * @param uri 请求URI
     * @param statusCode 响应状态码
     * @param duration 执行时间（毫秒）
     * @param userId 用户ID（可选）
     */
    public static void logApiCall(String method, String uri, int statusCode, long duration, String userId) {
        if (userId != null) {
            setUserContext(userId, null);
        }
        setOperationContext("API_CALL");
        
        String message = String.format("API调用 - %s %s, 状态码: %d, 耗时: %dms", 
                method, uri, statusCode, duration);
        
        if (statusCode >= 400) {
            log.warn(message + " [错误响应]");
        } else if (duration > 2000) {
            log.warn(message + " [慢接口]");
        } else {
            log.info(message);
        }
    }
    
    /**
     * 生成链路追踪ID
     * 
     * @return 唯一的追踪ID
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 获取当前链路追踪ID
     * 
     * @return 当前的追踪ID，如果不存在则返回null
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 当前的用户ID，如果不存在则返回null
     */
    public static String getCurrentUserId() {
        return MDC.get(USER_ID_KEY);
    }
    
    /**
     * 获取当前玩家ID
     * 
     * @return 当前的玩家ID，如果不存在则返回null
     */
    public static String getCurrentPlayerId() {
        return MDC.get(PLAYER_ID_KEY);
    }
}
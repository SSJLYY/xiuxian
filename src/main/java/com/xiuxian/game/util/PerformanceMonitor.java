package com.xiuxian.game.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控工具类
 * 
 * <p>用于监控方法执行时间、统计调用次数、记录性能指标等。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>方法执行时间监控</li>
 *   <li>API调用次数统计</li>
 *   <li>性能指标记录</li>
 *   <li>慢查询检测</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 开始监控
 * long startTime = PerformanceMonitor.startTiming("userLogin");
 * 
 * // 执行业务逻辑
 * // ...
 * 
 * // 结束监控
 * PerformanceMonitor.endTiming("userLogin", startTime);
 * }</pre>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */
@Slf4j
@Component
public class PerformanceMonitor {
    
    /**
     * 性能日志记录器
     */
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("performance");
    
    /**
     * API调用次数统计
     */
    private static final ConcurrentHashMap<String, AtomicLong> API_CALL_COUNT = new ConcurrentHashMap<>();
    
    /**
     * API总执行时间统计
     */
    private static final ConcurrentHashMap<String, AtomicLong> API_TOTAL_TIME = new ConcurrentHashMap<>();
    
    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000L;
    
    /**
     * 开始计时
     * 
     * @param operationName 操作名称
     * @return 开始时间戳（纳秒）
     */
    public static long startTiming(String operationName) {
        long startTime = System.nanoTime();
        log.debug("开始执行操作: {}", operationName);
        return startTime;
    }
    
    /**
     * 结束计时并记录性能指标
     * 
     * @param operationName 操作名称
     * @param startTime 开始时间戳（纳秒）
     */
    public static void endTiming(String operationName, long startTime) {
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        long durationMs = duration / 1_000_000; // 转换为毫秒
        
        // 更新统计信息
        API_CALL_COUNT.computeIfAbsent(operationName, k -> new AtomicLong(0)).incrementAndGet();
        API_TOTAL_TIME.computeIfAbsent(operationName, k -> new AtomicLong(0)).addAndGet(durationMs);
        
        // 记录性能日志
        if (durationMs > SLOW_QUERY_THRESHOLD) {
            PERFORMANCE_LOGGER.warn("慢操作检测 - 操作: {}, 耗时: {}ms", operationName, durationMs);
        } else {
            PERFORMANCE_LOGGER.info("操作完成 - 操作: {}, 耗时: {}ms", operationName, durationMs);
        }
        
        log.debug("操作执行完成: {}, 耗时: {}ms", operationName, durationMs);
    }
    
    /**
     * 记录API调用
     * 
     * @param apiName API名称
     * @param method HTTP方法
     * @param duration 执行时间（毫秒）
     */
    public static void recordApiCall(String apiName, String method, long duration) {
        String key = method + " " + apiName;
        
        // 更新统计信息
        API_CALL_COUNT.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        API_TOTAL_TIME.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(duration);
        
        // 记录API调用日志
        if (duration > SLOW_QUERY_THRESHOLD) {
            PERFORMANCE_LOGGER.warn("慢API检测 - API: {}, 耗时: {}ms", key, duration);
        } else {
            PERFORMANCE_LOGGER.debug("API调用 - API: {}, 耗时: {}ms", key, duration);
        }
    }
    
    /**
     * 获取API调用统计信息
     * 
     * @param apiName API名称
     * @return 统计信息字符串
     */
    public static String getApiStats(String apiName) {
        AtomicLong callCount = API_CALL_COUNT.get(apiName);
        AtomicLong totalTime = API_TOTAL_TIME.get(apiName);
        
        if (callCount == null || totalTime == null) {
            return String.format("API: %s - 无调用记录", apiName);
        }
        
        long count = callCount.get();
        long total = totalTime.get();
        double avgTime = count > 0 ? (double) total / count : 0;
        
        return String.format("API: %s - 调用次数: %d, 总耗时: %dms, 平均耗时: %.2fms", 
                apiName, count, total, avgTime);
    }
    
    /**
     * 打印所有API统计信息
     */
    public static void printAllStats() {
        PERFORMANCE_LOGGER.info("========== API性能统计 ==========");
        
        API_CALL_COUNT.forEach((apiName, callCount) -> {
            AtomicLong totalTime = API_TOTAL_TIME.get(apiName);
            if (totalTime != null) {
                long count = callCount.get();
                long total = totalTime.get();
                double avgTime = count > 0 ? (double) total / count : 0;
                
                PERFORMANCE_LOGGER.info("API: {} - 调用次数: {}, 总耗时: {}ms, 平均耗时: {:.2f}ms", 
                        apiName, count, total, avgTime);
            }
        });
        
        PERFORMANCE_LOGGER.info("================================");
    }
    
    /**
     * 清空统计信息
     */
    public static void clearStats() {
        API_CALL_COUNT.clear();
        API_TOTAL_TIME.clear();
        PERFORMANCE_LOGGER.info("性能统计信息已清空");
    }
    
    /**
     * 记录内存使用情况
     */
    public static void recordMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double usedPercent = (double) usedMemory / maxMemory * 100;
        
        PERFORMANCE_LOGGER.info("内存使用情况 - 已使用: {}MB, 总内存: {}MB, 最大内存: {}MB, 使用率: {:.2f}%",
                usedMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                usedPercent);
        
        // 内存使用率超过80%时发出警告
        if (usedPercent > 80) {
            PERFORMANCE_LOGGER.warn("内存使用率过高: {:.2f}%，建议检查内存泄漏", usedPercent);
        }
    }
    
    /**
     * 记录数据库连接池状态
     * 
     * @param activeConnections 活跃连接数
     * @param idleConnections 空闲连接数
     * @param totalConnections 总连接数
     * @param maxConnections 最大连接数
     */
    public static void recordConnectionPoolStatus(int activeConnections, int idleConnections, 
                                                int totalConnections, int maxConnections) {
        double usagePercent = (double) totalConnections / maxConnections * 100;
        
        PERFORMANCE_LOGGER.info("数据库连接池状态 - 活跃: {}, 空闲: {}, 总计: {}, 最大: {}, 使用率: {:.2f}%",
                activeConnections, idleConnections, totalConnections, maxConnections, usagePercent);
        
        // 连接池使用率超过90%时发出警告
        if (usagePercent > 90) {
            PERFORMANCE_LOGGER.warn("数据库连接池使用率过高: {:.2f}%，可能存在连接泄漏", usagePercent);
        }
    }
}
package com.xiuxian.game.common.config;

import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.common.util.PerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 日志记录切面
 * 
 * <p>使用AOP技术自动记录方法执行日志，包括�?/p>
 * <ul>
 *   <li>Controller方法的请求和响应日志</li>
 *   <li>Service方法的执行时间监�?/li>
 *   <li>异常日志记录</li>
 *   <li>性能监控</li>
 * </ul>
 * 
 * <p>切面会自动拦截以下包下的方法�?/p>
 * <ul>
 *   <li>com.xiuxian.game.controller.*</li>
 *   <li>com.xiuxian.game.service.*</li>
 * </ul>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {
    
    /**
     * Controller层切�?
     */
    @Pointcut("execution(* com.xiuxian.game.controller..*(..))")
    public void controllerPointcut() {
        // 切点定义
    }
    
    /**
     * Service层切�?
     */
    @Pointcut("execution(* com.xiuxian.game.service..*(..))")
    public void servicePointcut() {
        // 切点定义
    }
    
    /**
     * Controller方法执行环绕通知
     * 记录HTTP请求的详细信息和执行时间
     * 
     * @param joinPoint 连接�?
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        // 开始链路追�?
        String traceId = LogUtils.startTrace();
        
        // 获取HTTP请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        // 记录请求开�?
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录请求信息
            if (request != null) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String queryString = request.getQueryString();
                String clientIp = getClientIpAddress(request);
                
                log.info("========== HTTP请求开�?==========");
                log.info("追踪ID: {}", traceId);
                log.info("请求方法: {} {}", method, uri);
                if (queryString != null) {
                    log.info("查询参数: {}", queryString);
                }
                log.info("客户端IP: {}", clientIp);
                log.info("Controller: {}", fullMethodName);
                
                // 记录请求参数（排除敏感信息）
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    log.info("请求参数: {}", sanitizeArgs(args));
                }
                
                LogUtils.setOperationContext("HTTP_" + method);
            }
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录响应信息
            log.info("请求处理成功，耗时: {}ms", duration);
            log.info("========== HTTP请求结束 ==========");
            
            // 记录性能监控
            if (request != null) {
                String apiName = request.getMethod() + " " + request.getRequestURI();
                PerformanceMonitor.recordApiCall(apiName, request.getMethod(), duration);
                LogUtils.logApiCall(request.getMethod(), request.getRequestURI(), 200, duration, null);
            }
            
            return result;
            
        } catch (Exception e) {
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录异常信息
            log.error("请求处理失败，耗时: {}ms", duration);
            log.error("异常信息: {}", e.getMessage(), e);
            log.error("========== HTTP请求异常结束 ==========");
            
            // 记录错误日志
            LogUtils.logError(fullMethodName, e.getMessage(), e);
            
            // 记录API调用（错误状态）
            if (request != null) {
                LogUtils.logApiCall(request.getMethod(), request.getRequestURI(), 500, duration, null);
            }
            
            throw e;
            
        } finally {
            // 结束链路追踪
            LogUtils.endTrace();
        }
    }
    
    /**
     * Service方法执行环绕通知
     * 记录业务方法的执行时间和异常信息
     * 
     * @param joinPoint 连接�?
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        // 开始性能监控
        long startTime = PerformanceMonitor.startTiming(fullMethodName);
        
        try {
            log.debug("开始执行业务方�? {}", fullMethodName);
            
            // 记录方法参数（调试级别）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                log.debug("方法参数: {}", sanitizeArgs(args));
            }
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            log.debug("业务方法执行成功: {}", fullMethodName);
            
            return result;
            
        } catch (Exception e) {
            log.error("业务方法执行失败: {}", fullMethodName);
            log.error("异常信息: {}", e.getMessage(), e);
            
            // 记录错误日志
            LogUtils.logError(fullMethodName, e.getMessage(), e);
            
            throw e;
            
        } finally {
            // 结束性能监控
            PerformanceMonitor.endTiming(fullMethodName, startTime);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 多级代理的情况，取第一个IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 清理敏感参数信息
     * 将密码等敏感信息替换为星�?
     * 
     * @param args 方法参数数组
     * @return 清理后的参数字符�?
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                String argStr = arg.toString();
                
                // 检查是否包含敏感信�?
                if (containsSensitiveInfo(argStr)) {
                    sb.append("[SENSITIVE_DATA_HIDDEN]");
                } else {
                    // 限制参数长度，避免日志过�?
                    if (argStr.length() > 200) {
                        sb.append(argStr.substring(0, 200)).append("...");
                    } else {
                        sb.append(argStr);
                    }
                }
            }
        }
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * 检查字符串是否包含敏感信息
     * 
     * @param str 待检查的字符�?
     * @return 如果包含敏感信息返回true，否则返回false
     */
    private boolean containsSensitiveInfo(String str) {
        if (str == null) {
            return false;
        }
        
        String lowerStr = str.toLowerCase();
        String[] sensitiveKeywords = {
            "password", "pwd", "token", "secret", "key", "auth",
            "密码", "令牌", "密钥", "认证"
        };
        
        for (String keyword : sensitiveKeywords) {
            if (lowerStr.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
}

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
 * 日志切面配置类
 *
 * <p>通过AOP记录系统各层方法调用，包括：</p>
 * <ul>
 *   <li>Controller层请求日志</li>
 *   <li>Service层方法调用日志</li>
 *   <li>Mapper层SQL日志</li>
 *   <li>统一异常日志</li>
 * </ul>
 *
 * <p>拦截以下包路径下的方法调用：</p>
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
     * Controller切入点
     */
    @Pointcut("execution(* com.xiuxian.game.controller..*(..))")
    public void controllerPointcut() {
        // 空方法，仅作为切入点标记
    }

    /**
     * Service切入点
     */
    @Pointcut("execution(* com.xiuxian.game.service..*(..))")
    public void servicePointcut() {
        // 空方法，仅作为切入点标记
    }

    /**
     * Controller请求日志记录
     * 记录HTTP请求的详细信息，包括请求参数、响应状态和执行时间
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        // 生成请求追踪ID
        String traceId = LogUtils.startTrace();

        // 获取HTTP请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        try {
            // 记录HTTP请求信息
            if (request != null) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String queryString = request.getQueryString();
                String clientIp = getClientIpAddress(request);

                log.info("========== HTTP请求开始==========");
                log.info("追踪ID: {}", traceId);
                log.info("请求路径: {} {}", method, uri);
                if (queryString != null) {
                    log.info("查询参数: {}", queryString);
                }
                log.info("客户端IP: {}", clientIp);
                log.info("Controller: {}", fullMethodName);

                // 记录请求参数（过滤敏感信息）
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    log.info("请求参数: {}", sanitizeArgs(args));
                }

                LogUtils.setOperationContext("HTTP_" + method);
            }

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 计算执行时长
            long duration = System.currentTimeMillis() - startTime;

            // 记录成功日志
            log.info("HTTP请求完成，执行时间: {}ms", duration);
            log.info("========== HTTP请求结束 ==========");

            // 记录性能指标
            if (request != null) {
                String apiName = request.getMethod() + " " + request.getRequestURI();
                PerformanceMonitor.recordApiCall(apiName, request.getMethod(), duration);
                LogUtils.logApiCall(request.getMethod(), request.getRequestURI(), 200, duration, null);
            }

            return result;

        } catch (Exception e) {
            // 计算执行时长
            long duration = System.currentTimeMillis() - startTime;

            // 记录异常日志
            log.error("HTTP请求异常，执行时间: {}ms", duration);
            log.error("目标方法: {}", e.getMessage(), e);
            log.error("========== HTTP请求异常结束 ==========");

            // 记录错误日志
            LogUtils.logError(fullMethodName, e.getMessage(), e);

            // 记录API调用日志
            if (request != null) {
                LogUtils.logApiCall(request.getMethod(), request.getRequestURI(), 500, duration, null);
            }

            throw e;

        } finally {
            // 结束追踪
            LogUtils.endTrace();
        }
    }

    /**
     * Service方法日志记录
     * 记录Service层方法的调用情况，包括执行时间和异常信息
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        // 记录开始时间
        long startTime = PerformanceMonitor.startTiming(fullMethodName);

        try {
            log.debug("Service方法开始执行: {}", fullMethodName);

            // 记录方法参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                log.debug("方法参数: {}", sanitizeArgs(args));
            }

            // 执行目标方法
            Object result = joinPoint.proceed();

            log.debug("Service方法执行完成: {}", fullMethodName);

            return result;

        } catch (Exception e) {
            log.error("Service方法执行异常: {}", fullMethodName);
            log.error("异常信息: {}", e.getMessage(), e);

            // 记录错误日志
            LogUtils.logError(fullMethodName, e.getMessage(), e);

            throw e;

        } finally {
            // 记录性能指标
            PerformanceMonitor.endTiming(fullMethodName, startTime);
        }
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端真实IP地址
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
                // 多个IP时取第一个（经过代理的情况）
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 过滤方法参数中的敏感信息
     * 密码、Token等敏感信息会被替换为占位符，避免日志泄露
     *
     * @param args 方法参数数组
     * @return 过滤后的参数字符串
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

                // 过滤敏感信息
                if (containsSensitiveInfo(argStr)) {
                    sb.append("[SENSITIVE_DATA_HIDDEN]");
                } else {
                    // 过长参数截断处理
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
     * 判断字符串是否包含敏感关键词
     *
     * @param str 待检测的字符串
     * @return 是否包含敏感关键词，true表示包含，false表示安全
     */
    private boolean containsSensitiveInfo(String str) {
        if (str == null) {
            return false;
        }

        String lowerStr = str.toLowerCase();
        String[] sensitiveKeywords = {
            "password", "pwd", "token", "secret", "key", "auth",
            "密码", "凭证", "密钥", "认证"
        };

        for (String keyword : sensitiveKeywords) {
            if (lowerStr.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}

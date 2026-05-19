package com.xiuxian.game.common.config;

import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.common.util.PerformanceMonitor;
import com.xiuxian.game.common.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.xiuxian.game.modules.*.controller..*(..))")
    public void controllerPointcut() {
    }

    @Pointcut("execution(* com.xiuxian.game.modules.*.service..*(..))")
    public void servicePointcut() {
    }

    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = LogUtils.startTrace();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        long startTime = System.currentTimeMillis();

        try {
            if (request != null) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String queryString = request.getQueryString();
                String clientIp = RequestUtils.getClientIp(request);

                log.info("========== HTTP请求开始 ==========");
                log.info("追踪ID: {}", traceId);
                log.info("请求路径: {} {}", method, uri);
                if (queryString != null) {
                    log.info("查询参数: {}", queryString);
                }
                log.info("客户端IP: {}", clientIp);
                log.info("Controller: {}", fullMethodName);

                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    log.info("请求参数: {}", sanitizeArgs(args));
                }
                LogUtils.setOperationContext("HTTP_" + method);
            }

            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("HTTP请求完成，执行时间: {}ms", duration);
            log.info("========== HTTP请求结束 ==========");

            if (request != null) {
                String apiName = request.getMethod() + " " + request.getRequestURI();
                PerformanceMonitor.recordApiCall(apiName, request.getMethod(), duration);
                LogUtils.logApiCall(request.getMethod(), request.getRequestURI(), 200, duration, null);
            }
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LogUtils.logError(fullMethodName, e.getMessage(), e);
            if (request != null) {
                LogUtils.logApiCall(request.getMethod(), request.getRequestURI(), 500, duration, null);
            }
            throw e;
        } finally {
            LogUtils.endTrace();
        }
    }

    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        long startTime = PerformanceMonitor.startTiming(fullMethodName);

        try {
            log.debug("Service方法开始执行: {}", fullMethodName);
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                log.debug("方法参数: {}", sanitizeArgs(args));
            }
            Object result = joinPoint.proceed();
            log.debug("Service方法执行完成: {}", fullMethodName);
            return result;
        } catch (Exception e) {
            LogUtils.logError(fullMethodName, e.getMessage(), e);
            throw e;
        } finally {
            PerformanceMonitor.endTiming(fullMethodName, startTime);
        }
    }

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
                continue;
            }

            String argStr = arg.toString();
            if (containsSensitiveInfo(argStr)) {
                sb.append("[SENSITIVE_DATA_HIDDEN]");
            } else if (argStr.length() > 200) {
                sb.append(argStr, 0, 200).append("...");
            } else {
                sb.append(argStr);
            }
        }
        sb.append("]");
        return sb.toString();
    }

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

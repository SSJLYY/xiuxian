package com.xiuxian.game.aspect;

import com.xiuxian.game.annotation.RateLimit;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.security.JwtTokenProvider;
import com.xiuxian.game.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 限流切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final RateLimiter rateLimiter;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(rateLimit);
        
        if (!rateLimiter.isAllowed(key, rateLimit.maxRequests(), rateLimit.windowSeconds())) {
            log.warn("请求被限流: key={}, maxRequests={}, windowSeconds={}", 
                    key, rateLimit.maxRequests(), rateLimit.windowSeconds());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), rateLimit.message());
        }
        
        return joinPoint.proceed();
    }
    
    private String generateKey(RateLimit rateLimit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        switch (rateLimit.keyType()) {
            case USER_ID:
                return getUserIdKey(request);
            case IP:
                return getIpKey(request);
            case CUSTOM:
                return rateLimit.customKey();
            default:
                return "default";
        }
    }
    
    private String getUserIdKey(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                return "user:" + username;
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败，使用IP作为限流键", e);
        }
        return getIpKey(request);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private String getIpKey(HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        return "ip:" + ip;
    }
    
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
}
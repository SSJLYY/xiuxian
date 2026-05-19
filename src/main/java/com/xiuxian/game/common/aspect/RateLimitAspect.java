package com.xiuxian.game.common.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xiuxian.game.common.annotation.RateLimit;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.RateLimiter;
import com.xiuxian.game.common.util.RequestUtils;
import com.xiuxian.game.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiter rateLimiter;
    private final PlayerService playerService;

    @Around("@annotation(com.xiuxian.game.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return point.proceed();
        }

        String resourceName = rateLimit.value();
        if (resourceName.isEmpty()) {
            resourceName = method.getDeclaringClass().getName() + ":" + method.getName();
        }

        String rateLimitKey = buildRateLimitKey(resourceName, rateLimit.keyType());
        if (!rateLimiter.isAllowed(rateLimitKey, rateLimit.maxRequests(), rateLimit.windowSeconds())) {
            log.warn("Request rate limited: key={}, method={}.{}",
                    rateLimitKey,
                    method.getDeclaringClass().getSimpleName(),
                    method.getName());
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED, rateLimit.message());
        }

        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.IN);
            return point.proceed();
        } catch (BlockException e) {
            log.warn("Sentinel blocked request: resource={}, method={}.{}",
                    resourceName,
                    method.getDeclaringClass().getSimpleName(),
                    method.getName());
            throw new BusinessException(ErrorCode.SERVER_BUSY, rateLimit.message());
        } catch (Throwable e) {
            Tracer.trace(e);
            log.error("Sentinel resource failed: resource={}", resourceName, e);
            throw e;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private String buildRateLimitKey(String resourceName, RateLimit.KeyType keyType) {
        switch (keyType) {
            case USER:
                Integer playerId = resolveCurrentPlayerId();
                if (playerId != null) {
                    return resourceName + ":USER:" + playerId;
                }
                return resourceName + ":USER:anonymous";
            case IP:
            default:
                return resourceName + ":IP:" + resolveClientIp();
        }
    }

    private Integer resolveCurrentPlayerId() {
        try {
            return playerService.getCurrentPlayerId();
        } catch (Exception e) {
            log.debug("Unable to resolve current player for rate limit key: {}", e.getMessage());
            return null;
        }
    }

    private String resolveClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            return RequestUtils.getClientIp(request);
        }
        return "unknown";
    }
}

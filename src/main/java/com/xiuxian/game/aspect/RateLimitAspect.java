package com.xiuxian.game.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xiuxian.game.annotation.RateLimit;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限流切面
 * 使用 Sentinel 实现方法级限流
 *
 * @author shaun.sheng
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 环绕通知：限流处理
     */
    @Around("@annotation(com.xiuxian.game.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return point.proceed();
        }

        // 资源名称
        String resourceName = rateLimit.value();
        if (resourceName.isEmpty()) {
            resourceName = method.getDeclaringClass().getName() + ":" + method.getName();
        }

        Entry entry = null;
        try {
            // 进入资源
            entry = SphU.entry(resourceName, EntryType.IN);

            // 执行方法
            return point.proceed();

        } catch (BlockException e) {
            // 被限流
            log.warn("请求被限流: resource={}, method={}.{}",
                    resourceName,
                    method.getDeclaringClass().getSimpleName(),
                    method.getName());

            throw new BusinessException(ErrorCode.SERVER_BUSY, "系统繁忙，请稍后重试");

        } catch (Throwable e) {
            // 记录异常
            Tracer.trace(e);
            log.error("Sentinel 资源执行异常: resource={}", resourceName, e);
            throw e;

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}

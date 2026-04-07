package com.xiuxian.game.common.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xiuxian.game.common.annotation.RateLimit;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限流拦截器
 * 基于 Sentinel 的流量控制切面增强逻辑
 *
 * @author shaun.sheng
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 拦截@RateLimit注解的方法进行限流保护
     */
    @Around("@annotation(com.xiuxian.game.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        // 切点保证注解存在；防御性兜底：无注解时直接放行
        if (rateLimit == null) {
            return point.proceed();
        }

        // 获取限流资源名
        String resourceName = rateLimit.value();
        if (resourceName.isEmpty()) {
            resourceName = method.getDeclaringClass().getName() + ":" + method.getName();
        }

        Entry entry = null;
        try {
            // 创建Sentinel资源入口
            entry = SphU.entry(resourceName, EntryType.IN);

            // 执行业务方法
            return point.proceed();

        } catch (BlockException e) {
            // 触发限流，返回友好提示
            log.warn("请求被限流: resource={}, method={}.{}",
                    resourceName,
                    method.getDeclaringClass().getSimpleName(),
                    method.getName());

            throw new BusinessException(ErrorCode.SERVER_BUSY, "请求过于频繁，请稍后再试");

        } catch (Throwable e) {
            // 记录异常并重新抛出
            Tracer.trace(e);
            log.error("Sentinel资源记录异常: resource={}", resourceName, e);
            throw e;

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}

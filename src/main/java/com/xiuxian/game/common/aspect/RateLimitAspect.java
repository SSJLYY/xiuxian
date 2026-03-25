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
 * 闄愭祦鍒囬潰
 * 浣跨敤 Sentinel 瀹炵幇鏂规硶绾ч檺娴?
 *
 * @author shaun.sheng
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 鐜粫閫氱煡锛氶檺娴佸鐞?
     */
    @Around("@annotation(com.xiuxian.game.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return point.proceed();
        }

        // 璧勬簮鍚嶇О
        String resourceName = rateLimit.value();
        if (resourceName.isEmpty()) {
            resourceName = method.getDeclaringClass().getName() + ":" + method.getName();
        }

        Entry entry = null;
        try {
            // 杩涘叆璧勬簮
            entry = SphU.entry(resourceName, EntryType.IN);

            // 鎵ц鏂规硶
            return point.proceed();

        } catch (BlockException e) {
            // 琚檺娴?
            log.warn("璇锋眰琚檺娴? resource={}, method={}.{}",
                    resourceName,
                    method.getDeclaringClass().getSimpleName(),
                    method.getName());

            throw new BusinessException(ErrorCode.SERVER_BUSY, "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");

        } catch (Throwable e) {
            // 璁板綍寮傚父
            Tracer.trace(e);
            log.error("Sentinel 璧勬簮鎵ц寮傚父: resource={}", resourceName, e);
            throw e;

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}


package com.xiuxian.game.common.aspect;

import com.xiuxian.game.common.annotation.DataSource;
import com.xiuxian.game.common.config.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 鏁版嵁婧愬垏鎹㈠垏闈?
 * 鏍规嵁 @DataSource 娉ㄨВ鑷姩鍒囨崲鏁版嵁婧?
 *
 * @author shaun.sheng
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    /**
     * 鐜粫閫氱煡锛氭牴鎹敞瑙ｅ垏鎹㈡暟鎹簮
     */
    @Around("@annotation(com.xiuxian.game.common.annotation.DataSource) || @within(com.xiuxian.game.common.annotation.DataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 鑾峰彇娉ㄨВ锛堜紭鍏堟柟娉曟敞瑙ｏ紝鍏舵绫绘敞瑙ｏ級
        DataSource annotation = method.getAnnotation(DataSource.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(DataSource.class);
        }

        String originalDataSource = RoutingDataSource.getDataSource();

        try {
            if (annotation != null) {
                switch (annotation.value()) {
                    case MASTER:
                        RoutingDataSource.useMaster();
                        log.debug("鍒囨崲鍒颁富搴? {}.{}",
                                method.getDeclaringClass().getSimpleName(),
                                method.getName());
                        break;
                    case SLAVE:
                        RoutingDataSource.useSlave();
                        log.debug("鍒囨崲鍒颁粠搴? {}.{}",
                                method.getDeclaringClass().getSimpleName(),
                                method.getName());
                        break;
                    case AUTO:
                    default:
                        // 鑷姩鍒ゆ柇锛氬啓鏂规硶鐢ㄤ富搴擄紝璇绘柟娉曠敤浠庡簱
                        String methodName = method.getName();
                        if (isWriteMethod(methodName)) {
                            RoutingDataSource.useMaster();
                        } else {
                            RoutingDataSource.useSlave();
                        }
                        break;
                }
            } else {
                // 娌℃湁娉ㄨВ锛岄粯璁や娇鐢ㄤ粠搴擄紙璇诲鍐欏皯锛?
                RoutingDataSource.useSlave();
            }

            return point.proceed();

        } finally {
            // 鎭㈠鍘熸潵鐨勬暟鎹簮
            if (originalDataSource != null) {
                RoutingDataSource.setDataSource(originalDataSource);
            } else {
                RoutingDataSource.clearDataSource();
            }
        }
    }

    /**
     * 鍒ゆ柇鏄惁涓哄啓鏂规硶
     * 閫氳繃鏂规硶鍚嶅墠缂€鍒ゆ柇
     */
    private boolean isWriteMethod(String methodName) {
        String[] writePrefixes = {"insert", "update", "delete", "save", "add", "create", "remove", "delete"};
        String lowerName = methodName.toLowerCase();
        for (String prefix : writePrefixes) {
            if (lowerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}



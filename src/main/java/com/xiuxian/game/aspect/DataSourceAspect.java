package com.xiuxian.game.aspect;

import com.xiuxian.game.annotation.DataSource;
import com.xiuxian.game.config.RoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切换切面
 * 根据 @DataSource 注解自动切换数据源
 *
 * @author shaun.sheng
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    /**
     * 环绕通知：根据注解切换数据源
     */
    @Around("@annotation(com.xiuxian.game.annotation.DataSource) || @within(com.xiuxian.game.annotation.DataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取注解（优先方法注解，其次类注解）
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
                        log.debug("切换到主库: {}.{}",
                                method.getDeclaringClass().getSimpleName(),
                                method.getName());
                        break;
                    case SLAVE:
                        RoutingDataSource.useSlave();
                        log.debug("切换到从库: {}.{}",
                                method.getDeclaringClass().getSimpleName(),
                                method.getName());
                        break;
                    case AUTO:
                    default:
                        // 自动判断：写方法用主库，读方法用从库
                        String methodName = method.getName();
                        if (isWriteMethod(methodName)) {
                            RoutingDataSource.useMaster();
                        } else {
                            RoutingDataSource.useSlave();
                        }
                        break;
                }
            } else {
                // 没有注解，默认使用从库（读多写少）
                RoutingDataSource.useSlave();
            }

            return point.proceed();

        } finally {
            // 恢复原来的数据源
            if (originalDataSource != null) {
                RoutingDataSource.setDataSource(originalDataSource);
            } else {
                RoutingDataSource.clearDataSource();
            }
        }
    }

    /**
     * 判断是否为写方法
     * 通过方法名前缀判断
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

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
 * 数据源动态切换切面
 * 拦截标注了@DataSource 注解的方法，自动切换数据源
 *
 * @author shaun.sheng
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    /**
     * 拦截@DataSource注解的方法，动态切换数据源
     */
    @Around("@annotation(com.xiuxian.game.common.annotation.DataSource) || @within(com.xiuxian.game.common.annotation.DataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 优先取方法级注解，其次取类级注解
        DataSource annotation = method.getAnnotation(DataSource.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(DataSource.class);
        }

        // 切点保证至少有一处注解存在；防御性兜底：无注解时切换到从库
        if (annotation == null) {
            RoutingDataSource.useSlave();
            return point.proceed();
        }

        String originalDataSource = RoutingDataSource.getDataSource();

        try {
            switch (annotation.value()) {
                case MASTER:
                    RoutingDataSource.useMaster();
                    log.debug("切换到主库 {}.{}",
                            method.getDeclaringClass().getSimpleName(),
                            method.getName());
                    break;
                case SLAVE:
                    RoutingDataSource.useSlave();
                    log.debug("切换到从库 {}.{}",
                            method.getDeclaringClass().getSimpleName(),
                            method.getName());
                    break;
                case AUTO:
                default:
                    // 自动判断：写方法切主库，读方法切从库
                    if (isWriteMethod(method.getName())) {
                        RoutingDataSource.useMaster();
                    } else {
                        RoutingDataSource.useSlave();
                    }
                    break;
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
     * 写方法前缀：insert、update、delete、save、add、create、remove
     */
    private boolean isWriteMethod(String methodName) {
        String[] writePrefixes = {"insert", "update", "delete", "save", "add", "create", "remove"};
        String lowerName = methodName.toLowerCase();
        for (String prefix : writePrefixes) {
            if (lowerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

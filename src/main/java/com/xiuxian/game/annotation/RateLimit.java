package com.xiuxian.game.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 使用 Sentinel 实现
 *
 * @author shaun.sheng
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 资源名称
     * 默认为方法全限定名
     */
    String value() default "";

    /**
     * 每秒允许的请求数
     */
    double count() default 100;

    /**
     * 限流效果
     * - THROW: 直接抛出异常
     * - WAIT: 排队等待
     */
    String controlBehavior() default "THROW";

    /**
     * 最大排队等待时间（毫秒）
     */
    int maxQueueingTimeMs() default 500;

    /**
     * 描述
     */
    String desc() default "";
}

package com.xiuxian.game.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 支持基于 IP 或用户的限流，使用 Redis 或 Sentinel 实现
 *
 * @author shaun.sheng
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 资源名称，默认为方法全限定名
     */
    String value() default "";

    /**
     * 每秒允许的请求数（Sentinel 限流用）
     */
    double count() default 100;

    /**
     * 限流行为
     * - THROW: 直接抛出异常
     * - WAIT: 排队等待
     */
    String controlBehavior() default "THROW";

    /**
     * 最大排队等待时间（毫秒）
     */
    int maxQueueingTimeMs() default 500;

    /**
     * 限流 key 类型
     */
    KeyType keyType() default KeyType.IP;

    /**
     * 时间窗口内最大请求数
     */
    int maxRequests() default 100;

    /**
     * 时间窗口大小（秒）
     */
    int windowSeconds() default 60;

    /**
     * 触发限流时的提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 描述
     */
    String desc() default "";

    /**
     * 限流 key 类型枚举
     */
    enum KeyType {
        /** 基于 IP 限流 */
        IP,
        /** 基于用户限流 */
        USER
    }
}

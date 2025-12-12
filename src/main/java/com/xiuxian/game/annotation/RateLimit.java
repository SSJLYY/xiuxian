package com.xiuxian.game.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 限流键类型
     */
    enum KeyType {
        USER_ID,    // 按用户ID限流
        IP,         // 按IP地址限流
        CUSTOM      // 自定义键
    }
    
    /**
     * 限流键类型，默认按用户ID限流
     */
    KeyType keyType() default KeyType.USER_ID;
    
    /**
     * 自定义键（当keyType为CUSTOM时使用）
     */
    String customKey() default "";
    
    /**
     * 时间窗口内最大请求数
     */
    int maxRequests() default 10;
    
    /**
     * 时间窗口大小（秒）
     */
    int windowSeconds() default 60;
    
    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
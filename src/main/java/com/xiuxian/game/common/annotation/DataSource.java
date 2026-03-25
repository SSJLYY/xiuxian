package com.xiuxian.game.common.annotation;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * 用于标记方法或类需要使用特定的数据源，支持主从数据源切换
 *
 * @author shaun.sheng
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    /**
     * 数据源类型
     */
    enum Type {
        /**
         * 主数据源（写操作）
         */
        MASTER,
        /**
         * 从数据源（读操作）
         */
        SLAVE,
        /**
         * 自动选择，根据操作类型自动判断
         */
        AUTO
    }

    /**
     * 数据源类型，默认自动选择
     */
    Type value() default Type.AUTO;
}

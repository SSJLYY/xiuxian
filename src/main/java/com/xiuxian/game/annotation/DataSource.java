package com.xiuxian.game.annotation;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * 用于方法级别指定使用主库还是从库
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
         * 主库（写）
         */
        MASTER,
        /**
         * 从库（读）
         */
        SLAVE,
        /**
         * 自动（根据方法前缀判断）
         */
        AUTO
    }

    /**
     * 数据源类型，默认自动
     */
    Type value() default Type.AUTO;
}

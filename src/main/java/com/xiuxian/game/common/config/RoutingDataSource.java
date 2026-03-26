package com.xiuxian.game.common.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由配置类
 * 通过 ThreadLocal 记录当前线程使用的数据源，实现主从自动切换
 *
 * @author shaun.sheng
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * ThreadLocal 存储当前数据源标识
     * - null 或 "master": 使用主数据源（写操作）
     * - "slave": 使用从数据源（读操作）
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前使用的数据源
     *
     * @param dataSource 数据源标识 master/slave
     */
    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    /**
     * 获取当前使用的数据源
     *
     * @return 数据源标识
     */
    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前数据源设置
     */
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 强制使用主数据源（写操作）
     */
    public static void useMaster() {
        setDataSource("master");
    }

    /**
     * 强制使用从数据源（读操作）
     */
    public static void useSlave() {
        setDataSource("slave");
    }

    /**
     * 判断是否使用从数据源
     */
    public static boolean isUsingSlave() {
        return "slave".equals(getDataSource());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = CONTEXT_HOLDER.get();
        // 如果未设置，默认为从数据源（读多写少场景优化）
        // 如果设置了，则使用设置的值
        return dataSource != null ? dataSource : "slave";
    }
}

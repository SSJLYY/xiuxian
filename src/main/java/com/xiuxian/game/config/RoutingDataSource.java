package com.xiuxian.game.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由
 * 根据 ThreadLocal 中存储的数据源标识，动态切换数据源
 *
 * @author shaun.sheng
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * ThreadLocal 存储当前数据源标识
     * - null 或 "master": 使用主库（写）
     * - "slave": 使用从库（读）
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前数据源
     *
     * @param dataSource 数据源标识（master/slave）
     */
    public static void setDataSource(String dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    /**
     * 获取当前数据源
     *
     * @return 数据源标识
     */
    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除数据源标识，恢复默认
     */
    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 强制使用主库（写操作）
     */
    public static void useMaster() {
        setDataSource("master");
    }

    /**
     * 强制使用从库（读操作）
     */
    public static void useSlave() {
        setDataSource("slave");
    }

    /**
     * 判断当前是否使用从库
     */
    public static boolean isUsingSlave() {
        return "slave".equals(getDataSource());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = CONTEXT_HOLDER.get();
        // 如果没有设置，默认使用从库（读多写少场景）
        // 如果明确设置了，返回设置的值
        return dataSource != null ? dataSource : "slave";
    }
}

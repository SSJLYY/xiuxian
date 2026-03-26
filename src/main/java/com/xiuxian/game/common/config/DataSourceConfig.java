package com.xiuxian.game.common.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源自动配置类 - 支持读写分离配置
 * 主数据源用于写操作
 * 从数据源用于读操作
 *
 * @author shaun.sheng
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.datasource.readwrite.enabled", havingValue = "true", matchIfMissing = false)
public class DataSourceConfig {

    /**
     * 主数据源自动配置类
     */
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        log.info("主数据源连接池配置初始化完成");
        return dataSource;
    }

    /**
     * 从数据源自动配置类
     */
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        log.info("从数据源连接池配置初始化完成");
        return dataSource;
    }

    /**
     * 动态数据源路由
     * 使用 ThreadLocal 记录当前数据源，实现主从自动切换
     */
    @Bean(name = "routingDataSource")
    @Primary
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", master);
        targetDataSources.put("slave", slave);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(master); // 默认使用主数据源

        log.info("数据源配置：动态数据源路由初始化完成");
        return routingDataSource;
    }
}

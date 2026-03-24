package com.xiuxian.game.config;

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
 * 多数据源配置 - 支持读写分离
 * 主库：负责写入操作
 * 从库：负责读取操作
 *
 * @author shaun.sheng
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.datasource.readwrite.enabled", havingValue = "true", matchIfMissing = false)
public class DataSourceConfig {

    /**
     * 主库（写）数据源配置
     */
    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        log.info("主库（写）数据源初始化完成");
        return dataSource;
    }

    /**
     * 从库（读）数据源配置
     */
    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        log.info("从库（读）数据源初始化完成");
        return dataSource;
    }

    /**
     * 路由数据源
     * 根据 ThreadLocal 中的标记决定使用主库还是从库
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
        routingDataSource.setDefaultTargetDataSource(master); // 默认使用主库

        log.info("读写分离路由数据源初始化完成");
        return routingDataSource;
    }
}

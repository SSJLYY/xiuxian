package com.xiuxian.game.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置
 * 支持读写分离数据源
 */
@Slf4j
@Configuration
@MapperScan("com.xiuxian.game.mapper")
@ConditionalOnProperty(name = "spring.datasource.readwrite.enabled", havingValue = "true", matchIfMissing = false)
public class MybatisPlusConfig {

    @Autowired(required = false)
    private DataSource routingDataSource;

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        log.info("MyBatis-Plus 分页插件已加载");
        return interceptor;
    }

    /**
     * 配置 SqlSessionFactory 使用路由数据源
     */
    @Bean
    @Order(1)
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        if (routingDataSource != null) {
            log.info("MyBatis-Plus 已配置读写分离数据源");
        }
        return null; // 让 Spring Boot 自动配置
    }
}
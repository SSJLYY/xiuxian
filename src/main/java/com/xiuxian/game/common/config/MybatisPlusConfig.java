package com.xiuxian.game.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * 分页插件 + 乐观锁插件
 *
 * @author shaun.sheng
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /**
     * 添加分页插件和乐观锁插件
     * 注意：乐观锁插件必须在分页插件之前（排在前面）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 乐观锁插件（处理 @Version 字段）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        log.info("MyBatis-Plus 插件已加载: 乐观锁 + 分页");
        return interceptor;
    }
}

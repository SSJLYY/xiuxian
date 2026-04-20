package com.xiuxian.game.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务执行器配置
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * 邮件任务执行器 - 用于发送邮件的线程池
     * 优化说明：提高并发能力，支持批量邮件发送
     */
    @Bean("mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);        // 从 2 增加到 4
        executor.setMaxPoolSize(10);        // 从 5 增加到 10
        executor.setQueueCapacity(200);     // 从 100 增加到 200
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Mail-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("邮件任务执行器初始化完成，corePoolSize=4, maxPoolSize=10, queueCapacity=200");
        return executor;
    }
    
    /**
     * 排行榜任务执行器 - 用于更新排行榜的线程池
     * 优化说明：定时任务，保持单线程以确保顺序执行
     */
    @Bean("rankingTaskExecutor")
    public Executor rankingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);         // 保持单线程
        executor.setQueueCapacity(10);      // 从 50 减少到 10（定时任务不需要大队列）
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Ranking-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("排行榜任务执行器初始化完成，corePoolSize=1, maxPoolSize=1, queueCapacity=10");
        return executor;
    }
    
    /**
     * 统计数据任务执行器 - 用于处理统计数据的线程池
     * 优化说明：提高并发能力，支持多维度统计并行处理
     */
    @Bean("statisticsTaskExecutor")
    public Executor statisticsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 从 1 增加到 2
        executor.setMaxPoolSize(6);         // 从 3 增加到 6
        executor.setQueueCapacity(100);     // 从 20 增加到 100
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Statistics-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("统计数据任务执行器初始化完成，corePoolSize=2, maxPoolSize=6, queueCapacity=100");
        return executor;
    }
    
    /**
     * 通用任务执行器 - 用于处理通用后台任务的线程池
     * 优化说明：提高并发能力，支持多种后台任务并行处理
     */
    @Bean("generalTaskExecutor")
    public Executor generalTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);        // 从 3 增加到 4
        executor.setMaxPoolSize(15);        // 从 10 增加到 15
        executor.setQueueCapacity(500);     // 从 200 增加到 500
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("General-Task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("通用任务执行器初始化完成，corePoolSize=4, maxPoolSize=15, queueCapacity=500");
        return executor;
    }
}

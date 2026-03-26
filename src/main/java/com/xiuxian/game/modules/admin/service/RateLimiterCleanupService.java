package com.xiuxian.game.modules.admin.service;

import com.xiuxian.game.common.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 限流令牌桶清理服务
 * 定时清理过期的限流桶，防止内存泄漏
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterCleanupService {

    private final RateLimiter rateLimiter;

    /**
     * 每5分钟清理一次过期的限流桶
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupExpiredBuckets() {
        try {
            rateLimiter.cleanup();
            log.debug("限流桶清理完成");
        } catch (Exception e) {
            log.error("限流桶清理失败", e);
        }
    }
}

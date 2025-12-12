package com.xiuxian.game.service;

import com.xiuxian.game.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 限流器清理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterCleanupService {
    
    private final RateLimiter rateLimiter;
    
    /**
     * 每5分钟清理一次过期的令牌桶
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupExpiredBuckets() {
        try {
            rateLimiter.cleanup();
            log.debug("清理过期的限流令牌桶");
        } catch (Exception e) {
            log.error("清理限流令牌桶失败", e);
        }
    }
}
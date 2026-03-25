package com.xiuxian.game.modules.admin.service;

import com.xiuxian.game.common.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 限流器清理服�?
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterCleanupService {
    
    private final RateLimiter rateLimiter;
    
    /**
     * �?分钟清理一次过期的令牌�?
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void cleanupExpiredBuckets() {
        try {
            rateLimiter.cleanup();
            log.debug("清理过期的限流令牌桶");
        } catch (Exception e) {
            log.error("清理限流令牌桶失�?, e);
        }
    }
}

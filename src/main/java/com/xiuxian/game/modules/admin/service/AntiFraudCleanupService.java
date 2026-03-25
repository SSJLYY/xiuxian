package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 反作弊清理服�?
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiFraudCleanupService {
    
    private final AntiFraudService antiFraudService;
    
    /**
     * 每小时清理一次过期的异常行为计数�?
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupExpiredCounters() {
        try {
            antiFraudService.cleanupExpiredCounters();
            log.debug("清理过期的异常行为计数器");
        } catch (Exception e) {
            log.error("清理异常行为计数器失�?, e);
        }
    }
}

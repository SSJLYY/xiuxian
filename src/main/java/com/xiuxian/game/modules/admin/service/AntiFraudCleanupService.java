package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 反作弊计数器清理服务
 * 定时清理过期的反作弊计数器数据
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiFraudCleanupService {

    private final AntiFraudService antiFraudService;

    /**
     * 每小时清理一次过期的反作弊计数器
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredCounters() {
        try {
            antiFraudService.cleanupExpiredCounters();
            log.debug("反作弊过期计数器清理完成");
        } catch (Exception e) {
            log.error("反作弊过期计数器清理失败", e);
        }
    }
}

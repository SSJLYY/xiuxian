package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 安全会话清理服务
 * 定时清理过期的安全会话和封禁记录
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityCleanupService {

    private final AccountSecurityService accountSecurityService;

    /**
     * 每30分钟清理一次过期会话
     */
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredSessions() {
        try {
            accountSecurityService.cleanupExpiredSessions();
            log.debug("过期安全会话清理完成");
        } catch (Exception e) {
            log.error("过期安全会话清理失败", e);
        }
    }
}

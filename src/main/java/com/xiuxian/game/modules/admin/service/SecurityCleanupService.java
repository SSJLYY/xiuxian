package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 安全清理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityCleanupService {
    
    private final AccountSecurityService accountSecurityService;
    
    /**
     * �?0分钟清理一次过期会�?
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void cleanupExpiredSessions() {
        try {
            accountSecurityService.cleanupExpiredSessions();
            log.debug("清理过期用户会话");
        } catch (Exception e) {
            log.error("清理过期会话失败", e);
        }
    }
}

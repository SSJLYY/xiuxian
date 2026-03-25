package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 日志清理定时任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogCleanupService {
    
    private final PlayerLoginLogService playerLoginLogService;
    private final AdminOperationLogService adminOperationLogService;
    
    /**
     * 每天凌晨2点清理过期日�?
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredLogs() {
        log.info("开始清理过期日�?);
        
        try {
            // 清理玩家登录日志（保�?0天）
            playerLoginLogService.cleanExpiredLogs();
            
            // 清理管理员操作日志（保留180天）
            adminOperationLogService.cleanExpiredLogs();
            
            log.info("过期日志清理完成");
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
        }
    }
}

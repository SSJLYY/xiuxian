package com.xiuxian.game.modules.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.xiuxian.game.modules.player.service.PlayerLoginLogService;
import com.xiuxian.game.modules.admin.service.AdminOperationLogService;

/**
 * 日志清理服务
 * 定时清理过期的登录日志和操作日志
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogCleanupService {

    private final PlayerLoginLogService playerLoginLogService;
    private final AdminOperationLogService adminOperationLogService;

    /**
     * 每天凌晨2点执行日志清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredLogs() {
        log.info("开始执行过期日志清理任务");

        try {
            // 清理过期的玩家登录日志（保留最近30天）
            playerLoginLogService.cleanExpiredLogs();

            adminOperationLogService.cleanExpiredLogs();

            log.info("过期日志清理任务执行完成");
        } catch (Exception e) {
            log.error("过期日志清理任务执行失败", e);
        }
    }
}

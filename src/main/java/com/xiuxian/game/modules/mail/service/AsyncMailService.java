package com.xiuxian.game.modules.mail.service;

import com.xiuxian.game.modules.mail.entity.MailAttachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步邮件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMailService {

    private final MailService mailService;

    /**
     * 异步批量发送邮件
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendMailBatch(List<Integer> playerIds, String title,
                                                 String content, String mailType,
                                                 List<MailAttachment> attachments,
                                                 LocalDateTime expireAt) {
        try {
            log.info("开始批量发送邮件: 目标玩家数={}, 标题={}", playerIds.size(), title);

            int successCount = 0;
            int failCount = 0;

            for (Integer playerId : playerIds) {
                try {
                    mailService.sendMail(playerId, title, content, mailType, attachments, expireAt);
                    successCount++;

                    // 每发送100封邮件休息一下，避免过度占用资源
                    if (successCount % 100 == 0) {
                        Thread.sleep(100);
                        log.debug("已发送 {} 封邮件", successCount);
                    }

                } catch (Exception e) {
                    failCount++;
                    log.warn("发送邮件失败: playerId={}, error={}", playerId, e.getMessage());
                }
            }

            log.info("批量邮件发送完成: 成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("批量发送邮件异常", e);
            // 异步方法中通过 CompletableFuture 传递异常；此处 re-wrap 确保调用方能感知到失败
            throw new com.xiuxian.game.exception.BusinessException(
                    com.xiuxian.game.exception.ErrorCode.MAIL_BOX_FULL);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 异步发送单封邮件
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendMailAsync(Integer playerId, String title, String content,
                                                 String mailType, List<MailAttachment> attachments,
                                                 LocalDateTime expireAt) {
        try {
            mailService.sendMail(playerId, title, content, mailType, attachments, expireAt);
            log.debug("异步邮件发送成功: playerId={}, title={}", playerId, title);
        } catch (Exception e) {
            log.error("异步邮件发送失败: playerId={}, title={}", playerId, title, e);
            // 异步失败仅记录日志，不干扰其他玩家的邮件
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 异步发送系统通知邮件
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendSystemNotificationAsync(List<Integer> playerIds,
                                                               String title, String content) {
        try {
            log.info("开始发送系统通知邮件: 目标玩家数={}, 标题={}", playerIds.size(), title);

            LocalDateTime expireAt = LocalDateTime.now().plusDays(7); // 7天后过期

            for (Integer playerId : playerIds) {
                try {
                    mailService.sendMail(playerId, title, content, "SYSTEM", null, expireAt);
                } catch (Exception e) {
                    log.warn("发送系统通知失败: playerId={}, error={}", playerId, e.getMessage());
                }
            }

            log.info("系统通知邮件发送完成");

        } catch (Exception e) {
            log.error("发送系统通知邮件异常", e);
            throw new com.xiuxian.game.exception.BusinessException(
                    com.xiuxian.game.exception.ErrorCode.SYSTEM_ERROR);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 异步发送活动奖励邮件
     */
    @Async("mailTaskExecutor")
    public CompletableFuture<Void> sendActivityRewardAsync(List<Integer> playerIds, String title,
                                                           String content, List<MailAttachment> attachments) {
        try {
            log.info("开始发送活动奖励邮件: 目标玩家数={}, 标题={}", playerIds.size(), title);

            LocalDateTime expireAt = LocalDateTime.now().plusDays(30); // 30天后过期

            int batchSize = 50; // 每批处理50个玩家
            for (int i = 0; i < playerIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, playerIds.size());
                List<Integer> batchPlayerIds = playerIds.subList(i, endIndex);

                for (Integer playerId : batchPlayerIds) {
                    try {
                        mailService.sendMail(playerId, title, content, "ACTIVITY", attachments, expireAt);
                    } catch (Exception e) {
                        log.warn("发送活动奖励失败: playerId={}, error={}", playerId, e.getMessage());
                    }
                }

                // 每批之间休息一下
                if (endIndex < playerIds.size()) {
                    Thread.sleep(200);
                }

                log.debug("活动奖励邮件批次完成: {}/{}", endIndex, playerIds.size());
            }

            log.info("活动奖励邮件发送完成");

        } catch (Exception e) {
            log.error("发送活动奖励邮件异常", e);
            throw new com.xiuxian.game.exception.BusinessException(
                    com.xiuxian.game.exception.ErrorCode.SYSTEM_ERROR);
        }

        return CompletableFuture.completedFuture(null);
    }
}

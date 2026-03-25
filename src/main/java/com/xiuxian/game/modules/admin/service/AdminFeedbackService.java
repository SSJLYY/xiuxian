package com.xiuxian.game.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import com.xiuxian.game.modules.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Admin feedback service
 * Module boundary: access mail data via MailService, no direct Mapper injection.
 *
 * @author shaun.sheng
 */
@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private final MailService mailService; // module boundary: access mail data via MailService

    public Page<PlayerMail> getFeedbackList(int page, int size, Integer playerId, Boolean isRead) {
        return mailService.getFeedbackList(page, size, playerId, isRead);
    }

    public PlayerMail getFeedbackById(Long feedbackId) {
        return mailService.getFeedbackById(feedbackId);
    }

    public PlayerMail markAsRead(Long feedbackId) {
        return mailService.markFeedbackAsRead(feedbackId);
    }

    public boolean deleteFeedback(Long feedbackId) {
        return mailService.deleteFeedback(feedbackId);
    }

    public boolean replyToFeedback(Long feedbackId, String replyContent, Integer adminId) {
        return mailService.replyToFeedback(feedbackId, replyContent, adminId);
    }
}

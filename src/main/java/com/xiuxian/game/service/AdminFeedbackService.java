package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.entity.PlayerMail;
import com.xiuxian.game.mapper.PlayerMailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private final PlayerMailMapper playerMailMapper;

    /**
     * 获取反馈列表（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @param playerId 玩家ID（可选）
     * @param isRead 是否已读（可选）
     * @return 反馈分页列表
     */
    public Page<PlayerMail> getFeedbackList(int page, int size, Integer playerId, Boolean isRead) {
        Page<PlayerMail> pageObj = new Page<>(page, size);
        QueryWrapper<PlayerMail> queryWrapper = new QueryWrapper<>();
        
        // 只获取反馈类型的邮件（这里我们假设反馈邮件类型为"FEEDBACK"）
        queryWrapper.eq("mail_type", "FEEDBACK");
        
        if (playerId != null) {
            queryWrapper.eq("player_id", playerId);
        }
        
        if (isRead != null) {
            queryWrapper.eq("is_read", isRead);
        }
        
        queryWrapper.orderByDesc("created_at");
        return playerMailMapper.selectPage(pageObj, queryWrapper);
    }

    /**
     * 根据ID获取反馈详情
     *
     * @param feedbackId 反馈ID
     * @return 反馈详情
     */
    public PlayerMail getFeedbackById(Long feedbackId) {
        PlayerMail feedback = playerMailMapper.selectById(feedbackId);
        if (feedback != null && "FEEDBACK".equals(feedback.getMailType())) {
            return feedback;
        }
        return null;
    }

    /**
     * 标记反馈为已读
     *
     * @param feedbackId 反馈ID
     * @return 更新后的反馈
     */
    public PlayerMail markAsRead(Long feedbackId) {
        PlayerMail feedback = playerMailMapper.selectById(feedbackId);
        if (feedback != null && "FEEDBACK".equals(feedback.getMailType())) {
            feedback.setIsRead(true);
            playerMailMapper.updateById(feedback);
            return feedback;
        }
        throw new IllegalArgumentException("反馈不存在");
    }

    /**
     * 删除反馈
     *
     * @param feedbackId 反馈ID
     * @return 是否删除成功
     */
    public boolean deleteFeedback(Long feedbackId) {
        PlayerMail feedback = playerMailMapper.selectById(feedbackId);
        if (feedback != null && "FEEDBACK".equals(feedback.getMailType())) {
            return playerMailMapper.deleteById(feedbackId) > 0;
        }
        return false;
    }

    /**
     * 回复反馈
     *
     * @param feedbackId 反馈ID
     * @param replyContent 回复内容
     * @param adminId 管理员ID
     * @return 是否回复成功
     */
    public boolean replyToFeedback(Long feedbackId, String replyContent, Integer adminId) {
        PlayerMail feedback = playerMailMapper.selectById(feedbackId);
        if (feedback != null && "FEEDBACK".equals(feedback.getMailType())) {
            // 创建回复邮件
            PlayerMail replyMail = new PlayerMail();
            replyMail.setPlayerId(feedback.getPlayerId());
            replyMail.setTitle("回复: " + feedback.getTitle());
            replyMail.setContent(replyContent);
            replyMail.setMailType("SYSTEM");
            replyMail.setIsRead(false);
            replyMail.setHasAttachment(false);
            replyMail.setIsClaimed(false);
            replyMail.setExpireAt(java.time.LocalDateTime.now().plusDays(30)); // 30天后过期
            
            playerMailMapper.insert(replyMail);
            
            // 标记原反馈为已读
            feedback.setIsRead(true);
            playerMailMapper.updateById(feedback);
            
            return true;
        }
        return false;
    }
}
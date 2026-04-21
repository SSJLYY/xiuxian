package com.xiuxian.game.modules.mail.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// mail module entities (same module -- OK)
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
// mail module mappers (same module -- OK)
import com.xiuxian.game.modules.mail.mapper.MailAttachmentMapper;
import com.xiuxian.game.modules.mail.mapper.PlayerMailMapper;
// cross-module entities accessed via Service interfaces
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
// cross-module services (module boundary)
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.dto.request.SystemMailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final PlayerMailMapper mailMapper;
    private final MailAttachmentMapper attachmentMapper;
    // module boundary: access player/equipment data via Service, not direct Mapper injection
    private final PlayerService playerService;
    private final EquipmentService equipmentService;

    private static final int MAX_MAILBOX_SIZE = 100;

    /**
     * 发送系统邮件给单个玩家（DTO 版本，推荐使用）
     */
    @Transactional
    public void sendSystemMail(SystemMailRequest request) {
        log.info("发送系统邮件: playerId={}, title={}, itemType={}, itemId={}, quantity={}", 
                request.getPlayerId(), request.getTitle(), request.getItemType(), request.getItemId(), request.getQuantity());
        
        List<MailAttachment> attachments = new ArrayList<>();
        if (request.getItemType() != null && request.getItemId() != null && request.getQuantity() != null) {
            MailAttachment attachment = new MailAttachment();
            attachment.setItemType(request.getItemType());
            attachment.setItemId(request.getItemId());
            attachment.setQuantity(request.getQuantity());
            attachments.add(attachment);
        }
        
        sendMail(request.getPlayerId(), request.getTitle(), request.getContent(), "SYSTEM", attachments, LocalDateTime.now().plusDays(30));
    }

    /**
     * 发送系统邮件给单个玩家（多参数版本，兼容旧调用方）
     */
    @Transactional
    public void sendSystemMail(Integer playerId, String title, String content, String itemType, 
                              Integer itemId, Integer quantity) {
        sendSystemMail(SystemMailRequest.builder()
                .playerId(playerId).title(title).content(content)
                .itemType(itemType).itemId(itemId).quantity(quantity)
                .build());
    }

    /**
     * 发送邮件给单个玩家
     */
    @Transactional
    public void sendMail(Integer playerId, String title, String content, String mailType, 
                        List<MailAttachment> attachments, LocalDateTime expireAt) {
        log.info("发送邮件: playerId={}, title={}, mailType={}", playerId, title, mailType);
        
        // 检查邮箱容量
        long mailCount = mailMapper.selectCount(new QueryWrapper<PlayerMail>()
                .eq("player_id", playerId));
        if (mailCount >= MAX_MAILBOX_SIZE) {
            throw new BusinessException(ErrorCode.MAIL_BOX_FULL);
        }
        
        // 创建邮件
        PlayerMail mail = new PlayerMail();
        mail.setPlayerId(playerId);
        mail.setTitle(title);
        mail.setContent(content);
        mail.setMailType(mailType);
        mail.setIsRead(false);
        mail.setHasAttachment(attachments != null && !attachments.isEmpty());
        mail.setIsClaimed(false);
        mail.setExpireAt(expireAt);
        
        mailMapper.insert(mail);
        
        // 保存附件 - 批量设置mailId后一次性插入
        if (attachments != null && !attachments.isEmpty()) {
            List<MailAttachment> attachmentsToInsert = new ArrayList<>(attachments.size());
            for (MailAttachment attachment : attachments) {
                MailAttachment copy = new MailAttachment();
                copy.setMailId(mail.getId());
                copy.setItemType(attachment.getItemType());
                copy.setItemId(attachment.getItemId());
                copy.setQuantity(attachment.getQuantity());
                attachmentsToInsert.add(copy);
            }
            attachmentMapper.insertBatchSomeColumn(attachmentsToInsert);
        }
        
        log.info("邮件发送成功: mailId={}", mail.getId());
    }

    /**
     * 批量发送邮件
     */
    public void sendBatchMail(List<Integer> playerIds, String title, String content, 
                              String mailType, List<MailAttachment> attachments, LocalDateTime expireAt) {
        log.info("批量发送邮件：playerCount={}, title={}", playerIds.size(), title);
        
        for (Integer playerId : playerIds) {
            try {
                sendMail(playerId, title, content, mailType, attachments, expireAt);
            } catch (Exception e) {
                log.error("发送邮件失败：playerId={}", playerId, e);
            }
        }
    }

    /**
     * 获取邮件列表
     */
    public IPage<PlayerMail> getMailList(Integer playerId, int page, int size) {
        log.debug("获取邮件列表: playerId={}, page={}, size={}", playerId, page, size);
        
        IPage<PlayerMail> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<PlayerMail> wrapper = new QueryWrapper<>();
        wrapper.eq("player_id", playerId)
               .orderByDesc("created_at");
        
        return mailMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取邮件详情（含标记已读）
     * 标记已读是单条 updateById，不需要独立事务；
     * 若被外层事务调用则自动加入，独立调用时单条更新天然原子。
     */
    public PlayerMail getMailDetail(Integer playerId, Long mailId) {
        log.debug("获取邮件详情: playerId={}, mailId={}", playerId, mailId);
        
        PlayerMail mail = mailMapper.selectById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.MAIL_NOT_FOUND);
        }
        
        if (!mail.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.MAIL_ACCESS_DENIED);
        }
        
        // 标记为已读
        if (!mail.getIsRead()) {
            mail.setIsRead(true);
            mailMapper.updateById(mail);
        }
        
        return mail;
    }

    /**
     * 领取邮件附件
     */
    @Transactional
    public void claimAttachment(Integer playerId, Long mailId) {
        log.info("领取邮件附件: playerId={}, mailId={}", playerId, mailId);
        
        PlayerMail mail = mailMapper.selectById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.MAIL_NOT_FOUND);
        }
        
        if (!mail.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.MAIL_ACCESS_DENIED);
        }
        
        if (!mail.getHasAttachment()) {
            throw new BusinessException(ErrorCode.MAIL_NO_ATTACHMENT);
        }
        
        if (mail.getIsClaimed()) {
            throw new BusinessException(ErrorCode.MAIL_ALREADY_CLAIMED);
        }
        
        // 获取附件列表
        List<MailAttachment> attachments = attachmentMapper.selectList(
                new QueryWrapper<MailAttachment>().eq("mail_id", mailId));
        
        if (attachments.isEmpty()) {
            throw new BusinessException(ErrorCode.MAIL_NO_ATTACHMENT);
        }
        
        int claimedRows = mailMapper.claimAttachmentIfUnclaimed(mailId, playerId);
        if (claimedRows == 0) {
            throw new BusinessException(ErrorCode.MAIL_ALREADY_CLAIMED);
        }

        // 发放附件奖励
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }
        for (MailAttachment attachment : attachments) {
            grantAttachment(profile, attachment);
        }

        log.info("附件领取成功: mailId={}, attachmentCount={}", mailId, attachments.size());
    }

    /**
     * 发放附件奖励
     */
    private void grantAttachment(PlayerProfile profile, MailAttachment attachment) {
        String itemType = attachment.getItemType();
        Integer itemId = attachment.getItemId();
        Integer quantity = attachment.getQuantity();
        
        // 参数校验
        if (itemType == null || itemType.trim().isEmpty()) {
            log.error("附件类型为空: mailId={}, playerId={}", attachment.getMailId(), profile.getId());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "附件类型不能为空");
        }
        
        if (quantity == null || quantity <= 0) {
            log.error("附件数量无效: quantity={}, playerId={}", quantity, profile.getId());
            throw new BusinessException(ErrorCode.PARAM_ERROR, "附件数量必须大于0");
        }
        
        switch (itemType.toUpperCase()) {
            case "SPIRIT_STONES":
                profile.setSpiritStones(profile.getSpiritStones() + quantity);
                playerService.savePlayerProfile(profile);
                log.debug("发放灵石: playerId={}, quantity={}", profile.getId(), quantity);
                break;
                
            case "EXP":
                profile.setExp(profile.getExp() + quantity);
                playerService.savePlayerProfile(profile);
                log.debug("发放经验: playerId={}, quantity={}", profile.getId(), quantity);
                break;
                
            case "ITEM":
                if (itemId == null) {
                    log.error("物品ID为空: playerId={}", profile.getId());
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "物品ID不能为空");
                }
                PlayerItem existingItem = playerService.getPlayerItemByPlayerAndItem(profile.getId(), itemId);
                
                if (existingItem != null) {
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    playerService.updatePlayerItem(existingItem);
                } else {
                    PlayerItem newItem = new PlayerItem();
                    newItem.setPlayerId(profile.getId());
                    newItem.setItemId(itemId);
                    newItem.setQuantity(quantity);
                    newItem.setCreatedAt(LocalDateTime.now());
                    newItem.setUpdatedAt(LocalDateTime.now());
                    playerService.savePlayerItem(newItem);
                }
                log.debug("发放物品: playerId={}, itemId={}, quantity={}", profile.getId(), itemId, quantity);
                break;
                
            case "EQUIPMENT":
                if (itemId == null) {
                    log.error("装备ID为空: playerId={}", profile.getId());
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "装备ID不能为空");
                }
                equipmentService.grantEquipmentDirectly(profile.getId(), itemId);
                log.debug("发放装备: playerId={}, equipmentId={}", profile.getId(), itemId);
                break;
                
            default:
                log.error("未知的附件类型: itemType={}, playerId={}, mailId={}", itemType, profile.getId(), attachment.getMailId());
                throw new BusinessException(ErrorCode.PARAM_ERROR, "未知的附件类型: " + itemType);
        }
    }

    /**
     * 删除邮件
     */
    @Transactional
    public void deleteMail(Integer playerId, Long mailId) {
        log.info("删除邮件: playerId={}, mailId={}", playerId, mailId);
        
        PlayerMail mail = mailMapper.selectById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.MAIL_NOT_FOUND);
        }
        
        if (!mail.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.MAIL_ACCESS_DENIED);
        }
        
        // 删除附件
        attachmentMapper.delete(new QueryWrapper<MailAttachment>().eq("mail_id", mailId));
        
        // 删除邮件
        mailMapper.deleteById(mailId);
        
        log.info("邮件删除成功: mailId={}", mailId);
    }

    /**
     * 获取未读邮件数量
     */
    public long getUnreadCount(Integer playerId) {
        return mailMapper.selectCount(new QueryWrapper<PlayerMail>()
                .eq("player_id", playerId)
                .eq("is_read", false));
    }

    /**
     * 定时清理过期邮件
     * 每天凌晨3点执行。
     * 改为分批处理（每批500条），避免全表加载到内存 + 长事务问题。
     * 每批独立事务，减少DB连接持有时间。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredMails() {
        log.info("开始清理过期邮件");
        LocalDateTime now = LocalDateTime.now();
        int totalDeleted = 0;
        int batchSize = 500;
        List<Long> expiredIds;

        do {
            expiredIds = mailMapper.selectExpiredMailIds(now, batchSize);
            if (expiredIds.isEmpty()) break;

            // 每批附件+邮件一起删，独立事务减少锁持有时长
            cleanBatch(expiredIds);
            totalDeleted += expiredIds.size();
            log.debug("清理过期邮件批次: count={}", expiredIds.size());
        } while (expiredIds.size() == batchSize);

        log.info("过期邮件清理完成: totalDeleted={}", totalDeleted);
    }

    /**
     * 批量删除一批过期邮件及其附件（独立事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanBatch(List<Long> mailIds) {
        if (mailIds.isEmpty()) return;
        // 先删附件，再删邮件（遵循外键顺序）
        attachmentMapper.deleteBatchByMailIds(mailIds);
        mailMapper.deleteBatchByIds(mailIds);
    }

    /**
     * 获取邮件附件列表
     */
    public List<MailAttachment> getMailAttachments(Long mailId) {
        return attachmentMapper.selectList(
                new QueryWrapper<MailAttachment>().eq("mail_id", mailId));
    }

    // ===================== Admin Feedback interface (for AdminFeedbackService) =====================

    /** Get feedback mail list paged (admin use) */
    public Page<PlayerMail> getFeedbackList(int page, int size, Integer playerId, Boolean isRead) {
        Page<PlayerMail> pageObj = new Page<>(page, size);
        QueryWrapper<PlayerMail> qw = new QueryWrapper<PlayerMail>().eq("mail_type", "FEEDBACK");
        if (playerId != null) qw.eq("player_id", playerId);
        if (isRead != null) qw.eq("is_read", isRead);
        qw.orderByDesc("created_at");
        return mailMapper.selectPage(pageObj, qw);
    }

    /** Get single feedback mail by ID (admin use) */
    public PlayerMail getFeedbackById(Long feedbackId) {
        PlayerMail m = mailMapper.selectById(feedbackId);
        return (m != null && "FEEDBACK".equals(m.getMailType())) ? m : null;
    }

    /** Mark feedback as read (admin use) */
    public PlayerMail markFeedbackAsRead(Long feedbackId) {
        PlayerMail m = mailMapper.selectById(feedbackId);
        if (m == null || !"FEEDBACK".equals(m.getMailType())) throw new BusinessException(ErrorCode.PARAM_ERROR, "Feedback not found");
        m.setIsRead(true);
        mailMapper.updateById(m);
        return m;
    }

    /** Delete feedback mail (admin use) */
    public boolean deleteFeedback(Long feedbackId) {
        PlayerMail m = mailMapper.selectById(feedbackId);
        if (m == null || !"FEEDBACK".equals(m.getMailType())) return false;
        return mailMapper.deleteById(feedbackId) > 0;
    }

    /** Reply to feedback (admin use) */
    public boolean replyToFeedback(Long feedbackId, String replyContent, Integer adminId) {
        PlayerMail orig = mailMapper.selectById(feedbackId);
        if (orig == null || !"FEEDBACK".equals(orig.getMailType())) return false;
        PlayerMail reply = new PlayerMail();
        reply.setPlayerId(orig.getPlayerId());
        reply.setTitle("Reply: " + orig.getTitle());
        reply.setContent(replyContent);
        reply.setMailType("SYSTEM");
        reply.setIsRead(false);
        reply.setHasAttachment(false);
        reply.setIsClaimed(false);
        reply.setExpireAt(java.time.LocalDateTime.now().plusDays(30));
        mailMapper.insert(reply);
        orig.setIsRead(true);
        mailMapper.updateById(orig);
        return true;
    }
}



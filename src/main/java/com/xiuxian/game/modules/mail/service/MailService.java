package com.xiuxian.game.modules.mail.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.dto.request.SystemMailRequest;
import com.xiuxian.game.modules.equipment.entity.Equipment;
import com.xiuxian.game.modules.equipment.service.EquipmentService;
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import com.xiuxian.game.modules.mail.mapper.MailAttachmentMapper;
import com.xiuxian.game.modules.mail.mapper.PlayerMailMapper;
import com.xiuxian.game.modules.player.entity.PlayerItem;
import com.xiuxian.game.modules.player.entity.PlayerProfile;
import com.xiuxian.game.modules.player.mapper.PlayerProfileMapper;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.modules.shop.entity.Item;
import com.xiuxian.game.modules.shop.service.ItemService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private static final int MAX_MAILBOX_SIZE = 100;

    private final PlayerMailMapper mailMapper;
    private final MailAttachmentMapper attachmentMapper;
    private final PlayerProfileMapper playerProfileMapper;
    private final PlayerService playerService;
    private final EquipmentService equipmentService;
    private final ItemService itemService;
    private final PlatformTransactionManager transactionManager;

    @Transactional
    public void sendSystemMail(SystemMailRequest request) {
        log.info("Send system mail: playerId={}, title={}", request.getPlayerId(), request.getTitle());

        List<MailAttachment> attachments = new ArrayList<>();
        if (request.getItemType() != null && request.getQuantity() != null) {
            boolean requiresItemId = !"SPIRIT_STONES".equalsIgnoreCase(request.getItemType())
                    && !"EXP".equalsIgnoreCase(request.getItemType());
            if (!requiresItemId || request.getItemId() != null) {
                MailAttachment attachment = new MailAttachment();
                attachment.setItemType(request.getItemType());
                attachment.setItemId(request.getItemId());
                attachment.setQuantity(request.getQuantity());
                attachments.add(attachment);
            }
        }

        sendMail(request.getPlayerId(), request.getTitle(), request.getContent(),
                "SYSTEM", attachments, LocalDateTime.now().plusDays(30));
    }

    @Transactional
    public void sendSystemMail(Integer playerId, String title, String content, String itemType,
                               Integer itemId, Integer quantity) {
        sendSystemMail(SystemMailRequest.builder()
                .playerId(playerId)
                .title(title)
                .content(content)
                .itemType(itemType)
                .itemId(itemId)
                .quantity(quantity)
                .build());
    }

    @Transactional
    public void sendMail(Integer playerId, String title, String content, String mailType,
                         List<MailAttachment> attachments, LocalDateTime expireAt) {
        doSendMail(playerId, title, content, mailType, attachments, expireAt);
    }

    public void sendMailInNewTransaction(Integer playerId, String title, String content, String mailType,
                                         List<MailAttachment> attachments, LocalDateTime expireAt) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status ->
                doSendMail(playerId, title, content, mailType, attachments, expireAt));
    }

    public BatchSendResult sendBatchMail(List<Integer> playerIds, String title, String content,
                                         String mailType, List<MailAttachment> attachments, LocalDateTime expireAt) {
        log.info("Send batch mail: playerCount={}, title={}", playerIds.size(), title);

        BatchSendResult result = new BatchSendResult();
        for (Integer playerId : playerIds) {
            try {
                sendMailInNewTransaction(playerId, title, content, mailType, attachments, expireAt);
                result.addSuccessPlayerId(playerId);
            } catch (Exception e) {
                log.error("Send batch mail failed: playerId={}", playerId, e);
                result.addFailedPlayerId(playerId);
            }
        }
        return result;
    }

    public IPage<PlayerMail> getMailList(Integer playerId, int page, int size) {
        IPage<PlayerMail> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<PlayerMail> wrapper = new QueryWrapper<>();
        wrapper.eq("player_id", playerId).orderByDesc("created_at");

        IPage<PlayerMail> result = mailMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(this::normalizeMail);
        return result;
    }

    public PlayerMail getMailDetail(Integer playerId, Long mailId) {
        PlayerMail mail = mailMapper.selectById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.MAIL_NOT_FOUND);
        }
        if (!mail.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.MAIL_ACCESS_DENIED);
        }

        normalizeMail(mail);
        if (!Boolean.TRUE.equals(mail.getIsRead())) {
            mail.setIsRead(true);
            mailMapper.updateById(mail);
        }
        return mail;
    }

    @Transactional
    public void claimAttachment(Integer playerId, Long mailId) {
        PlayerMail mail = mailMapper.selectById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.MAIL_NOT_FOUND);
        }
        if (!mail.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.MAIL_ACCESS_DENIED);
        }

        normalizeMail(mail);
        if (!Boolean.TRUE.equals(mail.getHasAttachment())) {
            throw new BusinessException(ErrorCode.MAIL_NO_ATTACHMENT);
        }
        if (Boolean.TRUE.equals(mail.getIsClaimed())) {
            throw new BusinessException(ErrorCode.MAIL_ALREADY_CLAIMED);
        }

        List<MailAttachment> attachments = attachmentMapper.selectList(
                new QueryWrapper<MailAttachment>().eq("mail_id", mailId));
        if (attachments.isEmpty()) {
            throw new BusinessException(ErrorCode.MAIL_NO_ATTACHMENT);
        }

        int claimedRows = mailMapper.claimAttachmentIfUnclaimed(mailId, playerId);
        if (claimedRows == 0) {
            throw new BusinessException(ErrorCode.MAIL_ALREADY_CLAIMED);
        }

        PlayerProfile profile = playerProfileMapper.selectByIdForUpdate(playerId);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND);
        }

        boolean profileChanged = false;
        for (MailAttachment attachment : attachments) {
            if (grantAttachment(profile, attachment)) {
                profileChanged = true;
            }
        }
        if (profileChanged) {
            playerService.applyLevelUpsWithoutCommit(profile, 100);
            playerService.savePlayerProfile(profile);
        }
    }

    @Transactional
    public void deleteMail(Integer playerId, Long mailId) {
        PlayerMail mail = mailMapper.selectById(mailId);
        if (mail == null) {
            throw new BusinessException(ErrorCode.MAIL_NOT_FOUND);
        }
        if (!mail.getPlayerId().equals(playerId)) {
            throw new BusinessException(ErrorCode.MAIL_ACCESS_DENIED);
        }

        attachmentMapper.delete(new QueryWrapper<MailAttachment>().eq("mail_id", mailId));
        mailMapper.deleteById(mailId);
    }

    public long getUnreadCount(Integer playerId) {
        return mailMapper.selectCount(new QueryWrapper<PlayerMail>()
                .eq("player_id", playerId)
                .and(qw -> qw.eq("is_read", false).or().isNull("is_read")));
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredMails() {
        LocalDateTime now = LocalDateTime.now();
        int batchSize = 500;
        List<Long> expiredIds;

        do {
            expiredIds = mailMapper.selectExpiredMailIds(now, batchSize);
            if (expiredIds.isEmpty()) {
                break;
            }
            cleanBatch(expiredIds);
        } while (expiredIds.size() == batchSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cleanBatch(List<Long> mailIds) {
        if (mailIds.isEmpty()) {
            return;
        }
        attachmentMapper.deleteBatchByMailIds(mailIds);
        mailMapper.deleteBatchByIds(mailIds);
    }

    public List<MailAttachment> getMailAttachments(Long mailId) {
        List<MailAttachment> attachments = attachmentMapper.selectList(
                new QueryWrapper<MailAttachment>().eq("mail_id", mailId));
        attachments.forEach(this::fillAttachmentDisplayName);
        return attachments;
    }

    public Page<PlayerMail> getFeedbackList(int page, int size, Integer playerId, Boolean isRead) {
        Page<PlayerMail> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<PlayerMail> qw = new QueryWrapper<PlayerMail>().eq("mail_type", "FEEDBACK");
        if (playerId != null) {
            qw.eq("player_id", playerId);
        }
        if (isRead != null) {
            qw.eq("is_read", isRead);
        }
        qw.orderByDesc("created_at");
        Page<PlayerMail> result = mailMapper.selectPage(pageObj, qw);
        result.getRecords().forEach(this::normalizeMail);
        return result;
    }

    public PlayerMail getFeedbackById(Long feedbackId) {
        PlayerMail mail = mailMapper.selectById(feedbackId);
        if (mail != null && "FEEDBACK".equals(mail.getMailType())) {
            normalizeMail(mail);
            return mail;
        }
        return null;
    }

    public PlayerMail markFeedbackAsRead(Long feedbackId) {
        PlayerMail mail = mailMapper.selectById(feedbackId);
        if (mail == null || !"FEEDBACK".equals(mail.getMailType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Feedback not found");
        }
        normalizeMail(mail);
        mail.setIsRead(true);
        int updatedRows = mailMapper.updateById(mail);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Feedback not found");
        }
        return mail;
    }

    public boolean deleteFeedback(Long feedbackId) {
        PlayerMail mail = mailMapper.selectById(feedbackId);
        if (mail == null || !"FEEDBACK".equals(mail.getMailType())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Feedback not found");
        }
        int deletedRows = mailMapper.deleteById(feedbackId);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Feedback not found");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean replyToFeedback(Long feedbackId, String replyContent, Integer adminId) {
        PlayerMail original = mailMapper.selectById(feedbackId);
        if (original == null || !"FEEDBACK".equals(original.getMailType())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Feedback not found");
        }

        PlayerMail reply = new PlayerMail();
        reply.setPlayerId(original.getPlayerId());
        reply.setTitle("Reply: " + original.getTitle());
        reply.setContent(replyContent);
        reply.setMailType("SYSTEM");
        reply.setIsRead(false);
        reply.setHasAttachment(false);
        reply.setIsClaimed(false);
        reply.setExpireAt(LocalDateTime.now().plusDays(30));
        int insertedRows = mailMapper.insert(reply);
        if (insertedRows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Reply send failed");
        }

        original.setIsRead(true);
        int updatedRows = mailMapper.updateById(original);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Feedback not found");
        }
        return true;
    }

    private void doSendMail(Integer playerId, String title, String content, String mailType,
                            List<MailAttachment> attachments, LocalDateTime expireAt) {
        long mailCount = mailMapper.selectCount(new QueryWrapper<PlayerMail>().eq("player_id", playerId));
        if (mailCount >= MAX_MAILBOX_SIZE) {
            throw new BusinessException(ErrorCode.MAIL_BOX_FULL);
        }

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
    }

    private boolean grantAttachment(PlayerProfile profile, MailAttachment attachment) {
        String itemType = attachment.getItemType();
        Integer itemId = attachment.getItemId();
        Integer quantity = attachment.getQuantity();

        if (itemType == null || itemType.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Attachment type is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Attachment quantity must be positive");
        }

        switch (itemType.toUpperCase()) {
            case "SPIRIT_STONES":
                profile.setSpiritStones(defaultLong(profile.getSpiritStones()) + quantity);
                return true;
            case "EXP":
                profile.setExp(defaultLong(profile.getExp()) + quantity);
                return true;
            case "ITEM":
                if (itemId == null) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "Item id is required");
                }
                PlayerItem existingItem = playerService.getUnlockedPlayerItemByPlayerAndItem(profile.getId(), itemId);
                if (existingItem != null) {
                    existingItem.setQuantity((existingItem.getQuantity() == null ? 0 : existingItem.getQuantity()) + quantity);
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
                return false;
            case "EQUIPMENT":
                if (itemId == null) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "Equipment id is required");
                }
                equipmentService.grantEquipmentDirectly(profile.getId(), itemId);
                return false;
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "Unknown attachment type: " + itemType);
        }
    }

    private void fillAttachmentDisplayName(MailAttachment attachment) {
        if (attachment == null) {
            return;
        }
        String itemType = attachment.getItemType();
        Integer itemId = attachment.getItemId();
        if (itemType == null || itemType.trim().isEmpty()) {
            attachment.setItemName("附件");
            return;
        }

        switch (itemType.trim().toUpperCase()) {
            case "SPIRIT_STONES":
                attachment.setItemName("灵石");
                return;
            case "EXP":
                attachment.setItemName("经验");
                return;
            case "ITEM":
                Item item = itemId != null ? itemService.getItemById(itemId) : null;
                attachment.setItemName(item != null ? item.getName() : "物品");
                return;
            case "EQUIPMENT":
                Equipment equipment = itemId != null ? equipmentService.getEquipmentById(itemId) : null;
                attachment.setItemName(equipment != null ? equipment.getName() : "装备");
                return;
            default:
                attachment.setItemName(itemType);
        }
    }

    private void normalizeMail(PlayerMail mail) {
        if (mail == null) {
            return;
        }
        if (mail.getIsRead() == null) {
            mail.setIsRead(false);
        }
        if (mail.getHasAttachment() == null) {
            mail.setHasAttachment(false);
        }
        if (mail.getIsClaimed() == null) {
            mail.setIsClaimed(false);
        }
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    @Data
    public static class BatchSendResult {
        private List<Integer> successPlayerIds = new ArrayList<>();
        private List<Integer> failedPlayerIds = new ArrayList<>();

        public int getSuccessCount() {
            return successPlayerIds.size();
        }

        public int getFailCount() {
            return failedPlayerIds.size();
        }

        public List<Integer> getSuccessPlayerIds() {
            return Collections.unmodifiableList(successPlayerIds);
        }

        public List<Integer> getFailedPlayerIds() {
            return Collections.unmodifiableList(failedPlayerIds);
        }

        public void addSuccessPlayerId(Integer playerId) {
            successPlayerIds.add(playerId);
        }

        public void addFailedPlayerId(Integer playerId) {
            failedPlayerIds.add(playerId);
        }
    }
}

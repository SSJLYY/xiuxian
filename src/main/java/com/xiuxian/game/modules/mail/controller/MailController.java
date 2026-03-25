package com.xiuxian.game.modules.mail.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.common.annotation.RateLimit;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import com.xiuxian.game.modules.mail.service.MailService;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件控制�?
 * 
 * <p>处理玩家邮件相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>获取邮件列表</li>
 *   <li>查看邮件详情</li>
 *   <li>领取邮件附件</li>
 *   <li>删除邮件</li>
 *   <li>获取未读邮件数量</li>
 * </ul>
 * 
 * <p>所有接口都需要JWT Token认证，确保只有登录用户才能访问�?/p>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Validated
public class MailController {

    private final MailService mailService;
    private final PlayerService playerService;

    /**
     * 获取邮件列表
     * 
     * <p>返回当前登录玩家的邮件列表，支持分页查询�?/p>
     * 
     * <p>邮件列表包含�?/p>
     * <ul>
     *   <li>邮件标题</li>
     *   <li>邮件类型（系�?奖励/活动�?/li>
     *   <li>是否已读</li>
     *   <li>是否有附�?/li>
     *   <li>附件是否已领�?/li>
     *   <li>发送时�?/li>
     *   <li>过期时间</li>
     * </ul>
     * 
     * @param page 页码，默�?
     * @param size 每页数量，默�?0
     * @return 邮件列表分页数据
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<IPage<PlayerMail>>> getMailList(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取邮件列表: playerId={}, page={}, size={}", playerId, page, size);
            
            IPage<PlayerMail> mailList = mailService.getMailList(playerId, page, size);
            
            LogUtils.logUserAction(null, playerId, "GET_MAIL_LIST", 
                    "获取邮件列表: page=" + page + ", size=" + size);
            
            log.debug("获取邮件列表成功: playerId={}, total={}", playerId, mailList.getTotal());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", mailList));
            
        } catch (Exception e) {
            log.error("获取邮件列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取邮件详情
     * 
     * <p>返回指定邮件的详细信息，包括邮件内容和附件列表�?/p>
     * <p>查看邮件时会自动标记为已读�?/p>
     * 
     * @param mailId 邮件ID
     * @return 邮件详情和附件列�?
     */
    @GetMapping("/{mailId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMailDetail(@PathVariable Long mailId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取邮件详情: playerId={}, mailId={}", playerId, mailId);
            
            PlayerMail mail = mailService.getMailDetail(playerId, mailId);
            List<MailAttachment> attachments = mailService.getMailAttachments(mailId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("mail", mail);
            result.put("attachments", attachments);
            
            LogUtils.logUserAction(null, playerId, "GET_MAIL_DETAIL", 
                    "查看邮件详情: mailId=" + mailId);
            
            log.debug("获取邮件详情成功: playerId={}, mailId={}", playerId, mailId);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
            
        } catch (Exception e) {
            log.error("获取邮件详情失败: mailId={}, error={}", mailId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 领取邮件附件
     * 
     * <p>领取指定邮件的附件奖励，附件将发放到玩家账户�?/p>
     * 
     * <p>附件类型�?/p>
     * <ul>
     *   <li>灵石 - 直接增加到玩家灵石余�?/li>
     *   <li>经验 - 直接增加到玩家经验�?/li>
     *   <li>物品 - 添加到玩家背�?/li>
     *   <li>装备 - 添加到玩家装备库</li>
     * </ul>
     * 
     * <p>注意事项�?/p>
     * <ul>
     *   <li>每个邮件的附件只能领取一�?/li>
     *   <li>领取后邮件会标记为已领取</li>
     *   <li>背包满时无法领取物品类附�?/li>
     * </ul>
     * 
     * @param mailId 邮件ID
     * @return 领取结果
     */
    @PostMapping("/{mailId}/claim")
    @PreAuthorize("isAuthenticated()")
    @RateLimit(keyType = RateLimit.KeyType.USER_ID, maxRequests = 100, windowSeconds = 60, message = "领取邮件过于频繁，请稍后再试")
    public ResponseEntity<ApiResponse<Void>> claimAttachment(@PathVariable Long mailId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.info("领取邮件附件: playerId={}, mailId={}", playerId, mailId);
            
            mailService.claimAttachment(playerId, mailId);
            
            LogUtils.logUserAction(null, playerId, "CLAIM_MAIL_ATTACHMENT", 
                    "领取邮件附件: mailId=" + mailId);
            LogUtils.logBusiness("MAIL", "领取邮件附件", 
                    "playerId", playerId, "mailId", mailId);
            
            log.info("领取邮件附件成功: playerId={}, mailId={}", playerId, mailId);
            
            return ResponseEntity.ok(ApiResponse.success("领取成功", null));
            
        } catch (Exception e) {
            log.error("领取邮件附件失败: mailId={}, error={}", mailId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除邮件
     * 
     * <p>删除指定的邮件，包括邮件内容和附件信息�?/p>
     * 
     * <p>注意事项�?/p>
     * <ul>
     *   <li>只能删除自己的邮�?/li>
     *   <li>删除后无法恢�?/li>
     *   <li>未领取的附件将一并删�?/li>
     * </ul>
     * 
     * @param mailId 邮件ID
     * @return 删除结果
     */
    @DeleteMapping("/{mailId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMail(@PathVariable Long mailId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.info("删除邮件: playerId={}, mailId={}", playerId, mailId);
            
            mailService.deleteMail(playerId, mailId);
            
            LogUtils.logUserAction(null, playerId, "DELETE_MAIL", 
                    "删除邮件: mailId=" + mailId);
            
            log.info("删除邮件成功: playerId={}, mailId={}", playerId, mailId);
            
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            
        } catch (Exception e) {
            log.error("删除邮件失败: mailId={}, error={}", mailId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量删除邮件
     * 
     * <p>批量删除多个邮件�?/p>
     * 
     * @param mailIds 邮件ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> batchDeleteMails(@RequestBody List<Long> mailIds) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.info("批量删除邮件: playerId={}, count={}", playerId, mailIds.size());
            
            for (Long mailId : mailIds) {
                try {
                    mailService.deleteMail(playerId, mailId);
                } catch (Exception e) {
                    log.warn("删除邮件失败: mailId={}, error={}", mailId, e.getMessage());
                }
            }
            
            LogUtils.logUserAction(null, playerId, "BATCH_DELETE_MAIL", 
                    "批量删除邮件: count=" + mailIds.size());
            
            log.info("批量删除邮件成功: playerId={}, count={}", playerId, mailIds.size());
            
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            
        } catch (Exception e) {
            log.error("批量删除邮件失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 标记邮件为已�?
     * 
     * <p>手动标记邮件为已读状态�?/p>
     * <p>注意：查看邮件详情时会自动标记为已读，此接口用于不查看详情的情况�?/p>
     * 
     * @param mailId 邮件ID
     * @return 标记结果
     */
    @PostMapping("/{mailId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long mailId) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("标记邮件已读: playerId={}, mailId={}", playerId, mailId);
            
            // 通过获取详情来标记已�?
            mailService.getMailDetail(playerId, mailId);
            
            LogUtils.logUserAction(null, playerId, "MARK_MAIL_READ", 
                    "标记邮件已读: mailId=" + mailId);
            
            log.debug("标记邮件已读成功: playerId={}, mailId={}", playerId, mailId);
            
            return ResponseEntity.ok(ApiResponse.success("标记成功", null));
            
        } catch (Exception e) {
            log.error("标记邮件已读失败: mailId={}, error={}", mailId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取未读邮件数量
     * 
     * <p>返回当前玩家的未读邮件数量，用于显示红点提示�?/p>
     * 
     * @return 未读邮件数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取未读邮件数量: playerId={}", playerId);
            
            long unreadCount = mailService.getUnreadCount(playerId);
            
            log.debug("获取未读邮件数量成功: playerId={}, count={}", playerId, unreadCount);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", unreadCount));
            
        } catch (Exception e) {
            log.error("获取未读邮件数量失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


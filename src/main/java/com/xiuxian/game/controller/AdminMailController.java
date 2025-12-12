package com.xiuxian.game.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.entity.MailAttachment;
import com.xiuxian.game.service.MailService;
import com.xiuxian.game.service.PlayerService;
import com.xiuxian.game.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员邮件控制器
 * 
 * <p>处理管理员邮件管理相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>发送单个邮件</li>
 *   <li>批量发送邮件</li>
 *   <li>查看发送历史</li>
 * </ul>
 * 
 * <p>所有接口都需要ADMIN角色权限。</p>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/mail")
@RequiredArgsConstructor
@Validated
public class AdminMailController {

    private final MailService mailService;
    private final PlayerService playerService;

    /**
     * 发送邮件给单个玩家
     * 
     * <p>管理员向指定玩家发送邮件，可以包含附件奖励。</p>
     * 
     * <p>邮件类型：</p>
     * <ul>
     *   <li>SYSTEM - 系统邮件</li>
     *   <li>REWARD - 奖励邮件</li>
     *   <li>ACTIVITY - 活动邮件</li>
     * </ul>
     * 
     * <p>附件类型：</p>
     * <ul>
     *   <li>SPIRIT_STONES - 灵石</li>
     *   <li>EXP - 经验</li>
     *   <li>ITEM - 物品</li>
     *   <li>EQUIPMENT - 装备</li>
     * </ul>
     * 
     * @param request 邮件发送请求
     * @return 发送结果
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendMail(@Valid @RequestBody SendMailRequest request) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员发送邮件: adminId={}, playerId={}, title={}", 
                    adminId, request.getPlayerId(), request.getTitle());
            
            // 构建附件列表
            List<MailAttachment> attachments = buildAttachments(request.getAttachments());
            
            // 计算过期时间（默认7天）
            LocalDateTime expireAt = request.getExpireDays() != null && request.getExpireDays() > 0
                    ? LocalDateTime.now().plusDays(request.getExpireDays())
                    : LocalDateTime.now().plusDays(7);
            
            // 发送邮件
            mailService.sendMail(
                    request.getPlayerId(),
                    request.getTitle(),
                    request.getContent(),
                    request.getMailType(),
                    attachments,
                    expireAt
            );
            
            LogUtils.logUserAction(null, adminId, "ADMIN_SEND_MAIL", 
                    "管理员发送邮件: playerId=" + request.getPlayerId() + ", title=" + request.getTitle());
            LogUtils.logBusiness("ADMIN_MAIL", "管理员发送邮件", 
                    "adminId", adminId, "playerId", request.getPlayerId(), "mailType", request.getMailType());
            
            log.info("管理员发送邮件成功: adminId={}, playerId={}", adminId, request.getPlayerId());
            
            return ResponseEntity.ok(ApiResponse.success("发送成功", null));
            
        } catch (Exception e) {
            log.error("管理员发送邮件失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量发送邮件
     * 
     * <p>管理员向多个玩家批量发送相同内容的邮件。</p>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>全服公告邮件</li>
     *   <li>活动奖励发放</li>
     *   <li>补偿邮件</li>
     *   <li>节日祝福</li>
     * </ul>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>批量发送会异步处理，避免阻塞</li>
     *   <li>单个玩家发送失败不影响其他玩家</li>
     *   <li>邮箱已满的玩家会跳过</li>
     * </ul>
     * 
     * @param request 批量邮件发送请求
     * @return 发送结果
     */
    @PostMapping("/send-batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendBatchMail(@Valid @RequestBody SendBatchMailRequest request) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员批量发送邮件: adminId={}, playerCount={}, title={}", 
                    adminId, request.getPlayerIds().size(), request.getTitle());
            
            // 构建附件列表
            List<MailAttachment> attachments = buildAttachments(request.getAttachments());
            
            // 计算过期时间（默认7天）
            LocalDateTime expireAt = request.getExpireDays() != null && request.getExpireDays() > 0
                    ? LocalDateTime.now().plusDays(request.getExpireDays())
                    : LocalDateTime.now().plusDays(7);
            
            // 批量发送邮件
            mailService.sendBatchMail(
                    request.getPlayerIds(),
                    request.getTitle(),
                    request.getContent(),
                    request.getMailType(),
                    attachments,
                    expireAt
            );
            
            LogUtils.logUserAction(null, adminId, "ADMIN_SEND_BATCH_MAIL", 
                    "管理员批量发送邮件: playerCount=" + request.getPlayerIds().size() + ", title=" + request.getTitle());
            LogUtils.logBusiness("ADMIN_MAIL", "管理员批量发送邮件", 
                    "adminId", adminId, "playerCount", request.getPlayerIds().size(), "mailType", request.getMailType());
            
            log.info("管理员批量发送邮件成功: adminId={}, playerCount={}", 
                    adminId, request.getPlayerIds().size());
            
            return ResponseEntity.ok(ApiResponse.success("批量发送成功", null));
            
        } catch (Exception e) {
            log.error("管理员批量发送邮件失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 构建附件列表
     */
    private List<MailAttachment> buildAttachments(List<AttachmentDTO> attachmentDTOs) {
        if (attachmentDTOs == null || attachmentDTOs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<MailAttachment> attachments = new ArrayList<>();
        for (AttachmentDTO dto : attachmentDTOs) {
            MailAttachment attachment = new MailAttachment();
            attachment.setItemType(dto.getItemType());
            attachment.setItemId(dto.getItemId());
            attachment.setQuantity(dto.getQuantity());
            attachments.add(attachment);
        }
        
        return attachments;
    }

    /**
     * 发送邮件请求DTO
     */
    @Data
    public static class SendMailRequest {
        
        @NotNull(message = "玩家ID不能为空")
        private Integer playerId;
        
        @NotBlank(message = "邮件标题不能为空")
        private String title;
        
        @NotBlank(message = "邮件内容不能为空")
        private String content;
        
        @NotBlank(message = "邮件类型不能为空")
        private String mailType; // SYSTEM/REWARD/ACTIVITY
        
        private List<AttachmentDTO> attachments;
        
        private Integer expireDays; // 过期天数，默认7天
    }

    /**
     * 批量发送邮件请求DTO
     */
    @Data
    public static class SendBatchMailRequest {
        
        @NotEmpty(message = "玩家ID列表不能为空")
        private List<Integer> playerIds;
        
        @NotBlank(message = "邮件标题不能为空")
        private String title;
        
        @NotBlank(message = "邮件内容不能为空")
        private String content;
        
        @NotBlank(message = "邮件类型不能为空")
        private String mailType; // SYSTEM/REWARD/ACTIVITY
        
        private List<AttachmentDTO> attachments;
        
        private Integer expireDays; // 过期天数，默认7天
    }

    /**
     * 附件DTO
     */
    @Data
    public static class AttachmentDTO {
        
        @NotBlank(message = "附件类型不能为空")
        private String itemType; // SPIRIT_STONES/EXP/ITEM/EQUIPMENT
        
        private Integer itemId; // 物品ID或装备ID，灵石和经验时为null
        
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}

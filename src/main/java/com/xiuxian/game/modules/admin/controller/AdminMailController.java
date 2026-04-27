package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import com.xiuxian.game.modules.mail.service.MailService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 * 管理后台邮件管理 Controller
 *
 * <p>提供管理员向玩家发送系统邮件的接口，支持单发与批量发送。
 * 邮件可携带附件（灵石、经验、道具、装备等）。</p>
 * <ul>
 *   <li>单发：向指定玩家发送一封邮件</li>
 *   <li>批量发送：向多个玩家同时发送相同邮件</li>
 * </ul>
 *
 * <p>所有接口均需 ADMIN 角色权限。</p>
 *
 * @author shaun.sheng
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
    private final AdminAuthService adminAuthService;

    private Integer getCurrentAdminId() {
        return adminAuthService.getCurrentAdminId();
    }

    /**
     * 向单个玩家发送系统邮件
     *
     * <p>邮件类型说明：</p>
     * <ul>
     *   <li>SYSTEM - 系统通知邮件</li>
     *   <li>REWARD - 奖励发放邮件</li>
     *   <li>ACTIVITY - 活动邮件</li>
     * </ul>
     *
     * <p>附件类型说明：</p>
     * <ul>
     *   <li>SPIRIT_STONES - 灵石</li>
     *   <li>EXP - 经验值</li>
     *   <li>ITEM - 道具</li>
     *   <li>EQUIPMENT - 装备</li>
     * </ul>
     *
     * @param request 发送邮件请求体
     * @return 发送结果
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendMail(@Valid @RequestBody SendMailRequest request) {
        try {
            Integer adminId = getCurrentAdminId();
            
            log.info("管理员发送单封邮件: adminId={}, playerId={}, title={}", 
                    adminId, request.getPlayerId(), request.getTitle());
            
            // 构建附件列表
            List<MailAttachment> attachments = buildAttachments(request.getAttachments());
            
            // 计算过期时间（默认 7 天）
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
            
            return ResponseEntity.ok(ApiResponse.success("邮件发送成功", null));
            
        } catch (Exception e) {
            log.error("管理员发送邮件失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 向多个玩家批量发送系统邮件
     *
     * <p>批量发送时向 playerIds 列表中的每位玩家各发送一封内容相同的邮件。</p>
     * <ul>
     *   <li>支持同时向数千名玩家发送（内部异步处理）</li>
     *   <li>附件类型与单发一致</li>
     *   <li>邮件类型与单发一致</li>
     *   <li>未收取的邮件按 expireDays 自动过期</li>
     * </ul>
     *
     * <p>业务限制：</p>
     * <ul>
     *   <li>playerIds 不能为空</li>
     *   <li>批量发送的邮件内容和附件对所有玩家相同，如需差异化请多次调用单发接口</li>
     *   <li>邮件标题和正文不能为空</li>
     * </ul>
     *
     * @param request 批量发送邮件请求体
     * @return 发送结果
     */
    @PostMapping("/send-batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendBatchMail(@Valid @RequestBody SendBatchMailRequest request) {
        try {
            Integer adminId = getCurrentAdminId();
            
            log.info("管理员批量发送邮件: adminId={}, playerCount={}, title={}", 
                    adminId, request.getPlayerIds().size(), request.getTitle());
            
            // 构建附件列表
            List<MailAttachment> attachments = buildAttachments(request.getAttachments());
            
            // 计算过期时间（默认 7 天）
            LocalDateTime expireAt = request.getExpireDays() != null && request.getExpireDays() > 0
                    ? LocalDateTime.now().plusDays(request.getExpireDays())
                    : LocalDateTime.now().plusDays(7);
            
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
            
            log.info("管理员批量发送邮件成功: adminId={}, playerCount={}", adminId, request.getPlayerIds().size());
            
            return ResponseEntity.ok(ApiResponse.success("批量邮件发送成功", null));
            
        } catch (Exception e) {
            log.error("管理员批量发送邮件失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 将附件 DTO 列表转换为 MailAttachment 列表
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
     * 发送单封邮件请求 DTO
     */
    @Data
    public static class SendMailRequest {
        
        @NotNull(message = "玩家 ID 不能为空")
        private Integer playerId;
        
        @NotBlank(message = "邮件标题不能为空")
        private String title;
        
        @NotBlank(message = "邮件正文不能为空")
        private String content;
        
        @NotBlank(message = "邮件类型不能为空")
        private String mailType; // SYSTEM/REWARD/ACTIVITY
        
        private List<AttachmentDTO> attachments;
        
        private Integer expireDays;
    }

    /**
     * 批量发送邮件请求 DTO
     */
    @Data
    public static class SendBatchMailRequest {
        
        @NotEmpty(message = "玩家 ID 列表不能为空")
        private List<Integer> playerIds;
        
        @NotBlank(message = "邮件标题不能为空")
        private String title;
        
        @NotBlank(message = "邮件正文不能为空")
        private String content;
        
        @NotBlank(message = "邮件类型不能为空")
        private String mailType; // SYSTEM/REWARD/ACTIVITY
        
        private List<AttachmentDTO> attachments;
        
        private Integer expireDays;
    }

    /**
     * 邮件附件 DTO
     */
    @Data
    public static class AttachmentDTO {
        
        @NotBlank(message = "附件类型不能为空")
        private String itemType; // SPIRIT_STONES/EXP/ITEM/EQUIPMENT
        
        private Integer itemId; // 道具/装备时必填，灵石/经验时传 null
        
        @NotNull(message = "附件数量不能为空")
        private Integer quantity;
    }
}

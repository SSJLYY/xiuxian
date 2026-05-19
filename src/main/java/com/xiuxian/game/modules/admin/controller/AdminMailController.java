package com.xiuxian.game.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.mail.entity.MailAttachment;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import com.xiuxian.game.modules.mail.mapper.PlayerMailMapper;
import com.xiuxian.game.modules.mail.service.MailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/admin/mail")
@RequiredArgsConstructor
@Validated
public class AdminMailController {

    private static final List<String> ALLOWED_MAIL_TYPES = Arrays.asList("SYSTEM", "REWARD", "ACTIVITY");
    private static final List<String> ALLOWED_ATTACHMENT_TYPES = Arrays.asList("SPIRIT_STONES", "EXP", "ITEM", "EQUIPMENT");

    private final MailService mailService;
    private final AdminAuthService adminAuthService;
    private final PlayerMailMapper playerMailMapper;

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendMail(@Valid @RequestBody SendMailRequest request) {
        try {
            Integer adminId = adminAuthService.getCurrentAdminId();
            List<MailAttachment> attachments = buildAttachments(request.getAttachments());
            LocalDateTime expireAt = resolveExpireAt(request.getExpireDays());

            mailService.sendMail(
                    request.getPlayerId(),
                    request.getTitle(),
                    request.getContent(),
                    normalizeMailType(request.getMailType()),
                    attachments,
                    expireAt
            );

            LogUtils.logUserAction(null, adminId, "ADMIN_SEND_MAIL",
                    "admin send mail playerId=" + request.getPlayerId() + ", title=" + request.getTitle());
            return ResponseEntity.ok(ApiResponse.success("邮件发送成功", null));
        } catch (Exception e) {
            log.error("Admin send mail failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/send-batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MailService.BatchSendResult>> sendBatchMail(@Valid @RequestBody SendBatchMailRequest request) {
        try {
            Integer adminId = adminAuthService.getCurrentAdminId();
            List<MailAttachment> attachments = buildAttachments(request.getAttachments());
            LocalDateTime expireAt = resolveExpireAt(request.getExpireDays());

            MailService.BatchSendResult result = mailService.sendBatchMail(
                    request.getPlayerIds(),
                    request.getTitle(),
                    request.getContent(),
                    normalizeMailType(request.getMailType()),
                    attachments,
                    expireAt
            );

            LogUtils.logUserAction(null, adminId, "ADMIN_SEND_BATCH_MAIL",
                    "admin send batch mail success=" + result.getSuccessCount() + ", fail=" + result.getFailCount());

            if (result.getFailCount() > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ApiResponse.error("部分邮件发送失败", result));
            }
            return ResponseEntity.ok(ApiResponse.success("批量邮件发送成功", result));
        } catch (Exception e) {
            log.error("Admin send batch mail failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PlayerMail>>> getSystemMailList(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        try {
            Integer adminId = adminAuthService.getCurrentAdminId();
            Page<PlayerMail> pageRequest = PageUtil.createPage(page, size);
            Page<PlayerMail> result = playerMailMapper.selectPage(
                    pageRequest,
                    new QueryWrapper<PlayerMail>().eq("mail_type", "SYSTEM").orderByDesc("created_at")
            );

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_SYSTEM_MAIL_LIST",
                    "get system mail list page=" + page + ", size=" + size);
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            log.error("Get system mails failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    private LocalDateTime resolveExpireAt(Integer expireDays) {
        if (expireDays != null && expireDays > 0) {
            return LocalDateTime.now().plusDays(expireDays);
        }
        return LocalDateTime.now().plusDays(7);
    }

    private String normalizeMailType(String mailType) {
        String normalized = mailType.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_MAIL_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮件类型不合法");
        }
        return normalized;
    }

    private List<MailAttachment> buildAttachments(List<AttachmentDTO> attachmentDTOs) {
        if (attachmentDTOs == null || attachmentDTOs.isEmpty()) {
            return new ArrayList<>();
        }

        List<MailAttachment> attachments = new ArrayList<>(attachmentDTOs.size());
        for (AttachmentDTO dto : attachmentDTOs) {
            String itemType = dto.getItemType().trim().toUpperCase(Locale.ROOT);
            if (!ALLOWED_ATTACHMENT_TYPES.contains(itemType)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "附件类型不合法");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "附件数量必须大于0");
            }
            if (("ITEM".equals(itemType) || "EQUIPMENT".equals(itemType))
                    && (dto.getItemId() == null || dto.getItemId() <= 0)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "道具或装备附件必须提供有效ID");
            }

            MailAttachment attachment = new MailAttachment();
            attachment.setItemType(itemType);
            attachment.setItemId(dto.getItemId());
            attachment.setQuantity(dto.getQuantity());
            attachments.add(attachment);
        }
        return attachments;
    }

    @Data
    public static class SendMailRequest {
        @NotNull(message = "玩家ID不能为空")
        @Min(value = 1, message = "玩家ID必须大于0")
        private Integer playerId;

        @NotBlank(message = "邮件标题不能为空")
        private String title;

        @NotBlank(message = "邮件正文不能为空")
        private String content;

        @NotBlank(message = "邮件类型不能为空")
        private String mailType;

        private List<@Valid AttachmentDTO> attachments;

        @Min(value = 1, message = "过期天数必须大于0")
        private Integer expireDays;
    }

    @Data
    public static class SendBatchMailRequest {
        @NotEmpty(message = "玩家ID列表不能为空")
        private List<@NotNull @Min(value = 1, message = "玩家ID必须大于0") Integer> playerIds;

        @NotBlank(message = "邮件标题不能为空")
        private String title;

        @NotBlank(message = "邮件正文不能为空")
        private String content;

        @NotBlank(message = "邮件类型不能为空")
        private String mailType;

        private List<@Valid AttachmentDTO> attachments;

        @Min(value = 1, message = "过期天数必须大于0")
        private Integer expireDays;
    }

    @Data
    public static class AttachmentDTO {
        @NotBlank(message = "附件类型不能为空")
        private String itemType;

        private Integer itemId;

        @NotNull(message = "附件数量不能为空")
        @Min(value = 1, message = "附件数量必须大于0")
        private Integer quantity;
    }
}

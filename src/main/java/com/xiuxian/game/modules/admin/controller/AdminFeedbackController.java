package com.xiuxian.game.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.mail.entity.PlayerMail;
import com.xiuxian.game.modules.admin.service.AdminFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 玩家反馈管理控制器
 *
 * @author shaun.sheng
 */
@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;
    private final AdminAuthService adminAuthService;

    /**
     * 获取反馈列表（分页）
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PlayerMail>>> getFeedbackList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) Boolean isRead) {
        try {
            Page<PlayerMail> feedbacks = adminFeedbackService.getFeedbackList(page, size, playerId, isRead);
            return ResponseEntity.ok(ApiResponse.success("获取反馈列表成功", feedbacks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/{feedbackId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerMail>> getFeedbackDetail(@PathVariable Long feedbackId) {
        try {
            PlayerMail feedback = adminFeedbackService.getFeedbackById(feedbackId);
            if (feedback == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("反馈记录不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success("获取反馈详情成功", feedback));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 标记反馈为已读
     */
    @PostMapping("/{feedbackId}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerMail>> markAsRead(@PathVariable Long feedbackId) {
        try {
            PlayerMail feedback = adminFeedbackService.markAsRead(feedbackId);
            return ResponseEntity.ok(ApiResponse.success("已标记为已读", feedback));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除反馈
     */
    @DeleteMapping("/{feedbackId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long feedbackId) {
        try {
            boolean result = adminFeedbackService.deleteFeedback(feedbackId);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("删除反馈成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("操作失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 回复反馈
     */
    @PostMapping("/{feedbackId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> replyToFeedback(
            @PathVariable Long feedbackId,
            @RequestParam String replyContent) {
        try {
            Integer adminId = adminAuthService.getCurrentAdminId();
            boolean result = adminFeedbackService.replyToFeedback(feedbackId, replyContent, adminId);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("回复成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("回复失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}

package com.xiuxian.game.modules.announcement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import com.xiuxian.game.modules.announcement.entity.Announcement;
import com.xiuxian.game.modules.announcement.service.AnnouncementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
@Validated
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;
    private final AdminAuthService adminAuthService;

    private Integer getCurrentAdminId() {
        return adminAuthService.getCurrentAdminId();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        try {
            Integer adminId = getCurrentAdminId();
            Announcement announcement = announcementService.createAnnouncement(
                    request.getTitle(),
                    request.getContent(),
                    request.getAnnouncementType(),
                    request.getPriority(),
                    adminId,
                    request.getStartTime(),
                    request.getEndTime());

            LogUtils.logUserAction(null, adminId, "ADMIN_CREATE_ANNOUNCEMENT",
                    "创建公告: title=" + announcement.getTitle());
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "创建公告",
                    "adminId", adminId,
                    "announcementId", announcement.getId(),
                    "type", announcement.getAnnouncementType());

            return ResponseEntity.ok(ApiResponse.success("创建成功", announcement));
        } catch (Exception e) {
            log.error("创建公告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request) {
        try {
            Integer adminId = getCurrentAdminId();
            Announcement announcement = announcementService.updateAnnouncement(
                    id,
                    request.getTitle(),
                    request.getContent(),
                    request.getAnnouncementType(),
                    request.getPriority(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getIsActive());

            LogUtils.logUserAction(null, adminId, "ADMIN_UPDATE_ANNOUNCEMENT",
                    "更新公告: announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "更新公告",
                    "adminId", adminId,
                    "announcementId", id);

            return ResponseEntity.ok(ApiResponse.success("更新成功", announcement));
        } catch (Exception e) {
            log.error("更新公告失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        try {
            Integer adminId = getCurrentAdminId();
            announcementService.deleteAnnouncement(id);

            LogUtils.logUserAction(null, adminId, "ADMIN_DELETE_ANNOUNCEMENT",
                    "删除公告: announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "删除公告",
                    "adminId", adminId,
                    "announcementId", id);

            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (Exception e) {
            log.error("删除公告失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> publishAnnouncement(@PathVariable Long id) {
        try {
            Integer adminId = getCurrentAdminId();
            announcementService.publishAnnouncement(id);

            LogUtils.logUserAction(null, adminId, "ADMIN_PUBLISH_ANNOUNCEMENT",
                    "发布公告: announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "发布公告",
                    "adminId", adminId,
                    "announcementId", id);

            return ResponseEntity.ok(ApiResponse.success("发布成功", null));
        } catch (Exception e) {
            log.error("发布公告失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeAnnouncement(@PathVariable Long id) {
        try {
            Integer adminId = getCurrentAdminId();
            announcementService.revokeAnnouncement(id);

            LogUtils.logUserAction(null, adminId, "ADMIN_REVOKE_ANNOUNCEMENT",
                    "撤回公告: announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "撤回公告",
                    "adminId", adminId,
                    "announcementId", id);

            return ResponseEntity.ok(ApiResponse.success("撤回成功", null));
        } catch (Exception e) {
            log.error("撤回公告失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IPage<Announcement>>> getAllAnnouncements(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String status) {
        try {
            Integer adminId = getCurrentAdminId();
            IPage<Announcement> announcements = announcementService.getAllAnnouncements(page, size, status);

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ANNOUNCEMENT_LIST",
                    "获取公告列表: page=" + page + ", size=" + size);

            return ResponseEntity.ok(ApiResponse.success("获取成功", announcements));
        } catch (Exception e) {
            log.error("获取公告列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncementDetail(@PathVariable Long id) {
        try {
            Integer adminId = getCurrentAdminId();
            Announcement announcement = announcementService.getAnnouncementByIdForAdmin(id);

            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ANNOUNCEMENT_DETAIL",
                    "查看公告详情: announcementId=" + id);

            return ResponseEntity.ok(ApiResponse.success("获取成功", announcement));
        } catch (Exception e) {
            log.error("获取公告详情失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAnnouncementStats() {
        try {
            AnnouncementService.AnnouncementStats stats = announcementService.getAnnouncementStats();
            Map<String, Long> result = new HashMap<>();
            result.put("totalCount", stats.getTotalCount());
            result.put("publishedCount", stats.getPublishedCount());
            result.put("draftCount", stats.getDraftCount());
            result.put("revokedCount", stats.getRevokedCount());
            result.put("validCount", stats.getValidCount());
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
        } catch (Exception e) {
            log.error("获取公告统计失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Data
    public static class CreateAnnouncementRequest {
        @NotBlank(message = "公告标题不能为空")
        private String title;

        @NotBlank(message = "公告内容不能为空")
        private String content;

        @NotBlank(message = "公告类型不能为空")
        private String announcementType;

        @NotNull(message = "优先级不能为空")
        @Min(value = 0, message = "优先级不能小于0")
        @Max(value = 2, message = "优先级不能大于2")
        private Integer priority;

        @NotNull(message = "开始时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @NotNull(message = "结束时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }

    @Data
    public static class UpdateAnnouncementRequest {
        private String title;
        private String content;
        private String announcementType;

        @Min(value = 0, message = "优先级不能小于0")
        @Max(value = 2, message = "优先级不能大于2")
        private Integer priority;

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        private Boolean isActive;
    }
}

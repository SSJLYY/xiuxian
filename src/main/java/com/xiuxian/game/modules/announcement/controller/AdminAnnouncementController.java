package com.xiuxian.game.modules.announcement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.announcement.entity.Announcement;
import com.xiuxian.game.modules.announcement.service.AnnouncementService;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员公告控制器
 * 
 * <p>处理管理员公告管理相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>创建公告</li>
 *   <li>更新公告</li>
 *   <li>删除公告</li>
 *   <li>发布公告</li>
 *   <li>撤回公告</li>
 *   <li>获取所有公告列表（含草稿）</li>
 *   <li>获取公告统计信息</li>
 * </ul>
 * 
 * <p>所有接口都需要ADMIN角色权限�?/p>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/announcement")
@RequiredArgsConstructor
@Validated
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;
    private final PlayerService playerService;

    /**
     * 创建公告
     * 
     * <p>管理员创建新公告，初始状态为草稿�?/p>
     * 
     * <p>公告类型�?/p>
     * <ul>
     *   <li>SYSTEM - 系统公告</li>
     *   <li>MAINTENANCE - 维护公告</li>
     *   <li>ACTIVITY - 活动公告</li>
     *   <li>UPDATE - 更新公告</li>
     * </ul>
     * 
     * <p>显示类型�?/p>
     * <ul>
     *   <li>POPUP - 弹窗显示（登录时弹出�?/li>
     *   <li>SCROLL - 滚动显示（界面顶部滚动）</li>
     *   <li>LIST - 列表显示（公告列表中查看�?/li>
     * </ul>
     * 
     * <p>优先级：</p>
     * <ul>
     *   <li>0 - 普�?/li>
     *   <li>1 - 重要</li>
     *   <li>2 - 紧�?/li>
     * </ul>
     * 
     * @param request 公告创建请求
     * @return 创建的公告信�?
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员创建公�? adminId={}, title={}", adminId, request.getTitle());
            
            Announcement announcement = announcementService.createAnnouncement(
                    request.getTitle(),
                    request.getContent(),
                    request.getAnnouncementType(),
                    request.getPriority(),
                    request.getStartTime(),
                    request.getEndTime()
            );
            
            LogUtils.logUserAction(null, adminId, "ADMIN_CREATE_ANNOUNCEMENT", 
                    "管理员创建公�? title=" + request.getTitle());
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "管理员创建公�?, 
                    "adminId", adminId, "announcementId", announcement.getId(), 
                    "type", request.getAnnouncementType());
            
            log.info("管理员创建公告成�? adminId={}, announcementId={}", adminId, announcement.getId());
            
            return ResponseEntity.ok(ApiResponse.success("创建成功", announcement));
            
        } catch (Exception e) {
            log.error("管理员创建公告失�? {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新公告
     * 
     * <p>管理员更新公告信息�?/p>
     * 
     * <p>注意事项�?/p>
     * <ul>
     *   <li>可以更新草稿状态的公告</li>
     *   <li>已发布的公告也可以更新，但建议先撤回再更�?/li>
     *   <li>更新后需要重新发布才能生�?/li>
     * </ul>
     * 
     * @param id 公告ID
     * @param request 公告更新请求
     * @return 更新后的公告信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员更新公�? adminId={}, announcementId={}", adminId, id);
            
            Announcement announcement = announcementService.updateAnnouncement(
                    id,
                    request.getTitle(),
                    request.getContent(),
                    request.getAnnouncementType(),
                    request.getPriority(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getIsActive()
            );
            
            LogUtils.logUserAction(null, adminId, "ADMIN_UPDATE_ANNOUNCEMENT", 
                    "管理员更新公�? announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "管理员更新公�?, 
                    "adminId", adminId, "announcementId", id);
            
            log.info("管理员更新公告成�? adminId={}, announcementId={}", adminId, id);
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", announcement));
            
        } catch (Exception e) {
            log.error("管理员更新公告失�? announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除公告
     * 
     * <p>管理员删除公告�?/p>
     * 
     * <p>注意事项�?/p>
     * <ul>
     *   <li>删除后无法恢�?/li>
     *   <li>建议只删除草稿状态的公告</li>
     *   <li>已发布的公告建议使用撤回功能</li>
     * </ul>
     * 
     * @param id 公告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员删除公�? adminId={}, announcementId={}", adminId, id);
            
            announcementService.deleteAnnouncement(id);
            
            LogUtils.logUserAction(null, adminId, "ADMIN_DELETE_ANNOUNCEMENT", 
                    "管理员删除公�? announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "管理员删除公�?, 
                    "adminId", adminId, "announcementId", id);
            
            log.info("管理员删除公告成�? adminId={}, announcementId={}", adminId, id);
            
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            
        } catch (Exception e) {
            log.error("管理员删除公告失�? announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 发布公告
     * 
     * <p>管理员发布公告，将公告状态从草稿改为已发布�?/p>
     * 
     * <p>发布后：</p>
     * <ul>
     *   <li>公告将在有效期内对玩家可�?/li>
     *   <li>根据显示类型在不同位置展�?/li>
     *   <li>可以随时撤回</li>
     * </ul>
     * 
     * @param id 公告ID
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> publishAnnouncement(@PathVariable Integer id) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员发布公�? adminId={}, announcementId={}", adminId, id);
            
            announcementService.publishAnnouncement(id);
            
            LogUtils.logUserAction(null, adminId, "ADMIN_PUBLISH_ANNOUNCEMENT", 
                    "管理员发布公�? announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "管理员发布公�?, 
                    "adminId", adminId, "announcementId", id);
            
            log.info("管理员发布公告成�? adminId={}, announcementId={}", adminId, id);
            
            return ResponseEntity.ok(ApiResponse.success("发布成功", null));
            
        } catch (Exception e) {
            log.error("管理员发布公告失�? announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 撤回公告
     * 
     * <p>管理员撤回已发布的公告，将公告状态改为已撤回�?/p>
     * 
     * <p>撤回后：</p>
     * <ul>
     *   <li>公告将立即对玩家不可�?/li>
     *   <li>可以重新编辑后再次发�?/li>
     *   <li>撤回记录会保留在系统�?/li>
     * </ul>
     * 
     * @param id 公告ID
     * @return 撤回结果
     */
    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeAnnouncement(@PathVariable Integer id) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.info("管理员撤回公�? adminId={}, announcementId={}", adminId, id);
            
            announcementService.revokeAnnouncement(id);
            
            LogUtils.logUserAction(null, adminId, "ADMIN_REVOKE_ANNOUNCEMENT", 
                    "管理员撤回公�? announcementId=" + id);
            LogUtils.logBusiness("ADMIN_ANNOUNCEMENT", "管理员撤回公�?, 
                    "adminId", adminId, "announcementId", id);
            
            log.info("管理员撤回公告成�? adminId={}, announcementId={}", adminId, id);
            
            return ResponseEntity.ok(ApiResponse.success("撤回成功", null));
            
        } catch (Exception e) {
            log.error("管理员撤回公告失�? announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有公告列表（管理端）
     * 
     * <p>管理员查看所有公告，包括草稿、已发布、已撤回的公告�?/p>
     * 
     * <p>支持功能�?/p>
     * <ul>
     *   <li>分页查询</li>
     *   <li>按状态筛�?/li>
     *   <li>按创建时间倒序排列</li>
     * </ul>
     * 
     * @param page 页码，默�?
     * @param size 每页数量，默�?0
     * @param status 公告状态筛选（可选）：DRAFT/PUBLISHED/REVOKED
     * @return 公告列表分页数据
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IPage<Announcement>>> getAllAnnouncements(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String status) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.debug("管理员获取公告列�? adminId={}, page={}, size={}, status={}", 
                    adminId, page, size, status);
            
            IPage<Announcement> announcements = announcementService.getAllAnnouncements(page, size, status);
            
            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ANNOUNCEMENT_LIST", 
                    "管理员获取公告列�? page=" + page + ", size=" + size);
            
            log.debug("管理员获取公告列表成�? adminId={}, total={}", adminId, announcements.getTotal());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcements));
            
        } catch (Exception e) {
            log.error("管理员获取公告列表失�? {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取公告详情（管理端�?
     * 
     * <p>管理员查看公告详情，可以查看任何状态的公告�?/p>
     * 
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncementDetail(@PathVariable Long id) {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.debug("管理员获取公告详�? adminId={}, announcementId={}", adminId, id);
            
            // 管理员可以查看任何状态的公告，包括草稿和已撤回的
            Announcement announcement = announcementService.getAnnouncementById(id);
            
            LogUtils.logUserAction(null, adminId, "ADMIN_GET_ANNOUNCEMENT_DETAIL", 
                    "管理员查看公告详�? announcementId=" + id);
            
            log.debug("管理员获取公告详情成�? adminId={}, announcementId={}", adminId, id);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcement));
            
        } catch (Exception e) {
            log.error("管理员获取公告详情失�? announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取公告统计信息
     * 
     * <p>返回公告系统的统计数据，用于管理后台仪表板展示�?/p>
     * 
     * <p>统计信息包括�?/p>
     * <ul>
     *   <li>总公告数</li>
     *   <li>已发布公告数</li>
     *   <li>草稿公告�?/li>
     *   <li>已撤回公告数</li>
     *   <li>当前有效公告�?/li>
     * </ul>
     * 
     * @return 公告统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAnnouncementStats() {
        try {
            Integer adminId = playerService.getCurrentPlayerId();
            
            log.debug("管理员获取公告统�? adminId={}", adminId);
            
            AnnouncementService.AnnouncementStats stats = announcementService.getAnnouncementStats();
            
            Map<String, Long> result = new HashMap<>();
            result.put("totalCount", stats.getTotalCount());
            result.put("publishedCount", stats.getPublishedCount());
            result.put("draftCount", stats.getDraftCount());
            result.put("revokedCount", stats.getRevokedCount());
            result.put("validCount", stats.getValidCount());
            
            log.debug("管理员获取公告统计成�? adminId={}, totalCount={}", 
                    adminId, stats.getTotalCount());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", result));
            
        } catch (Exception e) {
            log.error("管理员获取公告统计失�? {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建公告请求DTO
     */
    @Data
    public static class CreateAnnouncementRequest {
        
        @NotBlank(message = "公告标题不能为空")
        private String title;
        
        @NotBlank(message = "公告内容不能为空")
        private String content;
        
        @NotBlank(message = "公告类型不能为空")
        private String announcementType; // SYSTEM/MAINTENANCE/ACTIVITY/UPDATE
        
        @NotNull(message = "优先级不能为�?)
        private Integer priority; // 0-普�?1-重要 2-紧�?
        
        @NotNull(message = "开始时间不能为�?)
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        @NotNull(message = "结束时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }

    /**
     * 更新公告请求DTO
     */
    @Data
    public static class UpdateAnnouncementRequest {
        
        private String title;
        
        private String content;
        
        private String announcementType; // SYSTEM/MAINTENANCE/ACTIVITY/UPDATE
        
        private Integer priority; // 0-普�?1-重要 2-紧�?
        
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        
        private Boolean isActive; // 是否激活（发布�?
    }
}


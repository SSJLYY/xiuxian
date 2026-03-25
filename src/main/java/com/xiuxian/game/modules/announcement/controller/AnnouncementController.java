package com.xiuxian.game.modules.announcement.controller;

import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.announcement.entity.Announcement;
import com.xiuxian.game.modules.announcement.service.AnnouncementService;
import com.xiuxian.game.modules.player.service.PlayerService;
import com.xiuxian.game.common.util.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制�?
 * 
 * <p>处理玩家公告查看相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>获取有效公告列表</li>
 *   <li>查看公告详情</li>
 *   <li>获取滚动公告</li>
 *   <li>获取弹窗公告</li>
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
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final PlayerService playerService;

    /**
     * 获取公告列表
     * 
     * <p>返回所有有效的公告列表，按优先级和创建时间排序�?/p>
     * 
     * <p>公告列表包含�?/p>
     * <ul>
     *   <li>公告标题</li>
     *   <li>公告类型（系�?维护/活动/更新�?/li>
     *   <li>优先级（0-普�?1-重要 2-紧急）</li>
     *   <li>显示类型（弹�?滚动/列表�?/li>
     *   <li>开始时间和结束时间</li>
     * </ul>
     * 
     * <p>只返回状态为"已发�?且在有效期内的公告�?/p>
     * 
     * @return 有效公告列表
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Announcement>>> getAnnouncementList() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取公告列表: playerId={}", playerId);
            
            List<Announcement> announcements = announcementService.getActiveAnnouncements();
            
            LogUtils.logUserAction(null, playerId, "GET_ANNOUNCEMENT_LIST", 
                    "获取公告列表");
            
            log.debug("获取公告列表成功: playerId={}, count={}", playerId, announcements.size());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcements));
            
        } catch (Exception e) {
            log.error("获取公告列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取公告详情
     * 
     * <p>返回指定公告的详细信息，包括完整的公告内容�?/p>
     * 
     * <p>公告详情包含�?/p>
     * <ul>
     *   <li>公告标题</li>
     *   <li>公告内容（完整文本）</li>
     *   <li>公告类型</li>
     *   <li>优先�?/li>
     *   <li>显示类型</li>
     *   <li>有效�?/li>
     * </ul>
     * 
     * <p>注意事项�?/p>
     * <ul>
     *   <li>只能查看已发布的公告</li>
     *   <li>过期公告会返回错�?/li>
     * </ul>
     * 
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncementDetail(@PathVariable Long id) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取公告详情: playerId={}, announcementId={}", playerId, id);
            
            Announcement announcement = announcementService.getAnnouncementById(id);
            
            LogUtils.logUserAction(null, playerId, "GET_ANNOUNCEMENT_DETAIL", 
                    "查看公告详情: announcementId=" + id);
            
            log.debug("获取公告详情成功: playerId={}, announcementId={}", playerId, id);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcement));
            
        } catch (Exception e) {
            log.error("获取公告详情失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 标记公告为已�?
     * 
     * <p>标记指定公告为已读状态（预留接口，当前版本暂不实现已读状态跟踪）�?/p>
     * 
     * @param id 公告ID
     * @return 标记结果
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("标记公告已读: playerId={}, announcementId={}", playerId, id);
            
            // 验证公告存在且有�?
            announcementService.getAnnouncementById(id);
            
            LogUtils.logUserAction(null, playerId, "MARK_ANNOUNCEMENT_READ", 
                    "标记公告已读: announcementId=" + id);
            
            log.debug("标记公告已读成功: playerId={}, announcementId={}", playerId, id);
            
            return ResponseEntity.ok(ApiResponse.success("标记成功", null));
            
        } catch (Exception e) {
            log.error("标记公告已读失败: announcementId={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取滚动公告列表
     * 
     * <p>返回所有需要在游戏界面顶部滚动显示的公告�?/p>
     * 
     * <p>滚动公告特点�?/p>
     * <ul>
     *   <li>显示类型为SCROLL</li>
     *   <li>在游戏主界面顶部滚动播放</li>
     *   <li>通常用于重要通知和活动信�?/li>
     * </ul>
     * 
     * @return 滚动公告列表
     */
    @GetMapping("/scroll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Announcement>>> getScrollAnnouncements() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取滚动公告: playerId={}", playerId);
            
            List<Announcement> announcements = announcementService.getScrollAnnouncements();
            
            log.debug("获取滚动公告成功: playerId={}, count={}", playerId, announcements.size());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcements));
            
        } catch (Exception e) {
            log.error("获取滚动公告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取最新公�?
     * 
     * <p>返回最新的一条有效公告，用于游戏主界面显示�?/p>
     * 
     * <p>最新公告特点：</p>
     * <ul>
     *   <li>返回优先级最高的公告</li>
     *   <li>如果优先级相同，返回最新创建的公告</li>
     *   <li>只返回已发布且在有效期内的公�?/li>
     * </ul>
     * 
     * @return 最新公�?
     */
    @GetMapping("/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Announcement>> getLatestAnnouncement() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取最新公�? playerId={}", playerId);
            
            Announcement announcement = announcementService.getLatestAnnouncement();
            
            log.debug("获取最新公告成�? playerId={}, announcementId={}", 
                    playerId, announcement != null ? announcement.getId() : null);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcement));
            
        } catch (Exception e) {
            log.error("获取最新公告失�? {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取弹窗公告列表
     * 
     * <p>返回需要在玩家登录时弹窗显示的重要公告�?/p>
     * 
     * <p>弹窗公告特点�?/p>
     * <ul>
     *   <li>显示类型为POPUP</li>
     *   <li>玩家登录时自动弹�?/li>
     *   <li>通常用于重大更新、维护通知�?/li>
     *   <li>最多返�?条最新的弹窗公告</li>
     * </ul>
     * 
     * @return 弹窗公告列表
     */
    @GetMapping("/popup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Announcement>>> getPopupAnnouncements() {
        try {
            Integer playerId = playerService.getCurrentPlayerId();
            
            log.debug("获取弹窗公告: playerId={}", playerId);
            
            List<Announcement> announcements = announcementService.getPopupAnnouncements();
            
            log.debug("获取弹窗公告成功: playerId={}, count={}", playerId, announcements.size());
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", announcements));
            
        } catch (Exception e) {
            log.error("获取弹窗公告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


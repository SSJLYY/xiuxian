package com.xiuxian.game.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.entity.Announcement;
import com.xiuxian.game.exception.BusinessException;
import com.xiuxian.game.exception.ErrorCode;
import com.xiuxian.game.mapper.AnnouncementMapper;
import com.xiuxian.game.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final CacheService cacheService;

    /**
     * 创建公告（完整版本）
     */
    @Transactional
    public Announcement createAnnouncement(String title, String content, String announcementType,
                                          Integer priority, String displayType, Integer createdBy,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        log.info("创建公告: title={}, type={}, priority={}", title, announcementType, priority);
        
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setAnnouncementType(announcementType);
        announcement.setPriority(priority != null ? priority : 0);
        announcement.setDisplayType(displayType != null ? displayType : "LIST");
        announcement.setStatus("DRAFT");
        announcement.setCreatedBy(createdBy);
        announcement.setStartTime(startTime);
        announcement.setEndTime(endTime);
        
        announcementMapper.insert(announcement);
        
        // 清除公告缓存
        clearAnnouncementCache();
        
        log.info("公告创建成功: id={}", announcement.getId());
        return announcement;
    }

    /**
     * 创建公告（简化版本，用于管理员控制器）
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement createAnnouncement(String title, String content, String announcementType,
                                          Integer priority, LocalDateTime startTime, LocalDateTime endTime) {
        return createAnnouncement(title, content, announcementType, priority, "LIST", 1, startTime, endTime);
    }

    /**
     * 更新公告
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement updateAnnouncement(Integer id, String title, String content,
                                          String announcementType, Integer priority,
                                          String displayType, LocalDateTime startTime,
                                          LocalDateTime endTime) {
        log.info("更新公告: id={}", id);
        
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        
        if (title != null) {
            announcement.setTitle(title);
        }
        if (content != null) {
            announcement.setContent(content);
        }
        if (announcementType != null) {
            announcement.setAnnouncementType(announcementType);
        }
        if (priority != null) {
            announcement.setPriority(priority);
        }
        if (displayType != null) {
            announcement.setDisplayType(displayType);
        }
        if (startTime != null) {
            announcement.setStartTime(startTime);
        }
        if (endTime != null) {
            announcement.setEndTime(endTime);
        }
        
        announcementMapper.updateById(announcement);
        
        log.info("公告更新成功: id={}", id);
        return announcement;
    }

    /**
     * 更新公告（用于管理员控制器，支持Long类型ID和isActive参数）
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement updateAnnouncement(Long id, String title, String content,
                                          String announcementType, Integer priority,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          Boolean isActive) {
        log.info("更新公告: id={}", id);
        
        Announcement announcement = announcementMapper.selectById(id.intValue());
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        
        if (title != null) {
            announcement.setTitle(title);
        }
        if (content != null) {
            announcement.setContent(content);
        }
        if (announcementType != null) {
            announcement.setAnnouncementType(announcementType);
        }
        if (priority != null) {
            announcement.setPriority(priority);
        }
        if (startTime != null) {
            announcement.setStartTime(startTime);
        }
        if (endTime != null) {
            announcement.setEndTime(endTime);
        }
        if (isActive != null) {
            announcement.setStatus(isActive ? "PUBLISHED" : "DRAFT");
        }
        
        announcementMapper.updateById(announcement);
        
        log.info("公告更新成功: id={}", id);
        return announcement;
    }

    /**
     * 发布公告
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void publishAnnouncement(Integer id) {
        log.info("发布公告: id={}", id);
        
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        
        announcement.setStatus("PUBLISHED");
        announcementMapper.updateById(announcement);
        
        // 清除公告缓存
        clearAnnouncementCache();
        
        log.info("公告发布成功: id={}", id);
    }

    /**
     * 撤回公告
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void revokeAnnouncement(Integer id) {
        log.info("撤回公告: id={}", id);
        
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        
        announcement.setStatus("REVOKED");
        announcementMapper.updateById(announcement);
        
        // 清除公告缓存
        clearAnnouncementCache();
        
        log.info("公告撤回成功: id={}", id);
    }

    /**
     * 删除公告
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void deleteAnnouncement(Integer id) {
        log.info("删除公告: id={}", id);
        
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        
        announcementMapper.deleteById(id);
        
        // 清除公告缓存
        clearAnnouncementCache();
        
        log.info("公告删除成功: id={}", id);
    }

    /**
     * 删除公告（支持Long类型ID）
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void deleteAnnouncement(Long id) {
        deleteAnnouncement(id.intValue());
    }

    /**
     * 获取公告详情
     */
    public Announcement getAnnouncementById(Long id) {
        log.debug("获取公告详情: id={}", id);
        
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        
        // 检查公告有效期
        if (!isAnnouncementValid(announcement)) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_EXPIRED);
        }
        
        return announcement;
    }

    /**
     * 获取有效公告列表（别名方法，用于兼容控制器）
     */
    public List<Announcement> getActiveAnnouncements() {
        return getValidAnnouncements();
    }

    /**
     * 获取最新公告
     * 
     * @return 最新的有效公告，如果没有则返回null
     */
    public Announcement getLatestAnnouncement() {
        log.debug("获取最新公告");
        
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
               .le("start_time", now)
               .ge("end_time", now)
               .orderByDesc("priority")
               .orderByDesc("created_at")
               .last("LIMIT 1");
        
        Announcement announcement = announcementMapper.selectOne(wrapper);
        
        log.debug("获取最新公告完成: {}", announcement != null ? announcement.getId() : "无");
        
        return announcement;
    }

    /**
     * 获取有效公告列表（玩家端）
     * 使用缓存提高性能
     */
    @SuppressWarnings("unchecked")
    public List<Announcement> getValidAnnouncements() {
        log.debug("获取有效公告列表");
        
        // 先从缓存获取
        List<Announcement> cachedAnnouncements = cacheService.get(CacheService.CacheKeys.ANNOUNCEMENT_LIST);
        if (cachedAnnouncements != null) {
            log.debug("从缓存获取公告列表");
            return cachedAnnouncements;
        }
        
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
               .le("start_time", now)
               .ge("end_time", now)
               .orderByDesc("priority")
               .orderByDesc("created_at");
        
        List<Announcement> announcements = announcementMapper.selectList(wrapper);
        
        // 存入缓存，缓存10分钟
        cacheService.put(CacheService.CacheKeys.ANNOUNCEMENT_LIST, announcements, 600);
        log.debug("公告列表已缓存，有效公告数量: {}", announcements.size());
        
        return announcements;
    }

    /**
     * 获取所有公告列表（管理端）
     */
    public IPage<Announcement> getAllAnnouncements(int page, int size, String status) {
        log.debug("获取所有公告列表: page={}, size={}, status={}", page, size, status);
        
        IPage<Announcement> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("created_at");
        
        return announcementMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取所有公告列表（管理端，不带状态过滤）
     */
    public IPage<Announcement> getAllAnnouncements(int page, int size) {
        return getAllAnnouncements(page, size, null);
    }

    /**
     * 获取滚动公告列表
     * 使用缓存提高性能
     */
    @Cacheable(value = "announcements", key = "'scroll_list'")
    public List<Announcement> getScrollAnnouncements() {
        log.debug("获取滚动公告列表");
        
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
               .eq("display_type", "SCROLL")
               .le("start_time", now)
               .ge("end_time", now)
               .orderByDesc("priority")
               .orderByDesc("created_at");
        
        return announcementMapper.selectList(wrapper);
    }

    /**
     * 获取弹窗公告列表
     * 使用缓存提高性能
     */
    @Cacheable(value = "announcements", key = "'popup_list'")
    public List<Announcement> getPopupAnnouncements() {
        log.debug("获取弹窗公告列表");
        
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
               .eq("display_type", "POPUP")
               .le("start_time", now)
               .ge("end_time", now)
               .orderByDesc("priority")
               .orderByDesc("created_at")
               .last("LIMIT 5"); // 最多显示5条弹窗公告
        
        return announcementMapper.selectList(wrapper);
    }

    /**
     * 检查公告是否有效
     * 验证公告的有效期
     */
    public boolean isAnnouncementValid(Announcement announcement) {
        if (announcement == null) {
            return false;
        }
        
        if (!"PUBLISHED".equals(announcement.getStatus())) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查开始时间
        if (announcement.getStartTime() != null && now.isBefore(announcement.getStartTime())) {
            return false;
        }
        
        // 检查结束时间
        if (announcement.getEndTime() != null && now.isAfter(announcement.getEndTime())) {
            return false;
        }
        
        return true;
    }

    /**
     * 批量检查并更新过期公告状态
     * 可以通过定时任务调用
     */
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public int updateExpiredAnnouncements() {
        log.info("开始更新过期公告状态");
        
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
               .lt("end_time", now);
        
        List<Announcement> expiredAnnouncements = announcementMapper.selectList(wrapper);
        
        for (Announcement announcement : expiredAnnouncements) {
            announcement.setStatus("REVOKED");
            announcementMapper.updateById(announcement);
        }
        
        log.info("过期公告状态更新完成: count={}", expiredAnnouncements.size());
        return expiredAnnouncements.size();
    }

    /**
     * 获取公告统计信息
     */
    public AnnouncementStats getAnnouncementStats() {
        log.debug("获取公告统计信息");
        
        long totalCount = announcementMapper.selectCount(null);
        long publishedCount = announcementMapper.selectCount(
                new QueryWrapper<Announcement>().eq("status", "PUBLISHED"));
        long draftCount = announcementMapper.selectCount(
                new QueryWrapper<Announcement>().eq("status", "DRAFT"));
        long revokedCount = announcementMapper.selectCount(
                new QueryWrapper<Announcement>().eq("status", "REVOKED"));
        
        LocalDateTime now = LocalDateTime.now();
        long validCount = announcementMapper.selectCount(
                new QueryWrapper<Announcement>()
                        .eq("status", "PUBLISHED")
                        .le("start_time", now)
                        .ge("end_time", now));
        
        return new AnnouncementStats(totalCount, publishedCount, draftCount, revokedCount, validCount);
    }

    /**
     * 公告统计信息内部类
     */
    public static class AnnouncementStats {
        private final long totalCount;
        private final long publishedCount;
        private final long draftCount;
        private final long revokedCount;
        private final long validCount;

        public AnnouncementStats(long totalCount, long publishedCount, long draftCount, 
                                long revokedCount, long validCount) {
            this.totalCount = totalCount;
            this.publishedCount = publishedCount;
            this.draftCount = draftCount;
            this.revokedCount = revokedCount;
            this.validCount = validCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getPublishedCount() {
            return publishedCount;
        }

        public long getDraftCount() {
            return draftCount;
        }

        public long getRevokedCount() {
            return revokedCount;
        }

        public long getValidCount() {
            return validCount;
        }
    }

    /**
     * 清除公告缓存
     */
    private void clearAnnouncementCache() {
        cacheService.remove(CacheService.CacheKeys.ANNOUNCEMENT_LIST);
        log.debug("公告缓存已清除");
    }
}
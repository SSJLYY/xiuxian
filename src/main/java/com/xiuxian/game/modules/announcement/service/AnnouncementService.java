package com.xiuxian.game.modules.announcement.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiuxian.game.common.exception.BusinessException;
import com.xiuxian.game.common.exception.ErrorCode;
import com.xiuxian.game.common.util.PageUtil;
import com.xiuxian.game.modules.admin.service.CacheService;
import com.xiuxian.game.modules.announcement.entity.Announcement;
import com.xiuxian.game.modules.announcement.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList("SYSTEM", "MAINTENANCE", "ACTIVITY", "UPDATE"));
    private static final Set<String> ALLOWED_DISPLAY_TYPES = new HashSet<>(Arrays.asList("LIST", "SCROLL", "POPUP"));
    private static final int MIN_PRIORITY = 0;
    private static final int MAX_PRIORITY = 2;

    private final AnnouncementMapper announcementMapper;
    private final CacheService cacheService;

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement createAnnouncement(String title, String content, String announcementType,
                                           Integer priority, LocalDateTime startTime, LocalDateTime endTime) {
        return createAnnouncement(title, content, announcementType, priority, "LIST", null, startTime, endTime);
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement createAnnouncement(String title, String content, String announcementType,
                                           Integer priority, Integer createdBy,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        return createAnnouncement(title, content, announcementType, priority, "LIST", createdBy, startTime, endTime);
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement createAnnouncement(String title, String content, String announcementType,
                                           Integer priority, String displayType, Integer createdBy,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        String normalizedTitle = normalizeRequiredText(title, "公告标题不能为空");
        String normalizedContent = normalizeRequiredText(content, "公告内容不能为空");
        String normalizedType = normalizeAnnouncementType(announcementType);
        String normalizedDisplayType = normalizeDisplayType(displayType);
        int normalizedPriority = normalizePriority(priority);
        validateTimeRange(startTime, endTime);

        Announcement announcement = new Announcement();
        announcement.setTitle(normalizedTitle);
        announcement.setContent(normalizedContent);
        announcement.setAnnouncementType(normalizedType);
        announcement.setPriority(normalizedPriority);
        announcement.setDisplayType(normalizedDisplayType);
        announcement.setStatus("DRAFT");
        announcement.setCreatedBy(createdBy);
        announcement.setStartTime(startTime);
        announcement.setEndTime(endTime);
        announcementMapper.insert(announcement);

        clearAnnouncementCache();
        log.info("创建公告成功: id={}, type={}, createdBy={}", announcement.getId(), normalizedType, createdBy);
        return announcement;
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement updateAnnouncement(Integer id, String title, String content,
                                           String announcementType, Integer priority,
                                           String displayType, LocalDateTime startTime,
                                           LocalDateTime endTime) {
        Announcement announcement = getAnnouncementOrThrow(id);
        applyAnnouncementUpdates(announcement, title, content, announcementType, priority, displayType, startTime, endTime);

        int updatedRows = announcementMapper.updateById(announcement);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        clearAnnouncementCache();
        log.info("更新公告成功: id={}", id);
        return announcement;
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public Announcement updateAnnouncement(Long id, String title, String content,
                                           String announcementType, Integer priority,
                                           LocalDateTime startTime, LocalDateTime endTime,
                                           Boolean isActive) {
        Integer announcementId = toAnnouncementId(id);
        Announcement announcement = getAnnouncementOrThrow(announcementId);
        applyAnnouncementUpdates(announcement, title, content, announcementType, priority, null, startTime, endTime);

        if (isActive != null) {
            if (isActive) {
                validatePublishable(announcement);
                announcement.setStatus("PUBLISHED");
            } else {
                announcement.setStatus("DRAFT");
            }
        }

        int updatedRows = announcementMapper.updateById(announcement);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        clearAnnouncementCache();
        log.info("更新公告成功: id={}", announcementId);
        return announcement;
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void publishAnnouncement(Integer id) {
        Announcement announcement = getAnnouncementOrThrow(id);
        validatePublishable(announcement);
        announcement.setStatus("PUBLISHED");

        int updatedRows = announcementMapper.updateById(announcement);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        clearAnnouncementCache();
        log.info("发布公告成功: id={}", id);
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void publishAnnouncement(Long id) {
        publishAnnouncement(toAnnouncementId(id));
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void revokeAnnouncement(Integer id) {
        Announcement announcement = getAnnouncementOrThrow(id);
        announcement.setStatus("REVOKED");

        int updatedRows = announcementMapper.updateById(announcement);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        clearAnnouncementCache();
        log.info("撤回公告成功: id={}", id);
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void revokeAnnouncement(Long id) {
        revokeAnnouncement(toAnnouncementId(id));
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void deleteAnnouncement(Integer id) {
        int deletedRows = announcementMapper.deleteById(id);
        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }

        clearAnnouncementCache();
        log.info("删除公告成功: id={}", id);
    }

    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public void deleteAnnouncement(Long id) {
        deleteAnnouncement(toAnnouncementId(id));
    }

    public Announcement getAnnouncementByIdForAdmin(Long id) {
        return getAnnouncementOrThrow(toAnnouncementId(id));
    }

    public Announcement getAnnouncementById(Long id) {
        Announcement announcement = getAnnouncementOrThrow(toAnnouncementId(id));
        if (!isAnnouncementValid(announcement)) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_EXPIRED);
        }
        return announcement;
    }

    public List<Announcement> getActiveAnnouncements() {
        return getValidAnnouncements();
    }

    public Announcement getLatestAnnouncement() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = buildPublishedValidQuery(new QueryWrapper<>(), now)
                .orderByDesc("priority")
                .orderByDesc("created_at")
                .last("LIMIT 1");
        return announcementMapper.selectOne(wrapper);
    }

    @SuppressWarnings("unchecked")
    public List<Announcement> getValidAnnouncements() {
        List<Announcement> cachedAnnouncements = cacheService.get(CacheService.CacheKeys.ANNOUNCEMENT_LIST);
        if (cachedAnnouncements != null) {
            return cachedAnnouncements;
        }

        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = buildPublishedValidQuery(new QueryWrapper<>(), now)
                .orderByDesc("priority")
                .orderByDesc("created_at");
        List<Announcement> announcements = announcementMapper.selectList(wrapper);
        cacheService.put(CacheService.CacheKeys.ANNOUNCEMENT_LIST, announcements, 600);
        return announcements;
    }

    public IPage<Announcement> getAllAnnouncements(int page, int size, String status) {
        IPage<Announcement> pageObj = PageUtil.createPage(page, size);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status.trim());
        }
        wrapper.orderByDesc("created_at");
        return announcementMapper.selectPage(pageObj, wrapper);
    }

    public IPage<Announcement> getAllAnnouncements(int page, int size) {
        return getAllAnnouncements(page, size, null);
    }

    @Cacheable(value = "announcements", key = "'scroll_list'")
    public List<Announcement> getScrollAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = buildPublishedValidQuery(new QueryWrapper<>(), now)
                .eq("display_type", "SCROLL")
                .orderByDesc("priority")
                .orderByDesc("created_at");
        return announcementMapper.selectList(wrapper);
    }

    @Cacheable(value = "announcements", key = "'popup_list'")
    public List<Announcement> getPopupAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = buildPublishedValidQuery(new QueryWrapper<>(), now)
                .eq("display_type", "POPUP")
                .orderByDesc("priority")
                .orderByDesc("created_at")
                .last("LIMIT 5");
        return announcementMapper.selectList(wrapper);
    }

    public boolean isAnnouncementValid(Announcement announcement) {
        if (announcement == null) {
            return false;
        }
        if (!"PUBLISHED".equals(announcement.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (announcement.getStartTime() != null && now.isBefore(announcement.getStartTime())) {
            return false;
        }
        if (announcement.getEndTime() != null && now.isAfter(announcement.getEndTime())) {
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    @CacheEvict(value = "announcements", allEntries = true)
    public int updateExpiredAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED").lt("end_time", now);

        List<Announcement> expiredAnnouncements = announcementMapper.selectList(wrapper);
        for (Announcement announcement : expiredAnnouncements) {
            announcement.setStatus("REVOKED");
            announcementMapper.updateById(announcement);
        }

        if (!expiredAnnouncements.isEmpty()) {
            clearAnnouncementCache();
            log.info("已更新过期公告状态: count={}", expiredAnnouncements.size());
        }
        return expiredAnnouncements.size();
    }

    public AnnouncementStats getAnnouncementStats() {
        long totalCount = announcementMapper.selectCount(null);
        long publishedCount = announcementMapper.selectCount(new QueryWrapper<Announcement>().eq("status", "PUBLISHED"));
        long draftCount = announcementMapper.selectCount(new QueryWrapper<Announcement>().eq("status", "DRAFT"));
        long revokedCount = announcementMapper.selectCount(new QueryWrapper<Announcement>().eq("status", "REVOKED"));
        long validCount = announcementMapper.selectCount(buildPublishedValidQuery(new QueryWrapper<>(), LocalDateTime.now()));
        return new AnnouncementStats(totalCount, publishedCount, draftCount, revokedCount, validCount);
    }

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

    private Announcement getAnnouncementOrThrow(Integer id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        return announcement;
    }

    private void applyAnnouncementUpdates(Announcement announcement, String title, String content,
                                          String announcementType, Integer priority,
                                          String displayType, LocalDateTime startTime,
                                          LocalDateTime endTime) {
        LocalDateTime effectiveStart = startTime != null ? startTime : announcement.getStartTime();
        LocalDateTime effectiveEnd = endTime != null ? endTime : announcement.getEndTime();
        validateTimeRange(effectiveStart, effectiveEnd);

        if (title != null) {
            announcement.setTitle(normalizeRequiredText(title, "公告标题不能为空"));
        }
        if (content != null) {
            announcement.setContent(normalizeRequiredText(content, "公告内容不能为空"));
        }
        if (announcementType != null) {
            announcement.setAnnouncementType(normalizeAnnouncementType(announcementType));
        }
        if (priority != null) {
            announcement.setPriority(normalizePriority(priority));
        }
        if (displayType != null) {
            announcement.setDisplayType(normalizeDisplayType(displayType));
        }
        if (startTime != null) {
            announcement.setStartTime(startTime);
        }
        if (endTime != null) {
            announcement.setEndTime(endTime);
        }
    }

    private void validatePublishable(Announcement announcement) {
        validateTimeRange(announcement.getStartTime(), announcement.getEndTime());
        if (announcement.getEndTime() != null && announcement.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告已过期，无法发布");
        }
    }

    private Integer toAnnouncementId(Long id) {
        if (id == null || id <= 0 || id > Integer.MAX_VALUE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告ID无效");
        }
        return id.intValue();
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, errorMessage);
        }
        return normalized;
    }

    private String normalizeAnnouncementType(String value) {
        String normalized = normalizeRequiredText(value, "公告类型不能为空").toUpperCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公告类型不合法");
        }
        return normalized;
    }

    private String normalizeDisplayType(String value) {
        String normalized = value == null ? "LIST" : normalizeRequiredText(value, "显示类型不能为空").toUpperCase();
        if (!ALLOWED_DISPLAY_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "显示类型不合法");
        }
        return normalized;
    }

    private int normalizePriority(Integer priority) {
        if (priority == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优先级不能为空");
        }
        if (priority < MIN_PRIORITY || priority > MAX_PRIORITY) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "优先级必须在0到2之间");
        }
        return priority;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "开始时间不能晚于结束时间");
        }
    }

    private QueryWrapper<Announcement> buildPublishedValidQuery(QueryWrapper<Announcement> wrapper, LocalDateTime now) {
        wrapper.eq("status", "PUBLISHED")
                .and(query -> query.isNull("start_time").or().le("start_time", now))
                .and(query -> query.isNull("end_time").or().ge("end_time", now));
        return wrapper;
    }

    private void clearAnnouncementCache() {
        cacheService.remove(CacheService.CacheKeys.ANNOUNCEMENT_LIST);
    }
}

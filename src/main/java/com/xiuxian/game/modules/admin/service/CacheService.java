package com.xiuxian.game.modules.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 本地内存缓存服务
 * 提供轻量级的本地缓存能力，支持 TTL 自动过期
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
public class CacheService {

    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public CacheService() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredCache, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * 写入缓存，并指定 TTL（秒）
     */
    public void put(String key, Object value, long ttlSeconds) {
        long expireTime = System.currentTimeMillis() + ttlSeconds * 1000;
        cache.put(key, new CacheItem(value, expireTime));
        log.debug("写入缓存: key={}, ttl={}s", key, ttlSeconds);
    }

    /**
     * 读取缓存（带类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheItem item = cache.get(key);
        if (item == null) {
            return null;
        }

        if (item.isExpired()) {
            cache.remove(key);
            log.debug("缓存已过期: key={}", key);
            return null;
        }

        try {
            return (T) item.getValue();
        } catch (ClassCastException e) {
            log.warn("缓存类型不匹配: key={}, expectedType={}, actualType={}",
                    key, type.getSimpleName(), item.getValue().getClass().getSimpleName());
            return null;
        }
    }

    /**
     * 读取缓存（不指定类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheItem item = cache.get(key);
        if (item == null) {
            return null;
        }

        if (item.isExpired()) {
            cache.remove(key);
            log.debug("缓存已过期: key={}", key);
            return null;
        }

        return (T) item.getValue();
    }

    /**
     * 删除缓存
     */
    public void remove(String key) {
        cache.remove(key);
        log.debug("删除缓存: key={}", key);
    }

    /**
     * 判断缓存是否存在
     */
    public boolean exists(String key) {
        CacheItem item = cache.get(key);
        if (item == null) {
            return false;
        }

        if (item.isExpired()) {
            cache.remove(key);
            return false;
        }

        return true;
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.clear();
        log.info("本地缓存已全部清空");
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        int totalCount = cache.size();
        int expiredCount = 0;

        for (CacheItem item : cache.values()) {
            if (item.isExpired()) {
                expiredCount++;
            }
        }

        return new CacheStats(totalCount, totalCount - expiredCount, expiredCount);
    }

    /**
     * 清理过期缓存（定时任务）
     */
    private void cleanExpiredCache() {
        try {
            int cleanedCount = 0;
            for (String key : cache.keySet()) {
                CacheItem item = cache.get(key);
                if (item != null && item.isExpired()) {
                    cache.remove(key);
                    cleanedCount++;
                }
            }

            if (cleanedCount > 0) {
                log.debug("清理过期缓存 {} 条", cleanedCount);
            }
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
        }
    }

    /**
     * 销毁时关闭调度器
     */
    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            log.info("本地缓存服务已关闭");
        } catch (InterruptedException e) {
            log.warn("本地缓存服务关闭被中断", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 缓存数据项
     */
    private static class CacheItem {
        private final Object value;
        private final long expireTime;

        public CacheItem(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int totalCount;
        private final int validCount;
        private final int expiredCount;

        public CacheStats(int totalCount, int validCount, int expiredCount) {
            this.totalCount = totalCount;
            this.validCount = validCount;
            this.expiredCount = expiredCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getValidCount() {
            return validCount;
        }

        public int getExpiredCount() {
            return expiredCount;
        }

        @Override
        public String toString() {
            return String.format("CacheStats{total=%d, valid=%d, expired=%d}",
                    totalCount, validCount, expiredCount);
        }
    }

    /**
     * 常用缓存 Key 前缀常量
     */
    public static class CacheKeys {
        public static final String RANKING_PREFIX = "ranking:";
        public static final String ANNOUNCEMENT_LIST = "announcement:list";
        public static final String ANNOUNCEMENT_PREFIX = "announcement:";
        public static final String CONFIG_PREFIX = "config:";
        public static final String PLAYER_PROFILE_PREFIX = "player:profile:";
        public static final String GUILD_PREFIX = "guild:";
        public static final String ACTIVITY_LIST = "activity:list";

        public static String rankingKey(String type) {
            return RANKING_PREFIX + type;
        }

        public static String announcementKey(Integer id) {
            return ANNOUNCEMENT_PREFIX + id;
        }

        public static String configKey(String key) {
            return CONFIG_PREFIX + key;
        }

        public static String playerProfileKey(Integer playerId) {
            return PLAYER_PROFILE_PREFIX + playerId;
        }

        public static String guildKey(Integer guildId) {
            return GUILD_PREFIX + guildId;
        }
    }
}

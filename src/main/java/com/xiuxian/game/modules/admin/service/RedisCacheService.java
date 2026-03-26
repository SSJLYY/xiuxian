package com.xiuxian.game.modules.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.common.config.DegradeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 提供 Redis 缓存的读写操作，支持本地缓存降级
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final DegradeConfig degradeConfig;

    // 本地缓存（Redis 不可用时降级）
    private final ConcurrentHashMap<String, CacheItem> localCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Redis 可用状态标志
    private volatile boolean redisAvailable = true;

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredLocalCache, 5, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::checkRedisHealth, 30, 30, TimeUnit.SECONDS);
        log.info("Redis 缓存服务初始化完成");
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
            log.info("Redis 缓存服务已关闭");
        } catch (InterruptedException e) {
            log.warn("Redis 缓存服务关闭被中断", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 写入缓存（支持降级到本地缓存）
     */
    public void put(String key, Object value, long ttlSeconds) {
        if (!degradeConfig.isFallbackEnabled()) {
            try {
                redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
                log.debug("写入 Redis 缓存成功: {}", key);
                return;
            } catch (Exception e) {
                log.warn("写入 Redis 失败，降级到本地缓存: {}", key, e);
                redisAvailable = false;
            }
        }

        long expireTime = System.currentTimeMillis() + ttlSeconds * 1000;
        localCache.put(key, new CacheItem(value, expireTime));
        log.debug("写入本地缓存: {}", key);
    }

    /**
     * 读取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!degradeConfig.isFallbackEnabled() && redisAvailable) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    log.debug("Redis 缓存命中: {}", key);
                    return (T) value;
                }
            } catch (Exception e) {
                log.warn("读取 Redis 失败，降级到本地缓存: {}", key, e);
            }
        }

        CacheItem item = localCache.get(key);
        if (item == null) {
            return null;
        }

        if (item.isExpired()) {
            localCache.remove(key);
            log.debug("本地缓存已过期: {}", key);
            return null;
        }

        log.debug("本地缓存命中: {}", key);
        return (T) item.getValue();
    }

    /**
     * 读取缓存（带类型校验）
     */
    public <T> T get(String key, Class<T> type) {
        T value = get(key);
        if (value != null && !type.isInstance(value)) {
            log.warn("缓存类型不匹配: key={}, 期望={}, 实际={}",
                    key, type.getSimpleName(), value.getClass().getSimpleName());
            return null;
        }
        return value;
    }

    /**
     * 删除缓存
     */
    public void remove(String key) {
        if (redisAvailable) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("删除 Redis 缓存失败: {}", key, e);
            }
        }

        localCache.remove(key);
        log.debug("删除缓存: {}", key);
    }

    /**
     * 判断缓存是否存在
     */
    public boolean exists(String key) {
        if (!degradeConfig.isFallbackEnabled() && redisAvailable) {
            try {
                Boolean exists = redisTemplate.hasKey(key);
                if (Boolean.TRUE.equals(exists)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("检查 Redis 缓存存在性失败: {}", key, e);
            }
        }

        CacheItem item = localCache.get(key);
        if (item == null) {
            return false;
        }

        if (item.isExpired()) {
            localCache.remove(key);
            return false;
        }

        return true;
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        if (redisAvailable) {
            try {
                redisTemplate.delete(localCache.keySet());
                log.info("Redis 缓存已清空");
            } catch (Exception e) {
                log.warn("清空 Redis 缓存失败", e);
            }
        }

        localCache.clear();
        log.info("本地缓存已清空");
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        int localTotal = localCache.size();
        int localExpired = 0;

        for (CacheItem item : localCache.values()) {
            if (item.isExpired()) {
                localExpired++;
            }
        }

        return new CacheStats(
                redisAvailable ? "ONLINE" : "OFFLINE",
                localTotal,
                localTotal - localExpired,
                localExpired
        );
    }

    /**
     * 清理过期的本地缓存
     */
    private void cleanExpiredLocalCache() {
        try {
            int cleanedCount = 0;
            for (String key : localCache.keySet()) {
                CacheItem item = localCache.get(key);
                if (item != null && item.isExpired()) {
                    localCache.remove(key);
                    cleanedCount++;
                }
            }

            if (cleanedCount > 0) {
                log.debug("清理过期本地缓存 {} 条", cleanedCount);
            }
        } catch (Exception e) {
            log.error("清理过期本地缓存失败", e);
        }
    }

    /**
     * 检测 Redis 健康状态
     */
    private void checkRedisHealth() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            if (!redisAvailable) {
                redisAvailable = true;
                log.info("Redis 连接已恢复，切换回 Redis 模式");
            }
        } catch (Exception e) {
            if (redisAvailable) {
                redisAvailable = false;
                log.warn("Redis 连接失败，切换到本地缓存降级模式");
            }
        }
    }

    /**
     * 本地缓存项
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
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CacheStats {
        private final String redisStatus;
        private final int localTotalCount;
        private final int localValidCount;
        private final int localExpiredCount;

        @Override
        public String toString() {
            return String.format("CacheStats{redis=%s, localTotal=%d, localValid=%d, localExpired=%d}",
                    redisStatus, localTotalCount, localValidCount, localExpiredCount);
        }
    }
}

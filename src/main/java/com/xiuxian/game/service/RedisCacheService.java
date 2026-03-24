package com.xiuxian.game.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.config.DegradeConfig;
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
 * 支持 Redis + 本地缓存双层缓存
 * 当 Redis 不可用时自动降级到本地缓存
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

    // 本地缓存（Redis 不可用时的降级方案）
    private final ConcurrentHashMap<String, CacheItem> localCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Redis 可用性标记
    private volatile boolean redisAvailable = true;

    @PostConstruct
    public void init() {
        // 启动本地缓存清理任务
        scheduler.scheduleAtFixedRate(this::cleanExpiredLocalCache, 5, 5, TimeUnit.MINUTES);
        // 启动 Redis 可用性检测
        scheduler.scheduleAtFixedRate(this::checkRedisHealth, 30, 30, TimeUnit.SECONDS);
        log.info("Redis 缓存服务初始化完成");
    }

    /**
     * 销毁方法 - 关闭线程池，防止资源泄漏
     */
    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            log.info("Redis 缓存服务线程池已关闭");
        } catch (InterruptedException e) {
            log.warn("线程池关闭被中断", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 存储缓存
     */
    public void put(String key, Object value, long ttlSeconds) {
        if (!degradeConfig.isFallbackEnabled()) {
            // 尝试使用 Redis
            try {
                redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
                log.debug("Redis缓存已存储: key={}, ttl={}秒", key, ttlSeconds);
                return;
            } catch (Exception e) {
                log.warn("Redis缓存失败，降级到本地缓存: key={}", key, e);
                redisAvailable = false;
            }
        }

        // 降级到本地缓存
        long expireTime = System.currentTimeMillis() + ttlSeconds * 1000;
        localCache.put(key, new CacheItem(value, expireTime));
        log.debug("本地缓存已存储: key={}, ttl={}秒", key, ttlSeconds);
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!degradeConfig.isFallbackEnabled() && redisAvailable) {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    log.debug("Redis缓存命中: key={}", key);
                    return (T) value;
                }
            } catch (Exception e) {
                log.warn("Redis缓存读取失败: key={}", key, e);
            }
        }

        // 降级到本地缓存
        CacheItem item = localCache.get(key);
        if (item == null) {
            return null;
        }

        if (item.isExpired()) {
            localCache.remove(key);
            log.debug("本地缓存已过期并移除: key={}", key);
            return null;
        }

        log.debug("本地缓存命中: key={}", key);
        return (T) item.getValue();
    }

    /**
     * 获取缓存（带类型）
     */
    public <T> T get(String key, Class<T> type) {
        T value = get(key);
        if (value != null && !type.isInstance(value)) {
            log.warn("缓存类型不匹配: key={}, expectedType={}, actualType={}",
                    key, type.getSimpleName(), value.getClass().getSimpleName());
            return null;
        }
        return value;
    }

    /**
     * 删除缓存
     */
    public void remove(String key) {
        // 删除 Redis
        if (redisAvailable) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis缓存删除失败: key={}", key, e);
            }
        }

        // 删除本地缓存
        localCache.remove(key);
        log.debug("缓存已删除: key={}", key);
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String key) {
        if (!degradeConfig.isFallbackEnabled() && redisAvailable) {
            try {
                Boolean exists = redisTemplate.hasKey(key);
                if (Boolean.TRUE.equals(exists)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Redis缓存检查失败: key={}", key, e);
            }
        }

        // 检查本地缓存
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
        // 清空 Redis
        if (redisAvailable) {
            try {
                redisTemplate.delete(localCache.keySet());
                log.info("Redis缓存已清空");
            } catch (Exception e) {
                log.warn("Redis缓存清空失败", e);
            }
        }

        // 清空本地缓存
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
     * 清理过期本地缓存
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
                log.debug("清理过期本地缓存: {} 个", cleanedCount);
            }
        } catch (Exception e) {
            log.error("清理过期本地缓存失败", e);
        }
    }

    /**
     * 检查 Redis 健康状态
     */
    private void checkRedisHealth() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            if (!redisAvailable) {
                redisAvailable = true;
                log.info("Redis 连接已恢复");
            }
        } catch (Exception e) {
            if (redisAvailable) {
                redisAvailable = false;
                log.warn("Redis 连接已断开，降级到本地缓存", e);
            }
        }
    }

    /**
     * 缓存项
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

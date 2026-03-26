package com.xiuxian.game.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于令牌桶算法的限流工具
 */
@Slf4j
@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * 判断请求是否允许通过
     *
     * @param key 用于标识不同接口的唯一键（如IP地址或用户ID）
     * @param maxRequests 窗口期内的最大请求数
     * @param windowSeconds 统计窗口时长（秒）
     * @return 是否允许通过
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(maxRequests, windowSeconds));
        return bucket.tryConsume();
    }

    /**
     * 获取剩余令牌数
     */
    public int getRemainingTokens(String key) {
        TokenBucket bucket = buckets.get(key);
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }

    /**
     * 清理过期的限流桶
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> {
            TokenBucket bucket = entry.getValue();
            return now - bucket.getLastRefillTime() > bucket.getWindowMillis() * 2;
        });
    }

    /**
     * 令牌桶内部类
     */
    private static class TokenBucket {
        private final int capacity;
        private final long windowMillis;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefillTime;

        public TokenBucket(int capacity, int windowSeconds) {
            this.capacity = capacity;
            this.windowMillis = windowSeconds * 1000L;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }

        public boolean tryConsume() {
            refill();
            return tokens.getAndDecrement() > 0;
        }

        public int getAvailableTokens() {
            refill();
            return Math.max(0, tokens.get());
        }

        public long getLastRefillTime() {
            return lastRefillTime.get();
        }

        public long getWindowMillis() {
            return windowMillis;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long lastRefill = lastRefillTime.get();

            if (now - lastRefill >= windowMillis) {
                // 时间窗口已过，重新填充令牌桶
                if (lastRefillTime.compareAndSet(lastRefill, now)) {
                    tokens.set(capacity);
                }
            }
        }
    }
}

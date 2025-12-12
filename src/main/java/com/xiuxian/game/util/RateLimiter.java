package com.xiuxian.game.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于内存的令牌桶限流器
 */
@Slf4j
@Component
public class RateLimiter {
    
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * 检查是否允许请求
     * 
     * @param key 限流键（通常是用户ID或IP地址）
     * @param maxRequests 最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @return 是否允许请求
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
     * 清理过期的令牌桶
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> {
            TokenBucket bucket = entry.getValue();
            return now - bucket.getLastRefillTime() > bucket.getWindowMillis() * 2;
        });
    }
    
    /**
     * 令牌桶实现
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
                // 时间窗口已过，重置令牌桶
                if (lastRefillTime.compareAndSet(lastRefill, now)) {
                    tokens.set(capacity);
                }
            }
        }
    }
}
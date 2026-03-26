package com.xiuxian.game.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类
 * 提供常用的缓存操作方法
 *
 * @author shaun.sheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基础操作 ====================

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}", key, e);
        }
    }

    /**
     * 设置缓存并设置过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}, timeout={}", key, timeout, e);
        }
    }

    /**
     * 设置缓存并设置过期时间（秒）
     */
    public void setEx(String key, Object value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存并设置过期时间（分钟）
     */
    public void setEx(String key, Object value, long minutes, boolean isMinute) {
        set(key, value, minutes, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return (T) value;
        } catch (Exception e) {
            log.error("缓存获取失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("缓存删除失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 批量删除缓存
     */
    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("批量缓存删除失败", e);
            return 0L;
        }
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
             log.error("缓存检查失败: key={}", key, e);
            return false;
        }
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取锁
     *
     * @param key     锁key
     * @param value   锁值（建议使用UUID）
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否获取成功
     */
    public Boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("获取锁失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 释放锁
     * 注意：需要匹配value，防止误删他人的锁
     */
    public Boolean unlock(String key, String value) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(new org.springframework.data.redis.core.script.DefaultRedisScript<>(script), java.util.Collections.singletonList(key), value);
            return true;
        } catch (Exception e) {
            log.error("释放锁失败: key={}", key, e);
            return false;
        }
    }

    // ==================== 排行榜相关====================

    /**
     * 有序集合添加成员
     */
    public Boolean zAdd(String key, Object member, double score) {
        try {
            return redisTemplate.opsForZSet().add(key, member, score);
        } catch (Exception e) {
            log.error("有序集合添加失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 有序集合获取排名（从小到大）
     */
    public Long zRank(String key, Object member) {
        try {
            return redisTemplate.opsForZSet().rank(key, member);
        } catch (Exception e) {
            log.error("有序集合排名查询失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 有序集合获取排名（从大到小）
     */
    public Long zRevRank(String key, Object member) {
        try {
            return redisTemplate.opsForZSet().reverseRank(key, member);
        } catch (Exception e) {
            log.error("有序集合排名查询失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 有序集合获取指定范围的成员
     */
    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.error("有序集合范围查询失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 有序集合获取指定范围的成员（从大到小）
     */
    public Set<Object> zRevRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.error("有序集合范围查询失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 有序集合获取成员的分数
     */
    public Double zScore(String key, Object member) {
        try {
            return redisTemplate.opsForZSet().score(key, member);
        } catch (Exception e) {
            log.error("有序集合分数查询失败: key={}", key, e);
            return null;
        }
    }

    // ==================== Hash 操作 ====================

    /**
     * Hash 设置值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Hash设置失败: key={}, field={}", key, field, e);
        }
    }

    /**
     * Hash 获取值
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        try {
            return (T) redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Hash获取失败: key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * Hash 删除字段
     */
    public Long hDel(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("Hash删除失败: key={}", key, e);
            return 0L;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 按前缀删除缓存
     */
    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("按前缀删除缓存: prefix={}, count={}", prefix, keys.size());
            }
        } catch (Exception e) {
            log.error("按前缀删除缓存失败: prefix={}", prefix, e);
        }
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取剩余过期时间
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取过期时间失败: key={}", key, e);
            return -1L;
        }
    }
}


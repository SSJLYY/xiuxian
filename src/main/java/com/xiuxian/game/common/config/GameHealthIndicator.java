package com.xiuxian.game.common.config;

import com.xiuxian.game.modules.admin.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 游戏应用健康检查指示器
 *
 * @author shaun.sheng
 */
@RequiredArgsConstructor
@Component("gameHealth")
public class GameHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheService redisCacheService;

    @Override
    public Health health() {
        try {
            // 检�?Redis 连接
            Boolean redisOk = checkRedis();

            if (Boolean.TRUE.equals(redisOk)) {
                return Health.up()
                        .withDetail("redis", "OK")
                        .withDetail("cache", "OK")
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "ERROR")
                        .withDetail("cache", "DEGRADED")
                        .withDetail("reason", "Redis connection failed, using local cache")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private Boolean checkRedis() {
        try {
            return redisTemplate.getConnectionFactory() != null &&
                    redisTemplate.getConnectionFactory().getConnection().ping() != null;
        } catch (Exception e) {
            return false;
        }
    }
}



package com.xiuxian.game.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置类
 * 注册 Redis 缓存管理器，支持 Session 分布式存储
 *
 * <p>安全说明：使用 BasicPolymorphicTypeValidator 替代
 * LaissezFaireSubTypeValidator，限制反序列化类型范围，防止 RCE 攻击。</p>
 *
 * @author shaun.sheng
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 构建安全的 Jackson2JsonRedisSerializer（供 redisTemplate 和 cacheManager 共用）
     * 使用 BasicPolymorphicTypeValidator 限制允许反序列化的包，避免 RCE 风险。
     */
    private Jackson2JsonRedisSerializer<Object> buildJsonSerializer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("com.xiuxian.game.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.math.")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(mapper);
        return serializer;
    }

    /**
     * RedisTemplate 缓存配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Object> jsonSerializer = buildJsonSerializer();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key 使用 String 序列化器
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value 使用 JSON 序列化器
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate 初始化完成");
        return template;
    }

    /**
     * CacheManager 缓存配置 - 支持不同缓存使用不同TTL和前缀
     *
     * <p>TTL 说明：
     * <ul>
     *   <li>tokenCache：2h，与 JWT accessToken 过期时间对齐（application.properties: jwt.expiration=7200s）</li>
     *   <li>rankingCache：5m，高频变更场景适当缩短</li>
     *   <li>auctionCache：1m，拍卖行实时性要求高</li>
     * </ul>
     * </p>
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = buildJsonSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 玩家数据缓存 - 30分钟TTL
        cacheConfigurations.put("playerCache", defaultConfig
                .entryTtl(Duration.ofMinutes(30))
                .prefixCacheNameWith("xiuxian:player:"));

        // 用户Token缓存 - 2小时TTL（与 JWT accessToken 过期时间对齐）
        cacheConfigurations.put("tokenCache", defaultConfig
                .entryTtl(Duration.ofHours(2))
                .prefixCacheNameWith("xiuxian:token:"));

        // 排行榜数据缓存 - 5分钟TTL
        cacheConfigurations.put("rankingCache", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("xiuxian:ranking:"));

        // 拍卖行数据缓存 - 1分钟TTL
        cacheConfigurations.put("auctionCache", defaultConfig
                .entryTtl(Duration.ofMinutes(1))
                .prefixCacheNameWith("xiuxian:auction:"));

        // 战斗数据缓存 - 10秒TTL（频繁更新）
        cacheConfigurations.put("combatCache", defaultConfig
                .entryTtl(Duration.ofSeconds(10))
                .prefixCacheNameWith("xiuxian:combat:"));

        // 游戏配置缓存 - 1小时TTL
        cacheConfigurations.put("configCache", defaultConfig
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("xiuxian:config:"));

        // NPC对话数据缓存 - 10分钟TTL
        cacheConfigurations.put("narrativeCache", defaultConfig
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("xiuxian:narrative:"));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}

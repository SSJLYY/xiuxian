package com.xiuxian.game.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
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
 * Redis 配置类
 * 支持缓存、分布式锁、Session共享
 *
 * @author shaun.sheng
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * RedisTemplate 配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // JSON 序列化配置
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key 使用 String 序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value 使用 JSON 序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate 初始化完成");
        return template;
    }

    /**
     * CacheManager 配置 - 按业务划分不同缓存空间
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

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

        // 用户Token缓存 - 24小时TTL
        cacheConfigurations.put("tokenCache", defaultConfig
                .entryTtl(Duration.ofHours(24))
                .prefixCacheNameWith("xiuxian:token:"));

        // 排行榜缓存 - 5分钟TTL
        cacheConfigurations.put("rankingCache", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("xiuxian:ranking:"));

        // 拍卖行缓存 - 1分钟TTL
        cacheConfigurations.put("auctionCache", defaultConfig
                .entryTtl(Duration.ofMinutes(1))
                .prefixCacheNameWith("xiuxian:auction:"));

        // 战斗缓存 - 10秒TTL（高频计算结果）
        cacheConfigurations.put("combatCache", defaultConfig
                .entryTtl(Duration.ofSeconds(10))
                .prefixCacheNameWith("xiuxian:combat:"));

        // 游戏配置缓存 - 1小时TTL
        cacheConfigurations.put("configCache", defaultConfig
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("xiuxian:config:"));

        // NPC对话缓存 - 10分钟TTL
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

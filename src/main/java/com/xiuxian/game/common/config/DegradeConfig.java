package com.xiuxian.game.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 降级配置属性类
 * 配置降级策略前缀，通过配置文件控制游戏各模块的降级行为
 * strategies 字段仅启动时由 Spring 注入，运行时只读
 *
 * @author shaun.sheng
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "combat")
public class DegradeConfig {

    /**
     * 掉落倍率系数，1.0 = 100%不掉落
     */
    private double dropRateMultiplier = 1.0;

    /**
     * 是否启用降级策略
     */
    private boolean fallbackEnabled = false;

    /**
     * 降级策略开关（仅启动时由 @ConfigurationProperties 注入，运行时不应修改）
     */
    private Map<String, Boolean> strategies = new HashMap<>();

    /**
     * 获取降级策略的不可变视图（防止运行时意外修改）
     */
    public Map<String, Boolean> getStrategies() {
        return Collections.unmodifiableMap(strategies);
    }

    /**
     * Defensive copy prevents external mutable maps from changing runtime degrade switches.
     */
    public void setStrategies(Map<String, Boolean> strategies) {
        this.strategies = strategies == null ? new HashMap<>() : new HashMap<>(strategies);
    }

    /**
     * 战斗掉落降级开关
     */
    public boolean isCombatDropReduced() {
        return fallbackEnabled && strategies.getOrDefault("reduceDrop", false);
    }

    /**
     * 排行榜是否只读缓存
     */
    public boolean isRankingCacheOnly() {
        return fallbackEnabled && strategies.getOrDefault("rankingCacheOnly", false);
    }

    /**
     * 拍卖功能是否禁用
     */
    public boolean isAuctionDisabled() {
        return fallbackEnabled && strategies.getOrDefault("disableAuction", false);
    }

    /**
     * 战斗日志是否异步写入
     */
    public boolean isCombatLogAsync() {
        return fallbackEnabled && strategies.getOrDefault("combatLogAsync", true);
    }

    /**
     * 初始化默认降级策略
     */
    public void initDefaultStrategies() {
        strategies.put("reduceDrop", false);
        strategies.put("rankingCacheOnly", false);
        strategies.put("disableAuction", false);
        strategies.put("combatLogAsync", true);
    }
}

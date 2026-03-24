package com.xiuxian.game.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务降级开关配置
 * 用于控制各业务模块的降级策略
 *
 * @author shaun.sheng
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "combat")
public class DegradeConfig {

    /**
     * 掉落倍率（0.1 = 10%掉落率）
     */
    private double dropRateMultiplier = 1.0;

    /**
     * 是否启用降级
     */
    private boolean fallbackEnabled = false;

    /**
     * 降级策略映射
     */
    private Map<String, Boolean> strategies = new HashMap<>();

    /**
     * 战斗降级：降低掉落率
     */
    public boolean isCombatDropReduced() {
        return fallbackEnabled && strategies.getOrDefault("reduceDrop", false);
    }

    /**
     * 排行榜降级：返回缓存数据
     */
    public boolean isRankingCacheOnly() {
        return fallbackEnabled && strategies.getOrDefault("rankingCacheOnly", false);
    }

    /**
     * 拍卖行降级：关闭竞价
     */
    public boolean isAuctionDisabled() {
        return fallbackEnabled && strategies.getOrDefault("disableAuction", false);
    }

    /**
     * 战斗日志降级：异步写入
     */
    public boolean isCombatLogAsync() {
        return fallbackEnabled && strategies.getOrDefault("combatLogAsync", true);
    }

    /**
     * 初始化默认策略
     */
    public void initDefaultStrategies() {
        strategies.put("reduceDrop", false);
        strategies.put("rankingCacheOnly", false);
        strategies.put("disableAuction", false);
        strategies.put("combatLogAsync", true);
    }
}

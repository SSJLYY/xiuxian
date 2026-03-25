package com.xiuxian.game.common.util;

import com.xiuxian.game.common.config.DegradeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 服务降级工具�?
 * 提供各种降级策略的工具方�?
 *
 * @author shaun.sheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DegradeUtils {

    private final DegradeConfig degradeConfig;

    /**
     * 执行降级方法
     * 当发生异常时，返回降级结�?
     *
     * @param fallback 降级方法
     * @param <T>      返回类型
     * @return 结果
     */
    public <T> T executeWithFallback(Supplier<T> fallback) {
        return executeWithFallback(fallback, null);
    }

    /**
     * 执行降级方法
     * 当发生异常时，记录日志并返回降级结果
     *
     * @param fallback   降级方法
     * @param errorMsg   错误消息
     * @param <T>        返回类型
     * @return 结果
     */
    public <T> T executeWithFallback(Supplier<T> fallback, String errorMsg) {
        try {
            return fallback.get();
        } catch (Exception e) {
            log.warn("执行降级: {}", errorMsg != null ? errorMsg : "unknown", e);
            return null;
        }
    }

    /**
     * 计算降级后的掉落倍率
     *
     * @return 掉落倍率
     */
    public double getDropRateMultiplier() {
        if (degradeConfig.isCombatDropReduced()) {
            log.info("战斗掉落已降�? 倍率={}", degradeConfig.getDropRateMultiplier());
            return degradeConfig.getDropRateMultiplier();
        }
        return 1.0;
    }

    /**
     * 判断是否启用战斗降级
     *
     * @return 是否降级
     */
    public boolean isCombatDegraded() {
        return degradeConfig.isFallbackEnabled();
    }

    /**
     * 判断是否返回缓存的排行榜
     *
     * @return 是否返回缓存
     */
    public boolean useRankingCacheOnly() {
        return degradeConfig.isRankingCacheOnly();
    }

    /**
     * 判断是否禁用拍卖�?
     *
     * @return 是否禁用
     */
    public boolean isAuctionDisabled() {
        return degradeConfig.isAuctionDisabled();
    }

    /**
     * 判断战斗日志是否异步写入
     *
     * @return 是否异步
     */
    public boolean isCombatLogAsync() {
        return degradeConfig.isCombatLogAsync();
    }

    /**
     * 包装需要降级的方法
     *
     * @param method     原始方法
     * @param fallback   降级方法
     * @param methodName 方法名称（用于日志）
     * @param <T>        返回类型
     * @return 结果
     */
    public <T> T wrapWithDegradation(Supplier<T> method, Supplier<T> fallback, String methodName) {
        try {
            return method.get();
        } catch (Exception e) {
            log.warn("方法执行降级: {}", methodName, e);
            try {
                return fallback.get();
            } catch (Exception ex) {
                log.error("降级方法也失�? {}", methodName, ex);
                return null;
            }
        }
    }
}



package com.xiuxian.game.common.util;

import com.xiuxian.game.common.config.DegradeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 降级工具类
 * 提供降级策略统一调用接口，支持配置开关控制各模块降级行为
 *
 * @author shaun.sheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DegradeUtils {

    private final DegradeConfig degradeConfig;

    /**
     * 执行降级方法调用
     * 正常执行目标方法，异常时返回null并记录警告日志
     *
     * @param fallback 降级方法
     * @param <T>     返回值类型
     * @return 结果
     */
    public <T> T executeWithFallback(Supplier<T> fallback) {
        return executeWithFallback(fallback, null);
    }

    /**
     * 执行降级方法调用
     * 正常执行目标方法，异常时返回null并记录指定错误信息
     *
     * @param fallback   降级方法
     * @param errorMsg   错误消息
     * @param <T>        返回值类型
     * @return 结果
     */
    public <T> T executeWithFallback(Supplier<T> fallback, String errorMsg) {
        try {
            return fallback.get();
        } catch (Exception e) {
            log.warn("降级方法执行失败: {}", errorMsg != null ? errorMsg : "unknown", e);
            return null;
        }
    }

    /**
     * 获取战斗掉落降级系数
     *
     * @return 降级系数
     */
    public double getDropRateMultiplier() {
        if (degradeConfig.isCombatDropReduced()) {
            log.info("战斗掉落降级生效: 掉落系数={}", degradeConfig.getDropRateMultiplier());
            return degradeConfig.getDropRateMultiplier();
        }
        return 1.0;
    }

    /**
     * 判断是否启用战斗降级
     *
     * @return 降级状态
     */
    public boolean isCombatDegraded() {
        return degradeConfig.isFallbackEnabled();
    }

    /**
     * 判断是否只使用排行榜缓存
     *
     * @return 是否只读缓存
     */
    public boolean useRankingCacheOnly() {
        return degradeConfig.isRankingCacheOnly();
    }

    /**
     * 判断拍卖功能是否禁用
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
     * 包装方法使其支持降级
     *
     * @param method     目标方法
     * @param fallback   降级方法
     * @param methodName 方法名称
     * @param <T>        返回值类型
     * @return 结果
     */
    public <T> T wrapWithDegradation(Supplier<T> method, Supplier<T> fallback, String methodName) {
        try {
            return method.get();
        } catch (Exception e) {
            log.warn("方法执行触发降级: {}", methodName, e);
            try {
                return fallback.get();
            } catch (Exception ex) {
                log.error("降级方法执行失败: {}", methodName, ex);
                return null;
            }
        }
    }
}

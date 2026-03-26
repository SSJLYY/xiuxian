package com.xiuxian.game.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控指标配置类
 * 集成 Prometheus + Micrometer
 *
 * @author shaun.sheng
 */
@Configuration
public class MetricsConfig {

    /**
     * 注册业务指标到 Micrometer 注册表
     */
    @Bean
    public BusinessMetrics businessMetrics(MeterRegistry registry) {
        return new BusinessMetrics(registry);
    }

    /**
     * 业务指标统计类
     */
    public static class BusinessMetrics {
        private final MeterRegistry registry;

        // 战斗相关计数器
        private final AtomicLong combatTotal = new AtomicLong(0);
        private final AtomicLong combatSuccess = new AtomicLong(0);
        private final AtomicLong combatFailure = new AtomicLong(0);

        // 玩家相关计数器
        private final AtomicLong playerLoginTotal = new AtomicLong(0);
        private final AtomicLong playerRegisterTotal = new AtomicLong(0);

        // 交易相关计数器
        private final AtomicLong tradeTotal = new AtomicLong(0);
        private final AtomicLong tradeSuccess = new AtomicLong(0);

        public BusinessMetrics(MeterRegistry registry) {
            this.registry = registry;

            // 注册计数器
            registry.counter("game.combat.total", "game", "xiuxian");
            registry.counter("game.combat.success", "game", "xiuxian");
            registry.counter("game.combat.failure", "game", "xiuxian");
            registry.counter("game.player.login.total", "game", "xiuxian");
            registry.counter("game.player.register.total", "game", "xiuxian");
            registry.counter("game.trade.total", "game", "xiuxian");
            registry.counter("game.trade.success", "game", "xiuxian");

            // 注册 Gauge
            registry.gauge("game.players.online", 0);
            registry.gauge("game.cache.hit.rate", 0);
        }

        // ========== 战斗相关指标 ==========

        public void recordCombat() {
            combatTotal.incrementAndGet();
            registry.counter("game.combat.total").increment();
        }

        public void recordCombatSuccess() {
            combatSuccess.incrementAndGet();
            registry.counter("game.combat.success").increment();
        }

        public void recordCombatFailure() {
            combatFailure.incrementAndGet();
            registry.counter("game.combat.failure").increment();
        }

        public long getCombatTotal() {
            return combatTotal.get();
        }

        // ========== 玩家相关指标 ==========

        public void recordPlayerLogin() {
            playerLoginTotal.incrementAndGet();
            registry.counter("game.player.login.total").increment();
        }

        public void recordPlayerRegister() {
            playerRegisterTotal.incrementAndGet();
            registry.counter("game.player.register.total").increment();
        }

        // ========== 交易相关指标 ==========

        public void recordTrade() {
            tradeTotal.incrementAndGet();
            registry.counter("game.trade.total").increment();
        }

        public void recordTradeSuccess() {
            tradeSuccess.incrementAndGet();
            registry.counter("game.trade.success").increment();
        }

        // ========== 在线玩家数 ==========

        public void updateOnlinePlayers(int count) {
            registry.gauge("game.players.online", count);
        }

        // ========== 缓存命中率 ==========

        public void updateCacheHitRate(double rate) {
            registry.gauge("game.cache.hit.rate", rate);
        }
    }

    /**
     * 配置 Prometheus 过滤链
     */
    @Bean
    public FilterRegistrationBean<WebMvcMetricsFilter> prometheusFilterRegistration(
            MeterRegistry registry) {
        FilterRegistrationBean<WebMvcMetricsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WebMvcMetricsFilter(registry));
        registration.addUrlPatterns("/*");
        registration.setName("prometheusMetricsFilter");
        registration.setOrder(1);
        return registration;
    }
}

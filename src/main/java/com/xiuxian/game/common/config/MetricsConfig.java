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
 * 监控指标配置
 * 集成 Prometheus + Micrometer
 *
 * @author shaun.sheng
 */
@Configuration
public class MetricsConfig {

    /**
     * 自定义业务指标注�?
     * 在需要的地方注入 MeterRegistry 使用
     */
    @Bean
    public BusinessMetrics businessMetrics(MeterRegistry registry) {
        return new BusinessMetrics(registry);
    }

    /**
     * 业务指标工具�?
     */
    public static class BusinessMetrics {
        private final MeterRegistry registry;

        // 战斗相关指标
        private final AtomicLong combatTotal = new AtomicLong(0);
        private final AtomicLong combatSuccess = new AtomicLong(0);
        private final AtomicLong combatFailure = new AtomicLong(0);

        // 玩家相关指标
        private final AtomicLong playerLoginTotal = new AtomicLong(0);
        private final AtomicLong playerRegisterTotal = new AtomicLong(0);

        // 交易相关指标
        private final AtomicLong tradeTotal = new AtomicLong(0);
        private final AtomicLong tradeSuccess = new AtomicLong(0);

        public BusinessMetrics(MeterRegistry registry) {
            this.registry = registry;

            // 初始化计数器
            registry.counter("game.combat.total", "game", "xiuxian");
            registry.counter("game.combat.success", "game", "xiuxian");
            registry.counter("game.combat.failure", "game", "xiuxian");
            registry.counter("game.player.login.total", "game", "xiuxian");
            registry.counter("game.player.register.total", "game", "xiuxian");
            registry.counter("game.trade.total", "game", "xiuxian");
            registry.counter("game.trade.success", "game", "xiuxian");

            // 初始�?gauge
            registry.gauge("game.players.online", 0);
            registry.gauge("game.cache.hit.rate", 0);
        }

        // ========== 战斗指标 ==========

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

        // ========== 玩家指标 ==========

        public void recordPlayerLogin() {
            playerLoginTotal.incrementAndGet();
            registry.counter("game.player.login.total").increment();
        }

        public void recordPlayerRegister() {
            playerRegisterTotal.incrementAndGet();
            registry.counter("game.player.register.total").increment();
        }

        // ========== 交易指标 ==========

        public void recordTrade() {
            tradeTotal.incrementAndGet();
            registry.counter("game.trade.total").increment();
        }

        public void recordTradeSuccess() {
            tradeSuccess.incrementAndGet();
            registry.counter("game.trade.success").increment();
        }

        // ========== 在线人数 ==========

        public void updateOnlinePlayers(int count) {
            registry.gauge("game.players.online", count);
        }

        // ========== 缓存命中�?==========

        public void updateCacheHitRate(double rate) {
            registry.gauge("game.cache.hit.rate", rate);
        }
    }

    /**
     * 配置 Prometheus 端点
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


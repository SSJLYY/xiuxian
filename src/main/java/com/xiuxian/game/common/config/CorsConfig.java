package com.xiuxian.game.common.config;

/**
 * CORS 配置已统一迁移到 SecurityConfig.corsConfigurationSource()。
 *
 * 原 CorsFilter Bean 已移除原因：
 * 1. SecurityConfig 中使用 setAllowedOriginPatterns("*") + setAllowCredentials(true) 的正确写法，
 *    而此处使用 setAllowedOrigins("*") + setAllowCredentials(true) 违反 CORS 规范（浏览器拒绝），
 *    Spring 在高版本中会抛出 IllegalArgumentException。
 * 2. 两套 CorsFilter 同时存在时，Spring Security 的 Filter 优先级会引起 CORS 响应头重复或丢失。
 *
 * 如需调整跨域策略，请修改 SecurityConfig.corsConfigurationSource()。
 *
 * @author shaun.sheng
 */
@org.springframework.context.annotation.Configuration
public class CorsConfig {
    // 保留空类，避免 @Value("${app.cors.allowed-origins}") 等配置项未使用导致启动报错
}

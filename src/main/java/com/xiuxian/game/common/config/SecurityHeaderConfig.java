package com.xiuxian.game.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 安全响应头配置
 * 
 * <p>配置关键的安全 HTTP 响应头，防止常见 Web 攻击：</p>
 * <ul>
 *   <li>Content-Security-Policy (CSP) - 防止 XSS 攻击</li>
 *   <li>X-Frame-Options - 防止点击劫持</li>
 *   <li>X-Content-Type-Options - 防止 MIME 类型欺骗</li>
 *   <li>Strict-Transport-Security (HSTS) - 强制 HTTPS</li>
 *   <li>X-XSS-Protection - 浏览器 XSS 防护</li>
 *   <li>Referrer-Policy - 控制引荐来源</li>
 *   <li>Permissions-Policy - 控制浏览器功能权限</li>
 * </ul>
 * 
 * @author xiuxian-game-team
 * @version 1.0
 * @since 2026-04-20
 */
@Configuration
@EnableWebSecurity
public class SecurityHeaderConfig {

    /**
     * 配置安全响应头
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 使用 JWT 验证）
            .csrf(csrf -> csrf.disable())
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 静态资源公开访问
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/**/*.png",
                    "/**/*.jpg",
                    "/**/*.jpeg",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.ico"
                ).permitAll()
                // API 端点需要认证
                .requestMatchers("/api/**").permitAll() // 暂时公开，后续加上 JWT 验证
                // 管理端点需要认证
                .requestMatchers("/admin/**", "/actuator/**").hasRole("ADMIN")
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            // 配置响应头
            .headers(headers -> headers
                // 1. X-Frame-Options: DENY - 防止点击劫持
                .frameOptions(FrameOptionsConfig::deny)
                
                // 2. X-Content-Type-Options: nosniff - 防止 MIME 类型欺骗
                .contentTypeOptions(Customizer.withDefaults())
                
                // 3. X-XSS-Protection: 1; mode=block - 浏览器 XSS 保护
                .xssProtection(xss -> xss.block(true))
                
                // 4. Strict-Transport-Security (HSTS) - 强制 HTTPS
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)  // 1 年
                    .includeSubDomains(true)    // 包含子域名
                    .preload(true)              // 允许预加载
                )
                
                // 5. Content-Security-Policy (CSP) - 内容安全策略
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +                          // 默认只允许同源资源
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +  // 允许内联和 eval（开发阶段，生产环境应移除）
                        "style-src 'self' 'unsafe-inline'; " +            // 允许内联样式
                        "img-src 'self' data: https:; " +                 // 允许 base64 和 HTTPS 图片
                        "font-src 'self' data:; " +                       // 允许 base64 字体
                        "connect-src 'self' https:; " +                   // 限制 AJAX 请求
                        "object-src 'none'; " +                           // 禁用 Flash 等插件
                        "frame-ancestors 'none'; " +                      // 禁止嵌入
                        "base-uri 'self'; " +                             // 限制 <base>标签
                        "form-action 'self'"                              // 限制表单提交
                    )
                )
                
                // 6. Referrer-Policy - 控制引荐来源
                .referrerPolicy(ref -> ref.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                ))
                
                // 7. Permissions-Policy - 控制浏览器功能权限
                .permissionsPolicy(permissions -> permissions.policy(
                    "camera=(), microphone=(), geolocation=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()"
                ))
            );
        
        return http.build();
    }
}

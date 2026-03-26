package com.xiuxian.game.common.config;

import com.xiuxian.game.common.security.AdminSecurityFilter;
import com.xiuxian.game.common.security.JwtAuthenticationFilter;
import com.xiuxian.game.common.security.SecurityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityFilter securityFilter;
    private final AdminSecurityFilter adminSecurityFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(auth -> auth
                        // 静态资源和错误页面放行
                        .antMatchers(
                                "/",
                                "/**/*.html",
                                "/index.html",
                                "/login.html",
                                "/adminLogin.html",
                                "/cultivate.html",
                                "/equipment.html",
                                "/pets.html",
                                "/admin.html",
                                "/test.html",
                                "/xiuxian-game/",
                                "/xiuxian-game/**/*.html",
                                "/xiuxian-game/index.html",
                                "/xiuxian-game/login.html",
                                "/xiuxian-game/adminLogin.html",
                                "/xiuxian-game/cultivate.html",
                                "/xiuxian-game/equipment.html",
                                "/xiuxian-game/pets.html",
                                "/xiuxian-game/admin.html",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/error/**"
                        ).permitAll()

                        // 玩家认证API放行
                        .antMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/validate"
                        ).permitAll()

                        // 管理员认证API放行
                        .antMatchers(
                                "/api/admin/auth/login"
                        ).permitAll()

                        // 公开数据API放行
                        .antMatchers(
                                "/api/players/public/**",
                                "/api/skills/public/**",
                                "/api/quests/public/**",
                                "/api/equipments/public/**",
                                "/api/inventory/public/**",
                                "/api/shop/public/**"
                        ).permitAll()

                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(adminSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

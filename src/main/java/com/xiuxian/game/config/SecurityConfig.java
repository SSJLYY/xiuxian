package com.xiuxian.game.config;

import com.xiuxian.game.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public void setJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        return firewall;
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
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS
            .csrf(csrf -> csrf.disable()) // 禁用CSRF
            .sessionManagement(session -> session.disable()) // 禁用会话管理
            .authorizeRequests(auth -> auth
                // 允许静态资源匿名访问
                .antMatchers("/").permitAll()
                .antMatchers("/index.html").permitAll()
                .antMatchers("/xiuxian-game/").permitAll()
                .antMatchers("/xiuxian-game/index.html").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/fonts/**").permitAll()
                .antMatchers("/assets/**").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/error/**").permitAll()
                // 允许认证相关API匿名访问
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                // 游戏相关路径也允许匿名访问（首页展示）
                .antMatchers("/api/players/**").permitAll()
                .antMatchers("/players/**").permitAll()
                .antMatchers("/api/skills/**").permitAll()
                .antMatchers("/skills/**").permitAll()
                .antMatchers("/api/quests/**").permitAll()
                .antMatchers("/quests/**").permitAll()
                .antMatchers("/api/equipments/**").permitAll()
                .antMatchers("/equipments/**").permitAll()
                .antMatchers("/api/equipment/**").permitAll()
                .antMatchers("/equipment/**").permitAll()
                .antMatchers("/api/inventory/**").permitAll()
                .antMatchers("/inventory/**").permitAll()
                .antMatchers("/api/shop/**").permitAll()
                .antMatchers("/shop/**").permitAll()
                // 其他所有请求也需要认证（如果需要的话）
                .anyRequest().permitAll())
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        
        // 配置防火墙
        http.setSharedObject(HttpFirewall.class, httpFirewall());
        
        return http.build();
    }
}
package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.request.AdminLoginRequest;
import com.xiuxian.game.dto.response.AdminLoginResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 后台管理认证控制器
 *
 * <p>独立于游戏登录系统，提供管理员专属的认证功能。</p>
 *
 * <p>功能包括：</p>
 * <ul>
 *   <li>管理员登录</li>
 *   <li>Token验证</li>
 *   <li>管理员登出</li>
 *   <li>获取当前管理员信息</li>
 * </ul>
 *
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "${admin.cors.allowed-origins:localhost,127.0.0.1}")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    /**
     * 管理员登录
     *
     * <p>管理员使用用户名和密码登录后台管理系统。</p>
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        log.info("管理员登录: username={}", request.getUsername());
        AdminLoginResponse response = adminAuthService.login(request);
        log.info("管理员登录成功: username={}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 验证管理员token
     *
     * <p>验证管理员的JWT Token是否有效。</p>
     *
     * @param request HTTP请求
     * @return 验证响应
     */
    @GetMapping("/validate")
    public ResponseEntity<AdminLoginResponse> validateToken(HttpServletRequest request) {
        String token = extractToken(request);
        log.debug("验证管理员token");
        AdminLoginResponse response = adminAuthService.validateToken(token);
        log.debug("验证管理员token完成: valid={}", response.isSuccess());
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员登出
     *
     * <p>管理员登出后台管理系统。</p>
     *
     * @param request HTTP请求
     * @return 登出响应
     */
    @PostMapping("/logout")
    public ResponseEntity<AdminLoginResponse> logout(HttpServletRequest request) {
        String token = extractToken(request);
        log.info("管理员登出");
        AdminLoginResponse response = adminAuthService.logout(token);
        log.info("管理员登出成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前管理员信息
     *
     * <p>获取当前登录管理员的详细信息。</p>
     *
     * @param request HTTP请求
     * @return 管理员信息
     */
    @GetMapping("/me")
    public ResponseEntity<AdminLoginResponse> getCurrentAdmin(HttpServletRequest request) {
        String token = extractToken(request);
        log.debug("获取当前管理员信息");
        AdminLoginResponse response = adminAuthService.getCurrentAdmin(token);
        log.debug("获取当前管理员信息完成");
        return ResponseEntity.ok(response);
    }

    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

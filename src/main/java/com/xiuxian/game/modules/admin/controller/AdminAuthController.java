package com.xiuxian.game.modules.admin.controller;

import com.xiuxian.game.dto.request.AdminLoginRequest;
import com.xiuxian.game.dto.response.AdminLoginResponse;
import com.xiuxian.game.modules.admin.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 后台管理认证控制�?- 独立于游戏登录系�?
 */
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    /**
     * 管理员登�?
     */
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 验证管理员token
     */
    @GetMapping("/validate")
    public ResponseEntity<AdminLoginResponse> validateToken(HttpServletRequest request) {
        String token = extractToken(request);
        AdminLoginResponse response = adminAuthService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * 管理员登�?
     */
    @PostMapping("/logout")
    public ResponseEntity<AdminLoginResponse> logout(HttpServletRequest request) {
        String token = extractToken(request);
        AdminLoginResponse response = adminAuthService.logout(token);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前管理员信�?
     */
    @GetMapping("/me")
    public ResponseEntity<AdminLoginResponse> getCurrentAdmin(HttpServletRequest request) {
        String token = extractToken(request);
        AdminLoginResponse response = adminAuthService.getCurrentAdmin(token);
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

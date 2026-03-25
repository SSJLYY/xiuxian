package com.xiuxian.game.modules.player.controller;

import com.xiuxian.game.common.annotation.RateLimit;
import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.modules.player.entity.User;
import com.xiuxian.game.modules.player.service.AuthService;
import com.xiuxian.game.modules.player.service.PlayerLoginLogService;
import com.xiuxian.game.common.util.LogUtils;
import com.xiuxian.game.common.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PlayerLoginLogService playerLoginLogService;

    @PostMapping("/register")
    @RateLimit(keyType = RateLimit.KeyType.IP, maxRequests = 3, windowSeconds = 300, message = "注册过于频繁，请5分钟后再�?)
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_REGISTER_ATTEMPT", request.getUsername(), "用户尝试注册", clientIp);

            LoginResponse response = authService.register(request);

            LogUtils.logUserAction(request.getUsername(), "REGISTER", "用户注册成功");
            LogUtils.logSecurity("USER_REGISTER_SUCCESS", request.getUsername(), "用户注册成功", clientIp);

            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_REGISTER_FAILED", request.getUsername(), "用户注册失败: " + e.getMessage(), clientIp);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @RateLimit(keyType = RateLimit.KeyType.IP, maxRequests = 5, windowSeconds = 60, message = "登录过于频繁，请1分钟后再�?)
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_LOGIN_ATTEMPT", request.getUsername(), "用户尝试登录", clientIp);

            LoginResponse response = authService.login(request);

            if (response.getPlayer() != null && response.getPlayer().getId() != null) {
                playerLoginLogService.recordLogin(response.getPlayer().getId(), httpRequest);
            }

            LogUtils.logUserAction(request.getUsername(), "LOGIN", "用户登录成功");
            LogUtils.logSecurity("USER_LOGIN_SUCCESS", request.getUsername(), "用户登录成功", clientIp);

            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_LOGIN_FAILED", request.getUsername(), "用户登录失败: " + e.getMessage(), clientIp);
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            LogUtils.logUserAction(user.getUsername(), "GET_USER_INFO", "获取用户信息");
            return ResponseEntity.ok(ApiResponse.success("获取当前用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        try {
            User currentUser = authService.getCurrentUser();
            String username = currentUser != null ? currentUser.getUsername() : "unknown";

            authService.logout();

            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logUserAction(username, "LOGOUT", "用户登出");
            LogUtils.logSecurity("USER_LOGOUT", username, "用户登出", clientIp);

            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<User>> validateToken() {
        try {
            User currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Token有效", currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token无效"));
        }
    }
}


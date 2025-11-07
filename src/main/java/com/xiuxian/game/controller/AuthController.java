package com.xiuxian.game.controller;

import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.service.AuthService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            // 从UserDetails中获取用户名，然后从数据库获取完整的User实体
            String username = userDetails.getUsername();
            System.out.println("=== DEBUG: AuthController.getCurrentUser ===");
            System.out.println("从SecurityContext获取的用户名: " + username);
            
            User user = authService.getUserByUsername(username);
            System.out.println("从数据库获取的用户: " + user);
            System.out.println("用户ID: " + user.getId());
            System.out.println("用户名: " + user.getUsername());
            System.out.println("邮箱: " + user.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("获取当前用户成功", user));
        } catch (Exception e) {
            System.out.println("=== DEBUG: AuthController.getCurrentUser ERROR ===");
            System.out.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
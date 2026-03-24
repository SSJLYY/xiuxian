package com.xiuxian.game.controller;

import com.xiuxian.game.annotation.RateLimit;
import com.xiuxian.game.dto.request.LoginRequest;
import com.xiuxian.game.dto.request.RegisterRequest;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.dto.response.LoginResponse;
import com.xiuxian.game.entity.User;
import com.xiuxian.game.service.AuthService;
import com.xiuxian.game.service.PlayerLoginLogService;
import com.xiuxian.game.util.LogUtils;
import com.xiuxian.game.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 * 
 * <p>处理用户认证相关的HTTP请求，包括：</p>
 * <ul>
 *   <li>用户注册</li>
 *   <li>用户登录</li>
 *   <li>用户登出</li>
 *   <li>获取当前用户信息</li>
 *   <li>Token验证</li>
 * </ul>
 * 
 * <p>所有接口都返回统一的ApiResponse格式，包含成功标识、消息和数据。</p>
 * 
 * <p>安全说明：</p>
 * <ul>
 *   <li>注册和登录接口无需认证</li>
 *   <li>其他接口需要JWT Token认证</li>
 *   <li>密码使用BCrypt加密存储</li>
 *   <li>登录失败会记录安全日志</li>
 * </ul>
 * 
 * @author xiuxian-game-team
 * @version 1.0.0
 * @since 2024-11-28
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 认证服务
     */
    private final AuthService authService;
    
    /**
     * 玩家登录日志服务
     */
    private final PlayerLoginLogService playerLoginLogService;

    /**
     * 用户注册
     * 
     * <p>创建新用户账号并自动创建对应的玩家档案。</p>
     * 
     * <p>注册流程：</p>
     * <ol>
     *   <li>验证用户名和邮箱是否已存在</li>
     *   <li>创建用户账号（密码加密存储）</li>
     *   <li>创建玩家档案（初始属性和资源）</li>
     *   <li>发放新手物品</li>
     *   <li>生成JWT Token</li>
     *   <li>返回登录响应</li>
     * </ol>
     * 
     * @param request 注册请求，包含用户名、密码、邮箱、昵称
     * @param httpRequest HTTP请求对象，用于获取客户端IP
     * @return 注册响应，包含JWT Token和用户信息
     */
    @PostMapping("/register")
    @RateLimit(keyType = RateLimit.KeyType.IP, maxRequests = 3, windowSeconds = 300, message = "注册过于频繁，请5分钟后再试")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("收到用户注册请求: 用户名={}, 邮箱={}, 昵称={}", 
                    request.getUsername(), request.getEmail(), request.getNickname());
            
            // 【P1-4 重构】使用统一的 RequestUtils.getClientIp() 替代私有方法
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_REGISTER_ATTEMPT", request.getUsername(), 
                    "用户尝试注册", clientIp);
            
            // 执行注册逻辑
            LoginResponse response = authService.register(request);
            
            // 记录注册成功日志
            LogUtils.logUserAction(request.getUsername(), "REGISTER", "用户注册成功");
            LogUtils.logSecurity("USER_REGISTER_SUCCESS", request.getUsername(), 
                    "用户注册成功", clientIp);
            
            log.info("用户注册成功: 用户名={}", request.getUsername());
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
            
        } catch (Exception e) {
            // 记录注册失败日志
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_REGISTER_FAILED", request.getUsername(), 
                    "用户注册失败: " + e.getMessage(), clientIp);
            
            log.warn("用户注册失败: 用户名={}, 错误={}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     * 
     * <p>验证用户凭据并生成JWT Token。</p>
     * 
     * <p>登录流程：</p>
     * <ol>
     *   <li>验证用户名和密码</li>
     *   <li>检查账号状态（是否锁定等）</li>
     *   <li>生成JWT Token</li>
     *   <li>更新最后登录时间</li>
     *   <li>返回用户信息和Token</li>
     * </ol>
     * 
     * @param request 登录请求，包含用户名和密码
     * @param httpRequest HTTP请求对象，用于获取客户端IP
     * @return 登录响应，包含JWT Token和用户信息
     */
    @PostMapping("/login")
    @RateLimit(keyType = RateLimit.KeyType.IP, maxRequests = 5, windowSeconds = 60, message = "登录过于频繁，请1分钟后再试")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("收到用户登录请求: 用户名={}", request.getUsername());
            
            // 记录登录尝试日志
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_LOGIN_ATTEMPT", request.getUsername(), 
                    "用户尝试登录", clientIp);
            
            // 执行登录逻辑
            LoginResponse response = authService.login(request);
            
            // 记录玩家登录日志
            if (response.getPlayer() != null && response.getPlayer().getId() != null) {
                playerLoginLogService.recordLogin(response.getPlayer().getId(), httpRequest);
            }
            
            // 记录登录成功日志
            LogUtils.logUserAction(request.getUsername(), "LOGIN", "用户登录成功");
            LogUtils.logSecurity("USER_LOGIN_SUCCESS", request.getUsername(), 
                    "用户登录成功", clientIp);
            
            log.info("用户登录成功: 用户名={}", request.getUsername());
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
            
        } catch (Exception e) {
            // 记录登录失败日志
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logSecurity("USER_LOGIN_FAILED", request.getUsername(), 
                    "用户登录失败: " + e.getMessage(), clientIp);
            
            log.warn("用户登录失败: 用户名={}, 错误={}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
        }
    }

    /**
     * 获取当前用户信息
     * 
     * <p>根据JWT Token获取当前登录用户的详细信息。</p>
     * 
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        try {
            log.debug("获取当前用户信息");
            
            User user = authService.getCurrentUser();
            
            // 记录用户操作日志
            LogUtils.logUserAction(user.getUsername(), "GET_USER_INFO", "获取用户信息");
            
            log.debug("获取当前用户信息成功: 用户名={}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("获取当前用户成功", user));
            
        } catch (Exception e) {
            log.error("获取当前用户信息失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登出
     * 
     * <p>清除用户的认证状态，使当前Token失效。</p>
     * 
     * @param httpRequest HTTP请求对象，用于获取客户端IP
     * @return 登出结果
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        try {
            // 获取当前用户信息
            User currentUser = authService.getCurrentUser();
            String username = currentUser != null ? currentUser.getUsername() : "unknown";
            
            log.info("用户请求登出: 用户名={}", username);
            
            // 执行登出逻辑
            authService.logout();
            
            // 记录登出日志
            String clientIp = RequestUtils.getClientIp(httpRequest);
            LogUtils.logUserAction(username, "LOGOUT", "用户登出");
            LogUtils.logSecurity("USER_LOGOUT", username, "用户登出", clientIp);
            
            log.info("用户登出成功: 用户名={}", username);
            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
            
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 验证Token有效性
     * 
     * <p>检查当前请求的JWT Token是否有效。</p>
     * <p>如果Token无效，Spring Security会在到达此方法前就拦截请求。</p>
     * 
     * @return Token验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<User>> validateToken() {
        try {
            log.debug("验证Token有效性");
            
            // 如果能够执行到这里，说明token有效
            // 因为Spring Security已经验证过了
            // 返回当前用户信息
            User currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Token有效", currentUser));
            
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Token无效"));
        }
    }
    
    // 【P1-4 已重构】获取客户端 IP 逻辑已迁移至 RequestUtils.getClientIp()
    // 原私有方法已删除，所有调用处已替换为 RequestUtils.getClientIp(request)
}
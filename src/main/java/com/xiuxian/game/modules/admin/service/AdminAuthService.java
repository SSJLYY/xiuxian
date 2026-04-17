package com.xiuxian.game.modules.admin.service;

import com.xiuxian.game.dto.request.AdminLoginRequest;
import com.xiuxian.game.dto.response.AdminLoginResponse;
import com.xiuxian.game.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理员认证服务
 * 负责管理员登录、Token 验证与注销
 * 密码验证使用 BCrypt（passwordEncoder.matches），配置中存储 BCrypt 哈希值
 *
 * @author shaun.sheng
 */
@Slf4j
@Service
public class AdminAuthService {

    @Value("${spring.security.user.name}")
    private String adminUsername;

    @Value("${spring.security.user.password}")
    private String adminPassword; // BCrypt 哈希值，如 $2a$10$xxxx

    @Value("${spring.security.admin.email:admin@xiuxian.local}")
    private String adminEmail;

    @Value("${spring.security.admin.fixed-id:1}")
    private Long adminFixedId;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ConcurrentHashMap<String, String> adminTokenCache = new ConcurrentHashMap<>();

    /**
     * 管理员登录
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录结果（含 Token）
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return AdminLoginResponse.error("用户名不能为空");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return AdminLoginResponse.error("密码不能为空");
            }

            // 校验用户名
            if (!adminUsername.equals(request.getUsername().trim())) {
                return AdminLoginResponse.error("用户名或密码错误");
            }

            // BCrypt 密码验证（adminPassword 配置中存储的是哈希值）
            if (!passwordEncoder.matches(request.getPassword(), adminPassword)) {
                return AdminLoginResponse.error("用户名或密码错误");
            }

            // 生成 Token
            String token = jwtTokenProvider.generateToken("admin_" + adminUsername);
            
            // 缓存 Token
            adminTokenCache.put(token, adminUsername);

            // 构建返回数据
            AdminLoginResponse.AdminUser adminUser = new AdminLoginResponse.AdminUser(
                adminFixedId,
                adminUsername,
                adminEmail,
                "ADMIN"
            );

            AdminLoginResponse.AdminData data = new AdminLoginResponse.AdminData(token, adminUser);

            return AdminLoginResponse.success("登录成功", data);

        } catch (Exception e) {
            log.error("管理员登录异常", e);
            return AdminLoginResponse.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 验证管理员 Token
     *
     * @param token 待验证的 Token
     * @return 验证结果
     */
    public AdminLoginResponse validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return AdminLoginResponse.error("Token 不能为空");
            }

            // 校验 Token 签名与有效期
            if (!jwtTokenProvider.validateToken(token)) {
                adminTokenCache.remove(token);
                return AdminLoginResponse.error("Token 已过期或无效");
            }

            // 从缓存中获取用户名
            String cachedUsername = adminTokenCache.get(token);
            if (cachedUsername == null) {
                return AdminLoginResponse.error("Token 不存在或已注销，请重新登录");
            }

            if (!adminUsername.equals(cachedUsername)) {
                adminTokenCache.remove(token);
                return AdminLoginResponse.error("Token 所属用户不匹配");
            }

            // 构建返回数据
            AdminLoginResponse.AdminUser adminUser = new AdminLoginResponse.AdminUser(
                adminFixedId,
                adminUsername,
                adminEmail,
                "ADMIN"
            );

            AdminLoginResponse.AdminData data = new AdminLoginResponse.AdminData(token, adminUser);

            return AdminLoginResponse.success("Token 有效", data);
        } catch (Exception e) {
            log.error("Token 验证异常", e);
            return AdminLoginResponse.error("Token 验证失败: " + e.getMessage());
        }
    }

    /**
     * 管理员注销
     *
     * @param token 当前登录 Token
     * @return 注销结果
     */
    public AdminLoginResponse logout(String token) {
        try {
            if (token != null) {
                adminTokenCache.remove(token);
            }
            return AdminLoginResponse.success("注销成功", null);
        } catch (Exception e) {
            log.error("管理员注销异常", e);
            return AdminLoginResponse.error("注销失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录管理员信息
     *
     * @param token 当前登录 Token
     * @return 管理员信息
     */
    public AdminLoginResponse getCurrentAdmin(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return AdminLoginResponse.error("Token 不能为空");
            }

            // 通过 Token 验证获取管理员信息
            AdminLoginResponse validateResult = validateToken(token);
            if (!validateResult.isSuccess()) {
                return validateResult;
            }

            return validateResult;

        } catch (Exception e) {
            log.error("获取当前管理员信息异常", e);
            return AdminLoginResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 通过 Token 获取管理员用户名
     *
     * @param token 登录 Token
     * @return 管理员用户名，Token 无效时返回 null
     */
    public String getAdminUsernameByToken(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return null;
        }
        return adminTokenCache.get(token);
    }

    /**
     * 校验 Token 是否为有效的管理员 Token
     *
     * @param token 待校验 Token
     * @return true 表示有效
     */
    public boolean isValidAdminToken(String token) {
        if (token == null) {
            return false;
        }
        
        String cachedUsername = adminTokenCache.get(token);
        if (cachedUsername == null) {
            return false;
        }

        return adminUsername.equals(cachedUsername);
    }
}

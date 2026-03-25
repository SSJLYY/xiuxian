package com.xiuxian.game.modules.admin.service;

import com.xiuxian.game.dto.request.AdminLoginRequest;
import com.xiuxian.game.dto.response.AdminLoginResponse;
import com.xiuxian.game.common.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 后台管理认证服务 - 独立于游戏登录系�?
 */
@Slf4j
@Service
public class AdminAuthService {

    @Value("${spring.security.user.name}")
    private String adminUsername;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 管理员token缓存 (生产环境建议使用Redis)
    private final ConcurrentHashMap<String, String> adminTokenCache = new ConcurrentHashMap<>();

    /**
     * 管理员登�?- 使用配置文件认证
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return AdminLoginResponse.error("用户名不能为�?);
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return AdminLoginResponse.error("密码不能为空");
            }

            // 验证用户名和密码是否匹配配置文件
            if (!adminUsername.equals(request.getUsername().trim())) {
                return AdminLoginResponse.error("用户名或密码错误");
            }

            if (!adminPassword.equals(request.getPassword())) {
                return AdminLoginResponse.error("用户名或密码错误");
            }

            // 生成管理员专用token
            String token = jwtTokenProvider.generateToken("admin_" + adminUsername);
            
            // 缓存token
            adminTokenCache.put(token, adminUsername);

            // 构建响应数据
            AdminLoginResponse.AdminUser adminUser = new AdminLoginResponse.AdminUser(
                1L, // 固定ID
                adminUsername,
                "admin@xiuxian.com", // 固定邮箱
                "ADMIN"
            );

            AdminLoginResponse.AdminData data = new AdminLoginResponse.AdminData(token, adminUser);

            return AdminLoginResponse.success("登录成功", data);

        } catch (Exception e) {
            log.error("管理员登录失�?, e);
            return AdminLoginResponse.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 验证管理员token - 使用配置文件认证
     */
    public AdminLoginResponse validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return AdminLoginResponse.error("Token不能为空");
            }

            // 验证token格式和有效�?
            if (!jwtTokenProvider.validateToken(token)) {
                adminTokenCache.remove(token);
                return AdminLoginResponse.error("Token无效或已过期");
            }

            // 检查缓�?
            String cachedUsername = adminTokenCache.get(token);
            if (cachedUsername == null) {
                return AdminLoginResponse.error("Token未找到，请重新登�?);
            }

            // 验证是否为配置的管理员用�?
            if (!adminUsername.equals(cachedUsername)) {
                adminTokenCache.remove(token);
                return AdminLoginResponse.error("用户权限不足");
            }

            // 构建响应数据
            AdminLoginResponse.AdminUser adminUser = new AdminLoginResponse.AdminUser(
                1L, // 固定ID
                adminUsername,
                "admin@xiuxian.com", // 固定邮箱
                "ADMIN"
            );

            AdminLoginResponse.AdminData data = new AdminLoginResponse.AdminData(token, adminUser);

            return AdminLoginResponse.success("Token验证成功", data);

        } catch (Exception e) {
            log.error("Token验证失败", e);
            return AdminLoginResponse.error("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 管理员登�?
     */
    public AdminLoginResponse logout(String token) {
        try {
            if (token != null) {
                adminTokenCache.remove(token);
            }
            return AdminLoginResponse.success("登出成功");
        } catch (Exception e) {
            log.error("管理员登出失�?, e);
            return AdminLoginResponse.error("登出失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前管理员信�?
     */
    public AdminLoginResponse getCurrentAdmin(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return AdminLoginResponse.error("Token不能为空");
            }

            // 验证token
            AdminLoginResponse validateResult = validateToken(token);
            if (!validateResult.isSuccess()) {
                return validateResult;
            }

            return validateResult;

        } catch (Exception e) {
            log.error("获取管理员信息失�?, e);
            return AdminLoginResponse.error("获取管理员信息失�? " + e.getMessage());
        }
    }

    /**
     * 根据token获取管理员用户名
     */
    public String getAdminUsernameByToken(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return null;
        }
        return adminTokenCache.get(token);
    }

    /**
     * 检查是否为有效的管理员token
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

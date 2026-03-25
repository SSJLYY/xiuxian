package com.xiuxian.game.common.security;

import com.xiuxian.game.modules.admin.service.AdminAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 管理员安全过滤器 - 独立于游戏用户过滤器
 * 完全处理管理员认证，避免被其他过滤器干扰
 */
@Component
public class AdminSecurityFilter implements Filter {

    @Autowired
    private AdminAuthService adminAuthService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // 只对管理员API进行过滤
        if (!requestURI.startsWith("/api/admin/")) {
            chain.doFilter(request, response);
            return;
        }

        // 管理员登录接口不需要验�?
        if (requestURI.equals("/api/admin/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        // 静态资源不需要验�?
        if (requestURI.endsWith(".html") || requestURI.endsWith(".css") || 
            requestURI.endsWith(".js") || requestURI.endsWith(".png") || 
            requestURI.endsWith(".jpg") || requestURI.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // 提取token
        String token = extractToken(httpRequest);
        if (token == null) {
            sendUnauthorizedResponse(httpResponse, "缺少认证token");
            return;
        }

        // 验证管理员token
        if (!adminAuthService.isValidAdminToken(token)) {
            sendUnauthorizedResponse(httpResponse, "无效的管理员token");
            return;
        }

        // 设置管理员认证上下文，避免被其他过滤器处�?
        String adminUsername = adminAuthService.getAdminUsernameByToken(token);
        if (adminUsername != null) {
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    "admin_" + adminUsername, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 验证通过，继续处理请�?
        chain.doFilter(request, response);
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

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"" + message + "\"}");
    }
}


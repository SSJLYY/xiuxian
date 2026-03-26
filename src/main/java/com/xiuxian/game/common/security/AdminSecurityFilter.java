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
 * 管理后台安全过滤器 - 验证管理员身份的专用过滤器
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

        // 放行：非管理员API路径
        if (!requestURI.startsWith("/api/admin/")) {
            chain.doFilter(request, response);
            return;
        }

        // 管理后台登录接口直接放行
        if (requestURI.equals("/api/admin/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        // 静态资源文件直接放行
        if (requestURI.endsWith(".html") || requestURI.endsWith(".css") ||
            requestURI.endsWith(".js") || requestURI.endsWith(".png") ||
            requestURI.endsWith(".jpg") || requestURI.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // 解析Token
        String token = extractToken(httpRequest);
        if (token == null) {
            sendUnauthorizedResponse(httpResponse, "缺少管理员Token");
            return;
        }

        // 验证Token有效性
        if (!adminAuthService.isValidAdminToken(token)) {
            sendUnauthorizedResponse(httpResponse, "无效的管理员Token");
            return;
        }

        // 将管理员信息注入安全上下文
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

        // 继续执行后续过滤器
        chain.doFilter(request, response);
    }

    /**
     * 从请求头中提取Token
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

package com.xiuxian.game.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiuxian.game.dto.response.ApiResponse;
import com.xiuxian.game.modules.player.service.AccountSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 安全过滤�?
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    
    private final AccountSecurityService accountSecurityService;
    private final ObjectMapper objectMapper;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 跳过静态资源和公开接口
        if (shouldSkipFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 检查IP黑名�?
        String clientIp = getClientIpAddress(request);
        if (accountSecurityService.isIpBlacklisted(clientIp)) {
            log.warn("拒绝黑名单IP访问: ip={}, uri={}", clientIp, requestURI);
            sendErrorResponse(response, "访问被拒�?);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean shouldSkipFilter(String requestURI) {
        return requestURI.startsWith("/css/") ||
               requestURI.startsWith("/js/") ||
               requestURI.startsWith("/images/") ||
               requestURI.startsWith("/fonts/") ||
               requestURI.startsWith("/assets/") ||
               requestURI.startsWith("/static/") ||
               requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/api/public/") ||
               requestURI.equals("/") ||
               requestURI.endsWith(".html") ||
               requestURI.endsWith(".ico") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".js") ||
               requestURI.endsWith(".png") ||
               requestURI.endsWith(".jpg") ||
               requestURI.endsWith(".jpeg") ||
               requestURI.endsWith(".gif") ||
               requestURI.endsWith(".svg") ||
               requestURI.endsWith(".woff") ||
               requestURI.endsWith(".woff2") ||
               requestURI.endsWith(".ttf") ||
               requestURI.endsWith(".eot");
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}


package com.xiuxian.game.common.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * HTTP 请求工具类
 *
 * <p>用于获取客户端真实IP地址的工具类，支持多层代理场景。</p>
 *
 * <p>说明：以下 1-4 种 header 在 Controller 获取客户端真实 IP 时可能无效，
 * 因为某些代理服务器不会传递原始 IP。</p>
 *
 * @author xiuxian
 * @version 1.0
 */
public final class RequestUtils {

    /**
     * 按优先级从高到低尝试从这些请求头中获取真实IP
     * X-Forwarded-For 是最常用的记录原始IP的代理头，
     * 其他头由不同代理软件或网关设置
     */
    private static final String[] PROXY_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private RequestUtils() {
        // 私有构造函数，禁止外部实例化
    }

    /**
     * 获取客户端真实IP地址
     * 优先从代理头中获取，如果没有则使用remoteAddr
     * 注意：只在有可信反向代理时才信任代理头
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        // 检查是否来自可信代理
        boolean isFromTrustedProxy = isTrustedProxy(request);
        
        if (isFromTrustedProxy) {
            // 信任代理头
            for (String header : PROXY_HEADERS) {
                String ip = request.getHeader(header);
                if (isValidIp(ip)) {
                    return ip.split(",")[0].trim(); // 取第一个IP
                }
            }
        }
        
        // 直接连接或不信任代理时使用remoteAddr
        return request.getRemoteAddr();
    }
    
    /**
     * 检查请求是否来自可信代理
     */
    private static boolean isTrustedProxy(HttpServletRequest request) {
        // 可以通过配置指定可信代理IP列表
        // 这里简单实现：检查X-Forwarded-For是否存在来判断是否有代理
        String remoteAddr = request.getRemoteAddr();
        try {
            InetAddress address = InetAddress.getByName(remoteAddr);
            if (address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                return true;
            }
            return remoteAddr != null
                    && (remoteAddr.startsWith("fc") || remoteAddr.startsWith("FC")
                    || remoteAddr.startsWith("fd") || remoteAddr.startsWith("FD"));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 校验IP是否合法
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}

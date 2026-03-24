package com.xiuxian.game.util;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 请求工具类
 *
 * <p>提供从 HttpServletRequest 中提取通用信息的静态工具方法。</p>
 *
 * <p>【P1-4 重构】原来 {@code getClientIpAddress()} 在多个 Controller 中重复定义，
 * 现统一提取到此工具类，消除代码重复，保持一致的 IP 解析逻辑。</p>
 *
 * @author xiuxian
 * @version 1.0
 */
public final class RequestUtils {

    /**
     * 需要检查的代理头，按优先级排列
     * X-Forwarded-For 是最常见的反向代理头，优先级最高
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
        // 工具类禁止实例化
    }

    /**
     * 获取客户端真实 IP 地址
     *
     * <p>处理常见的反向代理场景（Nginx、CDN、负载均衡等）。
     * 对于多级代理（X-Forwarded-For 包含多个 IP），取第一个（最接近客户端）的 IP。</p>
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址，无法获取时返回 {@code request.getRemoteAddr()}
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        for (String header : PROXY_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // 多级代理：取第一个非 unknown 的 IP
                if (ip.contains(",")) {
                    for (String part : ip.split(",")) {
                        String candidate = part.trim();
                        if (isValidIp(candidate)) {
                            return candidate;
                        }
                    }
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断 IP 字符串是否有效（非空、非 unknown）
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}

package com.xiuxian.game.common.util;

import javax.servlet.http.HttpServletRequest;

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
     *
     * <p>优先从代理服务器的请求头中获取真实IP，兼容Nginx、代理服务器或SLB等负载均衡场景。
     * 如果获取到多个IP（逗号分隔），会取第一个有效IP而非unknown。</p>
     *
     * @param request HTTP 请求对象
     * @return 客户端真实IP地址，如果无法获取则返回 {@code request.getRemoteAddr()}
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        for (String header : PROXY_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // 解析逗号分隔的IP列表，取第一个有效IP而非unknown
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
     * 校验IP是否合法
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}

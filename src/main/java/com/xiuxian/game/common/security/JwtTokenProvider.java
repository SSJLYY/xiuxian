package com.xiuxian.game.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Token 工具类
 * 提供 JWT Token 生成、解析和验证功能
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    private final String jwtSecret;
    private final long jwtExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret:xiuxian-game-secret-key-2024-very-long-and-secure}") String jwtSecret,
            @Value("${jwt.expiration:7200000}") long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        if (jwtSecret.equals("xiuxian-game-secret-key-2024-very-long-and-secure")) {
            log.warn("使用默认JWT密钥！请在生产环境中通过环境变量配置密钥");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     *
     * @param username 用户名（subject 决定 token 的有效期等属性）
     * @return JWT Token 字符串
     * @throws JwtException 如果 token 生成过程发生异常
     */
    public String generateToken(String username) {
        log.debug("生成JWT Token: username={}", username);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public void revokeToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            revokedTokens.add(token);
        }
    }

    public boolean isTokenRevoked(String token) {
        return token != null && revokedTokens.contains(token);
    }

    /**
     * 从Token中解析用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名
     * @throws JwtException 如果 token 无效或已过期
     */
    public String getUsernameFromToken(String token) {
        log.debug("解析JWT Token");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 验证JWT Token 的有效性
     *
     * @param authToken JWT Token 字符串
     * @return true 表示有效，false 表示无效或已过期/签名不匹配
     */
    public boolean validateToken(String authToken) {
        try {
            if (isTokenRevoked(authToken)) {
                return false;
            }
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }
}

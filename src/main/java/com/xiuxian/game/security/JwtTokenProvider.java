package com.xiuxian.game.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者
 * 负责 JWT 的生成、解析和验证
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     * 
     * @param username 用户名（将作为 subject 存入 token）
     * @return JWT Token 字符串
     * @throws JwtException 当 token 生成失败时抛出
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

    /**
     * 从 Token 中提取用户名
     * 
     * @param token JWT Token 字符串
     * @return 用户名
     * @throws JwtException 当 token 解析失败时抛出
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
     * 验证 JWT Token 有效性
     * 
     * @param authToken JWT Token 字符串
     * @return true 表示有效，false 表示无效/过期/签名错误
     */
    public boolean validateToken(String authToken) {
        try {
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

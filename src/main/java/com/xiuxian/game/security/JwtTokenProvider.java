package com.xiuxian.game.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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

    public String generateToken(String username) {
        System.out.println("=== DEBUG: JwtTokenProvider.generateToken ===");
        System.out.println("接收到的username参数: " + username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        try {
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            System.err.println("JWT Token生成失败: " + e.getMessage());
            throw new RuntimeException("JWT Token生成失败", e);
        }
    }

    public String getUsernameFromToken(String token) {
        System.out.println("=== DEBUG: getUsernameFromToken ===");
        System.out.println("解析的token: " + token);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            System.out.println("从token中提取的subject: " + subject);

            return subject;
        } catch (Exception e) {
            System.err.println("JWT Token解析失败: " + e.getMessage());
            throw new RuntimeException("JWT Token解析失败", e);
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT Token验证失败: " + e.getMessage());
            return false;
        }
    }
}
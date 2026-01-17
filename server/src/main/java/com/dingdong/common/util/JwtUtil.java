package com.dingdong.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // Access Token 有效期
    private Long expiration;

    @Value("${jwt.refresh-expiration:2592000000}") // Refresh Token 有效期，默认30天 (30 * 24 * 60 * 60 * 1000)
    private Long refreshExpiration;

    /**
     * 生成 Access Token
     *
     * @param userId   用户ID
     * @param nickname 用户昵称
     * @return JWT Token
     */
    public String generateToken(Long userId, String nickname) {
        return generateToken(userId, nickname, expiration, "access");
    }

    /**
     * 生成 Refresh Token
     *
     * @param userId 用户ID
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, null, refreshExpiration, "refresh");
    }

    private String generateToken(Long userId, String nickname, Long expireTime, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key);

        if (nickname != null) {
            builder.claim("nickname", nickname);
        }

        return builder.compact();
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中获取用户昵称
     *
     * @param token JWT Token
     * @return 用户昵称
     */
    public String getNicknameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("nickname", String.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证是否为 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 Token
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

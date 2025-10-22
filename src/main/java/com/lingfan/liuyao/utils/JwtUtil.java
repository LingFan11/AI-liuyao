package com.lingfan.liuyao.utils;

import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析、验证JWT Token
 * 
 * Token格式：
 * - Header: 算法和类型
 * - Payload: 用户ID、用户名、过期时间等
 * - Signature: 签名
 * 
 * 使用场景：
 * - 用户登录成功后生成Token
 * - 请求接口时验证Token
 * - 从Token中获取用户信息
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Slf4j
@Component
public class JwtUtil {
    
    /**
     * JWT密钥 - 从配置文件读取
     */
    @Value("${liuyao.jwt.secret}")
    private String secretKey;
    
    /**
     * JWT过期时间（毫秒） - 从配置文件读取
     * 默认：86400000ms = 24小时
     */
    @Value("${liuyao.jwt.expiration}")
    private Long expiration;
    
    /**
     * Token中的用户ID字段名
     */
    private static final String CLAIM_USER_ID = "userId";
    
    /**
     * Token中的用户名字段名
     */
    private static final String CLAIM_USERNAME = "username";
    
    /**
     * 生成JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        
        return generateToken(claims);
    }
    
    /**
     * 生成JWT Token（通用方法）
     * 
     * @param claims 自定义载荷
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析JWT Token，获取Claims
     * 
     * @param token JWT Token
     * @return Claims对象
     * @throws BusinessException Token无效或已过期
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期：{}", token);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的Token：{}", token);
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (MalformedJwtException e) {
            log.warn("Token格式错误：{}", token);
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (SignatureException e) {
            log.warn("Token签名验证失败：{}", token);
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            log.warn("Token为空：{}", token);
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return true=有效, false=无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get(CLAIM_USER_ID);
        
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token中用户ID格式错误");
        }
    }
    
    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_USERNAME, String.class);
    }
    
    /**
     * 判断Token是否过期
     * 
     * @param token JWT Token
     * @return true=已过期, false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (BusinessException e) {
            return true;
        }
    }
    
    /**
     * 获取Token过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 刷新Token
     * 生成新的Token，延长过期时间
     * 
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        
        // 创建新的claims，只保留自定义字段（userId, username等）
        // 注意：Claims对象是不可变的，不能直接修改，需要创建新的Map
        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put(CLAIM_USER_ID, claims.get(CLAIM_USER_ID));
        newClaims.put(CLAIM_USERNAME, claims.get(CLAIM_USERNAME));
        
        return generateToken(newClaims);
    }
    
    /**
     * 从Token中获取所有Claims
     * 
     * @param token JWT Token
     * @return Claims Map
     */
    public Map<String, Object> getAllClaimsFromToken(String token) {
        Claims claims = parseToken(token);
        return new HashMap<>(claims);
    }
    
    /**
     * 获取签名密钥
     * 
     * @return SecretKey
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

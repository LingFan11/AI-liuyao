package com.lingfan.liuyao.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.utils.ApiResponse;
import com.lingfan.liuyao.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT认证过滤器
 * 
 * 功能：
 * 1. 拦截所有HTTP请求
 * 2. 从请求头提取JWT Token
 * 3. 验证Token有效性
 * 4. 检查Token黑名单（Redis）
 * 5. 提取用户信息并存入SecurityContext
 * 6. 更新用户在线状态（Redis）
 * 7. Token自动续期（可选）
 * 
 * 执行顺序：
 * Spring Security过滤器链中，在UsernamePasswordAuthenticationFilter之前执行
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${liuyao.jwt.header}")
    private String tokenHeader;  // "Authorization"
    
    @Value("${liuyao.jwt.prefix}")
    private String tokenPrefix;  // "Bearer "
    
    /**
     * Redis Key前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_ONLINE_PREFIX = "user:online:";
    
    /**
     * 在线状态过期时间（分钟）
     */
    private static final int ONLINE_STATUS_EXPIRE_MINUTES = 30;
    
    /**
     * Token自动续期阈值（分钟）
     * Token剩余时间少于此值时自动续期
     */
    private static final int TOKEN_REFRESH_THRESHOLD_MINUTES = 30;
    
    /**
     * 白名单路径（不需要认证）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
        "/api/user/register",                   // 注册
        "/api/user/login",                      // 登录
        "/api/user/check-username",             // 检查用户名
        "/api/user/check-email",                // 检查邮箱
        "/api/user/check-phone",                // 检查手机号
        "/api/health",                          // 健康检查
        "/api/test/register/**",                // 注册测试接口（开发阶段）
        "/api/test/config/generate-token",      // 生成测试Token
        "/api/test/config/redis",               // Redis测试
        "/api/test/config/mongodb",             // MongoDB测试
        "/api/test/config/async",               // 线程池测试
        "/api/test/config/cors",                // 跨域测试
        "/api/test/config/logout-test",         // 登出测试
        // 注意：/api/test/config/jwt 不在白名单中，需要JWT认证
        "/swagger-ui/**",                       // Swagger UI
        "/v3/api-docs/**",                      // API文档
        "/doc.html",                            // Knife4j文档
        "/swagger-resources/**",                // Swagger资源
        "/webjars/**",                          // Web资源
        "/favicon.ico"                          // 图标
    );
    
    /**
     * 路径匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    /**
     * 核心过滤逻辑
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        log.debug("JWT过滤器拦截请求：{}", requestUri);
        
        // 1. 检查是否在白名单中，doFilter 通过过滤
        if (isWhiteList(requestUri)) {
            log.debug("白名单路径，跳过认证：{}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 2. 提取Token
        String token = extractToken(request);
        if (StrUtil.isBlank(token)) {
            log.warn("请求缺少Token：{}", requestUri);
            handleAuthenticationFailure(response, "未登录，请先登录", ErrorCode.UNAUTHORIZED.getCode());
            return;
        }
        
        try {
            // 3. 检查Token是否在黑名单（已登出）
            if (isTokenBlacklisted(token)) {
                log.warn("Token已失效（在黑名单中）：{}", token);
                handleAuthenticationFailure(response, "Token已失效，请重新登录", ErrorCode.TOKEN_INVALID.getCode());
                return;
            }
            
            // 4. 验证Token有效性
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败：{}", token);
                handleAuthenticationFailure(response, "Token无效", ErrorCode.TOKEN_INVALID.getCode());
                return;
            }
            
            // 5. 提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            log.debug("Token验证成功，用户ID：{}，用户名：{}", userId, username);
            
            // 6. 更新用户在线状态（Redis）
            updateUserOnlineStatus(userId);
            
            // 7. Token自动续期（可选）
            refreshTokenIfNeeded(token, response);
            
            // 8. 构建Authentication对象，存入SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("用户认证成功，已存入SecurityContext：userId={}", userId);
            
            // 9. 继续执行过滤器链
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("JWT认证过程发生异常", e);
            handleAuthenticationFailure(response, "认证失败：" + e.getMessage(), ErrorCode.UNAUTHORIZED.getCode());
        } finally {
            // 请求结束后清理SecurityContext，避免线程复用导致的安全问题
            SecurityContextHolder.clearContext();
        }
    }
    
    /**
     * 检查请求路径是否在白名单中
     * 
     * @param requestUri 请求路径
     * @return true=在白名单, false=不在白名单
     */
    private boolean isWhiteList(String requestUri) {
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 从请求头提取JWT Token
     * 
     * @param request HttpServletRequest
     * @return Token字符串，如果不存在返回null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeader);
        
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(tokenPrefix)) {
            return authHeader.substring(tokenPrefix.length());
        }
        
        return null;
    }
    
    /**
     * 检查Token是否在黑名单
     * 
     * 场景：用户登出、修改密码、管理员强制下线
     * 
     * @param token JWT Token
     * @return true=在黑名单, false=不在黑名单
     */
    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 更新用户在线状态
     * 
     * 每次请求成功认证后，刷新在线状态的过期时间
     * 30分钟未活动则自动下线
     * 
     * @param userId 用户ID
     */
    private void updateUserOnlineStatus(Long userId) {
        try {
            String key = USER_ONLINE_PREFIX + userId;
            redisTemplate.opsForValue().set(
                key,
                System.currentTimeMillis(),
                ONLINE_STATUS_EXPIRE_MINUTES,
                TimeUnit.MINUTES
            );
            log.debug("更新用户在线状态：userId={}, expireMinutes={}", userId, ONLINE_STATUS_EXPIRE_MINUTES);
        } catch (Exception e) {
            log.error("更新用户在线状态失败：userId={}", userId, e);
            // 不影响主流程，继续执行
        }
    }
    
    /**
     * Token自动续期
     * 
     * 如果Token剩余时间少于30分钟，自动刷新Token
     * 新Token通过响应头"New-Token"返回给前端
     * 
     * @param token 当前Token
     * @param response HttpServletResponse
     */
    private void refreshTokenIfNeeded(String token, HttpServletResponse response) {
        try {
            Date expiration = jwtUtil.getExpirationFromToken(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            long thresholdTime = TOKEN_REFRESH_THRESHOLD_MINUTES * 60 * 1000;
            
            // 剩余时间少于阈值，自动刷新
            if (remainingTime > 0 && remainingTime < thresholdTime) {
                String newToken = jwtUtil.refreshToken(token);
                response.setHeader("New-Token", newToken);
                log.info("Token即将过期，已自动续期。剩余时间：{}分钟", remainingTime / 60000);
            }
        } catch (Exception e) {
            log.error("Token自动续期失败", e);
            // 不影响主流程，继续执行
        }
    }
    
    /**
     * 处理认证失败，返回401响应
     * 
     * @param response HttpServletResponse
     * @param message 错误消息
     * @param code 错误码
     */
    private void handleAuthenticationFailure(
        HttpServletResponse response,
        String message,
        int code
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.error(code, message);
        
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}

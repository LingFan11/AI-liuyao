package com.lingfan.liuyao.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingfan.liuyao.config.SecurityWhiteList;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.mapper.UserMapper;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JWT认证过滤器（完全集成Spring Security权限体系）
 * 
 * 功能：
 * 1. 拦截所有HTTP请求，检查白名单
 * 2. 从请求头提取JWT Token并验证
 * 3. 检查Token黑名单（Redis）
 * 4. 提取用户信息、角色、权限
 * 5. 构建Spring Security的Authentication对象（包含GrantedAuthority）
 * 6. 更新用户在线状态（Redis）
 * 
 * 设计改进：
 * - 删除双重认证：不再需要AuthenticationInterceptor
 * - 权限直接加载到Spring Security的GrantedAuthority中
 * - 删除Token自动续期：改为主动刷新接口
 * - 使用统一白名单配置：SecurityWhiteList
 * 
 * 执行顺序：
 * Spring Security过滤器链中，在UsernamePasswordAuthenticationFilter之前执行
 * 
 * @author Liuyao Team
 * @since 2025-10-27（重构）
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${liuyao.jwt.header}")
    private String tokenHeader;  // "Authorization"
    
    @Value("${liuyao.jwt.prefix}")
    private String tokenPrefix;  // "Bearer "
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;  // "/api"
    
    // Redis Key前缀已统一到 CacheConstants 中
    
    /**
     * 在线状态过期时间（分钟）
     */
    private static final int ONLINE_STATUS_EXPIRE_MINUTES = 30;
    
    /**
     * 角色前缀（Spring Security约定）
     */
    private static final String ROLE_PREFIX = "ROLE_";
    
    /**
     * 权限前缀（自定义约定）
     */
    private static final String PERMISSION_PREFIX = "PERM_";
    
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
        
        // 1. 检查是否在白名单中
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
            // 3. 检查Token是否在黑名单（已登出或已刷新）
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
            
            // 6. 查询用户的角色和权限（从数据库）
            List<GrantedAuthority> authorities = loadUserAuthorities(userId);
            log.debug("加载用户权限：userId={}, authorities={}", userId, authorities.size());
            
            // 7. 更新用户在线状态（Redis）
            updateUserOnlineStatus(userId);
            
            // 8. 构建Authentication对象并存入SecurityContext
            // principal存userId，authorities存角色和权限
            // Spring Security会自动根据authorities进行权限校验
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("用户认证成功，已存入SecurityContext：userId={}, authorities={}", userId, authorities.size());
            
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
     * 检查请求路径是否在白名单中（使用统一配置）
     * 
     * @param requestUri 请求路径
     * @return true=在白名单, false=不在白名单
     */
    private boolean isWhiteList(String requestUri) {
        String[] whiteList = SecurityWhiteList.getAllPatternsWithContext(contextPath);
        for (String pattern : whiteList) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 加载用户的角色和权限（转换为Spring Security的GrantedAuthority）
     * 
     * @param userId 用户ID
     * @return GrantedAuthority列表
     */
    private List<GrantedAuthority> loadUserAuthorities(Long userId) {
        try {
            // 1. 查询用户角色（如：admin, user）
            List<String> roles = userMapper.selectUserRoles(userId);
            
            // 2. 查询用户权限（如：user:create, user:delete）
            List<String> permissions = userMapper.selectUserPermissions(userId);
            
            // 3. 转换为GrantedAuthority
            // 角色：添加ROLE_前缀（Spring Security约定）
            List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toList());
            
            // 权限：添加PERM_前缀（自定义约定，便于区分）
            List<GrantedAuthority> permissionAuthorities = permissions.stream()
                .map(perm -> new SimpleGrantedAuthority(PERMISSION_PREFIX + perm))
                .collect(Collectors.toList());
            
            authorities.addAll(permissionAuthorities);
            
            log.debug("用户权限加载完成：userId={}, roles={}, permissions={}, totalAuthorities={}",
                userId, roles.size(), permissions.size(), authorities.size());
            
            return authorities;
            
        } catch (Exception e) {
            log.error("加载用户权限失败：userId={}", userId, e);
            return new ArrayList<>();
        }
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
     * 场景：
     * 1. 用户登出
     * 2. Token已刷新（旧Token加入黑名单）
     * 3. 修改密码
     * 4. 管理员强制下线
     * 
     * @param token JWT Token
     * @return true=在黑名单, false=不在黑名单
     */
    private boolean isTokenBlacklisted(String token) {
        String key = CacheConstants.JWT_BLACKLIST_PREFIX + token;
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
            String key = CacheConstants.USER_ONLINE_PREFIX + userId;
            redisTemplate.opsForValue().set(
                key,
                System.currentTimeMillis(),
                CacheConstants.USER_ONLINE_TTL,
                TimeUnit.SECONDS
            );
            log.debug("更新用户在线状态：userId={}, expireTTL={}秒", userId, CacheConstants.USER_ONLINE_TTL);
        } catch (Exception e) {
            log.error("更新用户在线状态失败：userId={}", userId, e);
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

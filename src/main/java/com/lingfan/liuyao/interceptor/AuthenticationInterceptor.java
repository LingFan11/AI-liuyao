package com.lingfan.liuyao.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingfan.liuyao.annotation.RequiresLogin;
import com.lingfan.liuyao.annotation.RequiresPermissions;
import com.lingfan.liuyao.annotation.RequiresRoles;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.utils.ApiResponse;
import com.lingfan.liuyao.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证拦截器
 * 
 * 功能：
 * 1. 检查方法上的权限注解（@RequiresLogin, @RequiresRoles, @RequiresPermissions）
 * 2. 从SecurityContext获取用户ID
 * 3. 查询用户完整信息（包含角色和权限），优先从Redis缓存读取
 * 4. 构建UserContext并存入ThreadLocal
 * 5. 验证用户角色和权限
 * 6. 返回401（未认证）或403（权限不足）
 * 
 * 执行顺序：
 * JwtAuthenticationFilter（验证Token） → AuthenticationInterceptor（验证权限） → Controller
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 前置拦截：Controller方法调用前
     */
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        
        // 1. 判断是否是Controller方法
        if (!(handler instanceof HandlerMethod)) {
            // 不是Controller方法（如静态资源），直接放行
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 2. 获取方法或类上的权限注解（方法优先于类）
        RequiresLogin requiresLogin = getAnnotation(handlerMethod, RequiresLogin.class);
        RequiresRoles requiresRoles = getAnnotation(handlerMethod, RequiresRoles.class);
        RequiresPermissions requiresPermissions = getAnnotation(handlerMethod, RequiresPermissions.class);
        
        // 3. 如果没有任何权限注解，直接放行
        if (requiresLogin == null && requiresRoles == null && requiresPermissions == null) {
            log.debug("方法无权限注解，直接放行：{}", handlerMethod.getMethod().getName());
            return true;
        }
        
        // 4. 从SecurityContext获取userId（由JwtAuthenticationFilter存入）
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            log.warn("用户未登录，SecurityContext中没有userId");
            handleUnauthorized(response, "未登录，请先登录");
            return false;
        }
        
        log.debug("拦截器获取到userId：{}", userId);
        
        // 5. 查询用户完整信息（优先Redis缓存）
        UserInfo userInfo = getUserInfoWithRolesAndPermissions(userId);
        if (userInfo == null) {
            log.warn("用户不存在：userId={}", userId);
            handleUnauthorized(response, "用户不存在");
            return false;
        }
        
        // 6. 检查账号状态
        if (userInfo.getStatus() != 0) {
            log.warn("账号异常：userId={}, status={}", userId, userInfo.getStatus());
            String message = userInfo.getStatus() == 1 ? "账号已被锁定" : "账号已被禁用";
            handleUnauthorized(response, message);
            return false;
        }
        
        // 7. 构建UserContext并存入ThreadLocal
        UserContextHolder.UserContext context = UserContextHolder.UserContext.builder()
            .userId(userInfo.getUserId())
            .username(userInfo.getUsername())
            .roles(userInfo.getRoles())
            .permissions(userInfo.getPermissions())
            .build();
        UserContextHolder.setContext(context);
        
        log.debug("用户上下文已存入ThreadLocal：userId={}, roles={}, permissions={}",
            userId, userInfo.getRoles().size(), userInfo.getPermissions().size());
        
        // 8. 校验角色
        if (requiresRoles != null) {
            boolean hasRole = checkRoles(userInfo.getRoles(), requiresRoles);
            if (!hasRole) {
                log.warn("角色校验失败：userId={}, requiredRoles={}, userRoles={}",
                    userId, Arrays.toString(requiresRoles.value()), userInfo.getRoles());
                handleForbidden(response, "角色不足，无法访问该资源");
                return false;
            }
        }
        
        // 9. 校验权限
        if (requiresPermissions != null) {
            boolean hasPermission = checkPermissions(userInfo.getPermissions(), requiresPermissions);
            if (!hasPermission) {
                log.warn("权限校验失败：userId={}, requiredPermissions={}, userPermissions={}",
                    userId, Arrays.toString(requiresPermissions.value()), userInfo.getPermissions());
                handleForbidden(response, "权限不足，无法访问该资源");
                return false;
            }
        }
        
        // 验证通过，放行
        log.debug("权限校验通过，放行：userId={}", userId);
        return true;
    }
    
    /**
     * 后置拦截：清理ThreadLocal
     */
    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex
    ) {
        // 清理ThreadLocal，避免内存泄漏
        UserContextHolder.clearContext();
        log.debug("已清理ThreadLocal用户上下文");
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 获取方法或类上的注解（方法优先）
     * 
     * @param handlerMethod HandlerMethod对象
     * @param annotationClass 注解类
     * @return 注解实例，不存在返回null
     */
    private <T extends Annotation> T getAnnotation(HandlerMethod handlerMethod, Class<T> annotationClass) {
        // 1. 先查方法上的注解
        T annotation = handlerMethod.getMethodAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        // 2. 再查类上的注解
        return handlerMethod.getBeanType().getAnnotation(annotationClass);
    }
    
    /**
     * 查询用户完整信息（包含角色和权限）
     * 优先从Redis缓存读取，未命中则从MySQL查询并缓存
     * 
     * @param userId 用户ID
     * @return UserInfo对象，不存在返回null
     */
    private UserInfo getUserInfoWithRolesAndPermissions(Long userId) {
        // 1. 尝试从Redis缓存读取
        String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof UserInfo) {
                log.debug("从Redis缓存读取用户信息：userId={}", userId);
                return (UserInfo) cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取用户信息失败，将从MySQL查询：userId={}", userId, e);
        }
        
        // 2. Redis未命中，从MySQL查询
        log.debug("Redis未命中，从MySQL查询用户信息：userId={}", userId);
        
        // 2.1 查询User基础信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        
        // 2.2 查询用户的角色列表（联表查询：user_roles + roles）
        List<String> roles = userMapper.selectUserRoles(userId);
        log.debug("查询到用户角色：userId={}, roles={}", userId, roles);
        
        // 2.3 查询用户的权限列表（联表查询：user_roles + role_permissions + permissions）
        List<String> permissions = userMapper.selectUserPermissions(userId);
        log.debug("查询到用户权限：userId={}, permissions={}", userId, permissions);
        
        // 2.4 构建UserInfo对象
        UserInfo userInfo = new UserInfo(
            user.getId(),
            user.getUsername(),
            user.getStatus(),
            roles,
            permissions
        );
        
        // 2.5 存入Redis缓存（TTL 30分钟）
        try {
            redisTemplate.opsForValue().set(
                cacheKey,
                userInfo,
                CacheConstants.USER_INFO_TTL,
                TimeUnit.SECONDS
            );
            log.debug("用户信息已缓存到Redis：userId={}, TTL={}秒", userId, CacheConstants.USER_INFO_TTL);
        } catch (Exception e) {
            log.error("缓存用户信息到Redis失败：userId={}", userId, e);
        }
        
        return userInfo;
    }
    
    /**
     * 校验角色
     * 
     * @param userRoles 用户拥有的角色列表
     * @param annotation @RequiresRoles注解
     * @return true=通过, false=不通过
     */
    private boolean checkRoles(List<String> userRoles, RequiresRoles annotation) {
        String[] requiredRoles = annotation.value();
        RequiresRoles.Logical logical = annotation.logical();
        
        if (logical == RequiresRoles.Logical.AND) {
            // AND逻辑：必须拥有所有角色
            for (String role : requiredRoles) {
                if (!userRoles.contains(role)) {
                    log.debug("角色校验失败（AND）：缺少角色 {}", role);
                    return false;
                }
            }
            return true;
        } else {
            // OR逻辑：拥有任意一个角色即可
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    log.debug("角色校验通过（OR）：拥有角色 {}", role);
                    return true;
                }
            }
            log.debug("角色校验失败（OR）：不拥有任何所需角色 {}", Arrays.toString(requiredRoles));
            return false;
        }
    }
    
    /**
     * 校验权限
     * 
     * @param userPermissions 用户拥有的权限列表
     * @param annotation @RequiresPermissions注解
     * @return true=通过, false=不通过
     */
    private boolean checkPermissions(List<String> userPermissions, RequiresPermissions annotation) {
        String[] requiredPermissions = annotation.value();
        RequiresPermissions.Logical logical = annotation.logical();
        
        if (logical == RequiresPermissions.Logical.AND) {
            // AND逻辑：必须拥有所有权限
            for (String permission : requiredPermissions) {
                if (!userPermissions.contains(permission)) {
                    log.debug("权限校验失败（AND）：缺少权限 {}", permission);
                    return false;
                }
            }
            return true;
        } else {
            // OR逻辑：拥有任意一个权限即可
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    log.debug("权限校验通过（OR）：拥有权限 {}", permission);
                    return true;
                }
            }
            log.debug("权限校验失败（OR）：不拥有任何所需权限 {}", Arrays.toString(requiredPermissions));
            return false;
        }
    }
    
    /**
     * 处理未认证（401）
     * 
     * @param response HttpServletResponse
     * @param message 错误消息
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.error(
            ErrorCode.UNAUTHORIZED.getCode(),
            message
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * 处理权限不足（403）
     * 
     * @param response HttpServletResponse
     * @param message 错误消息
     */
    private void handleForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.error(
            ErrorCode.PERMISSION_DENIED.getCode(),
            message
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    
    /**
     * 用户信息DTO（内部类）
     * 用于Redis缓存，必须实现Serializable
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long userId;
        private String username;
        private Integer status;
        private List<String> roles;
        private List<String> permissions;
    }
}

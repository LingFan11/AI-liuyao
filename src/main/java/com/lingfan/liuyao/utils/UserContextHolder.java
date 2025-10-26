package com.lingfan.liuyao.utils;

import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户上下文持有者
 * 
 * 功能：
 * 1. 从Spring Security的SecurityContext获取当前登录用户ID
 * 2. 通过ThreadLocal存储用户完整上下文（角色、权限）
 * 3. 提供便捷的方法获取用户信息
 * 
 * 使用场景：
 * - Controller中获取当前用户ID、角色、权限
 * - Service中获取当前用户ID
 * - 业务逻辑中需要当前用户信息时
 * 
 * 注意：
 * - 必须在JWT认证通过后才能使用
 * - 白名单接口（如登录、注册）中无法获取用户信息
 * - AuthenticationInterceptor会在请求结束后自动清理ThreadLocal
 * 
 * @author Liuyao Team
 * @since 2025-10-24
 */
@Slf4j
public class UserContextHolder {
    
    /**
     * ThreadLocal存储用户完整上下文
     * 包含userId、username、roles、permissions
     */
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 获取当前登录用户ID
     * 
     * 工作原理：
     * 1. JwtAuthenticationFilter验证Token成功后，将userId存入SecurityContext
     * 2. 代码：authentication.setPrincipal(userId)
     * 3. 本方法从SecurityContext中提取userId
     * 
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                log.debug("SecurityContext中没有Authentication，用户未登录");
                return null;
            }
            
            Object principal = authentication.getPrincipal();
            
            if (principal == null) {
                log.debug("Authentication中没有Principal，用户未登录");
                return null;
            }
            
            // JwtAuthenticationFilter中存储的是Long类型的userId
            if (principal instanceof Long) {
                return (Long) principal;
            }
            
            // 兼容性处理：如果是其他类型，尝试转换
            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("无法将Principal转换为Long：{}", principal);
                    return null;
                }
            }
            
            log.warn("Principal类型不是Long：{}", principal.getClass().getName());
            return null;
            
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 检查是否已登录
     * 
     * @return true=已登录, false=未登录
     */
    public static boolean isAuthenticated() {
        Long userId = getCurrentUserId();
        return userId != null && userId > 0;
    }
    
    /**
     * 获取当前用户ID（必须登录）
     * 
     * 如果未登录，抛出异常
     * 适用于必须登录才能执行的业务逻辑
     * 
     * @return 用户ID
     * @throws IllegalStateException 未登录时抛出
     */
    public static Long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
        return userId;
    }
    
    /**
     * 清除当前用户上下文
     * 
     * 注意：一般不需要手动调用，JwtAuthenticationFilter会在finally中自动清理
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
    
    // ==================== ThreadLocal上下文管理（新增） ====================
    
    /**
     * 设置用户上下文到ThreadLocal
     * 由AuthenticationInterceptor调用
     * 
     * @param context 用户上下文
     */
    public static void setContext(UserContext context) {
        CONTEXT_HOLDER.set(context);
    }
    
    /**
     * 获取ThreadLocal中的用户上下文
     * 
     * @return 用户上下文，未设置返回null
     */
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        UserContext context = getContext();
        return context != null ? context.getUsername() : null;
    }
    
    /**
     * 获取当前用户角色列表
     * 
     * @return 角色编码列表，未登录返回空列表
     */
    public static List<String> getCurrentUserRoles() {
        UserContext context = getContext();
        return context != null && context.getRoles() != null 
            ? context.getRoles() 
            : new ArrayList<>();
    }
    
    /**
     * 获取当前用户权限列表
     * 
     * @return 权限编码列表，未登录返回空列表
     */
    public static List<String> getCurrentUserPermissions() {
        UserContext context = getContext();
        return context != null && context.getPermissions() != null 
            ? context.getPermissions() 
            : new ArrayList<>();
    }
    
    /**
     * 判断当前用户是否拥有指定角色
     * 
     * @param roleCode 角色编码
     * @return true=拥有, false=不拥有
     */
    public static boolean hasRole(String roleCode) {
        List<String> roles = getCurrentUserRoles();
        return roles.contains(roleCode);
    }
    
    /**
     * 判断当前用户是否拥有任意一个角色
     * 
     * @param roleCodes 角色编码数组
     * @return true=拥有任意一个, false=都不拥有
     */
    public static boolean hasAnyRole(String... roleCodes) {
        List<String> userRoles = getCurrentUserRoles();
        for (String roleCode : roleCodes) {
            if (userRoles.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断当前用户是否拥有指定权限
     * 
     * @param permissionCode 权限编码
     * @return true=拥有, false=不拥有
     */
    public static boolean hasPermission(String permissionCode) {
        List<String> permissions = getCurrentUserPermissions();
        return permissions.contains(permissionCode);
    }
    
    /**
     * 判断当前用户是否拥有任意一个权限
     * 
     * @param permissionCodes 权限编码数组
     * @return true=拥有任意一个, false=都不拥有
     */
    public static boolean hasAnyPermission(String... permissionCodes) {
        List<String> userPermissions = getCurrentUserPermissions();
        for (String permissionCode : permissionCodes) {
            if (userPermissions.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清除ThreadLocal上下文
     * 由AuthenticationInterceptor在afterCompletion中调用
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 获取当前用户ID（强制要求登录）
     * 如果未登录或上下文为空，抛出异常
     * 
     * @return 用户ID
     * @throws BusinessException 未登录时抛出
     */
    public static Long requireUserId() {
        UserContext context = getContext();
        if (context == null || context.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或会话已过期");
        }
        return context.getUserId();
    }
    
    // ==================== 内部类：用户上下文 ====================
    
    /**
     * 用户上下文DTO
     * 存储在ThreadLocal中，包含用户ID、用户名、角色、权限
     */
    @Data
    @Builder
    public static class UserContext {
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 角色编码列表
         * 例如：["admin", "vip_month", "user"]
         */
        private List<String> roles;
        
        /**
         * 权限编码列表
         * 例如：["user:view", "divination:create", "interpretation:ai"]
         */
        private List<String> permissions;
    }
}

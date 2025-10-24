package com.lingfan.liuyao.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文持有者
 * 
 * 功能：
 * 1. 从Spring Security的SecurityContext获取当前登录用户信息
 * 2. 提供便捷的方法获取用户ID
 * 3. 判断用户是否已登录
 * 
 * 使用场景：
 * - Controller中获取当前用户ID
 * - Service中获取当前用户ID
 * - 业务逻辑中需要当前用户信息时
 * 
 * 注意：
 * - 必须在JWT认证通过后才能使用
 * - 白名单接口（如登录、注册）中无法获取用户信息
 * 
 * @author Liuyao Team
 * @since 2025-10-24
 */
@Slf4j
public class UserContextHolder {
    
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
}

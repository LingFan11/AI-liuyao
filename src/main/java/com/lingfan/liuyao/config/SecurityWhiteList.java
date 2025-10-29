package com.lingfan.liuyao.config;

import org.springframework.stereotype.Component;

/**
 * 安全白名单配置（统一数据源）
 *
 * 使用场景：
 * - JwtAuthenticationFilter（JWT过滤器）
 * - SecurityConfig（Spring Security配置）
 * - 其他需要判断白名单的地方
 * 
 * 注意：
 * - 路径不包含 /api 前缀（Spring会自动处理context-path）
 * - 支持Ant风格通配符（*, **, ?）
 * 
 * @author Liuyao Team
 * @since 2025-10-27
 */
@Component
public class SecurityWhiteList {
    
    /**
     * 用户模块白名单
     */
    public static final String[] USER_PATHS = {
        "/user/register",           // 注册
        "/user/login",              // 登录
        "/user/refresh-token",      // 刷新Token（允许即将过期的Token访问）
        "/user/check-username",     // 检查用户名
        "/user/check-email",        // 检查邮箱
        "/user/check-phone"         // 检查手机号
    };
    
    /**
     * 系统接口白名单
     */
    public static final String[] SYSTEM_PATHS = {
        "/health",                  // 健康检查
        "/favicon.ico"              // 图标
    };
    
    /**
     * Swagger文档白名单
     */
    public static final String[] SWAGGER_PATHS = {
        "/swagger-ui/**",           // Swagger UI
        "/v3/api-docs/**",          // API文档
        "/doc.html",                // Knife4j文档
        "/swagger-resources/**",    // Swagger资源
        "/webjars/**"               // Web资源
    };
    
    /**
     * 测试接口白名单（开发阶段，生产环境应删除）
     */
    public static final String[] TEST_PATHS = {
        "/test/register/**",        // 注册测试
        "/test/login/**",           // 登录测试
        "/test/profile/**",         // 用户信息测试
        "/test/util/**",            // 工具类测试
        "/test/auth/public",        // 公开测试接口
        "/test/config/generate-token",  // 生成测试Token
        "/test/config/redis",       // Redis测试
        "/test/config/mongodb",     // MongoDB测试
        "/test/config/async",       // 线程池测试
        "/test/config/cors",        // 跨域测试
        "/test/config/logout-test", // 登出测试
        "/test/divination/**"       // 起卦测试
    };
    
    /**
     * 获取所有白名单路径（用于Spring Security）
     * 
     * @return 所有白名单路径数组
     */
    public static String[] getAllPatterns() {
        int totalLength = USER_PATHS.length + SYSTEM_PATHS.length 
                        + SWAGGER_PATHS.length + TEST_PATHS.length;
        String[] allPatterns = new String[totalLength];
        
        int index = 0;
        System.arraycopy(USER_PATHS, 0, allPatterns, index, USER_PATHS.length);
        index += USER_PATHS.length;
        
        System.arraycopy(SYSTEM_PATHS, 0, allPatterns, index, SYSTEM_PATHS.length);
        index += SYSTEM_PATHS.length;
        
        System.arraycopy(SWAGGER_PATHS, 0, allPatterns, index, SWAGGER_PATHS.length);
        index += SWAGGER_PATHS.length;
        
        System.arraycopy(TEST_PATHS, 0, allPatterns, index, TEST_PATHS.length);
        
        return allPatterns;
    }
    
    /**
     * 获取带context-path的白名单路径（用于JWT过滤器）
     * 
     * @param contextPath 上下文路径（如 "/api"）
     * @return 带context-path的白名单路径数组
     */
    public static String[] getAllPatternsWithContext(String contextPath) {
        String[] allPatterns = getAllPatterns();
        String[] patternsWithContext = new String[allPatterns.length];
        
        for (int i = 0; i < allPatterns.length; i++) {
            patternsWithContext[i] = contextPath + allPatterns[i];
        }
        
        return patternsWithContext;
    }
}

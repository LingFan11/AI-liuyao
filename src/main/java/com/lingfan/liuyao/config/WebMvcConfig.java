package com.lingfan.liuyao.config;

import com.lingfan.liuyao.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 
 * 功能：
 * 1. 注册AuthenticationInterceptor拦截器
 * 2. 配置拦截路径和排除路径
 * 
 * 注意：
 * - 拦截器的白名单需要与SecurityConfig的白名单保持一致
 * - 拦截器在JwtAuthenticationFilter之后执行
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    
    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
            // 拦截所有请求（注意：Spring MVC会自动去除context-path，这里不需要/api前缀）
            .addPathPatterns("/**")
            
            // 排除白名单路径（与SecurityConfig保持一致，不需要/api前缀）
            .excludePathPatterns(
                // 用户模块白名单
                "/user/register",                   // 注册
                "/user/login",                      // 登录
                "/user/check-username",             // 检查用户名
                "/user/check-email",                // 检查邮箱
                "/user/check-phone",                // 检查手机号
                
                // 系统接口
                "/health",                          // 健康检查
                
                // Swagger文档（开发阶段）
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/doc.html",
                "/swagger-resources/**",
                "/webjars/**"
            )
            
            // 执行顺序（数字越小越先执行）
            .order(1);
    }
}

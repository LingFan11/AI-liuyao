package com.lingfan.liuyao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 
 * 设计改进：
 * - 删除AuthenticationInterceptor（双重认证架构）
 * - 完全使用Spring Security的权限体系
 * - 权限验证在JwtAuthenticationFilter中完成
 * 
 * 注意：
 * 如果后续需要添加其他MVC配置（如CORS、静态资源映射等），可以在此类中配置
 * 
 * @author Liuyao Team
 * @since 2025-10-27（重构）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 所有权限验证已迁移到Spring Security
    // 不再需要自定义拦截器
}

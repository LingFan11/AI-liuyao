package com.lingfan.liuyao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 
 * 功能：
 * 解决前后端分离时的跨域问题
 * 允许指定域名访问后端API
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    /**
     * 配置跨域规则
     * 
     * @param registry CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            // 允许的域名
            .allowedOrigins(
                "http://localhost:3000",      // React开发环境
                "http://localhost:5173",      // Vite开发环境
                "http://localhost:8080"       // 本地测试
                // 生产环境域名后续添加
            )
            // 允许的HTTP方法
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            // 允许的Header
            .allowedHeaders("*")
            // 允许携带Cookie
            .allowCredentials(true)
            // 预检请求缓存时间（秒）
            .maxAge(3600);
    }
}

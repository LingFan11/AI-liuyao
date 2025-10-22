package com.lingfan.liuyao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类（临时版本）
 * 当前配置：允许所有请求通过，不进行认证
 * 
 * 说明：
 * - 这是一个临时配置，用于开发阶段测试
 * - 任务1.4会完善此配置，添加JWT认证、权限控制等
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * 配置安全过滤器链
     * 临时配置：允许所有请求通过
     * 
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（前后端分离不需要）
                .csrf(AbstractHttpConfigurer::disable)
                
                // 允许所有请求通过（临时配置）
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                );
        
        return http.build();
    }
}

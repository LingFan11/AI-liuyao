package com.lingfan.liuyao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.interceptor.JwtAuthenticationFilter;
import com.lingfan.liuyao.utils.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类（重构版）
 * 
 * 功能：
 * 1. 配置安全过滤器链
 * 2. 添加JWT认证过滤器（集成权限体系）
 * 3. 使用统一白名单配置（SecurityWhiteList）
 * 4. 禁用CSRF（前后端分离不需要）
 * 5. 配置无状态会话（不使用Session）
 * 6. 配置异常处理（401、403）
 * 
 * 设计改进：
 * - 使用统一白名单配置，避免多处维护
 * - JwtAuthenticationFilter已集成权限加载，不再需要拦截器
 * - 支持基于注解的权限控制（@PreAuthorize）
 * 
 * @author Liuyao Team
 * @since 2025-10-27（重构）
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 启用方法级权限控制（支持@PreAuthorize、@Secured等注解）
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 配置安全过滤器链
     * 
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（前后端分离项目不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用CORS（使用CorsConfig统一配置）
            .cors(AbstractHttpConfigurer::disable)
            
            // 配置会话管理：无状态（不创建Session）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(authorize -> authorize
                // 白名单路径：允许所有人访问
                // 使用统一配置SecurityWhiteList，避免多处维护
                // 注意：Spring Security会自动去除context-path，所以这里不需要/api前缀
                .requestMatchers(SecurityWhiteList.getAllPatterns()).permitAll()
                
                // 其他所有请求：需要认证
                // 权限验证由JwtAuthenticationFilter加载到GrantedAuthority中
                // Controller可使用@PreAuthorize("hasRole('ADMIN')")进行细粒度控制
                .anyRequest().authenticated()
            )
            
            // 添加JWT认证过滤器
            // 在UsernamePasswordAuthenticationFilter之前执行
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            
            // 配置异常处理
            .exceptionHandling(exception -> exception
                // 未认证时的处理（401）
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    
                    ApiResponse<Void> apiResponse = ApiResponse.error(
                        ErrorCode.UNAUTHORIZED.getCode(),
                        "未登录或登录已过期，请先登录"
                    );
                    
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                    response.getWriter().write(jsonResponse);
                })
                
                // 权限不足时的处理（403）
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    
                    ApiResponse<Void> apiResponse = ApiResponse.error(
                        ErrorCode.PERMISSION_DENIED.getCode(),
                        "权限不足，无法访问该资源"
                    );
                    
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                    response.getWriter().write(jsonResponse);
                })
            );
        
        return http.build();
    }
}

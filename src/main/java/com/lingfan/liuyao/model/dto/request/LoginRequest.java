package com.lingfan.liuyao.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求DTO
 * 
 * 支持三种登录方式：
 * 1. 用户名登录
 * 2. 邮箱登录
 * 3. 手机号登录
 * 
 * @author Liuyao Team
 * @since 2025-10-24
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 账号（用户名/邮箱/手机号）
     */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号（支持用户名/邮箱/手机号）", example = "testuser001")
    private String account;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
    
    /**
     * 登录IP（可选，由Controller填充）
     */
    @Schema(description = "登录IP（后端自动填充）", example = "127.0.0.1")
    private String loginIp;
}

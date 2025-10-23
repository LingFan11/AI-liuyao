package com.lingfan.liuyao.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 * 前端 -> 后端
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {
    
    @Schema(description = "用户名（3-50个字符，仅字母数字下划线）", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @Schema(description = "密码（6-20个字符）", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;
    
    @Schema(description = "邮箱（必填）", example = "zhangsan@test.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "手机号（必填，11位）", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "昵称（可选，默认使用用户名）", example = "张三")
    private String nickname;
    
    // ==================== 预留字段：验证码（后期启用） ====================
    
    @Schema(description = "邮箱验证码（后期启用）", example = "123456")
    private String emailCode;
    
    @Schema(description = "手机验证码（后期启用）", example = "123456")
    private String phoneCode;
}

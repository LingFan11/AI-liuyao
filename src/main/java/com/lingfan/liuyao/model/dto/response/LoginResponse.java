package com.lingfan.liuyao.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录响应DTO
 * 
 * 包含JWT Token和用户基本信息
 * 
 * @author Liuyao Team
 * @since 2025-10-24
 */
@Data
@Schema(description = "用户登录响应")
public class LoginResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * JWT Token
     */
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "testuser001")
    private String username;
    
    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "测试用户")
    private String nickname;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 用户等级
     */
    @Schema(description = "用户等级", example = "1")
    private Integer level;
    
    /**
     * 经验值
     */
    @Schema(description = "经验值", example = "0")
    private Integer experience;
    
    /**
     * VIP类型
     * 0-普通用户, 1-月度VIP, 2-年度VIP
     */
    @Schema(description = "VIP类型：0-普通用户, 1-月度VIP, 2-年度VIP", example = "0")
    private Integer vipType;
    
    /**
     * VIP到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "VIP到期时间", example = "2025-12-31 23:59:59")
    private LocalDateTime vipExpireTime;
    
    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "登录时间", example = "2025-10-24 08:30:00")
    private LocalDateTime loginTime;
}

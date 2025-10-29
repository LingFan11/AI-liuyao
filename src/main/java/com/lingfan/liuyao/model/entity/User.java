package com.lingfan.liuyao.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Data
@TableName("users")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // ==================== 账号信息 ====================
    
    /**
     * 用户名（唯一，非空）
     */
    private String username;
    
    /**
     * 密码（BCrypt加密，非空）
     */
    private String password;
    
    /**
     * 邮箱（唯一，非空）
     */
    private String email;
    
    /**
     * 手机号（唯一，非空）
     */
    private String phone;
    
    // ==================== 个人资料 ====================
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 个性签名
     */
    private String signature;
    
    // ==================== 等级和权限 ====================
    
    /**
     * 用户等级（默认1）
     */
    private Integer level;
    
    /**
     * 经验值（默认0）
     */
    private Integer experience;
    
    /**
     * VIP类型
     * 0-普通用户, 1-月度VIP, 2-年度VIP
     */
    private Integer vipType;
    
    /**
     * VIP到期时间
     */
    private LocalDateTime vipExpireTime;
    
    // ==================== 占卜次数 ====================
    
    /**
     * 今日占卜次数
     */
    private Integer dailyDivinationCount;
    
    /**
     * 总占卜次数
     */
    private Integer totalDivinationCount;
    
    /**
     * 最后占卜时间（精确到秒，包含日期信息）
     * 需要日期时使用: lastDivinationTime.toLocalDate()
     */
    private LocalDateTime lastDivinationTime;
    
    // ==================== 账号状态 ====================
    
    /**
     * 账号状态
     * 0-正常, 1-锁定, 2-禁用
     */
    private Integer status;
    
    /**
     * 登录失败次数
     */
    private Integer loginFailedCount;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    // ==================== 时间戳 ====================
    
    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标志
     * 0-未删除, 1-已删除
     */
    @TableLogic
    private Integer deleted;
}

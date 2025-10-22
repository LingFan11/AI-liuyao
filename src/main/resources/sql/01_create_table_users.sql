-- ================================
-- 六爻智能解卦系统 - 用户表创建脚本
-- ================================
-- 表名: users
-- 描述: 存储用户账号信息、个人资料和权限信息
-- 版本: 1.0.0
-- 日期: 2025-10-22
-- ================================

USE liuyao_db;

-- 删除表（如果存在）
DROP TABLE IF EXISTS users;

-- 创建用户表
CREATE TABLE users (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    
    -- 账号信息
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名（唯一）',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) UNIQUE DEFAULT NULL COMMENT '邮箱（唯一，可为空）',
    phone VARCHAR(20) UNIQUE DEFAULT NULL COMMENT '手机号（唯一，可为空）',
    
    -- 个人资料
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    signature VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
    
    -- 等级和权限
    level INT DEFAULT 1 COMMENT '用户等级（默认1级）',
    experience INT DEFAULT 0 COMMENT '经验值',
    vip_type INT DEFAULT 0 COMMENT 'VIP类型：0-普通用户, 1-月度VIP, 2-年度VIP',
    vip_expire_time TIMESTAMP NULL DEFAULT NULL COMMENT 'VIP到期时间',
    
    -- 占卜次数
    daily_divination_count INT DEFAULT 0 COMMENT '今日占卜次数',
    total_divination_count INT DEFAULT 0 COMMENT '总占卜次数',
    last_divination_date DATE DEFAULT NULL COMMENT '最后占卜日期',
    
    -- 账号状态
    status INT DEFAULT 0 COMMENT '账号状态：0-正常, 1-锁定, 2-禁用',
    login_failed_count INT DEFAULT 0 COMMENT '登录失败次数',
    last_login_time TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_vip_type (vip_type)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 显示创建成功信息
SELECT 'Table users created successfully!' AS Status;

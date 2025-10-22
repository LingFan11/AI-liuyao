-- ================================
-- 六爻智能解卦系统 - 历史记录表创建脚本
-- ================================
-- 表名: divination_histories
-- 描述: 存储用户的占卜历史记录和收藏信息
-- 版本: 1.0.0
-- 日期: 2025-10-22
-- ================================

USE liuyao_db;

-- 删除表（如果存在）
DROP TABLE IF EXISTS divination_histories;

-- 创建历史记录表
CREATE TABLE divination_histories (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史记录ID',
    
    -- 关联信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    hexagram_id BIGINT NOT NULL COMMENT '卦象ID',
    interpretation_id BIGINT DEFAULT NULL COMMENT '解释ID',
    
    -- 收藏信息
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    favorite_time TIMESTAMP NULL DEFAULT NULL COMMENT '收藏时间',
    
    -- 用户备注
    notes TEXT DEFAULT NULL COMMENT '用户备注',
    tags VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
    
    -- 验证信息
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否已验证结果',
    verification_result VARCHAR(20) DEFAULT NULL COMMENT '验证结果：accurate-准确, inaccurate-不准确, partially-部分准确',
    verification_notes TEXT DEFAULT NULL COMMENT '验证备注',
    verification_time TIMESTAMP NULL DEFAULT NULL COMMENT '验证时间',
    
    -- 访问信息
    view_count INT DEFAULT 1 COMMENT '查看次数',
    last_viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后查看时间',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_hexagram_id (hexagram_id),
    INDEX idx_interpretation_id (interpretation_id),
    INDEX idx_is_favorite (is_favorite),
    INDEX idx_last_viewed_at (last_viewed_at),
    INDEX idx_created_at (created_at),
    
    -- 唯一约束：一个用户对同一个卦象只能有一条历史记录
    UNIQUE KEY uk_user_hexagram (user_id, hexagram_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历史记录表';

-- 显示创建成功信息
SELECT 'Table divination_histories created successfully!' AS Status;

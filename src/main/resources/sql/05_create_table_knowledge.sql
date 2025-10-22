-- ================================
-- 六爻智能解卦系统 - 知识库表创建脚本
-- ================================
-- 表名: hexagram_knowledge, yao_knowledge, case_studies
-- 描述: 存储六十四卦知识库、爻辞知识库和案例库
-- 版本: 1.0.0
-- 日期: 2025-10-22
-- ================================

USE liuyao_db;

-- ================================
-- 1. 卦象知识库表
-- ================================
DROP TABLE IF EXISTS hexagram_knowledge;

CREATE TABLE hexagram_knowledge (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '知识库ID',
    
    -- 卦象基本信息
    sequence INT UNIQUE NOT NULL COMMENT '卦序（1-64）',
    hexagram_name VARCHAR(20) NOT NULL COMMENT '卦名（如：乾为天）',
    hexagram_symbol VARCHAR(10) NOT NULL COMMENT '卦象符号（如：☰☰）',
    hexagram_code VARCHAR(6) NOT NULL COMMENT '六爻编码（如：111111）',
    
    -- 卦象组成
    upper_trigram VARCHAR(10) NOT NULL COMMENT '上卦（如：乾）',
    lower_trigram VARCHAR(10) NOT NULL COMMENT '下卦（如：乾）',
    
    -- 卦宫与世应（六爻基础）
    palace VARCHAR(10) NOT NULL COMMENT '卦宫（乾、坎、艮、震、巽、离、坤、兑）',
    palace_category VARCHAR(10) COMMENT '宫位类别：本宫、一世、二世、三世、四世、五世、游魂、归魂',
    shi_line TINYINT NOT NULL COMMENT '世爻位置（1-6）',
    ying_line TINYINT NOT NULL COMMENT '应爻位置（1-6）',
    
    -- 互卦与综卦
    mutual_hex_number INT DEFAULT NULL COMMENT '互卦序号（1-64）',
    opposite_hex_number INT DEFAULT NULL COMMENT '综卦序号（1-64）',
    reverse_hex_number INT DEFAULT NULL COMMENT '错卦序号（1-64）',
    
    -- 卦辞
    hexagram_text TEXT NOT NULL COMMENT '卦辞原文',
    hexagram_explanation TEXT NOT NULL COMMENT '卦辞白话解释',
    
    -- 象辞
    image_text TEXT COMMENT '象辞原文',
    image_explanation TEXT COMMENT '象辞解释',
    
    -- 彖辞
    judgment_text TEXT COMMENT '彖辞原文',
    judgment_explanation TEXT COMMENT '彖辞解释',
    
    -- 五行属性
    element VARCHAR(10) COMMENT '五行属性（金、木、水、火、土）',
    
    -- 吉凶倾向
    fortune_tendency VARCHAR(20) COMMENT '吉凶倾向：auspicious-吉, inauspicious-凶, neutral-中',
    
    -- 适用场景
    suitable_for VARCHAR(255) COMMENT '适用场景（逗号分隔）',
    
    -- 图片
    image_url VARCHAR(255) DEFAULT NULL COMMENT '卦象图片URL',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_sequence (sequence),
    INDEX idx_hexagram_name (hexagram_name),
    INDEX idx_hexagram_code (hexagram_code),
    INDEX idx_palace (palace),
    INDEX idx_palace_category (palace_category),
    INDEX idx_fortune_tendency (fortune_tendency)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卦象知识库表';


-- ================================
-- 2. 爻辞知识库表
-- ================================
DROP TABLE IF EXISTS yao_knowledge;

CREATE TABLE yao_knowledge (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '爻辞ID',
    
    -- 关联信息
    hexagram_id BIGINT NOT NULL COMMENT '关联的卦象知识库ID',
    
    -- 爻位信息
    position INT NOT NULL COMMENT '爻位（1-初爻, 2-二爻, 3-三爻, 4-四爻, 5-五爻, 6-上爻）',
    position_name VARCHAR(10) NOT NULL COMMENT '爻位名称（如：初九、六二）',
    
    -- 爻辞
    yao_text TEXT NOT NULL COMMENT '爻辞原文',
    yao_explanation TEXT NOT NULL COMMENT '爻辞白话解释',
    
    -- 象辞
    yao_image_text TEXT COMMENT '爻象辞原文',
    yao_image_explanation TEXT COMMENT '爻象辞解释',
    
    -- 五行和六亲
    element VARCHAR(10) COMMENT '五行属性',
    relative VARCHAR(10) COMMENT '六亲（父母、兄弟、子孙、妻财、官鬼）',
    
    -- 吉凶
    fortune VARCHAR(20) COMMENT '吉凶：auspicious-吉, inauspicious-凶, neutral-中',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_hexagram_id (hexagram_id),
    INDEX idx_position (position),
    
    -- 唯一约束
    UNIQUE KEY uk_hexagram_position (hexagram_id, position)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爻辞知识库表';


-- ================================
-- 3. 案例库表
-- ================================
DROP TABLE IF EXISTS case_studies;

CREATE TABLE case_studies (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '案例ID',
    
    -- 案例基本信息
    title VARCHAR(100) NOT NULL COMMENT '案例标题',
    category VARCHAR(20) NOT NULL COMMENT '案例分类：career-事业, love-感情, wealth-财运, health-健康, study-学业, other-其他',
    
    -- 问题信息
    question TEXT NOT NULL COMMENT '占卜问题',
    background TEXT COMMENT '背景描述',
    
    -- 卦象信息
    hexagram_result VARCHAR(100) NOT NULL COMMENT '卦象结果（如：乾为天之天风姤）',
    hexagram_code VARCHAR(6) NOT NULL COMMENT '六爻编码',
    changing_lines VARCHAR(20) COMMENT '变爻位置',
    
    -- 解释信息
    interpretation TEXT NOT NULL COMMENT '解卦内容',
    key_points TEXT COMMENT '关键要点',
    
    -- 验证信息
    verification TEXT COMMENT '实际验证结果',
    verification_time VARCHAR(50) COMMENT '验证时间描述',
    accuracy VARCHAR(20) COMMENT '准确度：accurate-准确, inaccurate-不准确, partially-部分准确',
    
    -- 作者信息
    author VARCHAR(50) DEFAULT 'system' COMMENT '案例作者',
    source VARCHAR(100) COMMENT '案例来源',
    
    -- 公开状态
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_category (category),
    INDEX idx_is_public (is_public),
    INDEX idx_created_at (created_at),
    INDEX idx_view_count (view_count)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案例库表';

-- 显示创建成功信息
SELECT 'Tables hexagram_knowledge, yao_knowledge, case_studies created successfully!' AS Status;

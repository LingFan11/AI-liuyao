-- ================================
-- 六爻智能解卦系统 - 卦象表创建脚本
-- ================================
-- 表名: hexagrams
-- 描述: 存储用户起卦的卦象信息
-- 版本: 1.0.0
-- 日期: 2025-10-22
-- ================================

USE liuyao_db;

-- 删除表（如果存在）
DROP TABLE IF EXISTS hexagrams;

-- 创建卦象表
CREATE TABLE hexagrams (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '卦象ID',
    
    -- 关联信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 问题信息
    question TEXT NOT NULL COMMENT '占卜问题',
    category VARCHAR(20) DEFAULT 'other' COMMENT '占卜类别：career-事业, love-感情, wealth-财运, health-健康, study-学业, other-其他',
    
    -- 卦象信息
    hexagram_code VARCHAR(6) NOT NULL COMMENT '六爻编码（如：111000，1表示阳爻，0表示阴爻）',
    original_hex VARCHAR(50) NOT NULL COMMENT '本卦名称（如：乾为天）',
    original_hex_number INT NOT NULL COMMENT '本卦序号（1-64）',
    changed_hex VARCHAR(50) DEFAULT NULL COMMENT '变卦名称（如有变爻）',
    changed_hex_number INT DEFAULT NULL COMMENT '变卦序号（1-64）',
    changing_lines VARCHAR(20) DEFAULT NULL COMMENT '变爻位置（如：1,3,5表示初爻、三爻、五爻为变爻）',
    
    -- 六爻详细信息（JSON格式）
    yao_details TEXT COMMENT '六爻详细信息（JSON数组，包含每爻的阴阳、五行、六亲等）',
    
    -- 卦宫 / 世应 / 互卦 等规范化要素
    palace VARCHAR(10) DEFAULT NULL COMMENT '卦宫（乾、坎、艮、震、巽、离、坤、兑）',
    shi_line TINYINT DEFAULT NULL COMMENT '世爻位置（1-6）',
    ying_line TINYINT DEFAULT NULL COMMENT '应爻位置（1-6）',
    mutual_hex_number INT DEFAULT NULL COMMENT '互卦序号（1-64）',
    opposite_hex_number INT DEFAULT NULL COMMENT '综卦序号（1-64）',
    na_jia JSON NULL COMMENT '纳甲排盘（JSON：六爻地支、六亲、六神等）',
    
    -- 用神与五行（六爻断卦核心）
    yong_shen VARCHAR(10) DEFAULT NULL COMMENT '用神六亲（父母、兄弟、子孙、妻财、官鬼）',
    yong_shen_element VARCHAR(10) DEFAULT NULL COMMENT '用神五行（金、木、水、火、土）',
    yong_shen_branch VARCHAR(5) DEFAULT NULL COMMENT '用神地支（子丑寅卯辰巳午未申酉戌亥）',
    yong_shen_line TINYINT DEFAULT NULL COMMENT '用神所在爻位（1-6）',
    yong_shen_state VARCHAR(10) DEFAULT NULL COMMENT '用神旺衰状态（旺、相、休、囚、死）',
    
    -- 元神/忌神/仇神（生克关系）
    yuan_shen_line TINYINT DEFAULT NULL COMMENT '元神爻位（生用神的爻）',
    yuan_shen_element VARCHAR(10) DEFAULT NULL COMMENT '元神五行',
    ji_shen_line TINYINT DEFAULT NULL COMMENT '忌神爻位（克用神的爻）',
    ji_shen_element VARCHAR(10) DEFAULT NULL COMMENT '忌神五行',
    chou_shen_line TINYINT DEFAULT NULL COMMENT '仇神爻位（生忌神、克元神的爻）',
    chou_shen_element VARCHAR(10) DEFAULT NULL COMMENT '仇神五行',
    
    -- 空亡与特殊状态
    kong_wang VARCHAR(10) DEFAULT NULL COMMENT '空亡地支（如：子丑）',
    kong_wang_lines VARCHAR(20) DEFAULT NULL COMMENT '空亡爻位（如：1,3表示初爻、三爻空亡）',
    
    -- 时令影响（用于判断旺衰与同一性）
    yue_jian VARCHAR(5) DEFAULT NULL COMMENT '月建（地支）',
    ri_zhi VARCHAR(5) DEFAULT NULL COMMENT '日支（地支）',
    
    -- 同一性签名：由本卦/变卦/变爻/卦宫/世应/互卦/用神(类型+五行+爻位)/月建/日支等规范化后拼接并哈希（由应用层写入）
    signature CHAR(64) DEFAULT NULL COMMENT '同一性签名（SHA-256）',
    
    -- 起卦方式
    method INT NOT NULL COMMENT '起卦方式：1-手动投币, 2-时间起卦, 3-数字起卦, 4-随机起卦',
    method_detail TEXT COMMENT '起卦详情（JSON格式，记录起卦的具体参数）',
    
    -- 时空信息
    divination_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '占卜时间',
    location VARCHAR(100) DEFAULT NULL COMMENT '占卜地点',
    weather VARCHAR(50) DEFAULT NULL COMMENT '天气情况',
    lunar_date VARCHAR(50) DEFAULT NULL COMMENT '农历日期',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_divination_time (divination_time),
    INDEX idx_category (category),
    INDEX idx_original_hex_number (original_hex_number),
    INDEX idx_method (method),
    INDEX idx_palace (palace),
    INDEX idx_mutual_hex_number (mutual_hex_number),
    INDEX idx_yong_shen (yong_shen),
    INDEX idx_yong_shen_state (yong_shen_state),
    INDEX idx_created_at (created_at),
    
    -- 每个用户下，完全一致（含是否有变卦、卦宫、世应、互卦、用神五行等）的卦象仅允许一条
    UNIQUE KEY uk_user_hex_signature (user_id, signature)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卦象表';

-- 显示创建成功信息
SELECT 'Table hexagrams created successfully!' AS Status;

-- ================================
-- 六爻智能解卦系统 - 解释表创建脚本
-- ================================
-- 表名: interpretations
-- 描述: 存储卦象的解释信息（基础解释和AI解释）
-- 版本: 1.0.0
-- 日期: 2025-10-22
-- ================================

USE liuyao_db;

-- 删除表（如果存在）
DROP TABLE IF EXISTS interpretations;

-- 创建解释表
CREATE TABLE interpretations (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '解释ID',
    
    -- 关联信息
    hexagram_id BIGINT NOT NULL COMMENT '卦象ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 基础解释
    basic_interpretation TEXT COMMENT '基础解释（来自知识库）',
    hexagram_text TEXT COMMENT '卦辞原文',
    yao_texts TEXT COMMENT '爻辞原文（JSON数组）',
    
    -- AI解释
    ai_interpretation TEXT COMMENT 'AI智能解释',
    ai_model VARCHAR(50) DEFAULT 'qwen3-max' COMMENT 'AI模型名称',
    ai_prompt TEXT COMMENT 'AI提示词',
    
    -- 详细分析
    yao_analysis TEXT COMMENT '六爻详细分析（JSON格式）',
    five_elements_analysis TEXT COMMENT '五行生克分析',
    six_relatives_analysis TEXT COMMENT '六亲分析',
    world_response_analysis TEXT COMMENT '世应分析',
    
    -- 用神体系分析（六爻断卦核心）
    yong_shen_analysis TEXT COMMENT '用神旺衰分析',
    yuan_shen_analysis TEXT COMMENT '元神（生用神）分析',
    ji_shen_analysis TEXT COMMENT '忌神（克用神）分析',
    chou_shen_analysis TEXT COMMENT '仇神（生忌神、克元神）分析',
    
    -- 动爻与变化分析
    dong_yao_analysis TEXT COMMENT '动爻生克分析（JSON格式）',
    bian_yao_analysis TEXT COMMENT '变爻影响分析',
    
    -- 特殊状态分析
    kong_wang_analysis TEXT COMMENT '空亡影响分析',
    wang_shuai_analysis TEXT COMMENT '旺衰综合分析',
    yue_po_analysis TEXT COMMENT '月破分析',
    ri_po_analysis TEXT COMMENT '日破分析',
    he_chong_analysis TEXT COMMENT '合冲分析',
    
    -- 判断结果
    judgment VARCHAR(50) DEFAULT NULL COMMENT '吉凶判断：auspicious-吉, inauspicious-凶, neutral-中',
    judgment_score INT DEFAULT NULL COMMENT '吉凶评分（0-100，分数越高越吉）',
    advice TEXT COMMENT '行动建议',
    
    -- 置信度
    confidence DECIMAL(3,2) DEFAULT NULL COMMENT '置信度（0.00-1.00）',
    
    -- 时间戳
    interpreted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '解释时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除, 1-已删除',
    
    -- 索引
    INDEX idx_hexagram_id (hexagram_id),
    INDEX idx_user_id (user_id),
    INDEX idx_judgment (judgment),
    INDEX idx_judgment_score (judgment_score),
    INDEX idx_confidence (confidence),
    INDEX idx_interpreted_at (interpreted_at),
    INDEX idx_created_at (created_at),
    
    -- 唯一约束：一个卦象只能有一条解释记录
    UNIQUE KEY uk_hexagram_interpretation (hexagram_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='解释表';

-- 显示创建成功信息
SELECT 'Table interpretations created successfully!' AS Status;

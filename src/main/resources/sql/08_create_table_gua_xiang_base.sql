-- ================================
-- 六爻智能解卦系统 - 64卦基础数据表创建脚本
-- ================================
-- 表名: gua_xiang_base
-- 描述: 存储64卦的固有属性（程序生成的静态参考数据）
-- 版本: 1.0.0
-- 日期: 2025-10-29
-- ================================

USE liuyao_db;

-- 删除表（如果存在）
DROP TABLE IF EXISTS gua_xiang_base;

-- 创建64卦基础数据表
CREATE TABLE gua_xiang_base (
    -- 主键
    id INT PRIMARY KEY COMMENT '卦象ID（1-64）',
    
    -- 卦象基本信息
    gua_name VARCHAR(20) NOT NULL COMMENT '卦名（如"乾为天"、"天风姤"）',
    suo_shu_gong VARCHAR(10) NOT NULL COMMENT '所属宫（如"乾宫"、"坎宫"）',
    gong_wu_xing VARCHAR(5) NOT NULL COMMENT '宫五行（金、木、水、火、土）',
    
    -- 世应信息
    shi_yao_wei TINYINT NOT NULL COMMENT '世爻位（1-6）',
    ying_yao_wei TINYINT NOT NULL COMMENT '应爻位（1-6）',
    
    -- 上下卦
    shang_gua VARCHAR(5) NOT NULL COMMENT '上卦（乾、兑、离、震、巽、坎、艮、坤）',
    xia_gua VARCHAR(5) NOT NULL COMMENT '下卦（乾、兑、离、震、巽、坎、艮、坤）',
    
    -- 卦类型
    gua_lei_xing VARCHAR(10) NOT NULL COMMENT '卦类型（本宫、一世、二世、三世、四世、五世、游魂、归魂）',
    
    -- 索引
    INDEX idx_suo_shu_gong (suo_shu_gong),
    INDEX idx_gua_lei_xing (gua_lei_xing),
    INDEX idx_gua_name (gua_name)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='64卦基础数据表（静态参考数据）';

-- 显示创建成功信息
SELECT 'Table gua_xiang_base created successfully!' AS Status;

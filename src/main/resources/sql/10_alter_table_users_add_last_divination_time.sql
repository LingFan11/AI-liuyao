-- ================================
-- 六爻智能解卦系统 - 用户表增加字段
-- ================================
-- 功能: 为users表添加last_divination_time字段
-- 版本: 1.0.0
-- 日期: 2025-10-29
-- ================================

USE liuyao_db;

-- 添加最后占卜时间字段（精确到时分秒）
ALTER TABLE users 
ADD COLUMN last_divination_time TIMESTAMP NULL DEFAULT NULL COMMENT '最后占卜时间（精确到秒）' 
AFTER last_divination_date;

-- 显示修改成功信息
SELECT 'Column last_divination_time added to users table successfully!' AS Status;

-- ================================
-- 六爻智能解卦系统 - 测试数据插入脚本
-- ================================
-- 描述: 插入测试用户和测试占卜数据
-- 版本: 2.0.0
-- 日期: 2025-10-22
-- 更新: 新增卦宫、世应、用神体系等完整测试数据
-- 注意: 此脚本仅用于开发和测试环境，生产环境请勿执行
-- ================================

USE liuyao_db;

-- ================================
-- 1. 插入测试用户
-- ================================
-- 密码均为: 123456 (BCrypt加密后的值需要在应用中生成，这里使用占位符)
INSERT INTO users (username, password, email, phone, nickname, level, vip_type, status) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYIkXXXXXXXXXXXXXXXX', 'test1@liuyao.com', '13800138001', '测试用户1', 1, 0, 0),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYIkXXXXXXXXXXXXXXXX', 'test2@liuyao.com', '13800138002', '测试用户2', 5, 1, 0),
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYIkXXXXXXXXXXXXXXXX', 'admin@liuyao.com', '13800138888', '管理员', 99, 2, 0),
('vipuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYIkXXXXXXXXXXXXXXXX', 'vip@liuyao.com', '13900139000', 'VIP用户', 10, 2, 0);

-- ================================
-- 2. 插入测试卦象数据
-- ================================

-- 测试卦象1：乾为天（用户1，占事业，无变爻）
INSERT INTO hexagrams (user_id, question, category, hexagram_code, original_hex, original_hex_number,
    changed_hex, changed_hex_number, changing_lines,
    palace, shi_line, ying_line, mutual_hex_number,
    yong_shen, yong_shen_element, yong_shen_branch, yong_shen_line, yong_shen_state,
    yuan_shen_line, yuan_shen_element, ji_shen_line, ji_shen_element,
    yue_jian, ri_zhi, kong_wang, kong_wang_lines,
    signature, method, divination_time) VALUES
(1, '今年事业发展如何？', 'career', '111111', '乾为天', 1,
NULL, NULL, NULL,
'乾', 6, 3, 1,
'官鬼', '金', '戌', 5, '旺',
4, '土', NULL, NULL,
'寅', '辰', '子丑', NULL,
'a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2', 4, NOW());

-- 测试卦象2：坤为地变天地否（用户1，占合作，三爻变）
INSERT INTO hexagrams (user_id, question, category, hexagram_code, original_hex, original_hex_number,
    changed_hex, changed_hex_number, changing_lines,
    palace, shi_line, ying_line, mutual_hex_number,
    yong_shen, yong_shen_element, yong_shen_branch, yong_shen_line, yong_shen_state,
    yuan_shen_line, yuan_shen_element, ji_shen_line, ji_shen_element,
    yue_jian, ri_zhi, kong_wang, kong_wang_lines,
    signature, method, divination_time) VALUES
(1, '这次合作能否成功？', 'career', '000000', '坤为地', 2,
'天地否', 12, '1,2,3',
'坤', 6, 3, 2,
'妻财', '土', '未', 4, '相',
2, '金', 6, '木',
'寅', '辰', '子丑', NULL,
'b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3', 1, NOW());

-- 测试卦象3：水雷屯（用户2，占项目，无变爻）
INSERT INTO hexagrams (user_id, question, category, hexagram_code, original_hex, original_hex_number,
    changed_hex, changed_hex_number, changing_lines,
    palace, shi_line, ying_line, mutual_hex_number,
    yong_shen, yong_shen_element, yong_shen_branch, yong_shen_line, yong_shen_state,
    yuan_shen_line, yuan_shen_element, ji_shen_line, ji_shen_element,
    yue_jian, ri_zhi, kong_wang, kong_wang_lines,
    signature, method, divination_time) VALUES
(2, '新项目启动时机是否合适？', 'career', '010001', '水雷屯', 3,
NULL, NULL, NULL,
'震', 1, 4, 23,
'子孙', '木', '寅', 2, '旺',
5, '水', 4, '金',
'寅', '午', '戌亥', NULL,
'c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4', 2, NOW());

-- 测试卦象4：地天泰（用户2，占感情，无变爻）
INSERT INTO hexagrams (user_id, question, category, hexagram_code, original_hex, original_hex_number,
    changed_hex, changed_hex_number, changing_lines,
    palace, shi_line, ying_line, mutual_hex_number,
    yong_shen, yong_shen_element, yong_shen_branch, yong_shen_line, yong_shen_state,
    yuan_shen_line, yuan_shen_element, ji_shen_line, ji_shen_element,
    yue_jian, ri_zhi, kong_wang, kong_wang_lines,
    signature, method, divination_time) VALUES
(2, '感情发展顺利吗？', 'love', '000111', '地天泰', 11,
NULL, NULL, NULL,
'乾', 4, 1, 1,
'妻财', '土', '未', 6, '旺',
2, '金', 5, '木',
'寅', '午', '申酉', NULL,
'd4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4i5', 4, NOW());

-- 测试卦象5：天火同人（VIP用户，占团队，五爻动）
INSERT INTO hexagrams (user_id, question, category, hexagram_code, original_hex, original_hex_number,
    changed_hex, changed_hex_number, changing_lines,
    palace, shi_line, ying_line, mutual_hex_number,
    yong_shen, yong_shen_element, yong_shen_branch, yong_shen_line, yong_shen_state,
    yuan_shen_line, yuan_shen_element, ji_shen_line, ji_shen_element,
    yue_jian, ri_zhi, kong_wang, kong_wang_lines,
    signature, method, divination_time) VALUES
(4, '团队合作能否顺利？', 'career', '111101', '天火同人', 13,
'天山遁', 33, '5',
'离', 1, 4, 44,
'兄弟', '火', '巳', 3, '旺',
1, '木', 6, '水',
'巳', '午', '寅卯', NULL,
'e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4i5j6', 3, NOW());

-- ================================
-- 3. 插入测试解释数据
-- ================================

-- 为卦象1创建解释（乾为天）
INSERT INTO interpretations (hexagram_id, user_id, basic_interpretation, 
    yong_shen_analysis, yuan_shen_analysis, wang_shuai_analysis,
    judgment, judgment_score, confidence) VALUES
(1, 1, '乾为天卦，六爻纯阳，象征天道刚健。事业蒸蒸日上，宜积极进取。',
'用神官鬼持世，自身能力强，临月建，旺相有力。',
'元神父母爻在四爻，生用神有力，得贵人相助。',
'用神旺相，月建生扶，日辰不冲不克，整体旺衰为旺相，利于发展。',
'auspicious', 85, 0.85);

-- 为卦象2创建解释（坤为地变天地否）
INSERT INTO interpretations (hexagram_id, user_id, basic_interpretation,
    yong_shen_analysis, ji_shen_analysis, dong_yao_analysis, wang_shuai_analysis,
    judgment, judgment_score, confidence) VALUES
(2, 1, '坤为地变天地否，初有顺利后遇阻碍。合作需谨慎，防范风险。',
'用神妻财在四爻，相地，力量中等。三爻发动，变化较大。',
'忌神兄弟在六爻，克用神妻财，合作中有竞争或分利之象。',
'初爻、二爻、三爻皆动，三爻化为官鬼，增加变数。动爻过多，事情变化较大。',
'用神虽相地，但三爻动而化官鬼克之，整体力量偏弱，需谨慎应对。',
'neutral', 60, 0.70);

-- 为卦象3创建解释（水雷屯）
INSERT INTO interpretations (hexagram_id, user_id, basic_interpretation,
    yong_shen_analysis, yuan_shen_analysis, kong_wang_analysis, wang_shuai_analysis,
    judgment, judgment_score, confidence) VALUES
(3, 2, '水雷屯卦，万物初生之象。新项目启动会遇到困难，但不必灰心，坚持必有收获。',
'用神子孙在二爻，临月建，旺相有力。子孙主顺利、平安。',
'元神妻财在五爻，生用神子孙，但隔爻相生，力量稍弱。',
'空亡在戌亥，不影响用神和主要爻位，空亡对此卦影响较小。',
'用神旺相，得月建生扶，虽有困难但能克服，整体向好。',
'neutral', 55, 0.65);

-- 为卦象4创建解释（地天泰）
INSERT INTO interpretations (hexagram_id, user_id, basic_interpretation,
    yong_shen_analysis, yuan_shen_analysis, world_response_analysis, wang_shuai_analysis,
    judgment, judgment_score, confidence) VALUES
(4, 2, '地天泰卦，天地交泰之象。感情发展顺利，双方和谐美满。',
'用神妻财在六爻，旺相有力，感情基础稳固。',
'元神兄弟在二爻，虽为元神但兄弟克妻财，需注意朋友介入或第三者。',
'世爻在四爻，应爻在初爻。世应相生，双方感情和谐，互相扶持。',
'用神旺相，月建生扶，天地交泰，整体大吉，感情顺遂。',
'auspicious', 90, 0.90);

-- 为卦象5创建解释（天火同人）
INSERT INTO interpretations (hexagram_id, user_id, basic_interpretation,
    yong_shen_analysis, yuan_shen_analysis, dong_yao_analysis, bian_yao_analysis, wang_shuai_analysis,
    judgment, judgment_score, confidence) VALUES
(5, 4, '天火同人卦，志同道合之象。团队团结一致，合作必定成功。',
'用神兄弟在三爻，临月建旺相，团队力量强大。',
'元神子孙在初爻，生用神兄弟，团队成员和谐，创造力强。',
'五爻发动，为父母爻，生兄弟用神，有贵人相助，领导支持。',
'五爻父母动化为艮土，继续生用神，变化有利，团队发展向好。',
'用神旺相，得月建和日辰生扶，动爻生助，整体大吉，合作顺利。',
'auspicious', 88, 0.88);

-- ================================
-- 4. 插入测试历史记录
-- ================================

-- 用户1的历史记录
INSERT INTO divination_histories (user_id, hexagram_id, interpretation_id, is_favorite, notes) VALUES
(1, 1, 1, TRUE, '第一次占卜，结果很准！'),
(1, 2, 2, FALSE, '需要再观察一段时间');

-- 用户2的历史记录
INSERT INTO divination_histories (user_id, hexagram_id, interpretation_id, is_favorite, notes) VALUES
(2, 3, 3, FALSE, '初期困难正常'),
(2, 4, 4, TRUE, '感情卦，很开心！');

-- VIP用户的历史记录
INSERT INTO divination_histories (user_id, hexagram_id, interpretation_id, is_favorite, notes) VALUES
(4, 5, 5, TRUE, '团队项目，留作参考');

-- ================================
-- 5. 插入测试案例
-- ================================

INSERT INTO case_studies (title, category, question, hexagram_result, hexagram_code, interpretation, verification, accuracy, is_public) VALUES
('事业转型案例', 'career', '是否应该辞职创业？', '地天泰之天地否', '000111', 
'初期顺利但后期会遇到挑战，建议做好充分准备后再行动。用神妻财临月建旺相，但变卦为天地否，有阻隔之象。', 
'确实创业初期顺利，但半年后遇到资金困难，幸好提前准备了备用金。', 
'accurate', TRUE),

('感情复合案例', 'love', '分手后能否复合？', '水地比', '010000', 
'双方有复合的可能，但需要主动沟通，修复关系。用神妻财在世爻，有心复合。应爻生世爻，对方也有意。', 
'两个月后成功复合，感情比以前更好。', 
'accurate', TRUE),

('投资理财案例', 'wealth', '这笔投资能否盈利？', '天地否之地天泰', '111000',
'短期可能遇到困难，但长期看好，建议持有。用神妻财休囚，初期不利，但变卦地天泰，后期转好。',
'前三个月亏损，但一年后翻倍盈利。',
'accurate', TRUE);

-- ================================
-- 显示测试数据统计
-- ================================

SELECT '=== Test Data Summary ===' AS '';
SELECT COUNT(*) AS 'Test Users' FROM users WHERE username LIKE 'test%' OR username IN ('admin', 'vipuser');
SELECT COUNT(*) AS 'Test Hexagrams' FROM hexagrams;
SELECT COUNT(*) AS 'Test Interpretations' FROM interpretations;
SELECT COUNT(*) AS 'Test Histories' FROM divination_histories;
SELECT COUNT(*) AS 'Test Cases' FROM case_studies;
SELECT '' AS '';
SELECT '✅ Test data inserted successfully!' AS Status;
SELECT '' AS '';
SELECT '📝 Notes:' AS '';
SELECT '1. 测试用户密码均为: 123456 (需在应用中使用BCrypt加密)' AS '';
SELECT '2. 测试卦象包含完整的卦宫、世应、用神体系数据' AS '';
SELECT '3. signature字段为示例值，实际应由应用层计算生成' AS '';
SELECT '4. 测试解释包含用神/元神/忌神/旺衰等专业分析' AS '';

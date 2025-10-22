-- ================================
-- 六爻智能解卦系统 - 六十四卦数据插入脚本
-- ================================
-- 表名: hexagram_knowledge
-- 描述: 插入六十四卦基础数据（包含卦宫、世应、互卦等完整信息）
-- 版本: 2.0.0
-- 日期: 2025-10-22
-- 更新: 新增卦宫、世应、互卦、综卦、错卦等字段
-- ================================

USE liuyao_db;

-- 清空表数据（保留表结构）
TRUNCATE TABLE hexagram_knowledge;

-- ================================
-- 乾宫八卦
-- ================================

-- 第1卦 乾为天（乾宫本宫卦，六世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(1, '乾为天', '☰☰', '111111', '乾', '乾',
'乾', '本宫', 6, 3, 1, 2, 2,
'乾：元亨，利贞。',
'乾卦象征天，具有刚健、积极、创造的特质。元亨利贞四德齐备，大吉大利。事业蒸蒸日上，但需谨慎守正。',
'金', 'auspicious', '事业发展,创业投资,求职升迁');

-- ================================
-- 坤宫八卦
-- ================================

-- 第2卦 坤为地（坤宫本宫卦，六世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(2, '坤为地', '☷☷', '000000', '坤', '坤',
'坤', '本宫', 6, 3, 2, 1, 1,
'坤：元亨，利牝马之贞。君子有攸往，先迷后得，主利。',
'坤卦象征地，具有柔顺、包容、承载的特质。顺应天时，厚德载物。宜守不宜攻，以柔克刚。',
'土', 'auspicious', '守成发展,人际关系,团队合作');

-- ================================
-- 震宫八卦
-- ================================

-- 第3卦 水雷屯（震宫一世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(3, '水雷屯', '☵☳', '010001', '坎', '震',
'震', '一世', 1, 4, 23, 50, 50,
'屯：元亨，利贞。勿用有攸往，利建侯。',
'屯卦象征万物初生的艰难时期。如草木破土而出，虽有困难但充满生机。不宜贸然行动，宜守正待时。',
'水', 'neutral', '创业初期,新项目启动,困难求助');

-- 第4卦 山水蒙（坎宫一世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(4, '山水蒙', '☶☵', '100010', '艮', '坎',
'坎', '一世', 1, 4, 24, 49, 49,
'蒙：亨。匪我求童蒙，童蒙求我。',
'蒙卦象征启蒙教育。如童蒙求学，需要诚心求教。适合学习、请教、培训等事宜。',
'水', 'neutral', '学习进修,寻求指导,教育培训');

-- 第5卦 水天需（乾宫二世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(5, '水天需', '☵☰', '010111', '坎', '乾',
'乾', '二世', 2, 5, 37, 6, 35,
'需：有孚，光亨，贞吉。利涉大川。',
'需卦象征等待。前方有险阻，需要等待时机。诚信守正，终得亨通。不可急躁冒进。',
'水', 'neutral', '等待时机,耐心守候,蓄势待发');

-- 第6卦 天水讼（乾宫三世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(6, '天水讼', '☰☵', '111010', '乾', '坎',
'乾', '三世', 3, 6, 37, 5, 36,
'讼：有孚，窒惕，中吉。终凶。利见大人，不利涉大川。',
'讼卦象征争讼纠纷。天与水违行，上下不合。宜和解，不宜诉讼。中途止讼为吉，坚持到底必凶。',
'水', 'inauspicious', '纠纷调解,法律咨询,避免冲突');

-- 第7卦 地水师（坎宫二世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(7, '地水师', '☷☵', '000010', '坤', '坎',
'坎', '二世', 2, 5, 24, 8, 13,
'师：贞，丈人吉，无咎。',
'师卦象征军队、团队。需要有德高望重的领导者统帅。纪律严明，师出有名则吉。',
'水', 'neutral', '团队管理,人员调配,组织协调');

-- 第8卦 水地比（坎宫三世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(8, '水地比', '☵☷', '010000', '坎', '坤',
'坎', '三世', 3, 6, 23, 7, 14,
'比：吉。原筮，元永贞，无咎。',
'比卦象征亲近、辅助。众人亲附，团结一致。宜建立良好人际关系，互助合作。',
'水', 'auspicious', '人际交往,寻求合作,建立联盟');

-- 第9卦 风天小畜（巽宫一世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(9, '风天小畜', '☴☰', '011111', '巽', '乾',
'巽', '一世', 1, 4, 37, 10, 43,
'小畜：亨。密云不雨，自我西郊。',
'小畜卦象征小有积蓄。力量尚不足，需要继续积累。如密云未雨，时机未到。',
'木', 'neutral', '积累资源,稳步发展,小有收获');

-- 第10卦 天泽履（兑宫一世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(10, '天泽履', '☰☱', '111110', '乾', '兑',
'兑', '一世', 1, 4, 37, 9, 44,
'履：履虎尾，不咥人，亨。',
'履卦象征践履、行走。如履虎尾，险中求进。谨慎行事，礼仪得当则亨通。',
'金', 'neutral', '谨慎行事,遵守规则,礼仪交往');

-- 第11卦 地天泰（乾宫四世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(11, '地天泰', '☷☰', '000111', '坤', '乾',
'乾', '四世', 4, 1, 1, 12, 12,
'泰：小往大来，吉亨。',
'泰卦象征通泰、和顺。天地交泰，万物亨通。是极吉之卦，但盛极将衰，需居安思危。',
'土', 'auspicious', '事业发展,合作共赢,心想事成');

-- 第12卦 天地否（乾宫五世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(12, '天地否', '☰☷', '111000', '乾', '坤',
'乾', '五世', 5, 2, 1, 11, 11,
'否：否之匪人，不利君子贞，大往小来。',
'否卦象征闭塞不通。天地不交，万物不通。君子宜退守，小人得势。需要韬光养晦，等待转机。',
'金', 'inauspicious', '避险守成,静观其变,等待时机');

-- 第13卦 天火同人（离宫一世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(13, '天火同人', '☰☲', '111101', '乾', '离',
'离', '一世', 1, 4, 44, 7, 7,
'同人：同人于野，亨。利涉大川，利君子贞。',
'同人卦象征团结、聚合。志同道合之人相聚，众志成城。适合合作、结盟、公共事业。',
'火', 'auspicious', '团队合作,建立联盟,公共事务');

-- 第14卦 火天大有（离宫二世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(14, '火天大有', '☲☰', '101111', '离', '乾',
'离', '二世', 2, 5, 43, 8, 8,
'大有：元亨。',
'大有卦象征大有所得。如日中天，光明普照。事业兴旺，财运亨通。但需谦虚谨慎，防止骄傲。',
'火', 'auspicious', '事业成功,财富积累,名利双收');

-- 第15卦 地山谦（艮宫二世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(15, '地山谦', '☷☶', '000100', '坤', '艮',
'艮', '二世', 2, 5, 40, 16, 52,
'谦：亨，君子有终。',
'谦卦象征谦虚。高山隐于地下，谦卑自处。谦虚受益，骄傲受损。六十四卦中唯一六爻皆吉之卦。',
'土', 'auspicious', '谦虚做人,低调行事,长久发展');

-- 第16卦 雷地豫（坤宫一世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(16, '雷地豫', '☳☷', '001000', '震', '坤',
'坤', '一世', 1, 4, 2, 15, 51,
'豫：利建侯行师。',
'豫卦象征和悦、安乐。如春雷震地，万物欣悦。适合娱乐、庆典、发动群众。',
'木', 'auspicious', '庆祝活动,娱乐休闲,顺势而为');

-- 第17卦 泽雷随（震宫四世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(17, '泽雷随', '☱☳', '110001', '兑', '震',
'震', '四世', 4, 1, 54, 18, 58,
'随：元亨利贞，无咎。',
'随卦象征跟随、顺从。以诚心随人，以正道随事。选择正确的追随对象，顺应时势。',
'金', 'auspicious', '跟随潮流,顺应时势,听从建议');

-- 第18卦 山风蛊（巽宫五世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(18, '山风蛊', '☶☴', '100011', '艮', '巽',
'巽', '五世', 5, 2, 53, 17, 57,
'蛊：元亨，利涉大川。先甲三日，后甲三日。',
'蛊卦象征弊病、腐败。如蛊虫侵害，需要整治。适合改革、治理、清理旧弊。',
'木', 'neutral', '改革整顿,清理弊端,去旧换新');

-- 第19卦 地泽临（兑宫二世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(19, '地泽临', '☷☱', '000110', '坤', '兑',
'兑', '二世', 2, 5, 24, 20, 41,
'临：元亨利贞。至于八月有凶。',
'临卦象征君临、监管。以上临下，以大临小。事业渐入佳境，但需警惕盛极而衰。',
'土', 'auspicious', '领导管理,监督指导,把握时机');

-- 第20卦 风地观（巽宫三世卦）
INSERT INTO hexagram_knowledge (sequence, hexagram_name, hexagram_symbol, hexagram_code, upper_trigram, lower_trigram,
    palace, palace_category, shi_line, ying_line, mutual_hex_number, opposite_hex_number, reverse_hex_number,
    hexagram_text, hexagram_explanation, element, fortune_tendency, suitable_for) VALUES
(20, '风地观', '☴☷', '011000', '巽', '坤',
'巽', '三世', 3, 6, 23, 19, 42,
'观：盥而不荐，有孚颙若。',
'观卦象征观察、示范。如高台观景，观察形势。适合学习观察、树立榜样、宗教信仰。',
'木', 'neutral', '观察形势,学习榜样,考察调研');

-- ================================
-- 数据说明
-- ================================
-- 注意：
-- 1. 以上仅插入前20卦作为示例数据
-- 2. 完整的64卦数据需要补充后续44卦
-- 3. palace_category: 本宫/一世/二世/三世/四世/五世/游魂/归魂
-- 4. shi_line: 世爻位置（1-6），ying_line: 应爻位置（世爻相隔两位）
-- 5. mutual_hex_number: 互卦，取2-5爻组成
-- 6. opposite_hex_number: 综卦，卦象上下颠倒
-- 7. reverse_hex_number: 错卦，六爻阴阳全变

-- 显示插入成功信息
SELECT 'Hexagram knowledge data inserted successfully! (20 hexagrams with complete fields)' AS Status;
SELECT COUNT(*) AS 'Total Hexagrams' FROM hexagram_knowledge;

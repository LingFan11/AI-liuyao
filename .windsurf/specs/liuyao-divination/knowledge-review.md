# 六爻知识库分析报告

**创建时间**: 2025-10-28  
**分析者**: Linus Review  
**任务**: 六爻系统整体重构  
**目的**: 系统性梳理知识库，发现设计缺陷，构建完整的六爻预测系统架构

---

## 📚 阅读进度

### ✅ 已完成
- [x] knowledge-liuyao01.md - 八宫六十四卦基础数据
- [x] knowledge-liuyao02.md - 六亲理论与用神体系
- [x] knowledge-liuyao03.md - 动静生克、月将日辰、六神理论
- [x] knowledge-liuyao04.md - 六合六冲三合理论
- [x] knowledge-liuyao05.md - 六冲详解、暗动、动散理论
- [x] knowledge-liuyao06.md - 卦变生克理论
- [x] knowledge-liuyao07.md - 反吟、伏吟、旬空理论
- [x] knowledge-liuyao08.md - 生旺墓绝、应期断法、归魂游魂（卷一完）
- [x] knowledge-liuyao09.md - 月破理论、飞伏神理论（卷二）
- [x] knowledge-liuyao10.md - 进神退神理论（卷二）

### ⏳ 待阅读
- [ ] knowledge-liuyao11.md - （待确认）
- [ ] knowledge-liuyao12.md - （待确认）

---

## 🔴 关键发现：设计缺陷清单

### 1. ❌ 投币规则理解错误（已修正）

**错误来源**: 最初设计时对投币结果的映射理解有误

**正确规则**:
```
3个正面（交） → 老阳（9） → 动爻 → 阳爻 ━━━ ×
2个正面（单） → 少阳（7） → 静爻 → 阳爻 ━━━
1个正面（拆） → 少阴（8） → 静爻 → 阴爻 ━ ━
0个正面（重） → 老阴（6） → 动爻 → 阴爻 ━ ━ ○
```

**修正方案**:
```java
public enum YaoType {
    OLD_YANG(9, true, true, "老阳", "交"),
    YOUNG_YANG(7, true, false, "少阳", "单"),
    YOUNG_YIN(8, false, false, "少阴", "拆"),
    OLD_YIN(6, false, true, "老阴", "重");
    
    public final int value;
    public final boolean yang;
    public final boolean changing;
    public final String name;
    public final String coinName;
}
```

---

### 2. ❌ 世应位置规律未体现

**发现**: 世应位置不是随机的，有明确规律

**八宫世应规律**:
| 卦序 | 世爻位置 | 应爻位置 | 备注 |
|------|---------|---------|------|
| 本宫卦 | 6 | 3 | 如"乾为天" |
| 一世卦 | 1 | 4 | 如"天风姤" |
| 二世卦 | 2 | 5 | 如"天山遁" |
| 三世卦 | 3 | 6 | 如"天地否" |
| 四世卦 | 4 | 1 | 如"风地观" |
| 五世卦 | 5 | 2 | 如"山地剥" |
| 游魂卦 | 4 | 1 | 如"火地晋" |
| 归魂卦 | 3 | 6 | 如"火天大有" |

**数据库需要补充**:
```sql
ALTER TABLE hexagrams ADD COLUMN gua_order VARCHAR(10) 
COMMENT '卦序（本宫/一世/二世/三世/四世/五世/游魂/归魂）';
```

---

### 3. ❌ 八宫五行归属缺失

**发现**: 每个宫都有固定的五行属性，这是计算六亲的基础

**八宫五行表**:
| 宫位 | 五行属性 | 包含卦数 |
|------|---------|---------|
| 乾宫 | 金 | 8卦 |
| 兑宫 | 金 | 8卦 |
| 离宫 | 火 | 8卦 |
| 震宫 | 木 | 8卦 |
| 巽宫 | 木 | 8卦 |
| 坎宫 | 水 | 8卦 |
| 艮宫 | 土 | 8卦 |
| 坤宫 | 土 | 8卦 |

**六亲推算规则**（以乾宫金为例）:
- 生我者 → 土生金 → **父母**
- 我生者 → 金生水 → **子孙**
- 我克者 → 金克木 → **妻财**
- 克我者 → 火克金 → **官鬼**
- 比和者 → 金金相同 → **兄弟**

**影响**: 
- ✅ 六亲配置是预定义的，无需运行时计算
- ✅ 可以直接从数据库读取
- ❌ 之前设计的`LiuQinCalculator`是多余的

---

### 4. ❌ 爻的数据结构不完整

**当前设计问题**:
```java
// ❌ 混淆了运行时状态和静态配置
public class Yao {
    private Integer position;      // 运行时
    private YaoType type;          // 运行时
    private String diZhi;          // 静态配置
    private LiuQin liuQin;         // 静态配置
    // ... 混在一起
}
```

**正确的分层**:
```java
// 1. 静态配置（数据库实体）
@Entity
public class YaoConfig {
    private Integer position;      // 1-6
    private String diZhi;          // 子丑寅卯...
    private WuXing wuXing;         // 金木水火土
    private LiuQin liuQin;         // 父母兄弟...
    private Boolean shiYao;        // 是否世爻
    private Boolean yingYao;       // 是否应爻
}

// 2. 运行时状态（内存对象）
public class YaoState {
    private Integer position;
    private YaoType type;          // 老阳/少阳/老阴/少阴
    private Boolean changing;      // 是否动爻
}

// 3. 响应DTO（组装后）
public class YaoInfo {
    // 从YaoState来
    private YaoType type;
    private Boolean changing;
    
    // 从YaoConfig来
    private String diZhi;
    private WuXing wuXing;
    private LiuQin liuQin;
    
    // 动态计算
    private String liuShen;        // 根据日干计算
}
```

---

### 5. ✅ 六亲是预定义的（设计简化）

**好消息**: 知识库显示，每个卦的每一爻的六亲都是固定的

**示例**（乾为天）:
```
上爻：父母戌土  ━━━ 世
五爻：兄弟申金  ━━━
四爻：官鬼午火  ━━━
三爻：父母辰土  ━━━ 应
二爻：妻财寅木  ━━━
初爻：子孙子水  ━━━
```

**影响**:
- ✅ 删除`LiuQinCalculator`类
- ✅ 384条爻配置数据直接包含六亲信息
- ✅ 运行时只需查询，不需计算

---

### 6. ❌ 天干信息的处理未明确

**观察**: 知识库中大部分爻只有地支，没有天干

```
父母戌土  ← 只有"戌"（地支），没有天干
兄弟申金  ← 只有"申"（地支）
```

**但某些卦例中会出现天干**:
```
父母甲子水  ← 有天干"甲"
```

**结论**:
- 爻的基本配置只需**地支**
- 天干是在特定情况下**动态添加的**（可能与起卦日的干支有关）
- 数据库`YaoConfig`表**不需要天干字段**

---

## 📊 数据库设计修正

### hexagrams表（修正版）

```sql
CREATE TABLE hexagrams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code CHAR(6) NOT NULL UNIQUE COMMENT '卦象代码（111111）',
    name VARCHAR(20) NOT NULL COMMENT '卦名（乾为天）',
    
    -- ✅ 八宫信息
    gong VARCHAR(10) NOT NULL COMMENT '所属宫（乾宫/坎宫...）',
    gong_wu_xing VARCHAR(10) NOT NULL COMMENT '宫五行（金/木/水/火/土）',
    
    -- ✅ 卦序信息
    gua_order VARCHAR(10) NOT NULL COMMENT '卦序（本宫/一世/二世.../游魂/归魂）',
    
    -- ✅ 世应位置
    shi_position INT NOT NULL COMMENT '世爻位置（1-6）',
    ying_position INT NOT NULL COMMENT '应爻位置（1-6）',
    
    -- ✅ 卦象关系
    upper_trigram CHAR(3) NOT NULL COMMENT '外卦（111）',
    lower_trigram CHAR(3) NOT NULL COMMENT '内卦（111）',
    
    -- ✅ 特殊标记
    liu_he BOOLEAN DEFAULT FALSE COMMENT '六合卦',
    liu_chong BOOLEAN DEFAULT FALSE COMMENT '六冲卦',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_gong (gong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### yao_configs表（修正版）

```sql
CREATE TABLE yao_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hexagram_id BIGINT NOT NULL,
    
    -- ✅ 基础信息
    position INT NOT NULL COMMENT '爻位（1-6）',
    
    -- ✅ 纳甲信息
    di_zhi VARCHAR(5) NOT NULL COMMENT '地支（子丑寅...）',
    wu_xing VARCHAR(10) NOT NULL COMMENT '五行（金木水火土）',
    liu_qin VARCHAR(10) NOT NULL COMMENT '六亲（父母/兄弟...）',
    
    -- ✅ 世应标记
    shi_yao BOOLEAN DEFAULT FALSE COMMENT '是否世爻',
    ying_yao BOOLEAN DEFAULT FALSE COMMENT '是否应爻',
    
    FOREIGN KEY (hexagram_id) REFERENCES hexagrams(id),
    UNIQUE KEY uk_hexagram_position (hexagram_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🎯 任务3.1修正计划

### 第1步：数据准备
- [ ] 创建SQL脚本：`01_create_hexagrams_table.sql`
- [ ] 创建SQL脚本：`02_create_yao_configs_table.sql`
- [ ] 创建SQL脚本：`03_insert_64_hexagrams.sql`（基于knowledge-liuyao01.md）
- [ ] 创建SQL脚本：`04_insert_384_yao_configs.sql`（基于knowledge-liuyao01.md）

### 第2步：实体类修正
- [ ] 修正`YaoType`枚举（加入coinName）
- [ ] 删除混乱的`Yao`类
- [ ] 创建`YaoConfig`实体类（数据库映射）
- [ ] 创建`YaoState`类（运行时状态）
- [ ] 修正`Hexagram`实体类（补充字段）

### 第3步：核心服务简化
- [ ] 删除`LiuQinCalculator`（六亲是预定义的）
- [ ] 保留`LiuShenUtil`（六神需要根据日干计算）
- [ ] 简化`DivinationService`（只做数据组装）

### 第4步：测试验证
- [ ] 测试投币结果 → YaoType映射
- [ ] 测试卦象代码生成
- [ ] 测试世应位置正确性
- [ ] 测试六亲配置查询

---

## 📝 待解决的问题

### 问题1：天干何时需要？
- **现状**: 知识库中爻配置没有天干，只有地支
- **疑问**: 天干在什么情况下使用？
- **待确认**: 继续阅读后续文件

### 问题2：六神的配置规则
- **已知**: 六神根据起卦日的天干配置
- **待确认**: 配置规则的完整性（knowledge-liuyao02.md可能有详细说明）

### 问题3：伏神系统
- **已知**: 伏神是当用神不现时的补充
- **待确认**: 伏神的查找规则（需要继续阅读）

### 问题4：月建日辰的影响
- **已知**: 月建日辰对爻的旺衰有影响
- **待确认**: 具体的计算规则

---

## 🚀 下一步行动

1. ✅ **继续阅读knowledge-liuyao02.md**（六亲理论）
2. ⏳ 阅读knowledge-liuyao03.md（用神理论）
3. ⏳ 完成所有知识库文件的阅读
4. ⏳ 整理完整的设计文档
5. ⏳ 生成数据库初始化SQL
6. ⏳ 实现核心代码

---

## 📖 knowledge-liuyao02.md 核心发现

### 1. ✅ 六亲配置规则已确认

**发现**: 六亲配置有明确的算法规则

**六亲定义**（五行生克关系）:
- **父母** = 生我者
- **兄弟** = 比和者（与我同类）
- **子孙** = 我生者
- **妻财** = 我克者
- **官鬼** = 克我者

**配置示例**（乾宫为金）:
```
辰土 → 土生金 → 父母
寅木 → 金克木 → 妻财
子水 → 金生水 → 子孙
午火 → 火克金 → 官鬼
申金 → 金比金 → 兄弟
```

**重要结论**:
- ✅ 六亲可以通过算法计算
- ✅ 但每个卦的六亲配置是固定的
- ✅ 预先导入数据库比运行时计算更高效
- ❌ 不需要`LiuQinCalculator`类（算法可用于验证数据正确性）

---

### 2. ✅ 用神体系（解卦核心，非起卦）

**发现**: 用神是解卦阶段的概念，不属于任务3.1范围

**用神选取规则**:
| 占事类型 | 用神 | 示例 |
|---------|------|------|
| 占父母 | 父母爻 | 占父母、祖父母、师长 |
| 占功名 | 官鬼爻 | 占考试、升迁、官司 |
| 占兄弟 | 兄弟爻 | 占兄弟姐妹 |
| 占妻财 | 妻财爻 | 占妻子、财运、物品 |
| 占子孙 | 子孙爻 | 占子女、医药、六畜 |

**四神体系**:
- **用神** - 所占事项对应的六亲爻
- **元神** - 生用神之爻（帮助）
- **忌神** - 克用神之爻（阻碍）
- **仇神** - 克元神、生忌神之爻（敌人的帮手）

**影响设计**:
- ❌ 任务3.1不需要实现用神选择
- ✅ 但响应DTO要预留扩展空间
- ✅ 数据结构要支持后续解卦功能

---

### 3. ✅ 世应位置规律再确认

**文档明确说明**:
```
乾为天   世在六爻  应在三爻
天风姤   世在初爻  应在四爻
天山遁   世在二爻  应在五爻
天地否   世在三爻  应在六爻
风地观   世在四爻  应在初爻
山地剥   世在五爻  应在二爻
火地晋   世在四爻  应在初爻（游魂）
火天大有 世在三爻  应在六爻（归魂）
```

**规律总结**:
- 本宫卦到五世卦：世爻从6→1→2→3→4→5递增
- 应爻始终与世爻相隔2爻（中间隔1爻）
- 游魂卦特殊：世在4爻
- 归魂卦特殊：世在3爻

---

### 4. ❌ 动变规则需要注意

**发现**: 动爻产生变爻，但变卦的六亲需要特殊处理

**关键原则**（原文）:
> 安六亲者，姤卦之丑土戌土原是父母，今俱写兄弟。
> 盖六亲须照前卦之生克而安也。

**理解**:
- 变卦中的六亲**不是**按变卦的卦宫五行算
- 而是**按本卦的卦宫五行**计算
- 例如：泽天夬（坤宫，土）变天风姤（乾宫，金）
  - 姤卦本来：丑土、戌土是父母（土生金）
  - 但在变卦中：丑土、戌土写兄弟（因为本卦是坤宫土）

**影响设计**:
- ✅ 变卦的爻需要重新计算六亲（按本卦卦宫）
- ❌ 不能直接用变卦的卦象查数据库
- ⚠️ 这是一个复杂的业务逻辑

---

### 5. ✅ 五行生克理论（解卦基础）

**五行相生**:
```
金生水 → 水生木 → 木生火 → 火生土 → 土生金
```

**五行相克**:
```
金克木 → 木克土 → 土克水 → 水克火 → 火克金
```

**应用场景**（解卦阶段）:
- 分析用神旺衰
- 判断元神是否有力
- 判断忌神是否凶险

**影响设计**:
- ❌ 任务3.1不需要实现生克分析
- ✅ 但五行数据必须存储（爻配置中的wu_xing字段）
- ✅ 后续解卦功能会大量使用

---

### 6. ❌ 关键遗漏：六神配置规则未提及

**问题**: knowledge-liuyao02.md没有提到六神的配置规则

**已知信息**（来自之前的设计）:
- 六神根据起卦日的天干配置
- 六神顺序：青龙、朱雀、勾陈、螣蛇、白虎、玄武

**待确认**:
- 六神的完整配置表
- 六神从哪一爻开始配置
- 六神的作用和含义

**需要继续阅读后续文件**

---

### 7. ✅ 天干信息仍未明确

**观察**: knowledge-liuyao02.md中的卦例仍然只有地支

**示例**:
```
父母戌土  ━━━ 世
兄弟申金  ━━━
官鬼午火  ━━━
```

**结论**:
- 爻的基本配置只需地支
- 天干可能是在特定情况下动态添加
- 数据库yao_configs表不需要天干字段

---

## 🔄 设计修正（基于liuyao02）

### 修正1：变卦六亲计算逻辑

**问题**: 之前设计中，变卦直接查数据库获取六亲

**修正**: 变卦的六亲需要按本卦卦宫重新计算

```java
/**
 * 计算变爻的六亲
 * 注意：变卦中爻的六亲按本卦卦宫五行计算，不是按变卦卦宫
 */
public LiuQin calculateChangedYaoLiuQin(
    WuXing originalGongWuXing,  // 本卦卦宫五行
    WuXing changedYaoWuXing     // 变爻的五行
) {
    // 按五行生克关系计算
    if (changedYaoWuXing.sheng(originalGongWuXing)) {
        return LiuQin.FU_MU;  // 生我者为父母
    } else if (originalGongWuXing.sheng(changedYaoWuXing)) {
        return LiuQin.ZI_SUN;  // 我生者为子孙
    } else if (originalGongWuXing.ke(changedYaoWuXing)) {
        return LiuQin.QI_CAI;  // 我克者为妻财
    } else if (changedYaoWuXing.ke(originalGongWuXing)) {
        return LiuQin.GUAN_GUI;  // 克我者为官鬼
    } else {
        return LiuQin.XIONG_DI;  // 比和者为兄弟
    }
}
```

---

### 修正2：响应DTO预留解卦字段

```java
public class YaoInfo {
    // 运行时状态
    private YaoType type;
    private Boolean changing;
    
    // 静态配置
    private String diZhi;
    private WuXing wuXing;
    private LiuQin liuQin;
    
    // 动态计算
    private String liuShen;
    
    // ✅ 预留解卦字段（任务3.1不实现）
    private Boolean isYongShen;    // 是否用神
    private Boolean isYuanShen;    // 是否元神
    private Boolean isJiShen;      // 是否忌神
    private String wangShuai;      // 旺衰状态
}
```

---

### 修正3：五行枚举补充方法

```java
public enum WuXing {
    JIN("金"), MU("木"), SHUI("水"), HUO("火"), TU("土");
    
    public final String name;
    
    /**
     * 判断this是否生target
     */
    public boolean sheng(WuXing target) {
        return switch (this) {
            case JIN -> target == SHUI;
            case SHUI -> target == MU;
            case MU -> target == HUO;
            case HUO -> target == TU;
            case TU -> target == JIN;
        };
    }
    
    /**
     * 判断this是否克target
     */
    public boolean ke(WuXing target) {
        return switch (this) {
            case JIN -> target == MU;
            case MU -> target == TU;
            case TU -> target == SHUI;
            case SHUI -> target == HUO;
            case HUO -> target == JIN;
        };
    }
}
```

---

## 📊 六爻系统整体架构

### 🎯 系统功能全景

#### 阶段1：起卦模块（基础设施）
1. 投币起卦（6次投币 → 生成本卦）
2. 计算变卦（动爻变化 → 生成变卦）
3. 查询卦象数据（从数据库读取64卦配置）
4. 查询爻配置（六亲、五行、世应）
5. 计算六神（根据日干动态配置）
6. 计算干支信息（年月日时干支、旬空）

#### 阶段2：解卦模块（核心功能）
1. 用神选择（根据占事类型）
2. 元神/忌神/仇神判断
3. 旺衰分析（月建日辰影响）
4. 生克分析（五行生克关系）
5. 动变分析（动爻影响）
6. 吉凶判断（综合评估）

#### 阶段3：高级功能（扩展模块）
1. 伏神系统（用神不现的处理）
2. 六合六冲分析
3. 三合三会分析
4. 旬空处理
5. 卦变生克
6. 归魂游魂判断

---

## 📖 knowledge-liuyao03.md 核心发现

### 1. ✅ 六神配置规则找到了！

**完整的六神配置表**（从初爻到上爻）：

| 日干 | 初爻 | 二爻 | 三爻 | 四爻 | 五爻 | 上爻 |
|------|------|------|------|------|------|------|
| 甲乙 | 青龙 | 朱雀 | 勾陈 | 螣蛇 | 白虎 | 玄武 |
| 丙丁 | 朱雀 | 勾陈 | 螣蛇 | 白虎 | 玄武 | 青龙 |
| 戊日 | 勾陈 | 螣蛇 | 白虎 | 玄武 | 青龙 | 朱雀 |
| 己日 | 螣蛇 | 白虎 | 玄武 | 青龙 | 朱雀 | 勾陈 |
| 庚辛 | 白虎 | 玄武 | 青龙 | 朱雀 | 勾陈 | 螣蛇 |
| 壬癸 | 玄武 | 青龙 | 朱雀 | 勾陈 | 螣蛇 | 白虎 |

**六神含义**:
- **青龙** - 吉庆、喜事、文明
- **朱雀** - 文书、口舌、是非
- **勾陈** - 田土、勾连、迟滞
- **螣蛇** - 虚惊、怪异、虚诈
- **白虎** - 凶丧、疾病、刀兵
- **玄武** - 盗贼、暗昧、奸私

**重要原则**（原文）:
> 虎兴而遇吉神，不害其吉；龙动而逢凶曜，难掩其凶。

**结论**:
- ✅ 六神不决定吉凶，只是"附和之神"
- ✅ 卦之吉者，逢青龙而更吉；卦之凶者，逢蛇虎而更凶
- ✅ 玄武主盗贼、朱雀主是非，在特定占事中有指导意义

---

### 2. ❌ 月建日辰系统（解卦核心，极其复杂）

**月建的权力**:
> 月将掌一月之权，司三旬之令。操持万卜之提纲，巡查六爻之善恶。

**月建的作用**:
1. **生扶** - 能助卦爻之衰弱
2. **克制** - 能挫爻象之旺强
3. **合住** - 月建合爻，为月合
4. **冲破** - 月建冲爻，为月破

**日辰的权力**:
> 日辰为六爻之主宰，司四时之旺相。四时俱旺，操生杀之权，与月建同功。

**日辰的特殊作用**:
1. **冲动** - 冲旺相之静爻，即为暗动
2. **日破** - 冲衰弱之静爻，则为日破
3. **冲空则实** - 爻遇旬空，日辰冲起而为用
4. **合处逢冲** - 爻逢合住，遇日建以冲开

**月日配合规则**（极其复杂）:
```
爻临月建：
- 日冲 → 不破
- 日克 → 无伤
- 但生少克多 → 寡难敌众

爻临日建：
- 月冲 → 不破
- 月克 → 无伤
- 但月+动爻同克 → 破亦为破

月生日克 → 得生之七
月克日生 → 得生之八
```

**影响设计**:
- ⚠️ 这是解卦阶段的高级功能
- ⚠️ 算法极其复杂，需要大量条件判断
- ✅ 起卦阶段只需记录月建、日辰信息即可
- ✅ 解卦模块单独实现

---

### 3. ❌ 动静生克规则

**核心原则**:
> 卦有动爻，能克静爻，即使静爻旺相，亦不能克动爻。
> 盖静者，如坐如卧；动者，如行走之人也。

**动变生克规则**（重要！）:
> 变出之爻，能生克冲合本位之动爻，不能生克他爻。
> 而他爻与本位之动爻，亦不能生克变爻。

**理解**:
```
假设本卦：
上爻：酉金  ━ ━ ×→ 变出 巳火
五爻：卯木  ━━━
四爻：丑土  ━ ━ ×→ 变出 酉金

规则：
1. 巳火能克本位的酉金（回头克）
2. 巳火不能克五爻的卯木
3. 丑土能生上爻的酉金
4. 丑土不能生变出的酉金
5. 变出的酉金不能生克他爻
```

**影响设计**:
- ✅ 动爻和变爻的关系需要特殊处理
- ✅ 生克算法要考虑动静关系
- ⚠️ 这是解卦阶段的功能

---

### 4. ✅ 四时旺相规则（月令五行旺衰表）

**春季**（正二三月）:
- **旺** - 寅卯木
- **相** - 巳午火（木生火）
- **休囚** - 金、水、土

**夏季**（四五六月）:
- **旺** - 巳午火
- **相** - 辰戌丑未土（火生土）
- **休囚** - 金、木、水

**秋季**（七八九月）:
- **旺** - 申酉金
- **相** - 亥子水（金生水）
- **休囚** - 木、火、土

**冬季**（十十一十二月）:
- **旺** - 亥子水
- **相** - 寅卯木（水生木）
- **休囚** - 金、土、火

**影响设计**:
- ✅ 需要实现月令与五行旺衰的映射
- ✅ 解卦模块会大量使用
- ✅ 数据库可以预先存储（12个月×5个五行）

---

### 5. ❌ 旬空系统

**旬空规则**:
```
甲子旬 → 空戌亥
甲戌旬 → 空申酉
甲申旬 → 空午未
甲午旬 → 空辰巳
甲辰旬 → 空寅卯
甲寅旬 → 空子丑
```

**旬空的影响**:
- 爻逢旬空 → 目下为空
- 待出旬之日 → 则不空
- 爻临月建 → 逢空亦空（与传统说法不同）

**冲空规则**:
- 日辰冲空爻 → 冲空则实

**影响设计**:
- ✅ 起卦时需要计算旬空（根据日干支）
- ✅ GanZhiUtil需要实现旬空计算

---

### 6. ❌ 动爻逢冲规则

**重要发现**（与传统书籍不同）:
> 爻旺而动，冲之愈动；爻衰而动，冲之则散。
> 旺相者，冲之愈强，休囚无气者，间或有散，亦百中仅一二也。

**结论**:
- ✅ 动爻一般不散
- ✅ 只有休囚无气的动爻才可能散
- ⚠️ 这与很多书上说的"动爻逢冲必散"不同

---

## 🔄 系统架构更新（基于liuyao03）

### 数据结构补充

#### 1. 响应DTO需要补充月日信息

```java
public class DivinationResponse {
    // 本卦变卦信息
    private HexagramInfo original;
    private HexagramInfo changed;
    
    // ✅ 时空信息（更详细）
    private TimeInfo timeInfo;
    
    public static class TimeInfo {
        // 干支信息
        private String yearGanZhi;     // "辛丑年"
        private String monthGanZhi;    // "丙申月"
        private String dayGanZhi;      // "甲寅日"
        private String hourGanZhi;     // "丙寅时"
        
        // ✅ 提取出的关键信息
        private String monthJian;      // 月建："申"
        private String riChen;         // 日辰："寅"
        private String riGan;          // 日干："甲"（用于配置六神）
        
        // ✅ 旬空
        private String kongWang;       // "子丑"
        
        // ✅ 五行旺衰（可选，解卦用）
        private Map<WuXing, String> wangShuai;  // {MU: "旺", HUO: "相", ...}
    }
}
```

#### 2. 六神工具类实现

```java
@Component
public class LiuShenUtil {
    
    private static final Map<String, String[]> LIU_SHEN_MAP = Map.of(
        "甲", new String[]{"青龙", "朱雀", "勾陈", "螣蛇", "白虎", "玄武"},
        "乙", new String[]{"青龙", "朱雀", "勾陈", "螣蛇", "白虎", "玄武"},
        "丙", new String[]{"朱雀", "勾陈", "螣蛇", "白虎", "玄武", "青龙"},
        "丁", new String[]{"朱雀", "勾陈", "螣蛇", "白虎", "玄武", "青龙"},
        "戊", new String[]{"勾陈", "螣蛇", "白虎", "玄武", "青龙", "朱雀"},
        "己", new String[]{"螣蛇", "白虎", "玄武", "青龙", "朱雀", "勾陈"},
        "庚", new String[]{"白虎", "玄武", "青龙", "朱雀", "勾陈", "螣蛇"},
        "辛", new String[]{"白虎", "玄武", "青龙", "朱雀", "勾陈", "螣蛇"},
        "壬", new String[]{"玄武", "青龙", "朱雀", "勾陈", "螣蛇", "白虎"},
        "癸", new String[]{"玄武", "青龙", "朱雀", "勾陈", "螣蛇", "白虎"}
    );
    
    /**
     * 根据日干获取六神配置（从初爻到上爻）
     */
    public static String[] calculate(String dayGan) {
        return LIU_SHEN_MAP.get(dayGan);
    }
}
```

#### 3. 旬空计算工具

```java
public class GanZhiUtil {
    
    private static final Map<String, String> KONG_WANG_MAP = Map.ofEntries(
        Map.entry("甲子", "戌亥"),
        Map.entry("甲戌", "申酉"),
        Map.entry("甲申", "午未"),
        Map.entry("甲午", "辰巳"),
        Map.entry("甲辰", "寅卯"),
        Map.entry("甲寅", "子丑")
    );
    
    /**
     * 计算旬空
     * 根据日干支计算当前旬的空亡地支
     */
    public String calculateKongWang(String dayGanZhi) {
        // 找到所属旬
        // 例如：甲寅日属于"甲寅旬"，空子丑
        // 实现算法...
        return "子丑";
    }
}
```

---

### 系统模块划分更新

#### 起卦模块（阶段1，当前重构目标）
```
输入：投币结果 or 时间 or 数字
处理：
1. 生成本卦代码
2. 查询本卦数据（64卦表）
3. 计算变卦代码
4. 查询变卦数据
5. 计算六神（根据日干）
6. 计算干支信息
7. 计算旬空
输出：完整卦象信息
```

#### 解卦模块（阶段2，后续开发）
```
输入：卦象信息 + 占事类型
处理：
1. 选择用神
2. 判断元神/忌神/仇神
3. 分析月建日辰影响
4. 判断旺相休囚
5. 分析动静生克
6. 综合判断吉凶
输出：解卦结论
```

---

## 📖 knowledge-liuyao04.md 核心发现

### 1. ✅ 六合理论（地支相合）

**六合配对表**：

| 地支对 | 五行关系 | 含义 |
|--------|---------|------|
| 子丑合 | 水土合 | 和谐协作 |
| 寅亥合 | 木水合 | 贵人相助 |
| 卯戌合 | 木土合 | 团结一致 |
| 辰酉合 | 土金合 | 金玉良缘 |
| 巳申合 | 火金合 | 刚柔并济 |
| 午未合 | 火土合 | 相得益彰 |

**六合的六种作用类型**：
1. **日月合爻** - 日辰或月建与卦爻相合（力量最大）
2. **动爻相合** - 两个动爻之间相合
3. **爻动化合** - 动爻变出的爻与本位爻相合（化扶）
4. **卦逢六合** - 本卦就是六合卦（如天地否）
5. **六冲变六合** - 六冲卦变成六合卦（先难后易）
6. **六合变六合** - 六合卦再变六合卦（长久吉祥）

**爻合的四种状态**：
- **合起** - 静爻逢合，得合而起（增强力量）
- **合绊** - 动爻逢合，被合住绊住（束缚不动）
- **合好** - 动爻与动爻相合（和好相助）
- **化扶** - 爻动化出回头合（得扶助）

**重要发现：合中带克**：
> 子丑相合，但丑中有土克子水，若无生扶，言克不言合。

**影响设计**：
- ✅ 需要六合配对表（数据库或枚举）
- ✅ 需要判断六合的类型（日月合、动爻合等）
- ✅ 需要区分合起、合绊、合好、化扶
- ⚠️ 合中带克需要特殊判断

---

### 2. ✅ 六冲理论（地支相冲）

**六冲配对表**：

| 地支对 | 方位关系 | 含义 |
|--------|---------|------|
| 子午冲 | 南北对冲 | 分离破败 |
| 丑未冲 | 对角冲 | 散失无常 |
| 寅申冲 | 对角冲 | 冲击动荡 |
| 卯酉冲 | 东西对冲 | 破裂离散 |
| 辰戌冲 | 对角冲 | 冲突激烈 |
| 巳亥冲 | 对角冲 | 水火不容 |

**六冲的作用**：
1. **日月冲爻** - 日辰或月建冲卦爻（力量最大）
2. **动爻相冲** - 两个动爻之间相冲
3. **动爻冲静爻** - 动爻冲静止的爻
4. **卦逢六冲** - 本卦就是六冲卦（八纯卦）
5. **冲处逢合** - 相冲的爻遇到合的力量
6. **六合变六冲** - 六合卦变成六冲卦（由好转坏）

**冲的两种效果**：
- **冲动** - 冲旺相之静爻，激活力量（暗动）
- **冲破** - 冲衰弱之静爻，破坏损伤（日破）

**特殊情况**：
- 六冲卦变六合 → 先难后易、散而复聚（占婚姻主复合）
- 六合卦变六冲 → 先吉后凶、和而复散

---

### 3. ✅ 三合理论（三合成局）

**三合配对表**：

| 三合局 | 组成地支 | 合成五行 | 长生-帝旺-墓库 |
|--------|---------|---------|--------------|
| 申子辰 | 申、子、辰 | 水局 | 申长生、子帝旺、辰墓库 |
| 巳酉丑 | 巳、酉、丑 | 金局 | 巳长生、酉帝旺、丑墓库 |
| 寅午戌 | 寅、午、戌 | 火局 | 寅长生、午帝旺、戌墓库 |
| 亥卯未 | 亥、卯、未 | 木局 | 亥长生、卯帝旺、未墓库 |

**三合的四种成局方式**：
1. **三爻俱动成局** - 三个爻都发动（力量最强）
2. **两动一静成局** - 两个爻动，一个爻不动也能成局
3. **内卦变出成局** - 内卦初爻、三爻动，变出的爻成局
4. **外卦变出成局** - 外卦四爻、六爻动，变出的爻成局

**三合成局的特殊情况**：
- **虚一待用** - 只有两爻动，不成局，须待后来月日补凑
- **明暗皆动** - 一爻明动、一爻暗动，也作两爻动
- **空破入墓** - 局中有一爻值空破，待填实日成之；有一爻入墓，待冲开日成之
- **日月在局** - 日建或月建有一在局中，谓之局旺，更吉

**三合吉凶判断**：
- 占功名：合成官局 → 官旺大吉
- 占财运：合成财局 → 财库大吉
- 占婚姻：财官旺合局 → 白头相守
- 占官讼：三合成局 → 难以解散（不利）

**关键原则**：
> 用神旺则无不为吉，尤要世爻在局为美。
> 局生世爻吉，局克世爻凶。

---

## 🔄 系统设计补充（基于liuyao04）

### 数据库设计补充

#### 1. 六合配对表

```sql
CREATE TABLE liu_he_pairs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    di_zhi_1 VARCHAR(5) NOT NULL COMMENT '地支1',
    di_zhi_2 VARCHAR(5) NOT NULL COMMENT '地支2',
    wu_xing_1 VARCHAR(10) COMMENT '地支1五行',
    wu_xing_2 VARCHAR(10) COMMENT '地支2五行',
    he_zhong_dai_ke BOOLEAN DEFAULT FALSE COMMENT '是否合中带克',
    UNIQUE KEY uk_pair (di_zhi_1, di_zhi_2)
) COMMENT '六合配对表';

INSERT INTO liu_he_pairs (di_zhi_1, di_zhi_2, wu_xing_1, wu_xing_2, he_zhong_dai_ke) VALUES
('子', '丑', 'SHUI', 'TU', TRUE),   -- 土克水
('寅', '亥', 'MU', 'SHUI', FALSE),
('卯', '戌', 'MU', 'TU', FALSE),
('辰', '酉', 'TU', 'JIN', FALSE),
('巳', '申', 'HUO', 'JIN', TRUE),   -- 火克金
('午', '未', 'HUO', 'TU', FALSE);
```

#### 2. 六冲配对表

```sql
CREATE TABLE liu_chong_pairs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    di_zhi_1 VARCHAR(5) NOT NULL COMMENT '地支1',
    di_zhi_2 VARCHAR(5) NOT NULL COMMENT '地支2',
    fang_wei VARCHAR(20) COMMENT '方位关系',
    UNIQUE KEY uk_pair (di_zhi_1, di_zhi_2)
) COMMENT '六冲配对表';

INSERT INTO liu_chong_pairs (di_zhi_1, di_zhi_2, fang_wei) VALUES
('子', '午', '南北对冲'),
('丑', '未', '对角冲'),
('寅', '申', '对角冲'),
('卯', '酉', '东西对冲'),
('辰', '戌', '对角冲'),
('巳', '亥', '对角冲');
```

#### 3. 三合配对表

```sql
CREATE TABLE san_he_ju (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ju_name VARCHAR(20) NOT NULL COMMENT '局名（申子辰水局）',
    di_zhi_1 VARCHAR(5) NOT NULL COMMENT '长生',
    di_zhi_2 VARCHAR(5) NOT NULL COMMENT '帝旺',
    di_zhi_3 VARCHAR(5) NOT NULL COMMENT '墓库',
    he_wu_xing VARCHAR(10) NOT NULL COMMENT '合成五行',
    UNIQUE KEY uk_ju (di_zhi_1, di_zhi_2, di_zhi_3)
) COMMENT '三合成局表';

INSERT INTO san_he_ju (ju_name, di_zhi_1, di_zhi_2, di_zhi_3, he_wu_xing) VALUES
('申子辰水局', '申', '子', '辰', 'SHUI'),
('巳酉丑金局', '巳', '酉', '丑', 'JIN'),
('寅午戌火局', '寅', '午', '戌', 'HUO'),
('亥卯未木局', '亥', '卯', '未', 'MU');
```

---

### 工具类设计

#### 1. 六合判断工具

```java
@Component
public class LiuHeUtil {
    
    private static final Map<String, String> HE_MAP = Map.ofEntries(
        Map.entry("子", "丑"), Map.entry("丑", "子"),
        Map.entry("寅", "亥"), Map.entry("亥", "寅"),
        Map.entry("卯", "戌"), Map.entry("戌", "卯"),
        Map.entry("辰", "酉"), Map.entry("酉", "辰"),
        Map.entry("巳", "申"), Map.entry("申", "巳"),
        Map.entry("午", "未"), Map.entry("未", "午")
    );
    
    /**
     * 判断两个地支是否相合
     */
    public static boolean isHe(String diZhi1, String diZhi2) {
        return HE_MAP.get(diZhi1) != null && HE_MAP.get(diZhi1).equals(diZhi2);
    }
    
    /**
     * 判断是否合中带克
     */
    public static boolean isHeZhongDaiKe(String diZhi1, String diZhi2) {
        return (diZhi1.equals("子") && diZhi2.equals("丑")) ||
               (diZhi1.equals("丑") && diZhi2.equals("子")) ||
               (diZhi1.equals("巳") && diZhi2.equals("申")) ||
               (diZhi1.equals("申") && diZhi2.equals("巳"));
    }
}
```

#### 2. 六冲判断工具

```java
@Component
public class LiuChongUtil {
    
    private static final Map<String, String> CHONG_MAP = Map.ofEntries(
        Map.entry("子", "午"), Map.entry("午", "子"),
        Map.entry("丑", "未"), Map.entry("未", "丑"),
        Map.entry("寅", "申"), Map.entry("申", "寅"),
        Map.entry("卯", "酉"), Map.entry("酉", "卯"),
        Map.entry("辰", "戌"), Map.entry("戌", "辰"),
        Map.entry("巳", "亥"), Map.entry("亥", "巳")
    );
    
    /**
     * 判断两个地支是否相冲
     */
    public static boolean isChong(String diZhi1, String diZhi2) {
        return CHONG_MAP.get(diZhi1) != null && CHONG_MAP.get(diZhi1).equals(diZhi2);
    }
}
```

#### 3. 三合判断工具

```java
@Component
public class SanHeUtil {
    
    private static final Map<String, SanHeJu> SAN_HE_MAP = Map.of(
        "水局", new SanHeJu(Set.of("申", "子", "辰"), WuXing.SHUI),
        "金局", new SanHeJu(Set.of("巳", "酉", "丑"), WuXing.JIN),
        "火局", new SanHeJu(Set.of("寅", "午", "戌"), WuXing.HUO),
        "木局", new SanHeJu(Set.of("亥", "卯", "未"), WuXing.MU)
    );
    
    /**
     * 判断三个地支是否能成三合局
     */
    public static SanHeJu checkSanHe(Set<String> diZhiSet) {
        for (SanHeJu ju : SAN_HE_MAP.values()) {
            if (ju.getDiZhiSet().equals(diZhiSet)) {
                return ju;
            }
        }
        return null;
    }
    
    /**
     * 判断是否虚一待用（只有两个地支）
     */
    public static String checkXuYi(Set<String> diZhiSet) {
        if (diZhiSet.size() != 2) return null;
        
        for (SanHeJu ju : SAN_HE_MAP.values()) {
            Set<String> missing = new HashSet<>(ju.getDiZhiSet());
            missing.removeAll(diZhiSet);
            if (missing.size() == 1) {
                return missing.iterator().next();  // 返回缺的那个地支
            }
        }
        return null;
    }
}
```

---

### 解卦模块补充

```java
/**
 * 六合六冲三合分析服务
 */
@Service
public class HeChongAnalysisService {
    
    /**
     * 分析卦中的六合关系
     */
    public List<HeRelation> analyzeLiuHe(DivinationContext context) {
        List<HeRelation> relations = new ArrayList<>();
        
        // 1. 日月合爻
        for (YaoInfo yao : context.getYaos()) {
            if (LiuHeUtil.isHe(context.getMonthJian(), yao.getDiZhi())) {
                relations.add(new HeRelation("日月合爻", yao.getPosition()));
            }
        }
        
        // 2. 动爻相合
        List<YaoInfo> dongYaos = context.getYaos().stream()
            .filter(YaoInfo::isChanging)
            .collect(Collectors.toList());
        
        for (int i = 0; i < dongYaos.size(); i++) {
            for (int j = i + 1; j < dongYaos.size(); j++) {
                if (LiuHeUtil.isHe(dongYaos.get(i).getDiZhi(), 
                                    dongYaos.get(j).getDiZhi())) {
                    relations.add(new HeRelation("动爻相合", 
                        dongYaos.get(i).getPosition(), 
                        dongYaos.get(j).getPosition()));
                }
            }
        }
        
        // 3. 爻动化合
        // ... 其他逻辑
        
        return relations;
    }
    
    /**
     * 分析卦中的三合成局
     */
    public List<SanHeJu> analyzeSanHe(DivinationContext context) {
        // 实现三合成局判断
        // 包括：三爻俱动、两动一静、虚一待用、明暗皆动等情况
        return new ArrayList<>();
    }
}
```

---

## 📖 knowledge-liuyao05.md 核心发现

### 1. ✅ 六冲应用详解（9个实战卦例）

**六冲的六种作用**：
1. **日月冲爻** - 力量最强，月冲为月破，日冲分暗动/日破
2. **卦逢六冲** - 八纯卦，主分离散失
3. **六合变六冲** - 先吉后凶，有头无尾
4. **六冲变六冲** - 冲上加冲，彻底破裂
5. **动爻变冲** - 动化回头冲，自相矛盾
6. **爻与爻冲** - 动爻冲静爻，主动出击

**爻冲的五种情况**：
- **月冲** - 爻遇月冲为月破（力量最大）
- **日冲旺爻** - 静爻旺相遇日冲为暗动
- **日冲衰爻** - 休囚遇日冲为日破
- **动化回头冲** - 动爻自化回头冲，如逢仇敌
- **爻冲爻** - 爻遇爻冲，谓之相击

**关键发现：六冲变化的吉凶规律**

| 卦变情况 | 吉凶判断 | 应用场景 |
|---------|---------|---------|
| 六冲变六合 | 先难后易、散而复聚 | 占婚姻主复合 |
| 六合变六冲 | 先吉后凶、有头无尾 | 占延师主不久 |
| 六冲变六冲 | 冲上加冲、彻底破裂 | 占开业主难久 |
| 六冲静卦 | 主散失，但占凶事宜散 | 占官讼主速解 |

**重要原则**：
> 用神若旺，虽冲不碍；用神失陷，凶而又凶。

**特殊情况：冲空则实**
- 旬空之爻被日辰冲反而填实有用
- 例：辰土旬空，戌日冲辰，冲空则实，当日得财

---

### 2. ✅ 暗动理论（静爻被冲）

**暗动的定义**：
- **暗动** - 静爻旺相，被日辰冲之，为暗动（有力发动）
- **日破** - 静爻休囚，被日辰冲之，为日破（破败无用）

**暗动的判断**：
```
判断流程：
1. 是否被日辰冲？
2. 该爻是旺相还是休囚？
   - 旺相 → 暗动（激活）
   - 休囚 → 日破（破败）
```

**暗动的喜忌**：
- **喜** - 元神暗动生用神（暗中帮助）
- **喜** - 忌神明动克用神，元神暗动生用神（化险为夷）
- **忌** - 用神休囚，忌神暗动克用神（雪上加霜）

**重要发现**：
> 古人说暗动"福祸不知不觉，应期迟缓"，但实则未必。
> 暗动应验可以很迅速，如"占女痘"卦例，当日申时即应。

---

### 3. ✅ 动散理论（动爻被冲）

**核心理论**：
> 神兆机于动，动必有因。旺相者冲之不散，休囚者间或有散。

**动散的判断**：
```
动爻被冲是否散？
1. 旺相之爻 → 冲之不散
2. 有气之爻 → 冲之不散
3. 休囚之爻 → 偶有散（千百中一二）
```

**关键原因**：
- 爻既然动了，必然有其原因
- 虽然今日受冲制约，后逢值日时仍会发挥作用
- 并不会真正散失

**实战验证**：
- 占父出外：卯木父母动，酉日冲卯，古法说散，实则不散，果然春月归来

**理论辨析**：
- 批判《易冒》过分强调冲散理论
- 动化变鬼不是"散"，是"化回头克"，主凶
- 不可把子孙变鬼误判为"冲散"

---

### 4. ✅ 六害理论（删除）

**原文结论**：
> 六害全无应验，删之不录。

**影响设计**：
- ❌ 不需要实现六害判断
- ✅ 只需关注六合、六冲、三合

---

## 🔄 系统设计更新（基于liuyao05）

### 暗动与日破判断算法

```java
/**
 * 暗动与日破判断工具
 */
@Component
public class AnDongUtil {
    
    /**
     * 判断静爻被日辰冲后的状态
     * @param yao 静爻
     * @param riChen 日辰地支
     * @param month 月建（判断旺衰）
     * @return "暗动"/"日破"/null
     */
    public static String checkAnDongOrRiPo(YaoInfo yao, String riChen, String month) {
        // 1. 判断是否被日辰冲
        if (!LiuChongUtil.isChong(yao.getDiZhi(), riChen)) {
            return null;
        }
        
        // 2. 判断该爻是否静爻
        if (yao.isChanging()) {
            return null;  // 动爻不论暗动
        }
        
        // 3. 判断该爻的旺衰
        boolean isWang = WangShuaiUtil.isWangXiang(yao.getWuXing(), month);
        
        if (isWang) {
            return "暗动";  // 旺相之静爻被日冲为暗动
        } else {
            return "日破";  // 休囚之静爻被日冲为日破
        }
    }
}
```

### 六冲卦变判断算法

```java
/**
 * 六冲卦变判断工具
 */
@Component
public class LiuChongBianUtil {
    
    /**
     * 判断卦变情况
     */
    public static String checkGuaBian(Hexagram original, Hexagram changed) {
        boolean originalIsLiuChong = original.isLiuChong();
        boolean originalIsLiuHe = original.isLiuHe();
        boolean changedIsLiuChong = changed != null && changed.isLiuChong();
        boolean changedIsLiuHe = changed != null && changed.isLiuHe();
        
        if (originalIsLiuChong && changedIsLiuHe) {
            return "六冲变六合";  // 先难后易
        } else if (originalIsLiuHe && changedIsLiuChong) {
            return "六合变六冲";  // 先吉后凶
        } else if (originalIsLiuChong && changedIsLiuChong) {
            return "六冲变六冲";  // 冲上加冲
        } else if (originalIsLiuHe && changedIsLiuHe) {
            return "六合变六合";  // 终始吉祥
        }
        return null;
    }
    
    /**
     * 根据卦变情况给出吉凶提示
     */
    public static String getGuaBianHint(String guaBianType, String occupyType) {
        return switch (guaBianType) {
            case "六冲变六合" -> "先难后易，散而复聚。" + 
                (occupyType.equals("婚姻") ? "婚姻主复合。" : "");
            case "六合变六冲" -> "先吉后凶，有头无尾。事虽成但难久长。";
            case "六冲变六冲" -> "冲上加冲，彻底破裂。" + 
                (occupyType.equals("官讼") ? "官司速散为吉。" : "诸事难成。");
            case "六合变六合" -> "终始吉祥，长久安泰。";
            default -> "";
        };
    }
}
```

### 响应DTO补充

```java
public class DivinationResponse {
    // 原有字段...
    
    // ✅ 补充六冲相关信息
    private GuaBianInfo guaBianInfo;
    
    public static class GuaBianInfo {
        private String guaBianType;        // "六冲变六合"/"六合变六冲"等
        private String guaBianHint;        // 卦变吉凶提示
        private List<AnDongInfo> anDongList;  // 暗动列表
    }
    
    public static class AnDongInfo {
        private Integer position;          // 爻位
        private String type;              // "暗动"/"日破"
        private String diZhi;             // 地支
        private String reason;            // 原因："被日辰X冲"
    }
}
```

---

## 📊 知识库阅读总结（前5个文件）

### 已掌握的核心知识

#### 1. 起卦基础（liuyao01）
- ✅ 64卦完整数据（卦名、代码、世应、六亲配置）
- ✅ 八宫归属与五行
- ✅ 投币起卦规则

#### 2. 解卦理论（liuyao02）
- ✅ 六亲配置规则（生我/我生/我克/克我/比和）
- ✅ 用神体系（用神/元神/忌神/仇神）
- ✅ 五行生克理论

#### 3. 月日体系（liuyao03）
- ✅ 月建日辰的权力（生克冲合）
- ✅ 动静生克规则
- ✅ 四时旺相表
- ✅ 六神配置规则
- ✅ 旬空系统

#### 4. 合冲理论（liuyao04 + liuyao05）
- ✅ 六合配对与作用（合起/合绊/合好/化扶）
- ✅ 六冲配对与作用（暗动/日破/冲散）
- ✅ 三合成局（虚一待用/明暗皆动）
- ✅ 六冲变化规律（六冲变六合/六合变六冲）
- ✅ 暗动与日破判断
- ✅ 动散理论（神兆机于动）

---

## 📖 knowledge-liuyao06.md 核心发现

### 1. ✅ 卦变生克理论（卦宫五行生克）

**卦变定义**：
本卦变成变卦后，两个卦的**卦宫五行**之间产生的生克关系。

**八卦五行归属**：
| 八卦 | 五行 |
|------|------|
| 乾、兑 | 金 |
| 震、巽 | 木 |
| 坎 | 水 |
| 离 | 火 |
| 坤、艮 | 土 |

**卦变的五种类型**：

| 卦变类型 | 示例 | 五行关系 | 吉凶 |
|----------|------|----------|------|
| **变生** | 巽木→坎水 | 水生木 | 大吉 |
| **变克** | 震木→乾金 | 金克木 | 大凶 |
| **变墓** | （未详论） | 入墓 | 凶 |
| **变绝** | （未详论） | 入绝 | 大凶 |
| **变比和** | 震木→巽木 | 同类 | 平 |

**核心原则**：
> 凡遇卦化克者，不论用神之衰旺，皆以凶推。

---

### 2. ❌ 化来与化去（最重要的区分）

**化来**（大凶）：
- 定义：变卦来克本卦
- 示例：震木变兑金，金来克木
- 性质：回头克，如遇仇敌
- 吉凶：**不论用神旺衰，皆以凶推**

**化去**（不凶）：
- 定义：本卦去克变卦
- 示例：兑金变震木，金去克木
- 性质：我去克他，化克而不克
- 吉凶：**不为凶兆**

**判断方法**：
```
判断是化来还是化去？
1. 确定本卦卦宫五行（如震为木）
2. 确定变卦卦宫五行（如兑为金）
3. 判断生克关系：
   - 如果变卦克本卦（金克木） → 化来（凶）
   - 如果本卦克变卦（金克木，但本卦是金） → 化去（不凶）
```

---

### 3. ✅ 卦变克的凶险程度

**关键理论**：
> 卦体如人之根本，卦变克绝，如树连根拔起。
> 虽当时旺相，过时而衰；虽枝叶青翠，终难长久。

**9个实战卦例验证**：
1. 占代卜功名（巽木化乾金）→ 午月削职，七月而终
2. 占主病（离火化坎水）→ 午月虽旺，九月亥日卒
3. 占索房价（坎水化坤土）→ 小事应大凶，巳月覆舟而死
4. 占母病（坤土化巽木）→ 回头克
5. 占长子病（震木化兑金）→ 回头克

**应验特点**：
- 当时虽旺，过时则衰
- 小事占得大凶卦，也会应大凶
- 神机早兆，提前预警
- 不论用神旺衰，必应凶险

---

### 4. ✅ 与爻变的区别

**爻变**（之前学过）：
- 定义：单个动爻变化后与变出的爻之间的关系
- 范围：单爻层面
- 示例：午火动化子水，子午相冲（回头冲）

**卦变**（本章内容）：
- 定义：整个卦从本卦变成变卦后，两个卦的卦宫五行关系
- 范围：整卦层面
- 示例：震卦（木）变兑卦（金），金克木（化来）

**两者关系**：
- 卦变是宏观判断（根本）
- 爻变是微观判断（细节）
- 卦变克比爻变克更凶险

---

### 5. ❌ 批判《易冒》的错误

**错误1：日月当令非真论**
- 《易冒》认为：日月当令时卦变克不应验
- 正确理解：凶事将来，神机早兆，虽当时旺相，过时必衰

**错误2：半从往半从来论**
- 《易冒》说："半从往，半从来，半凶半吉"
- 正确理解：只要卦变克，就是大凶，不存在半凶半吉

**错误3：将卦变克误作其他**
- 《易冒》将明显的卦变克误作"反复休囚"
- 正确理解：只要看到卦变克，直接断凶，不必牵扯其他

---

## 🔄 系统设计更新（基于liuyao06）

### 卦变生克判断算法

```java
/**
 * 卦变生克判断工具
 */
@Component
public class GuaBianShengKeUtil {
    
    // 八卦五行映射
    private static final Map<String, WuXing> GUA_WU_XING = Map.of(
        "乾", WuXing.JIN,
        "兑", WuXing.JIN,
        "震", WuXing.MU,
        "巽", WuXing.MU,
        "坎", WuXing.SHUI,
        "离", WuXing.HUO,
        "坤", WuXing.TU,
        "艮", WuXing.TU
    );
    
    /**
     * 判断卦变类型
     * @param benGua 本卦
     * @param bianGua 变卦
     * @return 卦变类型和吉凶
     */
    public static GuaBianResult checkGuaBian(Hexagram benGua, Hexagram bianGua) {
        if (bianGua == null) {
            return null;
        }
        
        // 1. 获取本卦和变卦的卦宫
        String benGong = benGua.getGong();  // 如"震宫"
        String bianGong = bianGua.getGong();  // 如"兑宫"
        
        // 2. 提取卦名（去掉"宫"字）
        String benGuaName = extractGuaName(benGong);  // "震"
        String bianGuaName = extractGuaName(bianGong);  // "兑"
        
        // 3. 获取五行
        WuXing benWuXing = GUA_WU_XING.get(benGuaName);
        WuXing bianWuXing = GUA_WU_XING.get(bianGuaName);
        
        // 4. 判断生克关系
        if (benWuXing == bianWuXing) {
            return new GuaBianResult("变比和", "平", "变卦与本卦五行相同");
        } else if (bianWuXing.sheng(benWuXing)) {
            return new GuaBianResult("变生", "吉", "变卦生本卦，大吉之象");
        } else if (bianWuXing.ke(benWuXing)) {
            return new GuaBianResult("化来", "大凶", 
                "变卦克本卦，回头克，不论用神旺衰皆以凶推！");
        } else if (benWuXing.ke(bianWuXing)) {
            return new GuaBianResult("化去", "不凶", 
                "本卦克变卦，我去克他，化克而不克");
        } else if (benWuXing.sheng(bianWuXing)) {
            return new GuaBianResult("化泄", "稍凶", "本卦生变卦，力量外泄");
        }
        
        return null;
    }
    
    /**
     * 提取卦名（从"震宫"提取"震"）
     */
    private static String extractGuaName(String gong) {
        if (gong.endsWith("宫")) {
            return gong.substring(0, gong.length() - 1);
        }
        return gong;
    }
}

/**
 * 卦变结果
 */
public class GuaBianResult {
    private String type;        // "化来"/"化去"/"变生"/"变比和"
    private String jiXiong;     // "大凶"/"吉"/"平"/"不凶"
    private String description; // 描述
    
    // 构造函数和getter...
}
```

---

### 数据库设计补充

hexagrams表已经有`gong`字段（如"震宫"），可以直接使用，不需要额外存储卦宫五行。

---

### 响应DTO补充

```java
public class DivinationResponse {
    // 原有字段...
    
    // ✅ 补充卦变生克信息
    private GuaBianInfo guaBianInfo;
    
    public static class GuaBianInfo {
        // 六冲六合变化
        private String guaBianType;        // "六冲变六合"等
        private String guaBianHint;        // 卦变吉凶提示
        
        // ✅ 卦变生克
        private String shengKeType;        // "化来"/"化去"/"变生"等
        private String shengKeJiXiong;     // "大凶"/"吉"/"不凶"
        private String shengKeDesc;        // 详细描述
        
        // 暗动列表
        private List<AnDongInfo> anDongList;
    }
}
```

---

### 解卦流程补充

```java
/**
 * 解卦服务（补充卦变判断）
 */
@Service
public class JieGuaService {
    
    public DivinationResponse analyze(DivinationRequest request) {
        // 1. 起卦（已有）
        // 2. 查询卦象数据（已有）
        // 3. 计算六神（已有）
        
        // ✅ 4. 判断卦变生克
        GuaBianResult guaBianShengKe = GuaBianShengKeUtil.checkGuaBian(
            originalHexagram, changedHexagram);
        
        if (guaBianShengKe != null && "大凶".equals(guaBianShengKe.getJiXiong())) {
            // 卦变克，给出警告
            response.addWarning("⚠️ 卦变克：" + guaBianShengKe.getDescription());
        }
        
        // 5. 其他判断...
        
        return response;
    }
}
```

---

## 📊 知识库阅读总结（前6个文件）

### 已掌握的核心知识

#### 1. 起卦基础（liuyao01）
- ✅ 64卦完整数据
- ✅ 八宫归属与五行
- ✅ 投币起卦规则

#### 2. 解卦理论（liuyao02）
- ✅ 六亲配置规则
- ✅ 用神体系
- ✅ 五行生克理论

#### 3. 月日体系（liuyao03）
- ✅ 月建日辰的权力
- ✅ 动静生克规则
- ✅ 四时旺相表
- ✅ 六神配置规则
- ✅ 旬空系统

#### 4. 合冲理论（liuyao04 + liuyao05）
- ✅ 六合配对与作用
- ✅ 六冲配对与作用
- ✅ 三合成局
- ✅ 六冲变化规律
- ✅ 暗动与日破
- ✅ 动散理论

#### 5. 卦变理论（liuyao06）
- ✅ 卦变生克判断（化来/化去）
- ✅ 卦宫五行映射
- ✅ 卦变克的凶险程度
- ✅ 卦变与爻变的区别

---

## 📖 knowledge-liuyao07.md 核心发现

### 1. ✅ 反吟理论（卦变相冲相克）

**反吟定义**：
内外卦动而形成相冲相克的关系，主事情反复、进退两难。

**反吟的四种类型**：

| 类型 | 定义 | 示例 | 特点 |
|------|------|------|------|
| **卦变反吟** | 六爻全动，变成同一卦 | 乾变坤 | 变化最剧烈，大反复 |
| **爻变反吟** | 内外动而反吟，非同一卦 | 升变观 | 反复变化 |
| **外卦反吟** | 外卦（上三爻）反吟 | 观变坤 | 外事反复 |
| **内卦反吟** | 内卦（下三爻）反吟 | 巽变观 | 内事反复 |

**反吟核心特征**：
> 成而败、败而成，有而即无、无而即有，得而失、失而得，来而去、去而来，散而聚、聚而散，动而思静、静而思动。

**各占事应验**：
1. **占功名**：用爻旺相，迁而又迁；用神失陷，或降或升
2. **占财物**：聚散不常，买卖兴废往来不定
3. **占婚姻**：反复难成
4. **占疾病**：愈而又病
5. **占出行**：行至中途而返
6. **占行人**：外卦反吟，用神旺相必归，不然移他处
7. **占彼此形势**：内卦反吟我乱他定，外卦反吟他乱我定

**重要原则**：
> 用神旺相，不变冲克者，虽则反吟，事亦必成。
> 只恐用神化回头之冲克者，即如卦变，大凶之象。

---

### 2. ✅ 伏吟理论（动化同类）

**伏吟定义**：
动爻变化后，变爻与本爻地支相同，形成伏吟状态，主迟滞不动、停滞不前。

**伏吟的三种类型**：
- **内伏吟**：内卦（下三爻）伏吟，我心不开
- **外伏吟**：外卦（上三爻）伏吟，他意不安
- **内外伏吟**：内外卦都伏吟，内外忧郁呻吟

**伏吟核心特征**：
> 内外忧郁呻吟之象，久困宦途，淹留仕路。

**各占事应验**：
1. **占功名**：久困宦途，淹留仕路
2. **占财利**：财源耗散，本利消乏
3. **占坟墓宅舍**：欲迁而不能，守之而不利
4. **占婚姻**：忧而不乐
5. **占疾病**：久病呻吟
6. **占出行**：艰于动转
7. **占行人**：在外忧愁

**伏吟与反吟对比**：
- **反吟**：有冲有克，用神受克，得祸匪轻（更凶）
- **伏吟**：用神旺相，冲开之年月其志则伸；用神休囚，冲开之年月忧郁而已（较轻）

---

### 3. ✅ 旬空理论（空亡判断）

**旬空定义**：
六十甲子中，每十天为一旬，每旬中有两个地支不在这十天之内，这两个地支就是空亡。

**旬空配对表**：

| 旬首 | 空亡地支 | 说明 |
|------|----------|------|
| 甲子旬 | 戌、亥 | 甲子至癸酉10日 |
| 甲戌旬 | 申、酉 | 甲戌至癸未10日 |
| 甲申旬 | 午、未 | 甲申至癸巳10日 |
| 甲午旬 | 辰、巳 | 甲午至癸卯10日 |
| 甲辰旬 | 寅、卯 | 甲辰至癸丑10日 |
| 甲寅旬 | 子、丑 | 甲寅至癸亥10日 |

**空不为空的情况**：
1. **旺不为空** - 爻临月建或日辰旺相，虽空不为真空
2. **动不为空** - 动爻虽空不为空
3. **动而化空** - 动爻化出空亡也不为空
4. **伏而旺相** - 伏神旺相不为空
5. **有生扶者** - 有日辰或动爻生扶也不为空

**为空的情况**：
1. **月破为空** - 被月建冲破为空
2. **伏而被克** - 伏神被飞神克制为空
3. **真空为空** - 春土、夏金、秋木、冬火为真空

**核心方法：多占之法**
> 凡遇旬空，命之再占。卦吉者，许之出旬而不空；卦凶者，许之空矣。

**空亡之妙**：
> 天地之理，皆从空而生，故谓之"悬空以待"。
> 似有又无，似无又有，实有到底全空，亦有填实不空。

**判断原则**：
1. 不可即以为空，要综合分析
2. 视所占事近远：近事看旬内，远事看出空
3. 旬内之空：固为空，但有冲空之日、实空之时
4. 远大之事：须看大象，大象吉则太岁月建可填之
5. **最妙之法**：多占两卦合决

---

## 🔄 系统设计更新（基于liuyao07）

### 反吟判断算法

```java
/**
 * 反吟判断工具
 */
@Component
public class FanYinUtil {
    
    /**
     * 判断是否反吟
     * @param originalYaos 本卦六爻
     * @param changedYaos 变卦六爻
     * @return 反吟类型："内卦反吟"/"外卦反吟"/"内外反吟"/null
     */
    public static String checkFanYin(List<YaoInfo> originalYaos, List<YaoInfo> changedYaos) {
        if (changedYaos == null) return null;
        
        boolean neiGuaFanYin = checkNeiGuaFanYin(originalYaos, changedYaos);
        boolean waiGuaFanYin = checkWaiGuaFanYin(originalYaos, changedYaos);
        
        if (neiGuaFanYin && waiGuaFanYin) {
            return "内外反吟";
        } else if (neiGuaFanYin) {
            return "内卦反吟";
        } else if (waiGuaFanYin) {
            return "外卦反吟";
        }
        return null;
    }
    
    /**
     * 判断内卦是否反吟（下三爻）
     */
    private static boolean checkNeiGuaFanYin(List<YaoInfo> original, List<YaoInfo> changed) {
        int fanYinCount = 0;
        for (int i = 0; i < 3; i++) {  // 初爻、二爻、三爻
            YaoInfo origYao = original.get(i);
            YaoInfo changedYao = changed.get(i);
            
            // 判断是否相冲或相克
            if (origYao.isChanging() && 
                (LiuChongUtil.isChong(origYao.getDiZhi(), changedYao.getDiZhi()) ||
                 isKeRelation(origYao, changedYao))) {
                fanYinCount++;
            }
        }
        return fanYinCount >= 2;  // 至少两爻反吟
    }
    
    /**
     * 判断外卦是否反吟（上三爻）
     */
    private static boolean checkWaiGuaFanYin(List<YaoInfo> original, List<YaoInfo> changed) {
        int fanYinCount = 0;
        for (int i = 3; i < 6; i++) {  // 四爻、五爻、六爻
            YaoInfo origYao = original.get(i);
            YaoInfo changedYao = changed.get(i);
            
            if (origYao.isChanging() && 
                (LiuChongUtil.isChong(origYao.getDiZhi(), changedYao.getDiZhi()) ||
                 isKeRelation(origYao, changedYao))) {
                fanYinCount++;
            }
        }
        return fanYinCount >= 2;
    }
}
```

---

### 伏吟判断算法

```java
/**
 * 伏吟判断工具
 */
@Component
public class FuYinUtil {
    
    /**
     * 判断是否伏吟
     * @param originalYaos 本卦六爻
     * @param changedYaos 变卦六爻
     * @return 伏吟类型："内卦伏吟"/"外卦伏吟"/"内外伏吟"/null
     */
    public static String checkFuYin(List<YaoInfo> originalYaos, List<YaoInfo> changedYaos) {
        if (changedYaos == null) return null;
        
        boolean neiGuaFuYin = checkNeiGuaFuYin(originalYaos, changedYaos);
        boolean waiGuaFuYin = checkWaiGuaFuYin(originalYaos, changedYaos);
        
        if (neiGuaFuYin && waiGuaFuYin) {
            return "内外伏吟";
        } else if (neiGuaFuYin) {
            return "内卦伏吟";
        } else if (waiGuaFuYin) {
            return "外卦伏吟";
        }
        return null;
    }
    
    /**
     * 判断内卦是否伏吟
     */
    private static boolean checkNeiGuaFuYin(List<YaoInfo> original, List<YaoInfo> changed) {
        int fuYinCount = 0;
        for (int i = 0; i < 3; i++) {
            YaoInfo origYao = original.get(i);
            YaoInfo changedYao = changed.get(i);
            
            // 动爻化出同类地支
            if (origYao.isChanging() && 
                origYao.getDiZhi().equals(changedYao.getDiZhi())) {
                fuYinCount++;
            }
        }
        return fuYinCount >= 2;
    }
    
    /**
     * 判断外卦是否伏吟
     */
    private static boolean checkWaiGuaFuYin(List<YaoInfo> original, List<YaoInfo> changed) {
        int fuYinCount = 0;
        for (int i = 3; i < 6; i++) {
            YaoInfo origYao = original.get(i);
            YaoInfo changedYao = changed.get(i);
            
            if (origYao.isChanging() && 
                origYao.getDiZhi().equals(changedYao.getDiZhi())) {
                fuYinCount++;
            }
        }
        return fuYinCount >= 2;
    }
}
```

---

### 旬空判断算法

```java
/**
 * 旬空判断工具
 */
@Component
public class XunKongUtil {
    
    // 旬空配对表
    private static final Map<String, Set<String>> XUN_KONG_MAP = Map.of(
        "甲子", Set.of("戌", "亥"),
        "甲戌", Set.of("申", "酉"),
        "甲申", Set.of("午", "未"),
        "甲午", Set.of("辰", "巳"),
        "甲辰", Set.of("寅", "卯"),
        "甲寅", Set.of("子", "丑")
    );
    
    /**
     * 获取当日旬空地支
     * @param riChen 日辰（如"甲子"）
     * @return 旬空地支集合
     */
    public static Set<String> getXunKong(String riChen) {
        // 根据日辰确定所在旬
        String xunShou = getXunShou(riChen);
        return XUN_KONG_MAP.getOrDefault(xunShou, Set.of());
    }
    
    /**
     * 判断爻是否旬空
     */
    public static boolean isXunKong(YaoInfo yao, String riChen) {
        Set<String> xunKong = getXunKong(riChen);
        return xunKong.contains(yao.getDiZhi());
    }
    
    /**
     * 判断旬空是否为真空（空不为空的判断）
     */
    public static boolean isZhenKong(YaoInfo yao, String riChen, String month) {
        // 1. 旺不为空
        if (WangShuaiUtil.isWangXiang(yao.getWuXing(), month)) {
            return false;
        }
        
        // 2. 动不为空
        if (yao.isChanging()) {
            return false;
        }
        
        // 3. 真空为空（春土、夏金、秋木、冬火）
        if (isZhenKongWuXing(yao.getWuXing(), month)) {
            return true;
        }
        
        // 4. 月破为空
        if (LiuChongUtil.isChong(yao.getDiZhi(), month)) {
            return true;
        }
        
        return true;  // 默认为真空
    }
    
    /**
     * 判断是否真空五行
     */
    private static boolean isZhenKongWuXing(WuXing wuXing, String month) {
        String season = getSeason(month);
        return switch (season) {
            case "春" -> wuXing == WuXing.TU;
            case "夏" -> wuXing == WuXing.JIN;
            case "秋" -> wuXing == WuXing.MU;
            case "冬" -> wuXing == WuXing.HUO;
            default -> false;
        };
    }
}
```

---

## 📖 knowledge-liuyao08.md 核心发现（卷一完结）

### 1. ✅ 生旺墓绝理论（长生十二宫）

**核心原则**：
> 只验生旺墓绝，其余不验，不必用也。

**长生十二宫**：长生、沐浴、冠带、临官、帝旺、衰、病、死、墓、绝、胎、养

**只用四宫**：
- **长生** - 新生、开始、生机勃勃
- **帝旺** - 强盛、鼎盛、力量最强
- **墓** - 埋藏、困住、不得出
- **绝** - 断绝、死绝、无气

**五行生旺墓绝表**：

| 五行 | 长生 | 帝旺 | 墓 | 绝 |
|------|------|------|----|----|
| 金 | 巳 | 酉 | 丑 | 寅 |
| 木 | 亥 | 卯 | 未 | 申 |
| 水 | 申 | 子 | 辰 | 巳 |
| 火 | 寅 | 午 | 戌 | 亥 |
| 土 | 申 | 子 | 辰 | 巳 |

**口诀记忆**：
- 生：巳亥申寅申（金木水火土）
- 旺：酉卯子午子
- 墓：丑未辰戌辰
- 绝：寅申巳亥巳

**应用场景**：
1. **占财物**：财爻长生/帝旺主财源滚滚，入墓主财困，临绝主财无
2. **占功名**：官爻长生/帝旺主升迁有望，入墓主受阻，临绝主无望
3. **占疾病**：官鬼（病神）长生旺相主病重，入墓/临绝主病愈

---

### 2. ✅ 各门类题头总注（术语解释）

这一章是对后续章节中常用术语的统一解释，非常重要。

#### 用神宜旺
- 不指定旺于四时
- 得日月动爻生扶皆旺
- 逢长生帝旺皆旺

#### 用神化吉
- 化回头生
- 化长生
- 化帝旺
- 化比助
- 化日月

#### 用神化凶
- 化回头克
- 化绝
- 化墓
- 化空
- 化鬼
- 化退神

#### 三墓
- **入日墓** - 用爻入日辰之墓
- **入动墓** - 用爻入动爻之墓
- **动而化墓** - 用爻动化入墓

#### 身世
- **身** = **世爻**（非卦身、世身）
- 作者试验后发现卦身世身不验，统一用世爻

#### 变化统一
- **变爻** = **化爻**
- 不再区分"变"和"化"
- 重点看变爻对用神的生克关系

---

### 3. ✅ 应期断法（12种应期规则）

这是解卦最关键的部分，决定事情何时应验。

#### 1. 静而逢值逢冲
- **逢值**：用神静爻，逢用神临值之日应验
- **逢冲**：用神静爻，逢用神被冲之日应验
- 示例：子水静爻，逢子日或午日应验

#### 2. 动而逢合逢值
- **逢值**：用神动爻，逢用神临值之日应验
- **逢合**：用神动爻，逢与用神相合之日应验
- 示例：子水动爻，逢子日或丑日应验

#### 3. 太旺者逢墓逢冲
- **逢冲**：太旺者逢冲之日应验
- **逢墓**：太旺者入墓之日应验
- 原理：物极必反，太旺需制约

#### 4. 衰绝者遇生遇旺
- **遇生**：衰绝者逢生扶之日应验
- **遇旺**：衰绝者逢旺相之日应验
- 原理：衰弱需扶助

#### 5. 入三墓俱喜冲开
- 用神入墓，逢冲墓之日应验
- 示例：午火入戌墓，逢辰日冲开

#### 6. 遇六合亦宜相击
- 用神作合，需冲开合局才应验
- 示例：子丑合，逢午日或未日应验

#### 7. 月破喜逢填合
- **逢合**：月破逢合之日应验
- **逢填**：月破逢填实之日应验

#### 8. 旬空最爱填冲
- **逢填**：旬空逢填实之日应验
- **逢冲**：旬空逢冲之日应验

#### 9. 大象吉凶与克神
- **大象吉而受克**：逢克制克神之日应验（吉）
- **大象凶而受克**：逢生扶克神之日应验（凶）

#### 10. 元神忌神与应期
- **元神助用**：看用神旺衰
- **忌神克用**：看元神强弱

#### 11. 化进神化退神
- **化进神**：逢值或逢合之日
- **化退神**：逢值或逢冲之日

#### 12. 远近应期
- **远事**：定之于年月
- **近事**：应之于日时

---

### 4. ✅ 归魂游魂理论

**游魂卦**：
- 定义：八宫卦中每宫的第七卦
- 特征：主远行、游荡、不定
- 示例：乾宫第七卦火地晋

**归魂卦**：
- 定义：八宫卦中每宫的第八卦
- 特征：主拘泥不行、难以远离
- 示例：乾宫第八卦火天大有

**野鹤观点（重要）**：
> 须以用神为主，然后以此参之。
> 若舍用神，执此而断者，谬也。

**正确断法**：
1. 先看用神旺衰、生克制化
2. 再参考游魂归魂的特性
3. 综合判断吉凶

---

## 🔄 系统设计更新（基于liuyao08）

### 生旺墓绝判断算法

```java
/**
 * 生旺墓绝判断工具
 */
@Component
public class ShengWangMuJueUtil {
    
    // 五行生旺墓绝表
    private static final Map<WuXing, ShengWangMuJue> SHENG_WANG_MAP = Map.of(
        WuXing.JIN, new ShengWangMuJue("巳", "酉", "丑", "寅"),
        WuXing.MU, new ShengWangMuJue("亥", "卯", "未", "申"),
        WuXing.SHUI, new ShengWangMuJue("申", "子", "辰", "巳"),
        WuXing.HUO, new ShengWangMuJue("寅", "午", "戌", "亥"),
        WuXing.TU, new ShengWangMuJue("申", "子", "辰", "巳")
    );
    
    /**
     * 判断爻的生旺墓绝状态
     */
    public static String checkShengWangMuJue(YaoInfo yao, String diZhi) {
        ShengWangMuJue swmj = SHENG_WANG_MAP.get(yao.getWuXing());
        
        if (swmj.getChangSheng().equals(diZhi)) {
            return "长生";
        } else if (swmj.getDiWang().equals(diZhi)) {
            return "帝旺";
        } else if (swmj.getMu().equals(diZhi)) {
            return "墓";
        } else if (swmj.getJue().equals(diZhi)) {
            return "绝";
        }
        return null;
    }
    
    /**
     * 判断用神是否入墓（三墓）
     */
    public static List<String> checkRuMu(YaoInfo yongShen, String riChen, 
                                          List<YaoInfo> dongYaos, YaoInfo bianYao) {
        List<String> muList = new ArrayList<>();
        ShengWangMuJue swmj = SHENG_WANG_MAP.get(yongShen.getWuXing());
        String muDiZhi = swmj.getMu();
        
        // 1. 入日墓
        if (riChen.contains(muDiZhi)) {
            muList.add("入日墓");
        }
        
        // 2. 入动墓
        for (YaoInfo dongYao : dongYaos) {
            if (dongYao.getDiZhi().equals(muDiZhi)) {
                muList.add("入动墓");
                break;
            }
        }
        
        // 3. 动而化墓
        if (yongShen.isChanging() && bianYao != null && 
            bianYao.getDiZhi().equals(muDiZhi)) {
            muList.add("动而化墓");
        }
        
        return muList;
    }
}

/**
 * 生旺墓绝数据类
 */
@Data
@AllArgsConstructor
class ShengWangMuJue {
    private String changSheng;  // 长生
    private String diWang;      // 帝旺
    private String mu;          // 墓
    private String jue;         // 绝
}
```

---

### 应期判断算法框架

```java
/**
 * 应期判断服务
 */
@Service
public class YingQiService {
    
    /**
     * 判断应期
     */
    public List<YingQiResult> calculateYingQi(DivinationContext context, YaoInfo yongShen) {
        List<YingQiResult> results = new ArrayList<>();
        
        // 1. 静而逢值逢冲
        if (!yongShen.isChanging()) {
            results.add(new YingQiResult("逢值", yongShen.getDiZhi() + "日"));
            String chongDiZhi = LiuChongUtil.getChongDiZhi(yongShen.getDiZhi());
            results.add(new YingQiResult("逢冲", chongDiZhi + "日"));
        }
        
        // 2. 动而逢合逢值
        if (yongShen.isChanging()) {
            results.add(new YingQiResult("逢值", yongShen.getDiZhi() + "日"));
            String heDiZhi = LiuHeUtil.getHeDiZhi(yongShen.getDiZhi());
            results.add(new YingQiResult("逢合", heDiZhi + "日"));
        }
        
        // 3. 太旺者逢墓逢冲
        if (isTaiWang(yongShen, context)) {
            ShengWangMuJue swmj = ShengWangMuJueUtil.getShengWangMuJue(yongShen.getWuXing());
            results.add(new YingQiResult("逢墓", swmj.getMu() + "日"));
            String chongDiZhi = LiuChongUtil.getChongDiZhi(yongShen.getDiZhi());
            results.add(new YingQiResult("逢冲", chongDiZhi + "日"));
        }
        
        // 4. 衰绝者遇生遇旺
        if (isShuaiJue(yongShen, context)) {
            // 计算生扶之日
            // 计算旺相之日
        }
        
        // 5. 入三墓俱喜冲开
        List<String> muList = ShengWangMuJueUtil.checkRuMu(yongShen, 
            context.getRiChen(), context.getDongYaos(), context.getBianYao());
        if (!muList.isEmpty()) {
            ShengWangMuJue swmj = ShengWangMuJueUtil.getShengWangMuJue(yongShen.getWuXing());
            String chongMuDiZhi = LiuChongUtil.getChongDiZhi(swmj.getMu());
            results.add(new YingQiResult("冲开墓库", chongMuDiZhi + "日"));
        }
        
        // 6-12. 其他应期规则...
        
        return results;
    }
}

@Data
@AllArgsConstructor
class YingQiResult {
    private String type;    // 应期类型
    private String time;    // 应期时间
}
```

---

### 归魂游魂判断

```java
/**
 * 归魂游魂判断工具
 */
@Component
public class GuiHunYouHunUtil {
    
    /**
     * 判断是否归魂或游魂卦
     */
    public static String checkGuiHunYouHun(Hexagram hexagram) {
        String gong = hexagram.getGong();  // 如"乾宫"
        int sequence = hexagram.getSequence();  // 卦序（1-8）
        
        if (sequence == 7) {
            return "游魂卦";
        } else if (sequence == 8) {
            return "归魂卦";
        }
        return null;
    }
    
    /**
     * 获取游魂归魂提示
     */
    public static String getHint(String type, String occupyType) {
        if ("游魂卦".equals(type)) {
            return switch (occupyType) {
                case "出行" -> "游魂行千里，行止无定";
                case "行人" -> "游遍他乡，难归";
                case "家宅" -> "迁变不常";
                default -> "心无定向，迁改不常";
            };
        } else if ("归魂卦".equals(type)) {
            return switch (occupyType) {
                case "出行" -> "归魂不出疆，拘泥不行";
                case "行人" -> "难以远离，近期可归";
                default -> "诸事拘泥不行";
            };
        }
        return "";
    }
}
```

---

## 📊 《增删卜易》卷一知识体系总结

### 已完成的8个文件构成完整的六爻基础理论

#### 1. 起卦基础（liuyao01）
- 64卦完整数据
- 八宫归属
- 投币起卦规则

#### 2. 六亲与用神（liuyao02）
- 六亲配置规则
- 用神体系
- 五行生克

#### 3. 月日六神（liuyao03）
- 月建日辰权力
- 动静生克
- 六神配置
- 旬空系统

#### 4. 合冲理论（liuyao04 + liuyao05）
- 六合六冲三合
- 暗动与日破
- 动散理论

#### 5. 卦变理论（liuyao06）
- 卦变生克（化来/化去）
- 卦宫五行

#### 6. 反伏旬空（liuyao07）
- 反吟伏吟
- 旬空判断
- 多占之法

#### 7. 生旺墓绝（liuyao08）
- 长生十二宫
- 应期断法
- 归魂游魂

#### 8. 月破飞伏（liuyao09）
- 月破理论（野鹤新论突破古法）
- 飞伏神系统
- 多占法优化

---

## 📖 knowledge-liuyao09.md 核心发现（卷二开篇）

### 一、月破理论（YuePo Theory）

#### 1. 月破基本定义
**月破 = 月建冲卦爻**

月破配对：正月寅破申、二月卯破酉、三月辰破戌、四月巳破亥、五月午破子、六月未破丑、七月申破寅、八月酉破卯、九月戌破辰、十月亥破巳、十一月子破午、十二月丑破未。

#### 2. 古法 vs 野鹤新论（重大理论突破）

**古法观点（《易冒》《易林补遗》）**：
- ❌ 月破 = 百无一用
- ❌ 逢生生之不起，虽现于卦有亦如无

**野鹤新论（《增删卜易》突破性观点）**：
- ✅ **月破爻发动仍有用**：动则能伤于爻，变则能伤于动
- ✅ **神兆机于动**：既然动了就有原因，不能说无用
- ✅ **应期三法**：出月、实破之日、逢合之日
- ❌ **月破爻静止无生助**：到底而破，确实无用

#### 3. 实战卦例验证

**例一：占将来有官否（兑之讼）**
- 官鬼巳火发动生世，但逢亥月月破
- 野鹤断：月破爻发动仍有用，应期在实破之年
- **应验**：果于巳年承袭世职

**例二：占父何日归（乾之夬）**
- 父母戌土持世发动，逢辰月月破，化未土旬空，无生助
- 野鹤断：卯日有信（破而逢合），未日必归（出空）
- **应验**：果于卯日得信，乙未日抵家

**例三：占后运功名（艮之观）**
- 子水月破化空动生世
- 子月实破力微→降级调用
- 子年实破当权→原品起用
- 辰年子水入墓→得祸更重
- **应验**：半生凶吉，关乎一爻月破

**李我平评论**：
> 半生凶吉，关乎一爻之月破。**岂可谓之有亦如无，毫无所用耶？！**

---

### 二、飞伏神理论（FeiShen & FuShen）

#### 1. 飞伏神基本定义

**用神不上卦时的寻找顺序**：
1. **首选**：日月为用神
2. **次选**：本宫首卦寻伏神（八纯卦六亲俱全）
3. **最优**：多占一卦，用神自现（野鹤创新）

**飞神与伏神**：
- **伏神**：隐藏在卦爻下面的用神（本宫首卦中对应的爻）
- **飞神**：卦中显现的爻，在伏神的上面

**示例**：
```
占妻财，得"天风姤"卦（乾宫）
卦中无寅卯木财爻 → 用神不上卦
→ 到本宫首卦"乾为天"寻找
→ 乾卦寅木妻财在二爻
→ 寅木伏于姤卦二爻亥水之下
→ 亥水为飞神，寅木为伏神
→ 亥水生寅木，谓之"飞来生伏得长生"
```

#### 2. 伏神有用六法

1. 伏神得日月生
2. 伏神旺相
3. 伏神得飞神生（飞来生伏）
4. 伏神得动爻生
5. 日月动爻冲克飞神
6. 飞神空破休囚墓绝

**《黄金策》论述**："空下伏神，易于引拔。"

**野鹤扩展**：不仅飞神空亡伏神才能出，只要飞神临月破、临绝地、休囚无气、入墓，伏神都容易出现。

#### 3. 伏神无用五法

1. 伏神休囚无气
2. 伏神被日月冲克
3. 伏神被旺相飞神克（飞来克伏）
4. 伏神墓绝于日月飞爻
5. 伏神休囚值旬空月破

**《黄金策》辨析**：
《黄金策》曰："伏居空地，事与心违。"

**野鹤实战验证**：只要用神旺相而遇旬空，出空之日则出矣。关键在旺衰，不能一见旬空就断无用。

#### 4. 野鹤创新：多占法（重大方法论突破）

**核心观点**：
- ⭐ **不取伏神，多占几卦**：伏神法复杂难把握，多占法用神自现更稳妥
- ⭐ **批判互卦干化**：互卦法牵强附会，干化法舍近求远，误人子弟
- ⭐ **实战验证**：通过占父病五卦连占，既知目前之生，且知将来之死

**李我平评论**：
> 古法用伏神虽则有验，然伏神之衰旺休囚、刑冲克害、月破旬空，亦有难于把握者。此言多占两卦，自有用神，真秘法也。以此之秘，急欲传世，真婆心也！

**用神次序**：
1. 首选：卦中显现的用神
2. 次选：日月为用神
3. 三选：伏神（旺相有生扶）
4. 最优：多占一卦，用神自现

#### 5. 批判互卦、干化法

**批判互卦法（《易冒》）**：
- 案例：占子病，得"晋之剥"，子水子孙伏于初爻
- 《易冒》：飞伏变象皆无用神，互出水地比卦
- 野鹤批判：子水伏于初爻，得日辰生扶，至十月亥水当令，子孙出而逢生，明之极矣。**何用互卦？**

**批判干化法（《易冒》）**：
- 案例：占子存亡，得"观之萃"，世爻未土发动化出亥水子孙
- 《易冒》：水为子孙不现，却得丙月辛年，丙辛化水
- 野鹤批判：世爻未土发动，化出亥水子孙，变爻明明有子孙。**何故不取变爻而取干化？是误后人，非教后人也。**

---

### 三、系统设计建议

#### 1. 月破判断工具类（YuePoUtil）

```java
/**
 * 月破判断工具
 */
@Component
public class YuePoUtil {
    
    /**
     * 判断爻是否月破
     */
    public static boolean isYuePo(String yaoDiZhi, String yueJian) {
        return LiuChongUtil.isChong(yaoDiZhi, yueJian);
    }
    
    /**
     * 判断月破爻是否有用
     * 核心逻辑：
     * 1. 月破爻发动 → 有用但力量减弱
     * 2. 月破爻静止 → 有生助则有用，无生助则无用
     */
    public static YuePoStatus checkYuePoStatus(Yao yao, DivinationContext context) {
        if (!isYuePo(yao.getDiZhi(), context.getYueJian())) {
            return new YuePoStatus(false, true, null);
        }
        
        // 月破爻发动 → 有用但力量减弱
        if (yao.isChanging()) {
            return new YuePoStatus(true, true, "月破爻发动，有用但力量减弱");
        }
        
        // 月破爻静止 → 检查是否有生助
        boolean hasSupport = hasShengZhu(yao, context);
        if (hasSupport) {
            return new YuePoStatus(true, true, "月破爻静止，有生助则有用");
        } else {
            return new YuePoStatus(true, false, "月破爻静止无生助，到底而破");
        }
    }
    
    /**
     * 计算月破爻的应期
     * 三法：出月、实破之日、逢合之日
     */
    public static List<YingQi> calculateYuePoYingQi(Yao yao, DivinationContext context) {
        List<YingQi> results = new ArrayList<>();
        
        // 1. 出月
        String nextMonth = getNextMonth(context.getYueJian());
        results.add(new YingQi("出月", nextMonth + "月"));
        
        // 2. 实破之日（填实月破）
        results.add(new YingQi("实破之日", yao.getDiZhi() + "日"));
        
        // 3. 逢合之日
        String heDiZhi = LiuHeUtil.getHeDiZhi(yao.getDiZhi());
        if (heDiZhi != null) {
            results.add(new YingQi("逢合之日", heDiZhi + "日"));
        }
        
        return results;
    }
}
```

#### 2. 飞伏神判断工具类（FeiFuShenUtil）

```java
/**
 * 飞伏神判断工具
 */
@Component
public class FeiFuShenUtil {
    
    /**
     * 寻找伏神
     * 顺序：1.卦中用神 2.日月为用神 3.本宫首卦寻伏神
     */
    public static FuShenInfo findFuShen(Hexagram hexagram, String liuQin, 
                                         DivinationContext context) {
        // 1. 检查用神是否在卦中
        for (Yao yao : hexagram.getYaos()) {
            if (liuQin.equals(yao.getLiuQin())) {
                return new FuShenInfo(false, null, null, "用神明现");
            }
        }
        
        // 2. 检查日月是否为用神
        String riChenLiuQin = getLiuQinByDiZhi(context.getRiChen(), hexagram);
        if (liuQin.equals(riChenLiuQin)) {
            return new FuShenInfo(false, null, null, "日辰为用神");
        }
        
        String yueJianLiuQin = getLiuQinByDiZhi(context.getYueJian(), hexagram);
        if (liuQin.equals(yueJianLiuQin)) {
            return new FuShenInfo(false, null, null, "月建为用神");
        }
        
        // 3. 到本宫首卦寻找伏神
        Hexagram shouGua = getGongShouGua(hexagram.getGong());
        for (int i = 0; i < 6; i++) {
            Yao shouGuaYao = shouGua.getYaos().get(i);
            if (liuQin.equals(shouGuaYao.getLiuQin())) {
                Yao feiShen = hexagram.getYaos().get(i);
                return new FuShenInfo(true, shouGuaYao, feiShen, 
                    "伏于" + (i+1) + "爻" + feiShen.getDiZhi() + "之下");
            }
        }
        
        return null;
    }
    
    /**
     * 判断伏神是否有用
     * 六法有用，五法无用
     */
    public static FuShenStatus checkFuShenStatus(FuShenInfo fuShenInfo, 
                                                  DivinationContext context) {
        // 检查伏神有用六法
        // 1. 伏神得日月生
        // 2. 伏神旺相
        // 3. 伏神得飞神生（飞来生伏）
        // 4. 伏神得动爻生
        // 5. 日月动爻冲克飞神
        // 6. 飞神空破休囚墓绝
        
        // 检查伏神无用五法
        // 1. 伏神休囚无气
        // 2. 伏神被日月冲克
        // 3. 伏神被旺相飞神克（飞来克伏）
        // 4. 伏神墓绝于日月飞爻
        // 5. 伏神休囚值旬空月破
        
        // ... 详细实现 ...
    }
    
    /**
     * 建议多占法
     */
    public static String suggestDuoZhan(FuShenInfo fuShenInfo, 
                                        FuShenStatus status) {
        if (fuShenInfo.isFuShen() && !status.isUseful()) {
            return "伏神无用，建议多占一卦，用神自现更稳妥";
        }
        
        if (fuShenInfo.isFuShen() && status.isUseful()) {
            return "伏神虽有用，但衰旺休囚、刑冲克害难于把握，建议多占一卦验证";
        }
        
        return null;
    }
}
```

#### 3. DivinationResponse DTO 更新

```java
@Data
public class DivinationResponse {
    // ... 现有字段 ...
    
    // 月破信息
    private List<YuePoInfo> yuePoInfos;
    
    // 飞伏神信息
    private FuShenAnalysis fuShenAnalysis;
}

@Data
class YuePoInfo {
    private int yaoPosition;           // 爻位
    private String yaoDiZhi;           // 爻地支
    private boolean isYuePo;           // 是否月破
    private boolean isUseful;          // 是否有用
    private String reason;             // 原因
    private List<YingQi> yingQiList;   // 应期列表
}

@Data
class FuShenAnalysis {
    private String targetLiuQin;       // 目标六亲（用神）
    private boolean needFuShen;        // 是否需要伏神
    private FuShenInfo fuShenInfo;     // 伏神信息
    private FuShenStatus fuShenStatus; // 伏神状态
    private String suggestion;         // 建议（多占法）
}
```

---

### 四、关键理论突破总结

#### 1. 月破理论突破
- **古法错误**：月破百无一用
- **野鹤新论**：月破爻发动仍有用，神兆机于动
- **实战验证**：半生凶吉，关乎一爻月破
- **应期三法**：出月、实破之日、逢合之日

#### 2. 飞伏神方法论突破
- **古法复杂**：伏神法、互卦法、干化法
- **野鹤创新**：多占法，用神自现更稳妥
- **批判古法**：互卦牵强附会，干化舍近求远，误人子弟
- **实战验证**：五卦连占，既知目前之生，且知将来之死

#### 3. AI系统设计启示
- **月破判断**：不能一刀切，需区分动静、有无生助
- **飞伏神处理**：提供多占建议，而非强行使用伏神法
- **应期计算**：月破爻的应期计算需特殊处理
- **用户体验**：当用神不上卦时，建议用户重新占卜，而非复杂的伏神分析

---

## 📖 knowledge-liuyao10.md 核心发现（卷二）

### 一、进神退神基本定义

#### 1. 核心概念

**进神退神**：指动爻变化的方向和趋势。

**核心原则**：
- **所喜者，宜化进神**：用神、元神化进神 → 事业发展，长久兴旺
- **所忌者，宜化退神**：忌神、仇神化退神 → 灾祸减轻，逢凶化吉

**形象比喻**：
- **进神**：如春木之荣，有源之水，久远长久之象
- **退神**：如秋天花木，渐渐凋零，力量减弱

#### 2. 进神退神配对表

**水木火金**：同五行，地支顺进逆退

| 五行 | 进神配对 | 退神配对 |
|------|----------|----------|
| 水 | 亥→子 | 子→亥 |
| 木 | 寅→卯 | 卯→寅 |
| 火 | 巳→午 | 午→巳 |
| 金 | 申→酉 | 酉→申 |

**四库土**：按四季循环进退

| 进神循环 | 退神循环 |
|----------|----------|
| 丑→辰→未→戌→丑 | 辰→丑、未→辰、戌→未、丑→戌 |

**规律总结**：
- 水木火金：顺时针为进，逆时针为退
- 四库土：丑辰未戌循环为进，逆循环为退

---

### 二、野鹤进退四法（核心理论）

#### 进神之法有四

| 序号 | 条件 | 特点 | 应期 | 举例 |
|------|------|------|------|------|
| 1 | 动旺相而化旺相 | 乘势而进，立即见效 | 当时即应 | 春天寅木动化卯木，官星进神立刻升迁 |
| 2 | 动休囚而化休囚 | 待时而进，需等当令 | 等待旺相之时 | 秋天寅木动化卯木，需等春天才进 |
| 3 | 动爻变爻有一休囚 | 也能进，待旺相之日 | 休囚者得令之时 | 春天寅木动化丑土（土囚），等土旺之时 |
| 4 | 动爻变爻有一空破 | 也能进，待填实之日 | 出空、实破之日 | 寅木动化卯木旬空月破，等出空实破之日 |

**核心要点**：
- ✅ 进神不看旺衰，关键看填实之时
- ✅ 空破也能进，出空实破即应
- ✅ 旺相进得快，休囚待时进

#### 退神之法有四

| 序号 | 条件 | 特点 | 应期 | 举例 |
|------|------|------|------|------|
| 1 | 动旺相而化旺相，或有日月动爻生扶 | 占近事得时而不退 | 近事不退，远事待休囚时退 | 秋天酉金动化申金（退神），占近病不退 |
| 2 | 动休囚而化休囚 | 及时而退，退得快 | 当时即退 | 秋天寅木动化丑土（退神），木囚速退 |
| 3 | 动爻变爻有一旺相 | 待休囚之时而退 | 旺相者休囚之时 | 春天卯木动化寅木（退神），需等秋天才退 |
| 4 | 动爻变爻有一空破 | 待填实之日而退 | 出空、实破之日 | 戌土动化未土（退神）旬空月破，等出空实破之日 |

**核心要点**：
- ✅ 退神看旺衰，旺相不退，休囚速退
- ✅ 占近事看当时，占远事看将来
- ✅ 空破也能退，出空实破即应

---

### 三、理论突破：批判《易冒》错误理论

#### 1. 批判"动日月化空破不进"论

**《易冒》错误观点**：
- ❌ 动爻临日月而化空破，无阶无路，不能进

**野鹤/李我平正确理论**：
- ✅ **动爻临日月，逢空不空，逢破不破**
- ✅ **日月如天，虽化空破，如浮云掩日**
- ✅ **待实空实破之期，云开雾散，依然司令当权**

**实战验证**：
- **例二**：子孙持世化进神，旬空化空破，寅卯年连生九子
- **例六**：官动化进神，动非日月而化破，丑月得官差
- **例十二**：父爻休囚化空，出空即愈

#### 2. 批判"动破散变日月不前"论

**《易冒》错误观点**：
- ❌ 动值破散而变日月，我位既失，不能前进

**野鹤/李我平正确理论**：
- ✅ **既动而破，自有实破之期**
- ✅ **既动而散，自有填实之日**
- ✅ **化日月名为化旺，后日填实愈强愈旺**

**实战验证**：
- **例五**：世爻申金动化酉金（进神），月破，亥月出行（破而逢合）
- **例十六**：兄动化退，月破，戌月方回（实破之月）
- **例二十一**：财爻持世化进神，动日月而空，戌日开市大发

---

### 四、21个实战卦例精华总结

本章包含21个详细卦例，全面验证进退四法：

#### 功名科举类（3例）
- **例一**：占乡试（恒之大过）→ 官化进神，联捷
- **例七**：占功名出仕（同人之革）→ 子孙化退神，辰年出仕
- **例十九**：占乡试（兑之讼）→ 世爻化进神，动散化空也能中

#### 求子生育类（1例）
- **例二**：占何年生子（屯之节）→ 子孙持世化进神，旬空化空破，连生九子

#### 婚姻嫁娶类（1例）
- **例三**：占求婚（噬嗑之比）→ 财爻持世化进神，午日允婚

#### 官运升迁类（3例）
- **例四**：占自陈（师之明夷）→ 官化退神，次年革职
- **例六**：占得官差（节之需）→ 官动化进神，丑月得差
- **例十八**：占官府升迁（解之困）→ 官动化进神，巳月升职

#### 疾病健康类（5例）
- **例八**：占病（夬之大壮）→ 子孙化退神，旺相不退，用针愈
- **例九**：占妻病（临之泰）→ 兄动化进神，灵丹莫救
- **例十**：占病（乾之夬）→ 父化退神，精神命脉渐枯，丑月卒
- **例十二**：占父近病（萃之否）→ 父母化进神，休囚化空，戌日痊
- **例十七**：占子病（大有之睽）→ 父母临月建化退神，次日死

#### 父母安危类（2例）
- **例五**：占父归期（蹇之旅）→ 父化退神，世化进神，未日相会
- **例十一**：占母血崩（同人之解）→ 父临日建化退神，丑月卒

#### 流年凶吉类（2例）
- **例十三**：占流年（困之解）→ 酉金化退神，旺而不退，六月入墓亡
- **例十四**：占妻母病（随之否）→ 未土化进神，被冲散化空破未散，戌日卒

#### 出行归期类（2例）
- **例十五**：占出行（屯之节）→ 世爻化进神，月破待出月，亥月出行平安
- **例十六**：占兄归期（履之兑）→ 兄动化退神，月破，戌月方回

#### 财运开业类（2例）
- **例二十**：占母何时来（大有之井）→ 兄弟化退神，空化空也能退，妹次年三月至
- **例二十一**：占开金银铺（噬嗑之屯）→ 财爻持世化进神，动日月而空，戌日开市大发

**卦例验证要点**：
- ✅ 化空破也能进退，关键看出空实破之时
- ✅ 动日月化空破，依然司令当权
- ✅ 动破散变日月，化旺愈强愈旺
- ✅ 旺相不退，休囚速退
- ✅ 近事看当时，远事看将来

---

### 五、系统设计建议

#### 1. 进神退神判断工具类（JinTuiShenUtil）

```java
/**
 * 进神退神判断工具
 */
@Component
public class JinTuiShenUtil {
    
    /**
     * 判断是否进神或退神
     * @param dongYaoDiZhi 动爻地支
     * @param bianYaoDiZhi 变爻地支
     * @return "进神"/"退神"/null
     */
    public static String checkJinTuiShen(String dongYaoDiZhi, String bianYaoDiZhi) {
        // 水木火金：同五行，地支顺进逆退
        if (isJinShen(dongYaoDiZhi, bianYaoDiZhi)) {
            return "进神";
        } else if (isTuiShen(dongYaoDiZhi, bianYaoDiZhi)) {
            return "退神";
        }
        return null;
    }
    
    /**
     * 判断是否进神
     */
    private static boolean isJinShen(String dong, String bian) {
        // 水：亥→子
        if ("亥".equals(dong) && "子".equals(bian)) return true;
        // 木：寅→卯
        if ("寅".equals(dong) && "卯".equals(bian)) return true;
        // 火：巳→午
        if ("巳".equals(dong) && "午".equals(bian)) return true;
        // 金：申→酉
        if ("申".equals(dong) && "酉".equals(bian)) return true;
        // 土：丑→辰→未→戌→丑
        if ("丑".equals(dong) && "辰".equals(bian)) return true;
        if ("辰".equals(dong) && "未".equals(bian)) return true;
        if ("未".equals(dong) && "戌".equals(bian)) return true;
        if ("戌".equals(dong) && "丑".equals(bian)) return true;
        return false;
    }
    
    /**
     * 判断是否退神
     */
    private static boolean isTuiShen(String dong, String bian) {
        // 水：子→亥
        if ("子".equals(dong) && "亥".equals(bian)) return true;
        // 木：卯→寅
        if ("卯".equals(dong) && "寅".equals(bian)) return true;
        // 火：午→巳
        if ("午".equals(dong) && "巳".equals(bian)) return true;
        // 金：酉→申
        if ("酉".equals(dong) && "申".equals(bian)) return true;
        // 土：辰→丑、未→辰、戌→未、丑→戌
        if ("辰".equals(dong) && "丑".equals(bian)) return true;
        if ("未".equals(dong) && "辰".equals(bian)) return true;
        if ("戌".equals(dong) && "未".equals(bian)) return true;
        if ("丑".equals(dong) && "戌".equals(bian)) return true;
        return false;
    }
    
    /**
     * 判断进神是否有用（进神四法）
     * @param dongYao 动爻
     * @param bianYao 变爻
     * @param context 卦象上下文
     * @return 进神状态
     */
    public static JinShenStatus checkJinShenStatus(Yao dongYao, Yao bianYao, 
                                                    DivinationContext context) {
        // 1. 动旺相而化旺相，乘势而进
        if (isWangXiang(dongYao, context) && isWangXiang(bianYao, context)) {
            return new JinShenStatus(true, "乘势而进", "当时即应");
        }
        
        // 2. 动休囚而化休囚，待时而进
        if (isXiuQiu(dongYao, context) && isXiuQiu(bianYao, context)) {
            return new JinShenStatus(true, "待时而进", "等待旺相之时");
        }
        
        // 3. 动爻变爻有一而值休囚，亦得旺相之日而进
        if (isXiuQiu(dongYao, context) || isXiuQiu(bianYao, context)) {
            return new JinShenStatus(true, "待旺相之日而进", "休囚者得令之时");
        }
        
        // 4. 动爻变爻有一而值空破，待填实之日而进
        if (isKongPo(dongYao, context) || isKongPo(bianYao, context)) {
            return new JinShenStatus(true, "待填实之日而进", "出空、实破之日");
        }
        
        return new JinShenStatus(true, "进神有用", "综合判断");
    }
    
    /**
     * 判断退神是否有用（退神四法）
     */
    public static TuiShenStatus checkTuiShenStatus(Yao dongYao, Yao bianYao, 
                                                    DivinationContext context,
                                                    boolean isNearEvent) {
        // 1. 动旺相而化旺相，或有日月动爻生扶，占近事得时而不退
        if (isWangXiang(dongYao, context) && isWangXiang(bianYao, context)) {
            if (isNearEvent) {
                return new TuiShenStatus(false, "占近事得时而不退", "近事不退");
            } else {
                return new TuiShenStatus(true, "占远事待休囚时退", "休囚之时");
            }
        }
        
        // 2. 动休囚而化休囚，及时而退
        if (isXiuQiu(dongYao, context) && isXiuQiu(bianYao, context)) {
            return new TuiShenStatus(true, "及时而退", "当时即退");
        }
        
        // 3. 动爻变爻有一而旺相，待休囚之时而退
        if (isWangXiang(dongYao, context) || isWangXiang(bianYao, context)) {
            return new TuiShenStatus(true, "待休囚之时而退", "旺相者休囚之时");
        }
        
        // 4. 动爻变爻有一而逢空破，待填实之日而退
        if (isKongPo(dongYao, context) || isKongPo(bianYao, context)) {
            return new TuiShenStatus(true, "待填实之日而退", "出空、实破之日");
        }
        
        return new TuiShenStatus(true, "退神有用", "综合判断");
    }
}

@Data
@AllArgsConstructor
class JinShenStatus {
    private boolean isUseful;      // 是否有用
    private String description;    // 描述
    private String yingQi;         // 应期
}

@Data
@AllArgsConstructor
class TuiShenStatus {
    private boolean isTuiDiao;     // 是否退掉
    private String description;    // 描述
    private String yingQi;         // 应期
}
```

#### 2. DivinationResponse DTO 更新

```java
@Data
public class DivinationResponse {
    // ... 现有字段 ...
    
    // 进神退神信息
    private List<JinTuiShenInfo> jinTuiShenInfos;
}

@Data
class JinTuiShenInfo {
    private int yaoPosition;           // 爻位
    private String dongYaoDiZhi;       // 动爻地支
    private String bianYaoDiZhi;       // 变爻地支
    private String type;               // "进神"/"退神"
    private String liuQin;             // 六亲
    private boolean isXiShen;          // 是否喜神（用神、元神）
    private boolean isJiShen;          // 是否忌神（仇神、忌神）
    private String jiXiong;            // 吉凶判断
    private JinShenStatus jinShenStatus;  // 进神状态（如果是进神）
    private TuiShenStatus tuiShenStatus;  // 退神状态（如果是退神）
    private String yingQi;             // 应期
}
```

#### 3. 数据库设计（可选）

```sql
-- 进神退神配置表
CREATE TABLE jin_tui_shen_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    dong_yao_dizhi VARCHAR(2) NOT NULL COMMENT '动爻地支',
    bian_yao_dizhi VARCHAR(2) NOT NULL COMMENT '变爻地支',
    type ENUM('进神', '退神') NOT NULL COMMENT '类型',
    wu_xing VARCHAR(2) NOT NULL COMMENT '五行',
    description VARCHAR(100) COMMENT '描述',
    UNIQUE KEY uk_dong_bian (dong_yao_dizhi, bian_yao_dizhi)
) COMMENT='进神退神配置表';

-- 插入数据
-- 水：亥→子（进），子→亥（退）
INSERT INTO jin_tui_shen_config VALUES (NULL, '亥', '子', '进神', '水', '水进一位');
INSERT INTO jin_tui_shen_config VALUES (NULL, '子', '亥', '退神', '水', '水退一位');
-- 木：寅→卯（进），卯→寅（退）
INSERT INTO jin_tui_shen_config VALUES (NULL, '寅', '卯', '进神', '木', '木进一位');
INSERT INTO jin_tui_shen_config VALUES (NULL, '卯', '寅', '退神', '木', '木退一位');
-- ... 其他配置 ...
```

---

### 六、关键理论突破总结

#### 1. 进神退神不看空破（重大突破）
- **古法错误**：动日月化空破不进，动破散变日月不前
- **野鹤新论**：化空破也能进退，关键看出空实破之时
- **实战验证**：21个卦例全面验证

#### 2. 进退四法精准判断
- **进神四法**：旺相乘势进，休囚待时进，空破待填实
- **退神四法**：旺相近不退，休囚及时退，空破待填实
- **应期精准**：出空、实破、旺相、休囚之时

#### 3. 占近事与占远事区别
- **占近事**：旺相不退，当时见效
- **占远事**：待休囚时退，长远观察
- **实战应用**：占病看近，占运看远

#### 4. AI系统设计启示
- **自动判断进退**：根据地支配对自动识别
- **智能计算应期**：结合旺衰、空破、填实综合判断
- **吉凶分析**：喜神进吉、忌神退吉，反之则凶
- **用户提示**：明确告知应期和吉凶原因

---

**更新时间**: 2025-10-28 16:36  
**状态**: 进行中（已完成10/12文件，83.3%，卷二进行中）

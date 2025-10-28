# 六爻系统任务清单 - 阶段三、四更新版

> **基于30个核心功能 + Linus式架构优化**

---

## 阶段三：起卦核心功能（第3周）

### 设计理念
- **数据结构分离**：固有属性（Yao）与计算属性（YaoState）彻底分离
- **程序生成数据**：64卦数据用程序生成，而不是手写SQL
- **知识库覆盖**：100%覆盖knowledge-liuyao01.md和liuyao02.md

---

### 任务3.1：基础数据结构（Linus式优化）[P0]

#### 子任务3.1.1：核心枚举类

- [ ] **BaGua.java** - 八卦枚举
  - 位置: `enums/BaGua.java`
  - 内容: 乾、兑、离、震、巽、坎、艮、坤（8个）
  - 知识库: knowledge-liuyao01.md (10-104行)

- [ ] **TianGan.java** - 天干枚举
  - 位置: `enums/TianGan.java`
  - 内容: 甲乙丙丁戊己庚辛壬癸（10个）

- [ ] **DiZhi.java** - 地支枚举
  - 位置: `enums/DiZhi.java`
  - 内容: 子丑寅卯辰巳午未申酉戌亥（12个）

- [ ] **WuXing.java** - 五行枚举
  - 位置: `enums/WuXing.java`
  - 内容: 金木水火土（5个）
  - 方法: `getChangSheng()`, `getDiWang()`, `getMuKu()`, `getJueDi()`, `sheng()`, `ke()`
  - 知识库: knowledge-liuyao08.md (49-223行)

- [ ] **LiuQin.java** - 六亲枚举
  - 位置: `enums/LiuQin.java`
  - 内容: 父母、兄弟、子孙、妻财、官鬼（5个）

- [ ] **LiuShen.java** - 六神枚举
  - 位置: `enums/LiuShen.java`
  - 内容: 青龙、朱雀、勾陈、螣蛇、白虎、玄武（6个）
  - 方法: `getByRiGan(TianGan)` - 根据日干获取六神配置

- [ ] **WangShuai.java** - 旺衰枚举
  - 位置: `enums/WangShuai.java`
  - 内容: 旺、相、休、囚、死（5个，含力量权重）

- [ ] **ZhanBuLeiXing.java** - 占卜类型枚举
  - 位置: `enums/ZhanBuLeiXing.java`
  - 内容: 功名、财运、婚姻、疾病、出行等（15+个）
  - 知识库: knowledge-liuyao02.md (348-397行)

#### 子任务3.1.2：固有属性实体类

- [ ] **Yao.java** - 爻实体（不可变对象）
  - 位置: `model/entity/Yao.java`
  - 属性: `weiZhi`, `diZhi`, `liuQin`, `isDong`, `bianYao`
  - 特点: 只有getter，无setter

- [ ] **GuaXiang.java** - 卦象实体（不可变对象）
  - 位置: `model/entity/GuaXiang.java`
  - 属性: `id`, `guaName`, `suoShuGong`, `gongWuXing`, `shiYaoWei`, `yingYaoWei`, `yaoList`
  - 特点: 只有getter，无setter

#### 子任务3.1.3：计算属性类

- [ ] **YaoState.java** - 爻状态（计算属性）
  - 位置: `model/dto/YaoState.java`
  - 属性: `yao`, `wangShuai`, `xunKong`, `yuePo`, `anDong`, `dongSan`, `riHe`, `yueHe`, `riChong`, `yueChong`, `riSheng`, `yueSheng`, `ruMu`, `linJue`, `jinTuiType`
  - 特点: 由`YaoStateCalculator`统一计算

- [ ] **DivinationContext.java** - 起卦上下文（完整版）
  - 位置: `model/dto/DivinationContext.java`
  - 属性: `riGan`, `riChen`, `yueJian`, `divinationTime`, `benGua`, `bianGua`, `zhanBuLeiXing`, `wenShi`, `gender`, `yaoStateCache`
  - 方法: `getYaoState(int yaoWei)` - 获取爻状态（带缓存）

---

### 任务3.2：64卦数据生成器（Linus式）[P0]

**核心思想**: 用程序生成64卦数据，而不是手写64条INSERT语句

- [ ] **GongConfig.java** - 宫位配置类
  - 位置: `model/dto/GongConfig.java`
  - 属性: `gongName`, `gongWuXing`, `naJiaSeq`

- [ ] **NaJiaConfigurator.java** - 纳甲配置器
  - 位置: `utils/liuyao/NaJiaConfigurator.java`
  - 方法: `getNaJiaSequence(String gongName)` - 返回8宫纳甲序列
  - 知识库: knowledge-liuyao02.md (10-178行)

- [ ] **LiuQinGenerator.java** - 六亲生成器
  - 位置: `utils/liuyao/LiuQinGenerator.java`
  - 方法: `generate(WuXing gongWuXing, DiZhi yaoZhi)` - 根据宫五行和爻地支生成六亲
  - 知识库: knowledge-liuyao02.md (10-178行)

- [ ] **ShiYingLocator.java** - 世应定位器
  - 位置: `utils/liuyao/ShiYingLocator.java`
  - 方法: `getShiYaoWei(String guaLeiXing)`, `getYingYaoWei(int shiYaoWei)`
  - 知识库: knowledge-liuyao02.md (229-253行)

- [ ] **GuaXiangDataGenerator.java** - 64卦数据生成器
  - 位置: `utils/liuyao/GuaXiangDataGenerator.java`
  - 方法: `main()` - 生成64卦数据并输出SQL
  - 功能: 
    - 定义8宫配置
    - 遍历8宫，每宫生成8卦（本宫、一世~五世、游魂、归魂）
    - 自动配置六爻地支（纳甲法）
    - 自动配置六爻六亲
    - 自动配置世应位置
    - 验证数据完整性（assert检查）
    - 生成INSERT SQL语句
  - 知识库: knowledge-liuyao01.md (321-986行)

- [ ] **执行生成器**
  - 运行`GuaXiangDataGenerator.main()`
  - 生成文件: `src/main/resources/sql/08_insert_64_hexagrams.sql`
  - 验证数据：64卦、每宫8卦
  - 执行SQL导入数据库

---

### 任务3.3：起卦方法实现 [P0]

#### 子任务3.3.1：起卦基础架构

- [ ] **DivinationMethod.java** - 起卦方法接口
  - 位置: `service/divination/DivinationMethod.java`
  - 方法: `cast(DivinationContext ctx)` - 返回`DivinationResult`

- [ ] **DivinationResult.java** - 起卦结果
  - 位置: `model/dto/DivinationResult.java`
  - 属性: `benGua`, `bianGua`, `yaoList`, `dongYaoCount`

- [ ] **DivinationFactory.java** - 起卦工厂
  - 位置: `service/divination/DivinationFactory.java`
  - 方法: `getMethod(String type)` - 根据类型返回起卦方法

#### 子任务3.3.2：手动输入卦象法（优先！）

- [ ] **ManualInputDivinationMethod.java** - 手动输入卦象法
  - 位置: `service/divination/method/ManualInputDivinationMethod.java`
  - 输入格式: 6个爻的阴阳和动静信息
  - 逻辑: 
    1. 验证输入（必须6个爻，位置1-6，阴阳必填）
    2. 根据阴阳组合识别本卦（调用GuaXiangIdentifier）
    3. 根据动爻计算变卦（调用BianGuaCalculator）
    4. 返回DivinationResult
  - 使用场景: 
    - 用户线下已起卦（摇硬币、蓍草等），仅需系统解卦
    - 用户从书籍或其他来源获得卦象
    - 用户想测试特定卦象

#### 子任务3.3.3：钱币起卦法（自动）

- [ ] **CoinDivinationMethod.java** - 钱币起卦法
  - 位置: `service/divination/method/CoinDivinationMethod.java`
  - 逻辑: 
    - 6次投币（每次3枚硬币）
    - 3正=老阳（9，变爻），2正1反=少阴（8），2反1正=少阳（7），3反=老阴（6，变爻）
    - 生成本卦，根据变爻计算变卦
  - 知识库: knowledge-liuyao01.md (107-218行)

#### 子任务3.3.4：变卦计算器（公共工具）

- [ ] **BianGuaCalculator.java** - 变卦计算器
  - 位置: `utils/liuyao/BianGuaCalculator.java`
  - 方法: `calculate(GuaXiang benGua, List<Yao> yaoList)` - 根据动爻计算变卦
  - 知识库: knowledge-liuyao02.md (256-343行)

---

### 任务3.4：卦象识别器 [P0]

- [ ] **GuaXiangIdentifier.java** - 卦象识别器
  - 位置: `utils/liuyao/GuaXiangIdentifier.java`
  - 方法: `identify(List<YaoType> yaoList)` - 根据6个爻识别卦象
  - 逻辑: 
    - 将6个爻转换为二进制（阳=1，阴=0）
    - 查询数据库匹配卦象
    - 返回`GuaXiang`对象

---

### 任务3.5：起卦Service和Controller [P0]

- [ ] **DivinationService.java** - 起卦服务接口
  - 位置: `service/DivinationService.java`

- [ ] **DivinationServiceImpl.java** - 起卦服务实现
  - 位置: `service/impl/DivinationServiceImpl.java`

- [ ] **DivinationController.java** - 起卦控制器
  - 位置: `controller/divination/DivinationController.java`

- [ ] **DivinationTestController.java** - 起卦测试控制器
  - 位置: `controller/test/DivinationTestController.java`

---

## 阶段四：解卦核心功能（第4周）

### 设计理念
- **规则链引擎**：零if嵌套，易扩展
- **30个核心功能**：知识库100%覆盖
- **优先级驱动**：P0核心规则优先执行

---

### 任务4.1：地支关系工具 [P0]

- [ ] **DiZhiRelations.java** - 地支关系工具
  - 位置: `utils/liuyao/DiZhiRelations.java`
  - 方法: 
    - `getLiuHe(DiZhi dz)` - 获取六合地支
    - `getLiuChong(DiZhi dz)` - 获取六冲地支
    - `getWuXing(DiZhi dz)` - 获取地支对应五行
    - `isLiuHe(DiZhi dz1, DiZhi dz2)` - 判断是否六合
    - `isLiuChong(DiZhi dz1, DiZhi dz2)` - 判断是否六冲
  - 知识库: knowledge-liuyao04.md (34-369行), knowledge-liuyao05.md (42-503行)

---

### 任务4.2：旬空查询工具 [P0]

- [ ] **XunKongUtil.java** - 旬空工具
  - 位置: `utils/liuyao/XunKongUtil.java`
  - 方法: 
    - `getXunKong(TianGan riGan, DiZhi riZhi)` - 获取旬空地支
    - `isXunKong(DiZhi yaoZhi, TianGan riGan, DiZhi riZhi)` - 判断是否旬空
  - 知识库: knowledge-liuyao07.md (391-737行)

---

### 任务4.3：用神选取器 [P0]

- [ ] **YongShenSelector.java** - 用神选取器
  - 位置: `utils/liuyao/YongShenSelector.java`
  - 方法: `selectYongShen(String zhanBuLeiXing, String gender)` - 返回用神六亲
  - 逻辑: 
    - 占功名 → 官鬼爻
    - 占财运 → 妻财爻
    - 占婚姻 → 男占财，女占官
    - 占疾病 → 官鬼爻（病神）
    - ... 15+种占卜类型
  - 知识库: knowledge-liuyao02.md (346-401行)

---

### 任务4.4：旺衰判断器 [P0]

- [ ] **WangShuaiJudge.java** - 旺衰判断器
  - 位置: `utils/liuyao/WangShuaiJudge.java`
  - 方法: `judge(DiZhi yueJian, WuXing wuXing)` - 返回`WangShuai`枚举
  - 逻辑: 四时旺相规则
    - 春季：木旺、火相、土死、金囚、水休
    - 夏季：火旺、土相、金死、水囚、木休
    - 秋季：金旺、水相、木死、火囚、土休
    - 冬季：水旺、木相、火死、土囚、金休
  - 知识库: knowledge-liuyao03.md (100-121行)

---

### 任务4.5：爻状态计算器（核心）[P0]

- [ ] **YaoStateCalculator.java** - 爻状态计算器
  - 位置: `utils/liuyao/YaoStateCalculator.java`
  - 方法: `calculate(Yao yao, DivinationContext ctx)` - 返回`YaoState`对象
  - 计算内容: 
    - 旺衰状态
    - 旬空状态
    - 月破状态
    - 暗动状态（日冲旺相之静爻）
    - 动散状态（日冲动爻）
    - 日月合冲状态
    - 日月生克状态
    - 入墓状态
    - 临绝状态
    - 进神退神判断

---

### 任务4.6：解卦规则链引擎（Linus式核心）[P0]

#### 子任务4.6.1：规则接口定义

- [ ] **JieGuaRule.java** - 解卦规则接口
  - 位置: `service/interpretation/rule/JieGuaRule.java`
  - 方法: 
    - `getName()` - 规则名称
    - `getPriority()` - 优先级
    - `shouldApply()` - 是否应用此规则
    - `analyze()` - 执行规则分析
    - `shouldBreak()` - 是否中断后续规则

#### 子任务4.6.2：核心规则实现（8个P0规则）

- [ ] **WuGenRule.java** - 用神无根规则（优先级1）
  - 位置: `service/interpretation/rule/WuGenRule.java`
  - 逻辑: 用神月破+日克+休囚 = 无根，中断后续规则
  - 知识库: knowledge-liuyao02.md (609-691行)

- [ ] **XunKongRule.java** - 旬空规则（优先级2）
  - 位置: `service/interpretation/rule/XunKongRule.java`
  - 逻辑: 判断是否真空（动爻空、旺相空、日冲空、日月生扶空 = 非真空）
  - 知识库: knowledge-liuyao07.md (391-737行)

- [ ] **YuePoRule.java** - 月破规则（优先级3）
  - 位置: `service/interpretation/rule/YuePoRule.java`
  - 逻辑: 野鹤新论 - 月破爻发动仍有用
  - 知识库: knowledge-liuyao09.md (月破章)

- [ ] **SanMuRule.java** - 三墓规则（优先级4）
  - 位置: `service/interpretation/rule/SanMuRule.java`
  - 逻辑: 日墓+动墓+化墓，旺相者入墓非真墓
  - 知识库: knowledge-liuyao11.md (全章)

- [ ] **DongJingShengKeRule.java** - 动静生克规则（优先级5）
  - 位置: `service/interpretation/rule/DongJingShengKeRule.java`
  - 逻辑: 动克静有力，静克动无力
  - 知识库: knowledge-liuyao03.md (10-57行)

- [ ] **GuaBianRule.java** - 卦变规则（优先级6）
  - 位置: `service/interpretation/rule/GuaBianRule.java`
  - 逻辑: 变生、变克、变墓、变绝判断
  - 知识库: knowledge-liuyao06.md (32-441行)

- [ ] **SiShenRule.java** - 四神规则（优先级7）
  - 位置: `service/interpretation/rule/SiShenRule.java`
  - 逻辑: 元神、忌神、仇神分析
  - 知识库: knowledge-liuyao02.md (427-526行)

- [ ] **YingQiRule.java** - 应期规则（优先级8）
  - 位置: `service/interpretation/rule/YingQiRule.java`
  - 逻辑: 12条应期规则
  - 知识库: knowledge-liuyao08.md (399-641行)

#### 子任务4.6.3：解卦引擎

- [ ] **JieGuaEngine.java** - 解卦引擎
  - 位置: `service/interpretation/JieGuaEngine.java`
  - 方法: `analyze(DivinationContext ctx)` - 返回`JieGuaResult`
  - 逻辑: 
    1. 选取用神
    2. 执行规则链（按优先级）
    3. 综合判断

- [ ] **JieGuaResult.java** - 解卦结果
  - 位置: `model/dto/JieGuaResult.java`
  - 属性: `yongShen`, `judgements`, `finalResult`, `yingQi`, `siShen`

---

### 任务4.7：高级功能实现（16个P1规则）[P1]

- [ ] **FeiFuShenFinder.java** - 飞伏神查找器
  - 知识库: knowledge-liuyao09.md (飞伏神章)

- [ ] **JinTuiShenDetector.java** - 进神退神检测器
  - 知识库: knowledge-liuyao10.md (全章)

- [ ] **KeChuFengShengAnalyzer.java** - 克处逢生分析器
  - 知识库: knowledge-liuyao02.md (842-875行)

- [ ] **RiYuePeiHeAnalyzer.java** - 日月配合分析器
  - 知识库: knowledge-liuyao03.md (191-219行)

- [ ] **WuQiongZeBianAnalyzer.java** - 物穷则变分析器
  - 知识库: knowledge-liuyao03.md (253-279行)

- [ ] **LiuHeApplicationAnalyzer.java** - 六合应用分析器
  - 知识库: knowledge-liuyao04.md (34-369行)

- [ ] **LiuChongApplicationAnalyzer.java** - 六冲应用分析器
  - 知识库: knowledge-liuyao05.md (42-503行)

- [ ] **SanHeDetector.java** - 三合局检测器
  - 知识库: knowledge-liuyao04.md (497-727行)

- [ ] **FanYinFuYinDetector.java** - 反吟伏吟检测器
  - 知识库: knowledge-liuyao07.md (39-388行)

---

### 任务4.5：AI模型集成 [P0]

- [ ] 在application.properties配置LangChain4J参数
- [ ] 创建LangChain4J配置类 `LangChain4jConfig.java`
- [ ] 创建通义千问配置类 `QwenConfig.java`
- [ ] 创建Prompt模板常量类 `PromptConstants.java`
- [ ] 测试AI调用

---

### 任务4.6：AI智能解卦（核心！）[P0]

**设计理念**: **规则链引擎 + AI大模型** = 专业准确 + 通俗易懂

#### AI解卦数据流

```
起卦 → 规则链分析 → 构建Prompt → AI解释 → 流式返回
```

#### 子任务清单

- [ ] **PromptBuilder.java** - Prompt构建器
  - 位置: `utils/ai/PromptBuilder.java`
  - 功能: 将规则分析结果构建成AI Prompt

- [ ] **AiInterpretationService.java** - AI解卦服务
  - 位置: `service/AiInterpretationService.java`
  - 方法: `interpretWithAi()`, `interpretWithAiStream()`, `continueConversation()`

- [ ] **StreamingAiService.java** - 流式AI服务
  - 位置: `service/ai/StreamingAiService.java`
  - 功能: SSE推送、断开检测、超时控制

- [ ] **ConversationRecord.java** - 对话记录实体
  - 位置: `model/entity/ConversationRecord.java`
  - 存储: MongoDB

- [ ] **ConversationRecordRepository.java** - MongoDB Repository
  - 位置: `mapper/mongo/ConversationRecordRepository.java`

- [ ] **AiInterpretationController.java** - AI解卦控制器
  - 位置: `controller/interpretation/AiInterpretationController.java`
  - 接口: `/interpret`, `/stream`, `/continue`

- [ ] **FallbackInterpretationService.java** - 降级方案
  - 位置: `service/ai/FallbackInterpretationService.java`
  - 功能: AI不可用时返回结构化分析结果

- [ ] **RAG卦例检索（新增！）** 🆕
  - **VectorStoreConfig.java** - 向量数据库配置
  - **CaseVectorizer.java** - 卦例向量化工具
  - **CaseRetriever.java** - 卦例检索器
  - 导入50个经典卦例（MVP阶段）
  - 集成RAG到Prompt

---

### 任务4.7：解卦Service和Controller [P0]

- [ ] **InterpretationService.java** - 解卦服务接口
- [ ] **InterpretationServiceImpl.java** - 解卦服务实现
- [ ] **InterpretationController.java** - 解卦控制器
- [ ] **InterpretationTestController.java** - 解卦测试控制器

---

## 总结

### 功能统计
- **阶段三（起卦）**: 5个主任务，15+个类
- **阶段四（解卦）**: 7个主任务，45+个类（含AI+RAG）
- **知识库覆盖**: 30个核心功能，100%覆盖
- **AI集成**: Prompt工程、流式输出、多轮对话、降级方案、**RAG检索** 🆕

### 优先级分布
- **P0核心功能**: 15个（含RAG）
- **P1重要功能**: 16个
- **去除功能**: 6个（野鹤说"不用亦可"）

### 设计优势
- **Linus式架构**: 数据结构分离、规则链驱动、零if嵌套
- **程序生成数据**: 64卦数据自动生成，避免手写错误
- **知识库驱动**: 每个功能都有明确的知识库引用
- **RAG增强**: 经典案例参考，AI解卦更专业可信 🆕

### 技术栈
- **后端**: Spring Boot + MyBatis-Plus + Redis
- **数据库**: MySQL + MongoDB
- **向量数据库**: Pinecone 🆕
- **AI框架**: LangChain4J + 通义千问
- **RAG**: 经典卦例检索增强 🆕

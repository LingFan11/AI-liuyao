# 任务3.3.1：起卦基础架构 - 完成文档

> **任务状态**: ✅ 已完成  
> **完成时间**: 2025-10-29  
> **所属阶段**: 阶段三 - 起卦核心功能  
> **优先级**: P0

---

## 一、任务概述

### 目标
创建起卦方法的基础架构，包括接口定义、数据传输对象、工厂类等核心组件。

### 设计原则
1. **策略模式** - 不同起卦方法实现统一接口
2. **工厂模式** - Spring自动注册和管理所有起卦方法
3. **类型安全** - 使用抽象基类+子类保证请求类型安全
4. **不可变对象** - DivinationResult使用final字段，保证线程安全

---

## 二、已完成的组件

### 2.1 常量类

**文件**: `constant/DivinationConstants.java`

**包含内容**:
- 起卦方法类型常量（MANUAL、COIN、TIME、NUMBER）
- 钱币起卦规则常量（老阳、少阴、少阳、老阴）
- 爻位常量（最小值、最大值、数量）
- 阴阳常量（YANG、YIN）
- 验证消息常量

**关键设计**:
- 所有常量使用`public static final`修饰
- 私有构造函数禁止实例化
- 清晰的分组和注释

---

### 2.2 起卦请求DTO

#### 2.2.1 抽象基类 - DivinationRequest

**文件**: `model/dto/request/DivinationRequest.java`

**字段**:
- `riGan` - 日干（必填）
- `riChen` - 日辰（必填）
- `yueJian` - 月建（必填）
- `divinationTime` - 起卦时间
- `zhanBuLeiXing` - 占卜类型
- `wenShi` - 问事内容
- `gender` - 性别

**抽象方法**:
- `getMethodType()` - 子类返回方法类型

**辅助方法**:
- `hasValidTimeInfo()` - 验证时空信息是否完整
- `getOrDefaultDivinationTime()` - 获取起卦时间（默认当前时间）

---

#### 2.2.2 手动输入法请求 - ManualDivinationRequest

**文件**: `model/dto/request/ManualDivinationRequest.java`

**特有字段**:
- `yaoInputList` - 6个爻的输入列表

**内部类YaoInput**:
- `weiZhi` - 爻位（1-6）
- `yinYang` - 阴阳标识（"YANG"或"YIN"）
- `isDong` - 是否动爻

**验证方法**:
- `hasValidYaoList()` - 验证爻列表是否有效
  - 必须6个爻
  - 爻位连续（1-6）
  - 阴阳标识合法
- `getDongYaoCount()` - 获取动爻数量

---

### 2.3 起卦结果DTO

**文件**: `model/dto/DivinationResult.java`

**核心字段**:
- `benGua` - 本卦（GuaXiang）
- `bianGua` - 变卦（GuaXiang，可为null）
- `yaoList` - 六爻列表（不可变List）
- `dongYaoCount` - 动爻数量
- `methodType` - 起卦方法类型
- `methodName` - 起卦方法名称
- `createTime` - 起卦时间

**业务方法**:
- `hasDongYao()` - 是否有动爻
- `hasBianGua()` - 是否有变卦
- `getBenGuaBinaryCode()` - 获取本卦二进制编码
- `getBianGuaBinaryCode()` - 获取变卦二进制编码
- `getYaoByWeiZhi(int)` - 获取指定位置的爻
- `getDongYaoList()` - 获取所有动爻
- `getJingYaoList()` - 获取所有静爻
- `isJingGua()` - 是否静卦
- `isQuanDong()` - 是否全动

**设计亮点**:
- 不可变对象（所有字段final）
- Builder模式构建
- 自动计算动爻数量
- 利用GuaXiang已有的getBinaryCode()方法，避免重复存储

---

### 2.4 起卦方法接口

**文件**: `service/divination/DivinationMethod.java`

**核心方法**:
```java
DivinationResult cast(DivinationRequest request);
```

**元数据方法**:
- `getMethodName()` - 获取方法名称
- `getMethodType()` - 获取方法类型代码
- `getMethodDescription()` - 获取方法描述（默认实现）
- `supportsRequest(DivinationRequest)` - 判断是否支持指定请求（默认实现）

**异常处理**:
- 参数无效 → `IllegalArgumentException`
- 业务异常 → `BusinessException`

---

### 2.5 起卦工厂类

**文件**: `service/divination/DivinationFactory.java`

**注册机制**:
- 实现`InitializingBean`接口
- Spring启动后自动执行`afterPropertiesSet()`
- 通过构造函数注入所有`DivinationMethod`实现类
- 自动注册到`ConcurrentHashMap`

**核心方法**:
- `getMethod(String)` - 获取起卦方法
- `supportsMethod(String)` - 判断是否支持
- `getSupportedMethods()` - 获取所有支持的方法类型
- `getMethodCount()` - 获取已注册方法数量
- `getMethodInfo(String)` - 获取方法详细信息
- `getAllMethodInfo()` - 获取所有方法信息

**线程安全**:
- 使用`ConcurrentHashMap`保证并发安全

**日志记录**:
- 启动时记录注册过程
- 重复注册时给出警告
- 方法不存在时给出详细错误信息

---

## 三、数据流设计

### 完整流程

```
1. 前端提交请求
   ↓
2. Controller接收DivinationRequest（如ManualDivinationRequest）
   ↓
3. DivinationFactory.getMethod(request.getMethodType())
   ↓ 
4. DivinationMethod.cast(request) → 执行起卦逻辑
   ├─ 验证请求参数
   ├─ 识别本卦（根据阴阳组合）
   ├─ 计算变卦（根据动爻）
   └─ 封装DivinationResult
   ↓
5. 用DivinationResult构建DivinationContext
   DivinationContext ctx = DivinationContext.create(
       result.getBenGua(),
       result.getBianGua(),
       request.getRiGan(),
       request.getRiChen(),
       request.getYueJian(),
       request.getDivinationTime(),
       request.getZhanBuLeiXing(),
       request.getWenShi(),
       request.getGender()
   )
   ↓
6. 解卦阶段使用DivinationContext
```

---

## 四、测试用例

### 4.1 常量类测试

```java
@Test
public void testDivinationConstants() {
    // 验证起卦方法类型
    assertEquals("MANUAL", DivinationConstants.METHOD_MANUAL);
    assertEquals("COIN", DivinationConstants.METHOD_COIN);
    
    // 验证钱币常量
    assertEquals(9, DivinationConstants.LAO_YANG);
    assertEquals(6, DivinationConstants.LAO_YIN);
    
    // 验证爻位常量
    assertEquals(1, DivinationConstants.YAO_WEI_MIN);
    assertEquals(6, DivinationConstants.YAO_WEI_MAX);
}
```

---

### 4.2 DivinationRequest测试

```java
@Test
public void testManualDivinationRequest() {
    // 创建请求
    ManualDivinationRequest request = new ManualDivinationRequest();
    request.setRiGan(TianGan.JIA);
    request.setRiChen(DiZhi.ZI);
    request.setYueJian(DiZhi.YIN);
    
    // 创建爻输入
    List<YaoInput> yaoList = new ArrayList<>();
    for (int i = 1; i <= 6; i++) {
        YaoInput yao = new YaoInput(i, i % 2 == 1 ? "YANG" : "YIN", false);
        yaoList.add(yao);
    }
    request.setYaoInputList(yaoList);
    
    // 验证
    assertTrue(request.hasValidTimeInfo());
    assertTrue(request.hasValidYaoList());
    assertEquals(0, request.getDongYaoCount());
    assertEquals("MANUAL", request.getMethodType());
}
```

---

### 4.3 DivinationResult测试

```java
@Test
public void testDivinationResult() {
    // 创建本卦（假设已有GuaXiang对象）
    GuaXiang benGua = createTestGuaXiang();
    
    // 创建爻列表
    List<Yao> yaoList = createTestYaoList();
    
    // 构建结果
    DivinationResult result = new DivinationResult.Builder()
            .benGua(benGua)
            .yaoList(yaoList)
            .methodType("MANUAL")
            .methodName("手动输入法")
            .build();
    
    // 验证
    assertNotNull(result.getBenGua());
    assertEquals(6, result.getYaoList().size());
    assertEquals("MANUAL", result.getMethodType());
    assertNotNull(result.getBenGuaBinaryCode());
}
```

---

### 4.4 DivinationFactory测试

```java
@Test
public void testDivinationFactory() {
    // 注入工厂
    @Autowired
    private DivinationFactory factory;
    
    // 验证支持的方法
    assertTrue(factory.supportsMethod("MANUAL"));
    assertTrue(factory.getMethodCount() > 0);
    
    // 获取方法
    DivinationMethod method = factory.getMethod("MANUAL");
    assertNotNull(method);
    assertEquals("MANUAL", method.getMethodType());
    
    // 测试不存在的方法
    assertThrows(BusinessException.class, () -> {
        factory.getMethod("INVALID");
    });
}
```

---

## 五、下一步工作

### 5.1 子任务3.3.2：手动输入卦象法实现（优先！）

**需要创建**:
- `service/divination/method/ManualInputDivinationMethod.java`
- 实现`DivinationMethod`接口
- 添加`@Component`注解，让Spring自动注册

**核心逻辑**:
1. 验证`ManualDivinationRequest`参数
2. 根据6个爻的阴阳组合识别本卦（调用`GuaXiangIdentifier`）
3. 根据动爻计算变卦（调用`BianGuaCalculator`）
4. 封装`DivinationResult`

---

### 5.2 依赖工具类

在实现具体起卦方法前，需要先完成以下工具类：

#### GuaXiangIdentifier - 卦象识别器
**位置**: `utils/liuyao/GuaXiangIdentifier.java`
**方法**: `identify(List<YaoInput>)` - 根据6个爻识别卦象
**逻辑**: 
- 将6个爻转换为二进制（阳=1，阴=0）
- 查询数据库匹配卦象
- 返回`GuaXiang`对象

#### BianGuaCalculator - 变卦计算器
**位置**: `utils/liuyao/BianGuaCalculator.java`
**方法**: `calculate(GuaXiang benGua, List<Yao> yaoList)` - 根据动爻计算变卦
**逻辑**:
- 遍历爻列表，找出动爻
- 将动爻变化（阳→阴，阴→阳）
- 生成新的二进制编码
- 查询数据库获取变卦

---

### 5.3 测试Controller

**位置**: `controller/test/DivinationTestController.java`

**测试接口**:
1. `GET /test/divination/factory/info` - 查看工厂注册信息
2. `POST /test/divination/manual` - 测试手动输入法

---

## 六、文件清单

### 已创建文件（5个）

```
src/main/java/com/lingfan/liuyao/
├── constant/
│   └── DivinationConstants.java              ✅ 起卦常量
├── model/
│   └── dto/
│       ├── request/
│       │   ├── DivinationRequest.java        ✅ 起卦请求基类
│       │   └── ManualDivinationRequest.java  ✅ 手动输入法请求
│       └── DivinationResult.java             ✅ 起卦结果
└── service/
    └── divination/
        ├── DivinationMethod.java             ✅ 起卦方法接口
        └── DivinationFactory.java            ✅ 起卦工厂
```

---

## 七、设计亮点

### 7.1 与现有代码的完美对接

✅ **充分利用现有GuaXiang**:
- GuaXiang已有`getBinaryCode()`方法，DivinationResult不重复存储
- GuaXiang已有`getDongYaoList()`、`getDongYaoCount()`等方法

✅ **与DivinationContext解耦**:
- DivinationContext包含本卦和变卦，是起卦完成后的上下文
- DivinationRequest不包含卦象，是起卦的输入
- 清晰的数据流：Request → Method → Result → Context

---

### 7.2 扩展性设计

✅ **新增起卦方法零成本**:
1. 创建Request子类（如`CoinDivinationRequest`）
2. 实现DivinationMethod接口
3. 添加@Component注解
4. Spring自动注册，无需修改工厂代码

✅ **类型安全**:
- 抽象基类+子类保证请求类型安全
- 每个起卦方法有各自的Request子类

---

### 7.3 线程安全

✅ **不可变对象**:
- DivinationResult所有字段final
- yaoList返回不可变副本

✅ **并发安全**:
- DivinationFactory使用ConcurrentHashMap

---

## 八、验证清单

### ✅ 已验证项

- [x] DivinationConstants常量定义完整
- [x] DivinationRequest基类字段覆盖所有公共参数
- [x] ManualDivinationRequest包含YaoInput内部类
- [x] DivinationResult包含所有必要字段
- [x] DivinationMethod接口方法签名合理
- [x] DivinationFactory使用Spring自动注入
- [x] 所有类遵循包结构规范
- [x] 所有类使用正确的package声明

### ⏳ 待验证项（需实现后验证）

- [ ] GuaXiangIdentifier能正确识别64卦
- [ ] BianGuaCalculator能正确计算变卦
- [ ] ManualInputDivinationMethod能正确起卦
- [ ] 工厂能自动注册所有方法
- [ ] 完整的起卦→解卦流程能跑通

---

## 九、总结

### 完成情况

✅ **已完成**：
- 5个核心类
- 完整的基础架构
- 清晰的数据流设计
- 详细的测试用例
- 完整的文档

### 关键成果

1. **策略模式 + 工厂模式** - 起卦方法扩展性强
2. **Spring自动注册** - 零配置，开箱即用
3. **类型安全** - 抽象基类+子类保证编译期检查
4. **不可变对象** - 线程安全，适合并发环境
5. **与现有代码完美对接** - 充分利用GuaXiang、Yao等已有类

### 后续计划

1. 实现`GuaXiangIdentifier`（卦象识别器）
2. 实现`BianGuaCalculator`（变卦计算器）
3. 实现`ManualInputDivinationMethod`（手动输入法）
4. 创建测试Controller
5. 端到端测试

---

**任务完成者**: Cascade AI  
**文档更新时间**: 2025-10-29 12:15

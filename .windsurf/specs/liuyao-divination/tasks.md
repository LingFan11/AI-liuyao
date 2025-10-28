# 六爻智能解卦系统实施计划

## 项目概况

- **预计总工期**: 5周
- **开发模式**: 单任务会话，模块化开发
- **优先级**: P0-必须完成 > P1-重要功能 > P2-优化功能
- **测试策略**: 每完成一个模块进行单元测试和集成测试
- **开发规范**: 严格遵守包结构和命名规范，避免文件混乱

## 🔴 开发规范（必须严格遵守）

### 包结构规范（完整版）
```
com.lingfan.liuyao/
├── LiuyaoApplication.java        # 主应用类，只有这一个文件在根包
│
├── controller/                    # HTTP控制器层
│   ├── user/                     # 用户模块
│   │   └── UserController.java
│   ├── divination/               # 起卦模块
│   │   ├── ManualDivinationController.java
│   │   ├── AutoDivinationController.java
│   │   └── DivinationRecordController.java
│   ├── interpretation/           # 解卦模块
│   │   ├── InterpretationController.java
│   │   ├── AiInterpretationController.java
│   │   └── YaoAnalysisController.java
│   ├── history/                  # 历史记录模块
│   │   ├── HistoryController.java
│   │   ├── FavoriteController.java
│   │   └── StatisticsController.java
│   ├── knowledge/                # 知识库模块
│   │   ├── KnowledgeController.java
│   │   ├── CaseController.java
│   │   └── RagController.java
│   ├── admin/                    # 管理后台模块
│   │   ├── AdminController.java
│   │   ├── UserManagementController.java
│   │   ├── ContentManagementController.java
│   │   └── AiConfigController.java
│   ├── monitor/                  # 监控模块
│   │   └── HealthCheckController.java
│   └── test/                     # 测试控制器（开发阶段）
│       ├── RegisterTestController.java
│       └── ...其他测试控制器
│
├── service/                      # 服务接口层
│   ├── UserService.java
│   ├── DivinationService.java
│   ├── impl/                    # ⚠️ 所有实现类统一在这里
│   │   ├── UserServiceImpl.java
│   │   ├── DivinationServiceImpl.java
│   │   └── ...
│   ├── divination/              # 起卦相关子包（非Controller）
│   │   ├── DivinationMethod.java        # 接口
│   │   ├── DivinationFactory.java       # 工厂类
│   │   └── method/                      # 具体实现
│   │       ├── ManualDivinationMethod.java
│   │       ├── TimeDivinationMethod.java
│   │       └── ...
│   └── knowledge/               # 知识库相关子包
│       └── HexagramRepository.java
│
├── mapper/                       # MyBatis数据访问层
│   ├── UserMapper.java
│   ├── HexagramMapper.java
│   └── mongo/                   # MongoDB Repository
│       └── ConversationRecordRepository.java
│
├── model/                        # 数据模型层
│   ├── entity/                  # 数据库实体（对应表）
│   │   ├── User.java
│   │   ├── Hexagram.java
│   │   ├── DivinationHistory.java
│   │   └── ...
│   ├── dto/                     # 数据传输对象
│   │   ├── request/            # 请求DTO（前端→后端）
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── ...
│   │   ├── response/           # 响应DTO（后端→前端）
│   │   │   ├── LoginResponse.java
│   │   │   ├── UserProfileResponse.java
│   │   │   └── ...
│   │   └── DivinationContext.java  # 特殊DTO可以直接在dto下
│   └── vo/                      # 视图对象
│       └── UserVO.java
│
├── config/                       # 配置类
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   └── ...
│
├── utils/                        # 工具类
│   ├── JwtUtil.java
│   ├── RedisUtil.java
│   ├── cache/                   # 缓存相关
│   ├── security/                # 安全相关
│   ├── ai/                      # AI相关
│   └── async/                   # 异步相关
│
├── exception/                    # 异常类
│   ├── BusinessException.java
│   └── handler/
│       └── GlobalExceptionHandler.java
│
├── enums/                        # 枚举类
│   └── ErrorCode.java
│
├── constant/                     # 常量类（所有常量统一放这里）
│   ├── PromptConstants.java    # Prompt模板常量
│   ├── TermDictionary.java     # 术语字典常量
│   ├── CacheConstants.java     # 缓存Key常量
│   ├── ApiConstants.java       # API相关常量
│   └── BusinessConstants.java  # 业务常量
│
├── interceptor/                  # 拦截器
│   ├── AuthenticationInterceptor.java
│   └── JwtAuthenticationFilter.java
│
├── annotation/                   # 自定义注解
│   ├── RequiresLogin.java
│   ├── RequiresRoles.java
│   └── RequiresPermissions.java
│
└── aspect/                       # 切面类
    ├── OperationLogAspect.java
    └── SqlInjectionAspect.java
```

### 🔴 常量类组织规范（重要）

**原则：所有常量必须统一放在 `constant/` 包下，不能散落在各处！**

```
constant/
├── PromptConstants.java      # AI Prompt模板常量
├── TermDictionary.java       # 易经术语字典
├── CacheConstants.java       # Redis缓存Key常量
├── ApiConstants.java         # API路径、参数常量
├── BusinessConstants.java    # 业务常量（等级、VIP、次数限制等）
└── RegexConstants.java       # 正则表达式常量
```

**常量类示例**：

```java
// ✅ 正确：CacheConstants.java
package com.lingfan.liuyao.constant;

public class CacheConstants {
    // Redis Key前缀
    public static final String USER_PREFIX = "user:";
    public static final String HEXAGRAM_PREFIX = "hexagram:";
    
    // 缓存过期时间（秒）
    public static final int USER_CACHE_TTL = 1800;  // 30分钟
    public static final int HEXAGRAM_CACHE_TTL = 86400;  // 24小时
}

// ✅ 正确：BusinessConstants.java
package com.lingfan.liuyao.constant;

public class BusinessConstants {
    // 用户等级
    public static final int MAX_LEVEL = 99;
    public static final int EXP_PER_LEVEL = 100;
    
    // 占卜次数限制
    public static final int BASE_DIVINATION_TIMES = 3;
    public static final int VIP_MONTH_TIMES = 15;
    public static final int VIP_YEAR_TIMES = 30;
}

// ❌ 错误：在Service中定义常量
@Service
public class UserServiceImpl {
    private static final int MAX_LEVEL = 99;  // 错误！应该在constant包
}

// ❌ 错误：在Controller中定义常量
@RestController
public class UserController {
    private static final String API_PREFIX = "/api/user";  // 错误！
}
```

### 🚨 文件创建决策树（创建前必看）

```
准备创建文件时，问自己：

1️⃣ 这是什么类型的类？
   ├─ Controller? 
   │  └─ 属于哪个模块？
   │     ├─ 用户相关 → controller/user/
   │     ├─ 起卦相关 → controller/divination/
   │     ├─ 解卦相关 → controller/interpretation/
   │     ├─ 历史相关 → controller/history/
   │     ├─ 知识相关 → controller/knowledge/
   │     ├─ 管理相关 → controller/admin/
   │     └─ 测试接口 → controller/test/
   │
   ├─ Service接口? 
   │  └─ 直接放在 service/ 下
   │
   ├─ Service实现? 
   │  └─ ⚠️ 必须放在 service/impl/ 下
   │
   ├─ Entity? 
   │  └─ model/entity/
   │
   ├─ DTO? 
   │  ├─ 请求DTO → model/dto/request/
   │  └─ 响应DTO → model/dto/response/
   │
   ├─ Mapper? 
   │  ├─ MySQL → mapper/
   │  └─ MongoDB → mapper/mongo/
   │
   ├─ 常量? 
   │  └─ ⚠️ 必须放在 constant/ 下
   │
   ├─ 枚举? 
   │  └─ enums/
   │
   └─ 其他? 查看包结构图

2️⃣ 是否有类似功能的文件？
   └─ 先搜索现有文件，看它在哪个包

3️⃣ 文件命名是否符合规范？
   ├─ Controller: 模块名Controller.java
   ├─ Service: 模块名Service.java
   ├─ ServiceImpl: 模块名ServiceImpl.java
   ├─ Request DTO: 功能名Request.java
   ├─ Response DTO: 功能名Response.java
   ├─ 常量类: 功能名Constants.java
   └─ 枚举类: 功能名Enum.java
```

### ❌ 常见错误示例（绝对不能犯）

```java
// ❌ 错误1：在controller包下创建Service
controller/UserService.java  // 绝对错误！

// ✅ 正确
service/UserService.java

// ❌ 错误2：在service包下创建ServiceImpl
service/UserServiceImpl.java  // 绝对错误！

// ✅ 正确
service/impl/UserServiceImpl.java

// ❌ 错误3：在根包下创建业务类
com.lingfan.liuyao/UserController.java  // 绝对错误！

// ✅ 正确
com.lingfan.liuyao/controller/user/UserController.java

// ❌ 错误4：DTO不分request/response
model/dto/UserDTO.java  // 不够明确

// ✅ 正确
model/dto/request/UpdateUserRequest.java
model/dto/response/UserProfileResponse.java

// ❌ 错误5：测试Controller放在业务包
controller/user/UserTestController.java  // 错误！

// ✅ 正确
controller/test/UserTestController.java

// ❌ 错误6：常量散落在各处
service/impl/UserServiceImpl.java:
    private static final int MAX_LEVEL = 99;  // 错误！

controller/UserController.java:
    private static final String API_PREFIX = "/api";  // 错误！

// ✅ 正确：统一在constant包
constant/BusinessConstants.java:
    public static final int MAX_LEVEL = 99;
constant/ApiConstants.java:
    public static final String USER_API_PREFIX = "/api/user";
```

### ✅ 文件创建流程（强制执行）

```
第1步：确定文件类型
     ↓
第2步：查看"文件创建决策树"
     ↓
第3步：检查"常见错误示例"，确保不犯错
     ↓
第4步：在任务文档中找到对应任务，查看标注的"位置"
     ↓
第5步：创建文件
     ↓
第6步：立即在文件顶部添加package声明，确认包路径正确
```

### 📝 实战演练：正确创建UserController

**场景**：需要创建用户控制器

#### 步骤1：确定文件类型
```text
UserController → 这是Controller类
```

#### 步骤2：查看决策树
```text
Controller → 属于哪个模块？
→ 用户相关 → controller/user/
```

#### 步骤3：检查错误示例
```text
❌ controller/UserController.java       # 错误：直接在controller下
❌ controller/user/User.java           # 错误：没有Controller后缀
✅ controller/user/UserController.java  # 正确！
```

#### 步骤4：查看任务文档
```text
任务2.1 → 创建用户Controller `UserController.java`
         位置: `controller/user/UserController.java`
```

#### 步骤5：创建文件
```text
文件路径：src/main/java/com/lingfan/liuyao/controller/user/UserController.java
```

#### 步骤6：添加package声明
```java
package com.lingfan.liuyao.controller.user;  // ✅ 正确

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    // 业务代码
}
```

### 📝 实战演练：正确创建UserServiceImpl

**场景**：需要创建用户服务实现类

#### 完整步骤
```text
1. 文件类型：ServiceImpl
2. 决策树：Service实现 → service/impl/
3. 错误检查：
   ❌ service/UserServiceImpl.java          # 错误位置
   ✅ service/impl/UserServiceImpl.java     # 正确！
4. 任务文档：查看任务2.1标注的位置
5. 创建文件：src/main/java/com/lingfan/liuyao/service/impl/UserServiceImpl.java
6. Package声明：
```

```java
package com.lingfan.liuyao.service.impl;  // ✅ 正确

import com.lingfan.liuyao.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    // 业务逻辑
}
```

### 🔍 快速检查清单

创建完文件后，立即检查：

```text
□ package声明的包路径是否与文件实际路径一致？
□ 文件命名是否符合规范？
□ 是否在正确的模块子包下？
□ ServiceImpl是否在impl包下？
□ 测试Controller是否在test包下？
```

### ⚠️ 如果发现文件放错了

```text
立即操作：
1. 不要修改，先删除文件
2. 重新走一遍"文件创建流程"
3. 在正确位置重新创建
4. 绝对不要尝试移动文件（容易出错）
```

## 开发任务清单

### 阶段一：基础架构搭建（第1周）

#### 任务1.1：项目初始化和配置 [P0]
- [ ] 清理现有项目中的订单相关代码
- [ ] 更新pom.xml配置（项目名称、描述）
- [ ] 创建主应用类 `LiuyaoApplication.java`
  - **位置**: `src/main/java/com/lingfan/liuyao/`
- [ ] 配置application.properties（数据库、Redis、MongoDB连接）
  - **位置**: `src/main/resources/`
- [ ] 创建项目包结构（严格按照以下结构）
  - [ ] `com.lingfan.liuyao.controller` - 控制器包
  - [ ] `com.lingfan.liuyao.controller.user` - 用户模块控制器
  - [ ] `com.lingfan.liuyao.controller.divination` - 起卦模块控制器
  - [ ] `com.lingfan.liuyao.controller.interpretation` - 解卦模块控制器
  - [ ] `com.lingfan.liuyao.service` - 服务接口包
  - [ ] `com.lingfan.liuyao.service.impl` - 服务实现包
  - [ ] `com.lingfan.liuyao.mapper` - 数据访问包
  - [ ] `com.lingfan.liuyao.model.entity` - 实体类包
  - [ ] `com.lingfan.liuyao.model.dto.request` - 请求DTO包
  - [ ] `com.lingfan.liuyao.model.dto.response` - 响应DTO包
  - [ ] `com.lingfan.liuyao.model.vo` - 视图对象包
  - [ ] `com.lingfan.liuyao.config` - 配置类包
  - [ ] `com.lingfan.liuyao.utils` - 工具类包
  - [ ] `com.lingfan.liuyao.exception` - 异常类包
  - [ ] `com.lingfan.liuyao.enums` - 枚举类包
  - [ ] `com.lingfan.liuyao.constant` - 常量类包
  - [ ] `com.lingfan.liuyao.interceptor` - 拦截器包
  - [ ] `com.lingfan.liuyao.annotation` - 注解包
  - [ ] `com.lingfan.liuyao.aspect` - 切面包
- [ ] 每个包创建package-info.java说明文件
- [ ] 配置Knife4j API文档
- [ ] 创建健康检查控制器 `HealthController.java`
  - **位置**: `controller/HealthController.java`
- [ ] 测试基础框架运行

#### 任务1.2：数据库设计和初始化 [P0] ✅ **已完成并深度优化 2025-10-22**
- [x] 创建MySQL数据库 liuyao_db
- [x] 创建SQL脚本文件夹
  - **位置**: `src/main/resources/sql/`
  - **完成**: 10个SQL文件 + README.md
- [x] 创建用户表脚本 `01_create_table_users.sql`
  - 包含账号信息、个人资料、VIP信息、占卜统计
- [x] 创建卦象表脚本 `02_create_table_hexagrams.sql`
  - 包含卦象信息、起卦方式、时空信息
- [x] 创建解释表脚本 `03_create_table_interpretations.sql`
  - 包含基础解释、AI解释、吉凶判断
- [x] 创建历史记录表脚本 `04_create_table_divination_histories.sql`
  - 包含收藏、备注、验证信息
- [x] 创建知识库表脚本 `05_create_table_knowledge.sql`
  - 包含hexagram_knowledge、yao_knowledge、case_studies三张表
- [x] 插入六十四卦基础数据 `06_insert_hexagrams_data.sql`
  - 已插入前20卦示例数据
- [x] 创建测试数据脚本 `07_insert_test_data.sql`
  - 包含4个测试用户、5条卦象、5条解释、5条历史记录、3个案例
- [x] 创建一键执行脚本 `execute_all.sql`
- [x] 创建SQL文档 `README.md`
- [x] 创建任务文档 `task-1.2-database-design.md`
- [ ] 配置MyBatis-Plus代码生成器（推迟到需要时）
  - **位置**: `utils/MybatisPlusGenerator.java`
- [ ] 生成实体类（推迟到需要时）
  - **位置**: 必须在 `model/entity/` 包下
  - **命名**: `User.java`, `Hexagram.java`, `Interpretation.java`等
- [ ] 生成Mapper接口（推迟到需要时）
  - **位置**: 必须在 `mapper/` 包下
  - **命名**: `UserMapper.java`, `HexagramMapper.java`等

**📋 任务详情**: 见 `.windsurf/specs/liuyao-divination/task-1.2-database-design.md`

#### 任务1.3：通用工具类开发 [P0] ✅ **已完成 2025-10-22**
- [x] 创建统一响应类 `ApiResponse.java`
  - **位置**: `utils/ApiResponse.java`
- [x] 创建错误码枚举 `ErrorCode.java`
  - **位置**: `enums/ErrorCode.java`
- [x] 创建全局异常处理器 `GlobalExceptionHandler.java`
  - **位置**: `exception/handler/GlobalExceptionHandler.java`
- [x] 创建业务异常类 `BusinessException.java`
  - **位置**: `exception/BusinessException.java`
- [x] 创建JWT工具类 `JwtUtil.java`
  - **位置**: `utils/JwtUtil.java`
- [x] 创建Redis工具类 `RedisUtil.java`
  - **位置**: `utils/RedisUtil.java`
  - **功能**: 基础操作、防缓存穿透、击穿、雪崩
- [x] 创建Redis数据包装类 `RedisData.java`
  - **位置**: `model/dto/RedisData.java`
- [x] 创建加密工具类 `PasswordEncoder.java`
  - **位置**: `utils/PasswordEncoder.java`
  - **算法**: BCrypt
- [x] 创建测试控制器 `UtilTestController.java`
  - **位置**: `controller/test/UtilTestController.java`
- [x] 测试工具类功能

**📋 任务详情**: 见 `.windsurf/specs/liuyao-divination/task-1.3-utils-development.md`

#### 任务1.4：配置管理和安全基础 [P0] ✅ **已完成 2025-10-23**
- [x] 创建Spring Security配置类 `SecurityConfig.java`
  - **位置**: `config/SecurityConfig.java`
  - **功能**: 白名单配置、JWT过滤器集成、异常处理（401、403）
- [x] 创建JWT认证过滤器 `JwtAuthenticationFilter.java`
  - **位置**: `interceptor/JwtAuthenticationFilter.java`
  - **功能**: Token验证、Redis黑名单、用户在线状态、Token自动续期
- [x] 创建跨域配置类 `CorsConfig.java`
  - **位置**: `config/CorsConfig.java`
  - **功能**: 支持前端localhost:3000、5173、8080跨域访问
- [x] 创建Redis配置类 `RedisConfig.java`
  - **位置**: `config/RedisConfig.java`
  - **功能**: JSON序列化器、String序列化器、多级缓存管理器
- [x] 创建MongoDB配置类 `MongoConfig.java`
  - **位置**: `config/MongoConfig.java`
  - **功能**: LocalDateTime转换器、审计功能
- [x] 创建线程池配置类 `ThreadPoolConfig.java`
  - **位置**: `config/ThreadPoolConfig.java`
  - **功能**: 异步任务线程池（核心5、最大20、队列100）
- [x] 创建日志配置文件 `logback-spring.xml`
  - **位置**: `src/main/resources/logback-spring.xml`
  - **功能**: 控制台日志、文件日志、错误日志分离、滚动策略
- [x] 创建配置测试控制器 `ConfigTestController.java`
  - **位置**: `controller/test/ConfigTestController.java`
  - **接口**: JWT认证、生成Token、Redis、MongoDB、线程池、黑名单测试
- [x] 测试配置正确性
  - **工具**: PowerShell Invoke-WebRequest
  - **结果**: 10项测试全部通过 ✅

**📋 任务详情**: 见 `.windsurf/specs/liuyao-divination/task-1.4-config-security.md`

**🔑 关键技术**:
- JWT + Redis黑名单机制（解决Token无法主动失效问题）
- Token自动续期（剩余<30分钟自动刷新）
- 用户在线状态管理（30分钟TTL）
- Hutool工具类统一使用
- Jackson2JsonRedisSerializer序列化配置
- 多级缓存策略（用户30min、卦象24h）

### 阶段二：用户管理模块（第2周前半）

#### 任务2.1：用户注册功能 [P0] ✅ **已完成 2025-10-26**
- [x] 创建用户实体类 `User.java`
  - **位置**: `model/entity/User.java`
- [x] 创建用户注册请求DTO `RegisterRequest.java`
  - **位置**: `model/dto/request/RegisterRequest.java`
- [x] 创建用户VO `UserVO.java`
  - **位置**: `model/vo/UserVO.java`
- [x] 创建用户Service `UserRegisterService.java`
  - **位置**: `service/UserRegisterService.java`
- [x] 实现Service `UserRegisterServiceImpl.java`
  - **位置**: `service/impl/UserRegisterServiceImpl.java`
- [x] 创建用户Mapper `UserMapper.java`
  - **位置**: `mapper/UserMapper.java`
- [x] 创建用户Controller `UserController.java`
  - **位置**: `controller/user/UserController.java`
- [x] 实现用户名重复检查
- [x] 实现邮箱重复检查
- [x] 实现密码加密存储
- [x] 创建测试控制器 `RegisterTestController.java`
  - **位置**: `controller/test/RegisterTestController.java`
- [x] 测试注册功能

#### 任务2.2：用户登录功能 [P0] ✅ **已完成 2025-10-26**
- [x] 创建登录请求DTO `LoginRequest.java`
  - **位置**: `model/dto/request/LoginRequest.java`
- [x] 创建登录响应DTO `LoginResponse.java`
  - **位置**: `model/dto/response/LoginResponse.java`
- [x] 创建用户认证Service `UserAuthService.java`
  - **位置**: `service/UserAuthService.java`
- [x] 实现Service `UserAuthServiceImpl.java`
  - **位置**: `service/impl/UserAuthServiceImpl.java`
- [x] 在`UserController.java`添加登录接口
- [x] 实现用户名密码验证
- [x] 实现JWT Token生成
- [x] 实现登录失败次数限制
- [x] 实现账号锁定机制
- [x] Redis存储会话信息
- [x] 创建测试控制器 `LoginTestController.java`
  - **位置**: `controller/test/LoginTestController.java`
- [x] 测试登录功能

#### 任务2.3：用户信息管理 [P0] ✅ **已完成 2025-10-26**
- [x] 创建用户信息响应DTO `UserProfileResponse.java`
  - **位置**: `model/dto/response/UserProfileResponse.java`
- [x] 创建用户信息更新请求DTO `UpdateProfileRequest.java`
  - **位置**: `model/dto/request/UpdateProfileRequest.java`
- [x] 创建用户信息管理Service `UserProfileService.java`
  - **位置**: `service/UserProfileService.java`
- [x] 实现Service `UserProfileServiceImpl.java`
  - **位置**: `service/impl/UserProfileServiceImpl.java`
- [x] 在`UserController.java`添加接口
  - **位置**: `controller/user/UserController.java`
- [x] 实现获取用户信息接口
- [x] 实现更新用户信息接口
- [x] 实现头像上传功能
- [x] 实现用户等级计算
- [x] 实现VIP状态管理
- [x] 创建测试控制器 `ProfileTestController.java`
  - **位置**: `controller/test/ProfileTestController.java`
- [x] 测试用户信息功能

#### 任务2.4：认证拦截器 [P0] ✅ **已完成 2025-10-26**
- [x] 创建认证注解 `@RequiresLogin.java`
  - **位置**: `annotation/RequiresLogin.java`
- [x] 创建角色注解 `@RequiresRoles.java`
  - **位置**: `annotation/RequiresRoles.java`
- [x] 创建权限注解 `@RequiresPermissions.java`
  - **位置**: `annotation/RequiresPermissions.java`
- [x] 扩展用户上下文持有者 `UserContextHolder.java`
  - **位置**: `utils/UserContextHolder.java`
  - **功能**: 新增ThreadLocal支持，存储角色和权限
- [x] 创建认证拦截器 `AuthenticationInterceptor.java`
  - **位置**: `interceptor/AuthenticationInterceptor.java`
  - **功能**: 权限注解验证、Redis缓存、角色权限校验
- [x] 创建Web配置类 `WebMvcConfig.java`
  - **位置**: `config/WebMvcConfig.java`
- [x] 配置拦截器路径（拦截/api/**，排除白名单）
- [x] 实现权限校验（AND/OR逻辑）
- [x] 创建测试控制器 `AuthTestController.java`
  - **位置**: `controller/test/AuthTestController.java`
  - **接口**: 9个测试接口，覆盖所有场景
- [x] 新增UserMapper查询方法
  - `selectUserRoles(userId)` - 查询用户角色列表
  - `selectUserPermissions(userId)` - 查询用户权限列表
- [ ] 测试认证功能（待执行SQL并测试）

**📋 任务详情**: 见 `.windsurf/specs/liuyao-divination/task-2.4-authentication-interceptor.md`

**🔑 关键技术**:
- RBAC权限模型（用户-角色-权限多对多）
- ThreadLocal存储用户上下文
- Redis缓存用户信息（角色+权限）
- 自定义注解 + HandlerInterceptor
- AND/OR逻辑灵活控制权限

### 阶段三：起卦核心功能（第3周）🆕 **2025-10-28更新 - 基于30个核心功能+Linus式架构优化**

> **设计理念**：
> - 固有属性（Yao）与计算属性（YaoState）彻底分离
> - 64卦数据程序生成，而不是手写SQL
> - 知识库100%覆盖knowledge-liuyao01.md和liuyao02.md

#### 任务3.1：基础数据结构（Linus式优化）[P0]

**📋 设计原则**：
- 固有属性（Yao）与计算属性（YaoState）彻底分离
- 不可变对象设计，避免状态混乱
- 缓存计算结果，避免重复计算

**子任务3.1.1：核心枚举类（8个枚举）**

- [ ] **BaGua.java** - 八卦枚举
  - **位置**: `enums/BaGua.java`
  - **内容**: 乾、兑、离、震、巽、坎、艮、坤（8个）
  - **知识库**: knowledge-liuyao01.md (10-104行)

- [ ] **TianGan.java** - 天干枚举
  - **位置**: `enums/TianGan.java`
  - **内容**: 甲乙丙丁戊己庚辛壬癸（10个）

- [ ] **DiZhi.java** - 地支枚举
  - **位置**: `enums/DiZhi.java`
  - **内容**: 子丑寅卯辰巳午未申酉戌亥（12个）

- [ ] **WuXing.java** - 五行枚举
  - **位置**: `enums/WuXing.java`
  - **内容**: 金木水火土（5个）
  - **方法**: `getChangSheng()`, `getDiWang()`, `getMuKu()`, `getJueDi()`, `sheng()`, `ke()`
  - **知识库**: knowledge-liuyao08.md (49-223行)

- [ ] **LiuQin.java** - 六亲枚举
  - **位置**: `enums/LiuQin.java`
  - **内容**: 父母、兄弟、子孙、妻财、官鬼（5个）

- [ ] **LiuShen.java** - 六神枚举
  - **位置**: `enums/LiuShen.java`
  - **内容**: 青龙、朱雀、勾陈、螣蛇、白虎、玄武（6个）
  - **方法**: `getByRiGan(TianGan)` - 根据日干获取六神配置

- [ ] **WangShuai.java** - 旺衰枚举
  - **位置**: `enums/WangShuai.java`
  - **内容**: 旺、相、休、囚、死（5个，含力量权重）

- [ ] **ZhanBuLeiXing.java** - 占卜类型枚举
  - **位置**: `enums/ZhanBuLeiXing.java`
  - **内容**: 功名、财运、婚姻、疾病、出行等（15+个）
  - **知识库**: knowledge-liuyao02.md (348-397行)

**子任务3.1.2：固有属性实体类**

- [ ] **Yao.java** - 爻实体（不可变对象）
  - **位置**: `model/entity/Yao.java`
  - **属性**: `weiZhi`, `diZhi`, `liuQin`, `isDong`, `bianYao`
  - **特点**: 只有getter，无setter（不可变对象）

- [ ] **GuaXiang.java** - 卦象实体（不可变对象）
  - **位置**: `model/entity/GuaXiang.java`
  - **属性**: `id`, `guaName`, `suoShuGong`, `gongWuXing`, `shiYaoWei`, `yingYaoWei`, `yaoList`
  - **特点**: 只有getter，无setter

**子任务3.1.3：计算属性类**

- [ ] **YaoState.java** - 爻状态（计算属性）
  - **位置**: `model/dto/YaoState.java`
  - **属性**: `yao`, `wangShuai`, `xunKong`, `yuePo`, `anDong`, `dongSan`, `riHe`, `yueHe`, `riChong`, `yueChong`, `riSheng`, `yueSheng`, `ruMu`, `linJue`, `jinTuiType`
  - **特点**: 由`YaoStateCalculator`统一计算

- [ ] **DivinationContext.java** - 起卦上下文（完整版）
  - **位置**: `model/dto/DivinationContext.java`
  - **属性**: `riGan`, `riChen`, `yueJian`, `divinationTime`, `benGua`, `bianGua`, `zhanBuLeiXing`, `wenShi`, `gender`, `yaoStateCache`
  - **方法**: `getYaoState(int yaoWei)` - 获取爻状态（带缓存）

#### 任务3.2：64卦数据生成器（Linus式：程序生成，非手写）[P0]

**📋 核心思想**：用程序生成64卦数据，而不是手写64条INSERT语句

- [ ] **GongConfig.java** - 宫位配置类
  - **位置**: `model/dto/GongConfig.java`

- [ ] **NaJiaConfigurator.java** - 纳甲配置器
  - **位置**: `utils/liuyao/NaJiaConfigurator.java`
  - **方法**: `getNaJiaSequence(String gongName)` - 返回8宫纳甲序列
  - **知识库**: knowledge-liuyao02.md (10-178行)

- [ ] **LiuQinGenerator.java** - 六亲生成器
  - **位置**: `utils/liuyao/LiuQinGenerator.java`
  - **方法**: `generate(WuXing gongWuXing, DiZhi yaoZhi)` - 根据宫五行和爻地支生成六亲
  - **知识库**: knowledge-liuyao02.md (10-178行)

- [ ] **ShiYingLocator.java** - 世应定位器
  - **位置**: `utils/liuyao/ShiYingLocator.java`
  - **方法**: `getShiYaoWei(String guaLeiXing)`, `getYingYaoWei(int shiYaoWei)`
  - **知识库**: knowledge-liuyao02.md (229-253行)

- [ ] **GuaXiangDataGenerator.java** - 64卦数据生成器
  - **位置**: `utils/liuyao/GuaXiangDataGenerator.java`
  - **方法**: `main()` - 生成64卦数据并输出SQL
  - **功能**: 
    - 定义8宫配置
    - 遍历8宫，每宫生成8卦（本宫、一世~五世、游魂、归魂）
    - 自动配置六爻地支（纳甲法）
    - 自动配置六爻六亲
    - 自动配置世应位置
    - 验证数据完整性（assert检查）
    - 生成INSERT SQL语句
  - **知识库**: knowledge-liuyao01.md (321-986行)

- [ ] **执行生成器**
  - 运行`GuaXiangDataGenerator.main()`
  - 生成文件: `src/main/resources/sql/08_insert_64_hexagrams.sql`
  - 验证数据：64卦、每宫8卦
  - 执行SQL导入数据库

#### 任务3.3：起卦方法实现 [P0]

**子任务3.3.1：起卦基础架构**

- [ ] **DivinationMethod.java** - 起卦方法接口
  - **位置**: `service/divination/DivinationMethod.java`
  - **方法**: `cast(DivinationContext ctx)` - 返回`DivinationResult`

- [ ] **DivinationResult.java** - 起卦结果
  - **位置**: `model/dto/DivinationResult.java`
  - **属性**: `benGua`, `bianGua`, `yaoList`, `dongYaoCount`

- [ ] **DivinationFactory.java** - 起卦工厂
  - **位置**: `service/divination/DivinationFactory.java`
  - **方法**: `getMethod(String type)` - 根据类型返回起卦方法

**子任务3.3.2：手动输入卦象法（优先！）**

- [ ] **ManualInputDivinationMethod.java** - 手动输入卦象法
  - **位置**: `service/divination/method/ManualInputDivinationMethod.java`
  - **输入格式**: 
    ```json
    {
      "yaoList": [
        {"weiZhi": 1, "yinYang": "YANG", "isDong": false},  // 初爻：少阳
        {"weiZhi": 2, "yinYang": "YIN",  "isDong": false},  // 二爻：少阴
        {"weiZhi": 3, "yinYang": "YANG", "isDong": true},   // 三爻：老阳（动）
        {"weiZhi": 4, "yinYang": "YIN",  "isDong": true},   // 四爻：老阴（动）
        {"weiZhi": 5, "yinYang": "YANG", "isDong": false},  // 五爻：少阳
        {"weiZhi": 6, "yinYang": "YIN",  "isDong": false}   // 上爻：少阴
      ]
    }
    ```
  - **逻辑**: 
    1. 验证输入（必须6个爻，位置1-6，阴阳必填）
    2. 根据阴阳组合识别本卦（调用GuaXiangIdentifier）
    3. 根据动爻计算变卦（调用BianGuaCalculator）
    4. 返回DivinationResult
  - **使用场景**: 
    - 用户线下已起卦（摇硬币、蓍草等），仅需系统解卦
    - 用户从书籍或其他来源获得卦象
    - 用户想测试特定卦象

**子任务3.3.3：钱币起卦法（自动）**

- [ ] **CoinDivinationMethod.java** - 钱币起卦法
  - **位置**: `service/divination/method/CoinDivinationMethod.java`
  - **逻辑**: 
    - 6次投币（每次3枚硬币）
    - 3正=老阳（9，变爻），2正1反=少阴（8），2反1正=少阳（7），3反=老阴（6，变爻）
    - 生成本卦，根据变爻计算变卦
  - **知识库**: knowledge-liuyao01.md (107-218行)

**子任务3.3.4：变卦计算器（公共工具）**

- [ ] **BianGuaCalculator.java** - 变卦计算器
  - **位置**: `utils/liuyao/BianGuaCalculator.java`
  - **方法**: `calculate(GuaXiang benGua, List<Yao> yaoList)` - 根据动爻计算变卦
  - **知识库**: knowledge-liuyao02.md (256-343行)

#### 任务3.4：卦象识别器 [P0]

- [ ] **GuaXiangIdentifier.java** - 卦象识别器
  - **位置**: `utils/liuyao/GuaXiangIdentifier.java`
  - **方法**: `identify(List<YaoType> yaoList)` - 根据6个爻识别卦象
  - **逻辑**: 将6个爻转换为二进制（阳=1，阴=0），查询数据库匹配卦象

#### 任务3.5：起卦Service和Controller [P0]

- [ ] **DivinationService.java** + **DivinationServiceImpl.java**
  - **位置**: `service/` + `service/impl/`

- [ ] **DivinationController.java**
  - **位置**: `controller/divination/DivinationController.java`

- [ ] **DivinationTestController.java**
  - **位置**: `controller/test/DivinationTestController.java`

### 阶段四：解卦核心功能（第4周）🆕 **2025-10-28更新 - 规则链引擎+30个核心功能**

> **设计理念**：
> - 规则链引擎，零if嵌套，易扩展
> - 30个核心功能，知识库100%覆盖
> - 优先级驱动，P0核心规则优先执行

#### 任务4.1：基础工具类（5个核心工具）[P0]

**子任务4.1.1：地支关系工具**

- [ ] **DiZhiRelations.java** - 地支关系工具
  - **位置**: `utils/liuyao/DiZhiRelations.java`
  - **方法**: `getLiuHe()`, `getLiuChong()`, `getWuXing()`, `isLiuHe()`, `isLiuChong()`
  - **知识库**: knowledge-liuyao04.md (34-369行), knowledge-liuyao05.md (42-503行)

**子任务4.1.2：旬空查询工具**

- [ ] **XunKongUtil.java** - 旬空工具
  - **位置**: `utils/liuyao/XunKongUtil.java`
  - **方法**: `getXunKong()`, `isXunKong()`
  - **知识库**: knowledge-liuyao07.md (391-737行)

**子任务4.1.3：用神选取器**

- [ ] **YongShenSelector.java** - 用神选取器
  - **位置**: `utils/liuyao/YongShenSelector.java`
  - **方法**: `selectYongShen(String zhanBuLeiXing, String gender)` - 返回用神六亲
  - **知识库**: knowledge-liuyao02.md (346-401行)

**子任务4.1.4：旺衰判断器**

- [ ] **WangShuaiJudge.java** - 旺衰判断器
  - **位置**: `utils/liuyao/WangShuaiJudge.java`
  - **方法**: `judge(DiZhi yueJian, WuXing wuXing)` - 返回`WangShuai`枚举
  - **逻辑**: 四时旺相规则（春木旺、夏火旺、秋金旺、冬水旺）
  - **知识库**: knowledge-liuyao03.md (100-121行)

**子任务4.1.5：爻状态计算器（核心！）**

- [ ] **YaoStateCalculator.java** - 爻状态计算器
  - **位置**: `utils/liuyao/YaoStateCalculator.java`
  - **方法**: `calculate(Yao yao, DivinationContext ctx)` - 返回`YaoState`对象
  - **计算内容**: 旺衰、旬空、月破、暗动、动散、日月合冲、日月生克、入墓、临绝、进神退神

#### 任务4.2：解卦规则链引擎（Linus式核心）[P0]

**子任务4.2.1：规则接口定义**

- [ ] **JieGuaRule.java** - 解卦规则接口
  - **位置**: `service/interpretation/rule/JieGuaRule.java`
  - **方法**: `getName()`, `getPriority()`, `shouldApply()`, `analyze()`, `shouldBreak()`

**子任务4.2.2：核心规则实现（8个P0规则）**

- [ ] **WuGenRule.java** - 用神无根规则（优先级1）
  - **位置**: `service/interpretation/rule/WuGenRule.java`
  - **逻辑**: 用神月破+日克+休囚 = 无根，中断后续规则
  - **知识库**: knowledge-liuyao02.md (609-691行)

- [ ] **XunKongRule.java** - 旬空规则（优先级2）
  - **位置**: `service/interpretation/rule/XunKongRule.java`
  - **逻辑**: 判断是否真空（动爻空、旺相空、日冲空、日月生扶空 = 非真空）
  - **知识库**: knowledge-liuyao07.md (391-737行)

- [ ] **YuePoRule.java** - 月破规则（优先级3）
  - **位置**: `service/interpretation/rule/YuePoRule.java`
  - **逻辑**: 野鹤新论 - 月破爻发动仍有用
  - **知识库**: knowledge-liuyao09.md (月破章)

- [ ] **SanMuRule.java** - 三墓规则（优先级4）
  - **位置**: `service/interpretation/rule/SanMuRule.java`
  - **逻辑**: 日墓+动墓+化墓，旺相者入墓非真墓
  - **知识库**: knowledge-liuyao11.md (全章)

- [ ] **DongJingShengKeRule.java** - 动静生克规则（优先级5）
  - **位置**: `service/interpretation/rule/DongJingShengKeRule.java`
  - **逻辑**: 动克静有力，静克动无力
  - **知识库**: knowledge-liuyao03.md (10-57行)

- [ ] **GuaBianRule.java** - 卦变规则（优先级6）
  - **位置**: `service/interpretation/rule/GuaBianRule.java`
  - **逻辑**: 变生、变克、变墓、变绝判断
  - **知识库**: knowledge-liuyao06.md (32-441行)

- [ ] **SiShenRule.java** - 四神规则（优先级7）
  - **位置**: `service/interpretation/rule/SiShenRule.java`
  - **逻辑**: 元神、忌神、仇神分析
  - **知识库**: knowledge-liuyao02.md (427-526行)

- [ ] **YingQiRule.java** - 应期规则（优先级8）
  - **位置**: `service/interpretation/rule/YingQiRule.java`
  - **逻辑**: 12条应期规则
  - **知识库**: knowledge-liuyao08.md (399-641行)

**子任务4.2.3：解卦引擎**

- [ ] **JieGuaEngine.java** - 解卦引擎
  - **位置**: `service/interpretation/JieGuaEngine.java`
  - **方法**: `analyze(DivinationContext ctx)` - 返回`JieGuaResult`
  - **逻辑**: 1.选取用神 → 2.执行规则链（按优先级） → 3.综合判断

- [ ] **JieGuaResult.java** - 解卦结果
  - **位置**: `model/dto/JieGuaResult.java`
  - **属性**: `yongShen`, `judgements`, `finalResult`, `yingQi`, `siShen`

#### 任务4.3：高级功能实现（9个P1规则）[P1]

- [ ] **FeiFuShenFinder.java** - 飞伏神查找器
  - **知识库**: knowledge-liuyao09.md (飞伏神章)

- [ ] **JinTuiShenDetector.java** - 进神退神检测器
  - **知识库**: knowledge-liuyao10.md (全章)

- [ ] **KeChuFengShengAnalyzer.java** - 克处逢生分析器
  - **知识库**: knowledge-liuyao02.md (842-875行)

- [ ] **RiYuePeiHeAnalyzer.java** - 日月配合分析器
  - **知识库**: knowledge-liuyao03.md (191-219行)

- [ ] **WuQiongZeBianAnalyzer.java** - 物穷则变分析器
  - **知识库**: knowledge-liuyao03.md (253-279行)

- [ ] **LiuHeApplicationAnalyzer.java** - 六合应用分析器
  - **知识库**: knowledge-liuyao04.md (34-369行)

- [ ] **LiuChongApplicationAnalyzer.java** - 六冲应用分析器
  - **知识库**: knowledge-liuyao05.md (42-503行)

- [ ] **SanHeDetector.java** - 三合局检测器
  - **知识库**: knowledge-liuyao04.md (497-727行)

- [ ] **FanYinFuYinDetector.java** - 反吟伏吟检测器
  - **知识库**: knowledge-liuyao07.md (39-388行)

#### 任务4.4：解卦Service和Controller [P0]

- [ ] **InterpretationService.java** + **InterpretationServiceImpl.java**
  - **位置**: `service/` + `service/impl/`

- [ ] **InterpretationController.java**
  - **位置**: `controller/interpretation/InterpretationController.java`

- [ ] **InterpretationTestController.java**
  - **位置**: `controller/test/InterpretationTestController.java`

#### 任务4.5：AI模型集成 [P0]

- [ ] 在application.properties配置LangChain4J参数
  - **位置**: `src/main/resources/application.properties`
- [ ] 创建LangChain4J配置类 `LangChain4jConfig.java`
  - **位置**: `config/LangChain4jConfig.java`
- [ ] 创建通义千问配置类 `QwenConfig.java`
  - **位置**: `config/QwenConfig.java`
- [ ] 创建Prompt模板常量类 `PromptConstants.java`
  - **位置**: `constant/PromptConstants.java`
- [ ] 测试AI调用

#### 任务4.6：AI智能解卦（核心！）[P0]

**📋 设计理念**：
- **规则链引擎 + AI大模型** = 专业准确 + 通俗易懂
- 规则链引擎：执行六爻理论分析（用神、旺衰、动静生克等）
- AI大模型：将分析结果转化为自然语言解释

**子任务4.6.1：AI解卦数据流设计**

```
用户输入
  ↓
起卦（任务3.3）
  ↓
规则链引擎分析（任务4.2）
  ├─ 用神选取
  ├─ 旺衰判断
  ├─ 旬空、月破检查
  ├─ 动静生克分析
  ├─ 卦变分析
  └─ 应期推算
  ↓
构建AI Prompt
  ├─ 卦象信息（本卦、变卦、动爻）
  ├─ 规则分析结果（JSON格式）
  ├─ 用户问题（占卜类型、具体问题）
  └─ 六爻知识库（RAG检索相关理论）
  ↓
调用AI（LangChain4J + 通义千问）
  ↓
流式返回解释
  ├─ 卦象概述
  ├─ 用神分析
  ├─ 吉凶判断
  ├─ 应期推断
  └─ 建议指导
```

**子任务4.6.2：Prompt工程设计**

- [ ] **PromptBuilder.java** - Prompt构建器
  - **位置**: `utils/ai/PromptBuilder.java`
  - **方法**: `buildInterpretationPrompt(DivinationContext ctx, JieGuaResult result, String question)`
  - **Prompt结构**:
    ```
    【系统角色】你是一位精通《增删卜易》《野鹤老人占卜全书》的六爻大师
    
    【卦象信息】
    - 本卦：{guaName}（{shangGua}上{xiaGua}下）
    - 变卦：{bianGuaName}
    - 动爻：{dongYaoList}
    - 起卦时间：{divinationTime}
    - 月建：{yueJian}，日辰：{riChen}
    
    【规则分析结果】（由规则链引擎计算）
    - 用神：{yongShen}（{liuQin}），旺衰：{wangShuai}
    - 用神状态：{yongShenState}（旬空？月破？日克？）
    - 元神：{yuanShen}，状态：{yuanShenState}
    - 忌神：{jiShen}，状态：{jiShenState}
    - 动爻生克：{dongJingShengKe}
    - 卦变分析：{guaBian}
    - 应期推算：{yingQi}
    
    【用户问题】
    - 占卜类型：{zhanBuLeiXing}
    - 具体问题：{question}
    
    【任务】
    请根据以上卦象和分析结果，用通俗易懂的语言为用户解卦：
    1. 先概述卦象含义
    2. 分析用神旺衰及吉凶
    3. 结合动爻和卦变给出判断
    4. 推断应期（何时应验）
    5. 给出实用建议
    
    注意：
    - 严格基于规则分析结果，不要臆测
    - 语言通俗，避免过多术语
    - 如果规则分析显示"用神无根"或"凶象明显"，要如实告知
    ```

**子任务4.6.3：AI解卦Service实现**

- [ ] **AiInterpretationService.java** + **AiInterpretationServiceImpl.java**
  - **位置**: `service/` + `service/impl/`
  - **核心方法**:
    - `interpretWithAi(DivinationContext ctx, JieGuaResult result, String question)` - 同步解卦
    - `interpretWithAiStream(...)` - 流式解卦（SSE）
    - `continueConversation(String conversationId, String userMessage)` - 多轮对话

**子任务4.6.4：流式输出实现**

- [ ] **StreamingAiService.java** - 流式AI服务
  - **位置**: `service/ai/StreamingAiService.java`
  - **功能**: 
    - SSE推送（Server-Sent Events）
    - 客户端断开检测
    - 超时控制（30秒）
    - 错误处理和降级

**子任务4.6.5：对话记录管理**

- [ ] **ConversationRecord.java** - 对话记录实体
  - **位置**: `model/entity/ConversationRecord.java`
  - **属性**: `conversationId`, `userId`, `divinationId`, `messages`, `createTime`

- [ ] **ConversationRecordRepository.java** - MongoDB Repository
  - **位置**: `mapper/mongo/ConversationRecordRepository.java`
  - **功能**: 存储对话历史，支持多轮对话

**子任务4.6.6：AI解卦Controller**

- [ ] **AiInterpretationController.java** - AI解卦控制器
  - **位置**: `controller/interpretation/AiInterpretationController.java`
  - **接口**:
    - `POST /api/interpretation/ai/interpret` - 一次性解卦
    - `GET /api/interpretation/ai/stream` - 流式解卦（SSE）
    - `POST /api/interpretation/ai/continue` - 继续对话

**子任务4.6.7：降级方案**

- [ ] **FallbackInterpretationService.java** - 降级解卦服务
  - **位置**: `service/ai/FallbackInterpretationService.java`
  - **功能**: AI服务不可用时，返回规则链引擎的结构化分析结果
  - **场景**: 
    - AI服务超时
    - API配额用尽
    - 网络故障

**子任务4.6.8：RAG卦例检索（核心增强！）[P0]**

- [ ] **VectorStoreConfig.java** - 向量数据库配置
  - **位置**: `config/VectorStoreConfig.java`
  - **支持**: Pinecone / Milvus / Chroma（优先Pinecone）
  - **配置**: API Key、索引名称、维度（1536，OpenAI标准）

- [ ] **CaseVectorizer.java** - 卦例向量化工具
  - **位置**: `utils/ai/CaseVectorizer.java`
  - **方法**: 
    - `embedCase(CaseStudy case)` - 单个卦例向量化
    - `batchEmbed(List<CaseStudy> cases)` - 批量向量化
  - **向量化内容**: 卦名 + 占卜类型 + 问题描述 + 结果

- [ ] **CaseRetriever.java** - 卦例检索器
  - **位置**: `service/ai/CaseRetriever.java`
  - **方法**:
    - `retrieveSimilarCases(GuaXiang benGua, GuaXiang bianGua, String category, int topK)` - 相似度检索
    - `retrieveByKeywords(String keywords, int topK)` - 关键词检索
  - **检索策略**:
    - 根据卦象相似度（本卦、变卦）
    - 根据占卜类型（财运、功名、婚姻等）
    - 混合检索，加权排序
  - **返回**: Top 3 最相关卦例

- [ ] **导入经典卦例数据**
  - **来源**: 《增删卜易》《野鹤老人占卜全书》
  - **数量**: MVP阶段50个，后续扩充到100+
  - **分类**: 财运、功名、婚姻、疾病、出行等
  - **格式**: 
    ```json
    {
      "guaName": "火地晋",
      "bianGuaName": "火天大有",
      "category": "财运",
      "question": "占求财",
      "result": "月内求财成功，辰日应验",
      "source": "《增删卜易》",
      "content": "某日占财运，得火地晋之火天大有..."
    }
    ```

- [ ] **集成RAG到Prompt**
  - 在`PromptBuilder.java`中添加RAG结果注入
  - Prompt结构增加"【相关卦例】"部分
  - 示例：
    ```
    【相关卦例】（参考）
    案例1：《增删卜易》- 占财运得火地晋
      卦象：火地晋之火天大有
      结果：月内求财成功，辰日应验
      
    案例2：《野鹤老人占卜全书》- 占求财
      卦象：火地晋变爻
      结果：用神旺相，财运亨通
    ```

**子任务4.6.9：测试**

- [ ] **AiInterpretationTestController.java** - AI解卦测试控制器
  - **位置**: `controller/test/AiInterpretationTestController.java`
  - **测试用例**:
    - 测试不同占卜类型（功名、财运、婚姻等）
    - 测试不同卦象（吉卦、凶卦、中平）
    - 测试流式输出
    - 测试降级方案
    - **测试RAG检索质量** ← 🆕
    - **测试卦例相似度匹配** ← 🆕

### 阶段五：历史记录和知识库（第4周前半）

#### 任务5.1：历史记录管理 [P0]
- [ ] 已在任务3.4完成`DivinationHistory.java`实体
- [ ] 创建历史记录Service `HistoryService.java`
  - **位置**: `service/HistoryService.java`
- [ ] 实现Service `HistoryServiceImpl.java`
  - **位置**: `service/impl/HistoryServiceImpl.java`
- [ ] 创建历史记录Controller `HistoryController.java`
  - **位置**: `controller/history/HistoryController.java`
- [ ] 实现历史记录保存
- [ ] 实现历史列表查询（分页）
- [ ] 实现搜索功能（关键词）
- [ ] 实现详情查询
- [ ] 实现删除功能
- [ ] 创建测试控制器 `HistoryTestController.java`
  - **位置**: `controller/test/HistoryTestController.java`
- [ ] 测试历史记录

#### 任务5.2：收藏功能 [P1]
- [ ] 创建收藏请求DTO `FavoriteRequest.java`
  - **位置**: `model/dto/request/FavoriteRequest.java`
- [ ] 创建收藏响应DTO `FavoriteResponse.java`
  - **位置**: `model/dto/response/FavoriteResponse.java`
- [ ] 创建收藏Service `FavoriteService.java`
  - **位置**: `service/FavoriteService.java`
- [ ] 实现Service `FavoriteServiceImpl.java`
  - **位置**: `service/impl/FavoriteServiceImpl.java`
- [ ] 创建收藏Controller `FavoriteController.java`
  - **位置**: `controller/history/FavoriteController.java`
- [ ] 实现收藏/取消收藏
- [ ] 实现收藏列表查询
- [ ] 实现收藏数量限制
- [ ] 实现收藏分类
- [ ] 创建测试控制器 `FavoriteTestController.java`
  - **位置**: `controller/test/FavoriteTestController.java`
- [ ] 测试收藏功能

#### 任务5.3：统计分析功能 [P1]
- [ ] 创建统计查询请求DTO `StatisticsQueryRequest.java`
  - **位置**: `model/dto/request/StatisticsQueryRequest.java`
- [ ] 创建统计响应DTO `StatisticsResponse.java`
  - **位置**: `model/dto/response/StatisticsResponse.java`
- [ ] 创建统计Service `StatisticsService.java`
  - **位置**: `service/StatisticsService.java`
- [ ] 实现Service `StatisticsServiceImpl.java`
  - **位置**: `service/impl/StatisticsServiceImpl.java`
- [ ] 创建统计辅助类 `StatisticsHelper.java`
  - **位置**: `utils/StatisticsHelper.java`
- [ ] 创建统计Controller `StatisticsController.java`
  - **位置**: `controller/history/StatisticsController.java`
- [ ] 实现占卜次数统计
- [ ] 实现分类统计
- [ ] 实现卦象频率统计
- [ ] 实现时间维度统计
- [ ] 实现统计图表数据接口
- [ ] 缓存统计结果
- [ ] 创建测试控制器 `StatisticsTestController.java`
  - **位置**: `controller/test/StatisticsTestController.java`
- [ ] 测试统计功能

#### 任务5.4：知识库功能 [P1]
- [ ] 创建知识库查询请求DTO `KnowledgeQueryRequest.java`
  - **位置**: `model/dto/request/KnowledgeQueryRequest.java`
- [ ] 创建知识库响应DTO `KnowledgeResponse.java`
  - **位置**: `model/dto/response/KnowledgeResponse.java`
- [ ] 创建知识库Service `KnowledgeService.java`
  - **位置**: `service/KnowledgeService.java`
- [ ] 实现Service `KnowledgeServiceImpl.java`
  - **位置**: `service/impl/KnowledgeServiceImpl.java`
- [ ] 创建术语字典 `TermDictionary.java`
  - **位置**: `constant/TermDictionary.java`
- [ ] 创建知识库Controller `KnowledgeController.java`
  - **位置**: `controller/knowledge/KnowledgeController.java`
- [ ] 导入六十四卦详细资料
- [ ] 实现卦象列表接口
- [ ] 实现卦象详情接口
- [ ] 实现爻辞查询接口
- [ ] 实现术语解释功能
- [ ] 实现全文搜索
- [ ] 创建测试控制器 `KnowledgeTestController.java`
  - **位置**: `controller/test/KnowledgeTestController.java`
- [ ] 测试知识库功能

#### 任务5.5：案例管理（RAG数据源）[P0] 🔥

> **重要性提升**：案例数据是RAG检索的核心数据源，必须优先实现！

**子任务5.5.1：案例数据模型**

- [ ] **CaseStudy.java** - 案例实体类
  - **位置**: `model/entity/CaseStudy.java`
  - **属性**: 
    - `id`, `guaName`, `bianGuaName`, `shangGua`, `xiaGua`
    - `category`（占卜类型：财运、功名、婚姻等）
    - `question`（问题描述）
    - `result`（结果判断）
    - `source`（来源：《增删卜易》等）
    - `content`（完整案例内容）
    - `dongYaoList`（动爻列表）
    - `tags`（标签：吉、凶、应期快等）
    - `embedding`（向量，BLOB类型，可选）

- [ ] **CaseStudyMapper.java** - 案例Mapper
  - **位置**: `mapper/CaseStudyMapper.java`
  - **方法**: CRUD + 分类查询 + 标签查询

**子任务5.5.2：案例Service和Controller**

- [ ] **CaseService.java** + **CaseServiceImpl.java**
  - **位置**: `service/` + `service/impl/`
  - **方法**:
    - `importCases(List<CaseStudy> cases)` - 批量导入
    - `queryCasesByCategory(String category)` - 分类查询
    - `queryCasesByGua(String guaName)` - 卦象查询
    - `getCaseDetail(Long id)` - 详情查询

- [ ] **CaseController.java** - 案例控制器
  - **位置**: `controller/knowledge/CaseController.java`
  - **接口**:
    - `GET /api/cases` - 案例列表（分页）
    - `GET /api/cases/{id}` - 案例详情
    - `GET /api/cases/category/{category}` - 分类查询
    - `GET /api/cases/gua/{guaName}` - 按卦象查询

**子任务5.5.3：经典案例数据导入**

- [ ] **准备案例数据**
  - **来源**: 
    - 《增删卜易》精选案例（30个）
    - 《野鹤老人占卜全书》精选案例（20个）
    - 现代案例整理（可选）
  - **格式**: Excel/JSON，便于批量导入
  - **分类覆盖**: 财运、功名、婚姻、疾病、出行、诉讼等

- [ ] **案例向量化**
  - 调用`CaseVectorizer.batchEmbed()`批量向量化
  - 存储到向量数据库（Pinecone）
  - 同时存储到MySQL（原始数据）

- [ ] **验证数据质量**
  - 检查案例完整性（必填字段）
  - 检查分类覆盖度
  - 测试检索效果

**子任务5.5.4：测试**

- [ ] **CaseTestController.java** - 案例测试控制器
  - **位置**: `controller/test/CaseTestController.java`
  - **测试**:
    - 测试案例导入
    - 测试分类查询
    - 测试卦象查询
    - 测试向量化质量

### 阶段六：高级功能和优化（第4周后半）

#### 任务6.1：RAG优化和扩展 [P1]

> **说明**：核心RAG功能已在任务4.6.8实现，本任务专注优化和扩展

**子任务6.1.1：扩充卦例库**

- [ ] **扩充案例数量到500+**
  - 《增删卜易》深度挖掘（100+个案例）
  - 《野鹤老人占卜全书》深度挖掘（100+个案例）
  - 其他经典著作（《卜筮正宗》《易隐》等）
  - 现代优质案例整理（有验证结果的）

- [ ] **完善案例分类**
  - 细化占卜类型（15+个类别）
  - 添加应期标签（快应、慢应、难应）
  - 添加难度标签（简单、中等、复杂）

**子任务6.1.2：优化检索算法**

- [ ] **混合检索策略**
  - 语义相似度检索（向量检索）
  - 关键词检索（全文检索）
  - 结构化筛选（卦象、类型）
  - 加权融合排序

- [ ] **ReRanker优化**
  - 引入ReRanker模型（Cohere/BGE）
  - 二次排序，提升Top 3精准度

- [ ] **检索参数调优**
  - 调优相似度阈值
  - 调优Top K数量
  - A/B测试不同策略

**子任务6.1.3：知识库向量化**

- [ ] **六爻理论知识向量化**
  - 提取knowledge-liuyao01~12.md核心理论
  - 分段向量化（按章节）
  - 支持理论知识检索

- [ ] **术语解释向量化**
  - 六爻专业术语库
  - 支持术语相似查询

**子任务6.1.4：RAG效果评估**

- [ ] **评估指标**
  - 检索准确率（Precision@3）
  - 检索召回率（Recall@10）
  - 用户满意度（人工标注）

- [ ] **持续优化**
  - 收集用户反馈
  - 标注优质案例对
  - 微调检索策略

**子任务6.1.5：测试**

- [ ] **RagTestController.java** - RAG测试控制器
  - **位置**: `controller/test/RagTestController.java`
  - **测试**:
    - 测试混合检索
    - 测试ReRanker效果
    - 测试不同相似度阈值
    - 测试知识库检索

#### 任务6.2：性能优化 [P1]
- [ ] 创建缓存配置类 `CacheConfig.java`
  - **位置**: `config/CacheConfig.java`
- [ ] 创建多级缓存管理器 `MultiLevelCacheManager.java`
  - **位置**: `utils/cache/MultiLevelCacheManager.java`
- [ ] 创建数据库优化配置 `DatabaseOptimizationConfig.java`
  - **位置**: `config/DatabaseOptimizationConfig.java`
- [ ] 创建批量查询工具 `BatchQueryUtil.java`
  - **位置**: `utils/BatchQueryUtil.java`
- [ ] 创建数据库索引脚本 `create_indexes.sql`
  - **位置**: `src/main/resources/sql/create_indexes.sql`
- [ ] 创建AI批处理器 `AiBatchProcessor.java`
  - **位置**: `utils/ai/AiBatchProcessor.java`
- [ ] 创建异步任务队列 `AsyncTaskQueue.java`
  - **位置**: `utils/async/AsyncTaskQueue.java`
- [ ] 创建性能测试控制器 `PerformanceTestController.java`
  - **位置**: `controller/test/PerformanceTestController.java`
- [ ] 实现多级缓存策略
- [ ] 优化数据库查询
- [ ] 添加数据库索引
- [ ] 优化AI调用批处理
- [ ] 压力测试

#### 任务6.3：安全加固 [P0]
- [ ] 创建安全配置类 `SecurityEnhanceConfig.java`
  - **位置**: `config/SecurityEnhanceConfig.java`
- [ ] 创建SQL注入防护切面 `SqlInjectionAspect.java`
  - **位置**: `aspect/SqlInjectionAspect.java`
- [ ] 创建XSS过滤器 `XssFilter.java`
  - **位置**: `interceptor/XssFilter.java`
- [ ] 创建CSRF防护拦截器 `CsrfInterceptor.java`
  - **位置**: `interceptor/CsrfInterceptor.java`
- [ ] 创建Prompt注入防护 `PromptGuard.java`
  - **位置**: `utils/security/PromptGuard.java`
- [ ] 创建数据脱敏工具 `DataMaskUtil.java`
  - **位置**: `utils/security/DataMaskUtil.java`
- [ ] 创建安全审计日志 `SecurityAuditLog.java`
  - **位置**: `model/entity/SecurityAuditLog.java`
- [ ] 实现SQL注入防护
- [ ] 实现XSS防护
- [ ] 实现CSRF防护
- [ ] 实现Prompt注入防护
- [ ] 实现敏感数据脱敏
- [ ] 实现操作日志记录
- [ ] 安全测试

#### 任务6.4：监控和日志 [P1]
- [ ] 已在任务1.4完成logback配置
- [ ] 创建操作日志实体 `OperationLog.java`
  - **位置**: `model/entity/OperationLog.java`
- [ ] 创建操作日志切面 `OperationLogAspect.java`
  - **位置**: `aspect/OperationLogAspect.java`
- [ ] 创建监控配置类 `MonitorConfig.java`
  - **位置**: `config/MonitorConfig.java`
- [ ] 创建健康检查Controller `HealthCheckController.java`
  - **位置**: `controller/monitor/HealthCheckController.java`
- [ ] 创建告警服务 `AlertService.java`
  - **位置**: `service/AlertService.java`
- [ ] 实现操作日志记录
- [ ] 实现异常日志收集
- [ ] 配置性能监控（Micrometer）
- [ ] 实现健康检查接口
- [ ] 配置告警机制
- [ ] 测试监控功能

### 阶段七：管理后台（第5周前半）

#### 任务7.1：管理员功能 [P1]
- [ ] 创建管理员实体 `Admin.java`
  - **位置**: `model/entity/Admin.java`
- [ ] 创建管理员角色实体 `AdminRole.java`
  - **位置**: `model/entity/AdminRole.java`
- [ ] 创建管理员权限实体 `AdminPermission.java`
  - **位置**: `model/entity/AdminPermission.java`
- [ ] 创建管理员登录DTO `AdminLoginRequest.java`
  - **位置**: `model/dto/request/AdminLoginRequest.java`
- [ ] 创建管理员Service `AdminService.java`
  - **位置**: `service/AdminService.java`
- [ ] 实现Service `AdminServiceImpl.java`
  - **位置**: `service/impl/AdminServiceImpl.java`
- [ ] 创建管理员Controller `AdminController.java`
  - **位置**: `controller/admin/AdminController.java`
- [ ] 实现管理员登录
- [ ] 实现权限管理
- [ ] 实现操作日志
- [ ] 创建测试控制器 `AdminTestController.java`
  - **位置**: `controller/test/AdminTestController.java`
- [ ] 测试管理功能

#### 任务7.2：用户管理后台 [P1]
- [ ] 创建用户管理Service `UserManagementService.java`
  - **位置**: `service/UserManagementService.java`
- [ ] 实现Service `UserManagementServiceImpl.java`
  - **位置**: `service/impl/UserManagementServiceImpl.java`
- [ ] 创建用户管理Controller `UserManagementController.java`
  - **位置**: `controller/admin/UserManagementController.java`
- [ ] 实现用户列表查询
- [ ] 实现用户搜索功能
- [ ] 实现用户详情查看
- [ ] 实现用户禁用/启用
- [ ] 实现VIP管理
- [ ] 测试用户管理

#### 任务7.3：内容管理 [P1]
- [ ] 创建内容管理请求DTO `ContentManagementRequest.java`
  - **位置**: `model/dto/request/ContentManagementRequest.java`
- [ ] 创建公告实体 `Announcement.java`
  - **位置**: `model/entity/Announcement.java`
- [ ] 创建内容管理Service `ContentManagementService.java`
  - **位置**: `service/ContentManagementService.java`
- [ ] 实现Service `ContentManagementServiceImpl.java`
  - **位置**: `service/impl/ContentManagementServiceImpl.java`
- [ ] 创建内容管理Controller `ContentManagementController.java`
  - **位置**: `controller/admin/ContentManagementController.java`
- [ ] 实现知识库编辑
- [ ] 实现案例管理
- [ ] 实现公告发布
- [ ] 创建测试控制器 `ContentManagementTestController.java`
  - **位置**: `controller/test/ContentManagementTestController.java`
- [ ] 测试内容管理

#### 任务7.4：AI配置管理 [P2]
- [ ] 创建AI配置实体 `AiModelConfig.java`
  - **位置**: `model/entity/AiModelConfig.java`
- [ ] 创建Prompt模板实体 `PromptTemplate.java`
  - **位置**: `model/entity/PromptTemplate.java`
- [ ] 创建AI配置请求DTO `AiConfigRequest.java`
  - **位置**: `model/dto/request/AiConfigRequest.java`
- [ ] 创建AI配置Service `AiConfigService.java`
  - **位置**: `service/AiConfigService.java`
- [ ] 实现Service `AiConfigServiceImpl.java`
  - **位置**: `service/impl/AiConfigServiceImpl.java`
- [ ] 创建AI配置Controller `AiConfigController.java`
  - **位置**: `controller/admin/AiConfigController.java`
- [ ] 实现模型参数配置
- [ ] 实现Prompt模板管理
- [ ] 实现测试功能
- [ ] 创建测试控制器 `AiConfigTestController.java`
  - **位置**: `controller/test/AiConfigTestController.java`
- [ ] 测试配置管理

### 阶段八：测试和部署（第5周后半）

#### 任务8.1：集成测试 [P0]
- [ ] 编写集成测试用例
- [ ] 执行API测试
- [ ] 执行业务流程测试
- [ ] 修复发现的问题

#### 任务8.2：性能测试 [P0]
- [ ] JMeter压力测试
- [ ] 数据库性能测试
- [ ] Redis性能测试
- [ ] 优化性能瓶颈

#### 任务8.3：部署准备 [P0]
- [ ] 编写部署文档
- [ ] 准备Docker镜像
- [ ] 配置Nginx
- [ ] 配置域名和SSL
- [ ] 数据库备份策略
- [ ] 准备监控方案

#### 任务8.4：上线和验收 [P0]
- [ ] 生产环境部署
- [ ] 功能验收测试
- [ ] 性能验收测试
- [ ] 安全验收测试
- [ ] 编写用户手册

## 里程碑

### M1：基础框架完成（第1周末）✅ **已完成**
- ✅ 项目框架搭建完成
- ✅ 数据库设计完成
- ✅ 基础工具类完成

### M2：用户系统完成（第2周中）🟡 **进行中**
- ✅ 用户注册登录完成
- 🟡 认证授权完成（JWT过滤器已完成，注解待创建）
- ✅ 用户管理完成

### M3：起卦功能完成（第2周末）
- 所有起卦方式实现
- 卦象记录完成
- 占卜限制实现

### M4：AI解卦完成（第3周末）
- 基础解释完成
- AI智能解析完成
- 流式输出完成

### M5：完整功能交付（第4周末）
- 历史记录完成
- 知识库完成
- 性能优化完成

### M6：生产就绪（第5周末）
- 管理后台完成
- 全面测试完成
- 部署上线完成

## 风险和依赖

### 技术风险
1. **AI服务稳定性**：需要准备降级方案
2. **性能瓶颈**：需要提前进行压力测试
3. **数据安全**：需要严格的安全审计

### 依赖项
1. **阿里云通义千问API**：需要申请和配置
2. **天气API**：需要第三方服务
3. **六十四卦数据**：需要准备完整数据

### 缓解措施
1. 准备AI服务降级方案（使用预设回复）
2. 实施多级缓存减少数据库压力
3. 定期安全审计和渗透测试

## 🔴 文件创建流程（强制遵守）

### 创建文件前必须做的3件事
1. **确认包位置**：对照包结构规范，确定文件应该在哪个包
2. **检查文件命名**：确保文件名符合命名规范
3. **查看任务文档**：查看任务文档中标注的**位置**说明

### 文件创建检查清单
```
□ 这个文件是什么类型？(Controller/Service/Entity/DTO等)
□ 应该放在哪个包？(严格对照包结构)
□ 文件命名是否正确？
□ 是否有依赖的文件需要先创建？
□ 创建后是否需要在其他文件引用？
```

### 严禁的错误行为
❌ **绝对不能**在错误的包下创建文件
❌ **绝对不能**创建重复功能的类
❌ **绝对不能**随意修改包结构
❌ **绝对不能**删除已经创建的文件（除非确认无依赖）
❌ **绝对不能**跳过步骤，必须按照开发顺序

### 正确的开发顺序
```
Entity → Mapper → DTO → Service接口 → ServiceImpl → Controller → 测试
```

## 开发规范

### 代码规范
1. 遵循阿里巴巴Java开发规范
2. 所有API必须有Swagger文档
3. 关键代码必须有单元测试
4. 代码覆盖率不低于70%
5. **每个类文件必须在正确的包下**
6. **Service实现类必须在service/impl包下**
7. **Controller必须在controller的子包下（按模块分）**

### 命名规范
1. **Controller**: `模块名Controller.java` (如 `UserController.java`)
2. **Service接口**: `模块名Service.java` (如 `UserService.java`)
3. **Service实现**: `模块名ServiceImpl.java` (如 `UserServiceImpl.java`)
4. **Entity**: `表名驼峰.java` (如 `User.java`, `DivinationHistory.java`)
5. **Request DTO**: `功能Request.java` (如 `LoginRequest.java`)
6. **Response DTO**: `功能Response.java` (如 `LoginResponse.java`)
7. **Mapper**: `实体名Mapper.java` (如 `UserMapper.java`)

### Git规范
1. 分支命名：feature/模块名称
2. 提交信息：[模块] 功能描述
3. 每完成一个子任务提交一次

### 测试规范
1. 单元测试：Service层覆盖率>80%
2. 集成测试：Controller层全覆盖
3. 性能测试：关键接口响应<1秒

## 资源需求

### 开发环境
- JDK 17
- Maven 3.8+
- MySQL 8.0
- Redis 6.0+
- MongoDB 5.0+
- IDEA 2023+

### 生产环境
- 服务器：2核4G起步
- 带宽：5Mbps起步
- 存储：100GB SSD
- 备份：每日自动备份

## 交付物清单

### 代码交付
1. 完整源代码
2. 单元测试代码
3. 集成测试代码

### 文档交付
1. API接口文档
2. 数据库设计文档
3. 部署文档
4. 用户手册
5. 运维手册

### 其他交付
1. Docker镜像
2. 部署脚本
3. 监控配置
4. 测试报告

---

## 📌 更新日志

### 🆕 2025-10-28 重大更新（19:15）- RAG集成到AI解卦

**更新范围**：任务4.6、任务5.5、任务6.1

**核心变更**：
1. **任务4.6：AI智能解卦增强**
   - 新增子任务4.6.8：RAG卦例检索 [P0]
   - 集成向量数据库（Pinecone）
   - 实现卦例相似度检索
   - 将检索结果注入Prompt，提升AI解卦质量

2. **任务5.5：案例管理优先级提升**
   - 优先级：~~[P2]~~ → **[P0]** 🔥
   - 原因：案例数据是RAG的核心数据源
   - MVP阶段：导入50个经典案例
   - 后续扩展：100+ → 500+

3. **任务6.1：RAG实现调整**
   - 原：向量化和RAG实现（从零开始）
   - 现：RAG优化和扩展（核心已在4.6实现）
   - 专注：扩充案例库、优化检索算法、效果评估

**设计理念**：
- **规则链引擎 + RAG + AI大模型** = 理论准确 + 案例参考 + 通俗易懂
- RAG提供经典案例参考，让AI解卦更专业、更可信

**数据流**：
```
起卦 → 规则链分析 → RAG检索卦例 → 构建Prompt → AI解释
```

---

### 🆕 2025-10-28 重大更新 - 阶段三、四全面优化

**更新范围**：阶段三（起卦核心功能）+ 阶段四（解卦核心功能）

**核心优化**：
1. **Linus式架构优化**
   - 数据结构分离：固有属性（Yao）vs 计算属性（YaoState）
   - 规则链引擎：零if嵌套，易扩展
   - 程序生成数据：64卦数据自动生成，避免手写错误

2. **知识库100%覆盖**
   - 30个核心功能全部实现
   - 每个功能都有明确的知识库引用（文件名+行号）
   - 去除6个"野鹤说不用亦可"的功能

3. **任务清单优化**
   - 阶段三：5个主任务，15+个类
   - 阶段四：7个主任务，45+个类（含RAG）
   - 详细拆分到子任务，易于执行

**设计理念**：
- 基于30个核心功能设计
- Linus式代码优化（数据结构第一、消除特殊情况、实用主义）
- 知识库驱动（每个功能明确引用knowledge-liuyao01~12.md）

**详细设计文档**：
- `.windsurf/specs/liuyao-divination/tasks-phase3-4-updated.md`（完整版）
- `.windsurf/specs/liuyao-divination/design-knowledge-mapping.md`（设计映射）

**下一步**：
- 从任务3.1（基础数据结构）开始执行
- 单任务会话原则，逐个击破
- 每个子任务完成后立即测试

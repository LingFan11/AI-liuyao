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

#### 任务1.4：配置管理和安全基础 [P0]
- [ ] 创建Spring Security配置类 `SecurityConfig.java`
  - **位置**: `config/SecurityConfig.java`
- [ ] 创建JWT认证过滤器 `JwtAuthenticationFilter.java`
  - **位置**: `interceptor/JwtAuthenticationFilter.java`
- [ ] 创建跨域配置类 `CorsConfig.java`
  - **位置**: `config/CorsConfig.java`
- [ ] 创建Redis配置类 `RedisConfig.java`
  - **位置**: `config/RedisConfig.java`
- [ ] 创建MongoDB配置类 `MongoConfig.java`
  - **位置**: `config/MongoConfig.java`
- [ ] 创建线程池配置类 `ThreadPoolConfig.java`
  - **位置**: `config/ThreadPoolConfig.java`
- [ ] 创建日志配置文件 `logback-spring.xml`
  - **位置**: `src/main/resources/logback-spring.xml`
- [ ] 创建配置测试控制器 `ConfigTestController.java`
  - **位置**: `controller/test/ConfigTestController.java`
- [ ] 测试配置正确性

### 阶段二：用户管理模块（第2周前半）

#### 任务2.1：用户注册功能 [P0]
- [ ] 创建用户实体类 `User.java`
  - **位置**: `model/entity/User.java`
- [ ] 创建用户注册请求DTO `RegisterRequest.java`
  - **位置**: `model/dto/request/RegisterRequest.java`
- [ ] 创建用户VO `UserVO.java`
  - **位置**: `model/vo/UserVO.java`
- [ ] 创建用户Service `UserService.java`
  - **位置**: `service/UserService.java`
- [ ] 实现Service `UserServiceImpl.java`
  - **位置**: `service/impl/UserServiceImpl.java`
- [ ] 创建用户Controller `UserController.java`
  - **位置**: `controller/user/UserController.java`
- [ ] 实现用户名重复检查
- [ ] 实现邮箱重复检查
- [ ] 实现密码加密存储
- [ ] 创建测试控制器 `RegisterTestController.java`
  - **位置**: `controller/test/RegisterTestController.java`
- [ ] 测试注册功能

#### 任务2.2：用户登录功能 [P0]
- [ ] 创建登录请求DTO `LoginRequest.java`
  - **位置**: `model/dto/request/LoginRequest.java`
- [ ] 创建登录响应DTO `LoginResponse.java`
  - **位置**: `model/dto/response/LoginResponse.java`
- [ ] 在`UserService.java`添加登录方法
- [ ] 在`UserServiceImpl.java`实现登录逻辑
- [ ] 在`UserController.java`添加登录接口
- [ ] 实现用户名密码验证
- [ ] 实现JWT Token生成
- [ ] 实现登录失败次数限制
- [ ] 实现账号锁定机制
- [ ] Redis存储会话信息
- [ ] 创建测试控制器 `LoginTestController.java`
  - **位置**: `controller/test/LoginTestController.java`
- [ ] 测试登录功能

#### 任务2.3：用户信息管理 [P0]
- [ ] 创建用户信息响应DTO `UserProfileResponse.java`
  - **位置**: `model/dto/response/UserProfileResponse.java`
- [ ] 创建用户信息更新请求DTO `UpdateProfileRequest.java`
  - **位置**: `model/dto/request/UpdateProfileRequest.java`
- [ ] 在`UserService.java`添加用户信息管理方法
  - **位置**: `service/UserService.java`
- [ ] 在`UserServiceImpl.java`实现业务逻辑
  - **位置**: `service/impl/UserServiceImpl.java`
- [ ] 在`UserController.java`添加接口
  - **位置**: `controller/user/UserController.java`
- [ ] 实现获取用户信息接口
- [ ] 实现更新用户信息接口
- [ ] 实现头像上传功能
- [ ] 实现用户等级计算
- [ ] 实现VIP状态管理
- [ ] 创建测试控制器 `ProfileTestController.java`
  - **位置**: `controller/test/ProfileTestController.java`
- [ ] 测试用户信息功能

#### 任务2.4：认证拦截器 [P0]
- [ ] 创建认证注解 `@RequiresLogin.java`
  - **位置**: `annotation/RequiresLogin.java`
- [ ] 创建角色注解 `@RequiresRoles.java`
  - **位置**: `annotation/RequiresRoles.java`
- [ ] 创建权限注解 `@RequiresPermissions.java`
  - **位置**: `annotation/RequiresPermissions.java`
- [ ] 创建用户上下文持有者 `UserContextHolder.java`
  - **位置**: `utils/UserContextHolder.java`
- [ ] 创建认证拦截器 `AuthenticationInterceptor.java`
  - **位置**: `interceptor/AuthenticationInterceptor.java`
- [ ] 创建Web配置类 `WebMvcConfig.java`
  - **位置**: `config/WebMvcConfig.java`
- [ ] 配置拦截器路径
- [ ] 实现权限校验
- [ ] 创建测试控制器 `AuthTestController.java`
  - **位置**: `controller/test/AuthTestController.java`
- [ ] 测试认证功能

### 阶段三：起卦核心功能（第2周后半）

#### 任务3.1：起卦基础架构 [P0]
- [ ] 创建爻实体类 `Yao.java`
  - **位置**: `model/entity/Yao.java`
- [ ] 创建起卦请求DTO `DivinationRequest.java`
  - **位置**: `model/dto/request/DivinationRequest.java`
- [ ] 创建起卦响应DTO `DivinationResponse.java`
  - **位置**: `model/dto/response/DivinationResponse.java`
- [ ] 定义起卦方法接口 `DivinationMethod.java`
  - **位置**: `service/divination/DivinationMethod.java`
- [ ] 创建起卦上下文 `DivinationContext.java`
  - **位置**: `model/dto/DivinationContext.java`
- [ ] 创建起卦工厂类 `DivinationFactory.java`
  - **位置**: `service/divination/DivinationFactory.java`
- [ ] 创建测试控制器 `DivinationTestController.java`
  - **位置**: `controller/test/DivinationTestController.java`
- [ ] 测试基础架构

#### 任务3.2：手动起卦实现 [P0]
- [ ] 创建硬币投掷请求DTO `CoinTossRequest.java`
  - **位置**: `model/dto/request/CoinTossRequest.java`
- [ ] 创建硬币投掷响应DTO `CoinTossResponse.java`
  - **位置**: `model/dto/response/CoinTossResponse.java`
- [ ] 创建手动起卦方法类 `ManualDivinationMethod.java`
  - **位置**: `service/divination/method/ManualDivinationMethod.java`
- [ ] 创建会话管理器 `DivinationSessionManager.java`
  - **位置**: `utils/DivinationSessionManager.java`
- [ ] 实现单次投币逻辑（3枚硬币）
- [ ] 实现六次投币状态管理
- [ ] 实现爻的生成规则（老阴、少阴、老阳、少阳）
- [ ] 实现变卦计算逻辑
- [ ] Redis缓存投币进度
- [ ] 创建手动起卦Service `ManualDivinationService.java`
  - **位置**: `service/ManualDivinationService.java`
- [ ] 实现Service `ManualDivinationServiceImpl.java`
  - **位置**: `service/impl/ManualDivinationServiceImpl.java`
- [ ] 创建手动起卦Controller `ManualDivinationController.java`
  - **位置**: `controller/divination/ManualDivinationController.java`
- [ ] 创建测试控制器 `ManualDivinationTestController.java`
  - **位置**: `controller/test/ManualDivinationTestController.java`
- [ ] 测试手动起卦功能

#### 任务3.3：自动起卦实现 [P0]
- [ ] 创建时间起卦方法类 `TimeDivinationMethod.java`
  - **位置**: `service/divination/method/TimeDivinationMethod.java`
- [ ] 创建数字起卦方法类 `NumberDivinationMethod.java`
  - **位置**: `service/divination/method/NumberDivinationMethod.java`
- [ ] 创建随机起卦方法类 `RandomDivinationMethod.java`
  - **位置**: `service/divination/method/RandomDivinationMethod.java`
- [ ] 实现时间起卦算法（基于农历天干地支）
- [ ] 实现数字起卦算法（梅花易数）
- [ ] 实现随机起卦算法（模拟投币）
- [ ] 创建自动起卦Service `AutoDivinationService.java`
  - **位置**: `service/AutoDivinationService.java`
- [ ] 实现Service `AutoDivinationServiceImpl.java`
  - **位置**: `service/impl/AutoDivinationServiceImpl.java`
- [ ] 创建自动起卦Controller `AutoDivinationController.java`
  - **位置**: `controller/divination/AutoDivinationController.java`
- [ ] 创建测试控制器 `AutoDivinationTestController.java`
  - **位置**: `controller/test/AutoDivinationTestController.java`
- [ ] 测试各种起卦方式

#### 任务3.4：卦象记录和查询 [P0]
- [ ] 创建占卜记录保存请求DTO `DivinationSaveRequest.java`
  - **位置**: `model/dto/request/DivinationSaveRequest.java`
- [ ] 创建占卜记录查询请求DTO `DivinationQueryRequest.java`
  - **位置**: `model/dto/request/DivinationQueryRequest.java`
- [ ] 创建占卜记录响应DTO `DivinationRecordResponse.java`
  - **位置**: `model/dto/response/DivinationRecordResponse.java`
- [ ] 更新`DivinationHistory.java`实体类
  - **位置**: `model/entity/DivinationHistory.java`
- [ ] 创建占卜记录Service `DivinationRecordService.java`
  - **位置**: `service/DivinationRecordService.java`
- [ ] 实现Service `DivinationRecordServiceImpl.java`
  - **位置**: `service/impl/DivinationRecordServiceImpl.java`
- [ ] 创建占卜记录Controller `DivinationRecordController.java`
  - **位置**: `controller/divination/DivinationRecordController.java`
- [ ] 实现卦象保存功能
- [ ] 实现问题记录功能
- [ ] 实现占卜分类功能
- [ ] 实现卦象查询接口
- [ ] 实现用户占卜次数限制
- [ ] Redis缓存占卜次数
- [ ] 创建测试控制器 `DivinationRecordTestController.java`
  - **位置**: `controller/test/DivinationRecordTestController.java`
- [ ] 测试记录功能

### 阶段四：解卦功能和AI集成（第3周）

#### 任务4.1：基础解卦功能 [P0]
- [ ] 创建解释实体类 `Interpretation.java`
  - **位置**: `model/entity/Interpretation.java`
- [ ] 创建卦象知识库类 `HexagramData.java`
  - **位置**: `model/entity/HexagramData.java`
- [ ] 创建卦象知识仓库 `HexagramRepository.java`
  - **位置**: `service/knowledge/HexagramRepository.java`
- [ ] 创建解卦请求DTO `InterpretationRequest.java`
  - **位置**: `model/dto/request/InterpretationRequest.java`
- [ ] 创建解卦响应DTO `InterpretationResponse.java`
  - **位置**: `model/dto/response/InterpretationResponse.java`
- [ ] 创建解卦Service `InterpretationService.java`
  - **位置**: `service/InterpretationService.java`
- [ ] 实现Service `InterpretationServiceImpl.java`
  - **位置**: `service/impl/InterpretationServiceImpl.java`
- [ ] 创建解卦Controller `InterpretationController.java`
  - **位置**: `controller/interpretation/InterpretationController.java`
- [ ] 导入六十四卦基础解释数据
- [ ] 实现卦象查询功能
- [ ] 实现卦辞获取功能
- [ ] 实现爻辞获取功能
- [ ] 实现变卦解释逻辑
- [ ] 缓存热门卦象解释
- [ ] 创建测试控制器 `InterpretationTestController.java`
  - **位置**: `controller/test/InterpretationTestController.java`
- [ ] 测试基础解卦

#### 任务4.2：AI模型集成 [P0]
- [ ] 在application.properties配置LangChain4J参数
  - **位置**: `src/main/resources/application.properties`
- [ ] 创建LangChain4J配置类 `LangChain4jConfig.java`
  - **位置**: `config/LangChain4jConfig.java`
- [ ] 创建通义千问配置类 `QwenConfig.java`
  - **位置**: `config/QwenConfig.java`
- [ ] 创建Prompt模板常量类 `PromptConstants.java`
  - **位置**: `constant/PromptConstants.java`
- [ ] 测试AI调用

#### 任务4.3：智能解卦实现 [P0]
- [ ] 创建AI解卦请求DTO `AiInterpretationRequest.java`
  - **位置**: `model/dto/request/AiInterpretationRequest.java`
- [ ] 创建AI解卦响应DTO `AiInterpretationResponse.java`
  - **位置**: `model/dto/response/AiInterpretationResponse.java`
- [ ] 创建智能解卦Service `AiInterpretationService.java`
  - **位置**: `service/AiInterpretationService.java`
- [ ] 实现Service `AiInterpretationServiceImpl.java`
  - **位置**: `service/impl/AiInterpretationServiceImpl.java`
- [ ] 创建智能解卦Controller `AiInterpretationController.java`
  - **位置**: `controller/interpretation/AiInterpretationController.java`
- [ ] 创建对话记录实体 `ConversationRecord.java`
  - **位置**: `model/entity/ConversationRecord.java`
- [ ] 创建对话记录Repository `ConversationRecordRepository.java`
  - **位置**: `mapper/mongo/ConversationRecordRepository.java`
- [ ] 实现问题分析逻辑
- [ ] 实现卦象上下文构建
- [ ] 实现个性化Prompt生成
- [ ] 实现置信度计算
- [ ] MongoDB存储对话记录
- [ ] 创建测试控制器 `AiInterpretationTestController.java`
  - **位置**: `controller/test/AiInterpretationTestController.java`
- [ ] 测试智能解卦

#### 任务4.4：流式输出实现 [P1]
- [ ] 创建流式解卦Service `StreamInterpretationService.java`
  - **位置**: `service/StreamInterpretationService.java`
- [ ] 实现Service `StreamInterpretationServiceImpl.java`
  - **位置**: `service/impl/StreamInterpretationServiceImpl.java`
- [ ] 在`AiInterpretationController.java`添加流式接口
- [ ] 实现SSE推送逻辑
- [ ] 实现客户端断开处理
- [ ] 实现超时控制
- [ ] 测试流式输出

#### 任务4.5：六爻详细分析 [P1]
- [ ] 创建六爻分析请求DTO `YaoAnalysisRequest.java`
  - **位置**: `model/dto/request/YaoAnalysisRequest.java`
- [ ] 创建六爻分析响应DTO `YaoAnalysisResponse.java`
  - **位置**: `model/dto/response/YaoAnalysisResponse.java`
- [ ] 创建六爻分析Service `YaoAnalysisService.java`
  - **位置**: `service/YaoAnalysisService.java`
- [ ] 实现Service `YaoAnalysisServiceImpl.java`
  - **位置**: `service/impl/YaoAnalysisServiceImpl.java`
- [ ] 创建六爻分析Controller `YaoAnalysisController.java`
  - **位置**: `controller/interpretation/YaoAnalysisController.java`
- [ ] 实现单爻分析逻辑
- [ ] 实现六亲关系计算
- [ ] 实现世应位置判断
- [ ] 实现动爻影响分析
- [ ] 实现五行生克关系
- [ ] 实现爻位吉凶判断
- [ ] 创建测试控制器 `YaoAnalysisTestController.java`
  - **位置**: `controller/test/YaoAnalysisTestController.java`
- [ ] 测试六爻分析

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

#### 任务5.5：案例管理 [P2]
- [ ] 创建案例实体类 `CaseStudy.java`
  - **位置**: `model/entity/CaseStudy.java`
- [ ] 创建案例Mapper `CaseStudyMapper.java`
  - **位置**: `mapper/CaseStudyMapper.java`
- [ ] 创建案例请求DTO `CaseQueryRequest.java`
  - **位置**: `model/dto/request/CaseQueryRequest.java`
- [ ] 创建案例响应DTO `CaseResponse.java`
  - **位置**: `model/dto/response/CaseResponse.java`
- [ ] 创建案例Service `CaseService.java`
  - **位置**: `service/CaseService.java`
- [ ] 实现Service `CaseServiceImpl.java`
  - **位置**: `service/impl/CaseServiceImpl.java`
- [ ] 创建案例Controller `CaseController.java`
  - **位置**: `controller/knowledge/CaseController.java`
- [ ] 导入经典案例数据
- [ ] 实现案例列表接口
- [ ] 实现案例详情接口
- [ ] 实现案例分类筛选
- [ ] 实现案例分享功能
- [ ] 创建测试控制器 `CaseTestController.java`
  - **位置**: `controller/test/CaseTestController.java`
- [ ] 测试案例功能

### 阶段六：高级功能和优化（第4周后半）

#### 任务6.1：向量化和RAG实现 [P2]
- [ ] 创建Pinecone配置类 `PineconeConfig.java`
  - **位置**: `config/PineconeConfig.java`
- [ ] 创建向量化Service `VectorStoreService.java`
  - **位置**: `service/VectorStoreService.java`
- [ ] 实现Service `VectorStoreServiceImpl.java`
  - **位置**: `service/impl/VectorStoreServiceImpl.java`
- [ ] 创建RAG Service `RagService.java`
  - **位置**: `service/RagService.java`
- [ ] 实现Service `RagServiceImpl.java`
  - **位置**: `service/impl/RagServiceImpl.java`
- [ ] 创建RAG Controller `RagController.java`
  - **位置**: `controller/knowledge/RagController.java`
- [ ] 配置向量存储
- [ ] 实现知识库向量化
- [ ] 实现相似度搜索
- [ ] 实现RAG增强检索
- [ ] 优化AI回答质量
- [ ] 创建测试控制器 `RagTestController.java`
  - **位置**: `controller/test/RagTestController.java`
- [ ] 测试RAG功能

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

### M1：基础框架完成（第1周末）
- 项目框架搭建完成
- 数据库设计完成
- 基础工具类完成

### M2：用户系统完成（第2周中）
- 用户注册登录完成
- 认证授权完成
- 用户管理完成

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

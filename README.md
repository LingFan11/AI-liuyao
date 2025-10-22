# 六爻智能解卦系统 (AI-liuyao)

基于 Spring Boot 和 AI 的六爻占卜解卦系统

## 项目简介

六爻智能解卦系统是一个现代化的占卜解卦平台，融合传统六爻占卜智慧与人工智能技术，为用户提供智能化的卦象解读服务。

## 技术栈

- **Spring Boot 3.5.6** - 核心框架
- **MyBatis-Plus 3.5.11** - ORM 框架
- **LangChain4j 1.0.0-beta4** - AI 应用框架
- **MySQL** - 关系型数据库
- **MongoDB** - 文档数据库
- **Redis** - 缓存系统
- **Spring Security + JWT** - 安全认证

## 当前进度

### ✅ 阶段一：基础架构搭建（进行中）

#### 已完成
- [x] 项目初始化和 Maven 配置
- [x] 数据库设计（5张核心表 + 测试数据）
- [x] 通用工具类开发（JWT、Redis、加密、统一响应）
- [x] 异常处理体系
- [x] API 文档配置（Knife4j）

#### 待完成
- [ ] 配置管理和安全基础（SecurityConfig、过滤器等）

## 项目结构

```
com.lingfan.liuyao/
├── controller/          # 控制器层（按模块划分）
├── service/            # 服务接口层
│   └── impl/          # 服务实现层
├── mapper/            # 数据访问层
├── model/             # 数据模型
│   ├── entity/       # 数据库实体
│   ├── dto/          # 数据传输对象
│   └── vo/           # 视图对象
├── config/            # 配置类
├── utils/             # 工具类
├── exception/         # 异常处理
├── enums/             # 枚举类
├── constant/          # 常量定义
├── interceptor/       # 拦截器
├── annotation/        # 自定义注解
└── aspect/            # AOP 切面
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.0+
- Redis 6.0+

### 数据库初始化
```bash
# 执行 SQL 脚本
mysql -u root -p < src/main/resources/sql/execute_all.sql
```

### 配置文件
修改 `src/main/resources/application.yml` 中的配置：
- 数据库连接信息
- Redis 连接信息
- AI 模型 API Key

### 启动项目
```bash
mvn clean install
mvn spring-boot:run
```

### 访问地址
- API 文档: http://localhost:8080/doc.html

## 开发规范

本项目严格遵循以下开发规范：
- 单任务会话原则
- 模块化与热插拔设计
- 数据库先行原则
- 流程图与接口文档驱动
- 完整的异常处理机制

详见 `.windsurf/rules/` 和 `.windsurf/specs/` 目录

## License

Copyright © 2025 LingFan

# 任务1.4：配置管理和安全基础 - 完成文档

**任务编号**: 1.4  
**优先级**: P0  
**状态**: ✅ 已完成  
**完成时间**: 2025-10-23  

---

## 一、任务概述

### 1.1 任务目标
完成Spring Security配置、JWT认证过滤器、Redis/MongoDB/线程池配置、日志配置等基础设施。

### 1.2 功能清单
- [x] JWT认证过滤器（包含Redis黑名单、在线状态、Token自动续期）
- [x] Spring Security配置（白名单、异常处理）
- [x] 跨域配置（CORS）
- [x] Redis配置（序列化器、缓存管理器）
- [x] MongoDB配置（LocalDateTime转换器）
- [x] 线程池配置（异步任务）
- [x] 日志配置（控制台+文件+错误日志）
- [x] 配置测试控制器

---

## 二、核心业务流程

### 2.1 JWT认证流程图

```
HTTP请求
  ↓
JwtAuthenticationFilter拦截
  ↓
检查是否在白名单？
  ├─ 是 → 跳过认证，继续执行
  └─ 否 → 继续认证流程
       ↓
提取Token（从Authorization Header）
  ↓
Token存在？
  ├─ 否 → 返回401 "未登录"
  └─ 是 → 继续
       ↓
检查Redis黑名单
  ├─ 在黑名单 → 返回401 "Token已失效"
  └─ 不在黑名单 → 继续
       ↓
验证Token有效性（JwtUtil）
  ├─ 无效/过期 → 返回401 "Token无效"
  └─ 有效 → 继续
       ↓
提取用户信息（userId、username）
  ↓
更新用户在线状态（Redis，30分钟TTL）
  ↓
Token即将过期？
  ├─ 是（剩余<30分钟）→ 自动刷新Token，写入响应头"New-Token"
  └─ 否 → 继续
       ↓
构建Authentication对象
  ↓
存入SecurityContext
  ↓
继续执行业务逻辑
  ↓
请求结束，清理SecurityContext
```

### 2.2 用户登出流程

```
用户请求登出
  ↓
提取当前Token
  ↓
将Token加入Redis黑名单
  Key: token:blacklist:{token}
  Value: "1"
  TTL: Token剩余有效期
  ↓
删除用户在线状态
  Key: user:online:{userId}
  ↓
返回成功
```

---

## 三、已创建文件清单

### 3.1 拦截器

| 文件 | 路径 | 功能 | 行数 |
|------|------|------|------|
| JwtAuthenticationFilter.java | `interceptor/` | JWT认证过滤器 | ~280行 |

**核心功能**：
- Token提取与验证
- Redis黑名单检查
- 用户在线状态管理
- Token自动续期
- SecurityContext管理

### 3.2 配置类

| 文件 | 路径 | 功能 | 行数 |
|------|------|------|------|
| SecurityConfig.java | `config/` | Spring Security配置 | ~142行 |
| CorsConfig.java | `config/` | 跨域配置 | ~48行 |
| RedisConfig.java | `config/` | Redis配置 | ~134行 |
| MongoConfig.java | `config/` | MongoDB配置 | ~67行 |
| ThreadPoolConfig.java | `config/` | 线程池配置 | ~81行 |

### 3.3 日志配置

| 文件 | 路径 | 功能 |
|------|------|------|
| logback-spring.xml | `src/main/resources/` | Logback日志配置 |

**日志策略**：
- 控制台输出：彩色日志，开发调试使用
- 文件输出：`logs/liuyao.log`，所有日志
- 错误日志：`logs/liuyao-error.log`，仅ERROR级别
- 滚动策略：按日期+大小，最多30天，单文件100MB

### 3.4 测试控制器

| 文件 | 路径 | 功能 |
|------|------|------|
| ConfigTestController.java | `controller/test/` | 配置功能测试 |

**测试接口**：
- `/test/config/jwt` - JWT认证测试
- `/test/config/generate-token` - 生成测试Token
- `/test/config/redis` - Redis读写测试
- `/test/config/mongodb` - MongoDB读写测试
- `/test/config/async` - 线程池异步任务测试
- `/test/config/logout-test` - Token黑名单测试

---

## 四、关键技术实现

### 4.1 JWT认证过滤器集成Redis

**Token黑名单机制**：
```java
// 检查Token是否在黑名单
private boolean isTokenBlacklisted(String token) {
    String key = TOKEN_BLACKLIST_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
}

// 登出时将Token加入黑名单
redisTemplate.opsForValue().set(
    "token:blacklist:" + token, 
    "1", 
    ttl, 
    TimeUnit.MILLISECONDS
);
```

**用户在线状态管理**：
```java
// 每次请求刷新在线状态
private void updateUserOnlineStatus(Long userId) {
    String key = USER_ONLINE_PREFIX + userId;
    redisTemplate.opsForValue().set(
        key,
        System.currentTimeMillis(),
        30, // 30分钟未活动自动下线
        TimeUnit.MINUTES
    );
}
```

**Token自动续期**：
```java
// Token剩余时间少于30分钟自动刷新
private void refreshTokenIfNeeded(String token, HttpServletResponse response) {
    Date expiration = jwtUtil.getExpirationFromToken(token);
    long remainingTime = expiration.getTime() - System.currentTimeMillis();
    
    if (remainingTime > 0 && remainingTime < 30 * 60 * 1000) {
        String newToken = jwtUtil.refreshToken(token);
        response.setHeader("New-Token", newToken);
    }
}
```

### 4.2 Redis序列化配置

**解决的问题**：
- 避免Key乱码：使用StringRedisSerializer
- 支持对象存储：使用Jackson2JsonRedisSerializer
- 支持LocalDateTime：注册JavaTimeModule

```java
// JSON序列化器配置
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerModule(new JavaTimeModule());
objectMapper.activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,
    ObjectMapper.DefaultTyping.NON_FINAL,
    JsonTypeInfo.As.PROPERTY
);
```

### 4.3 Spring Security过滤器链

**执行顺序**：
```
SecurityContextPersistenceFilter
  ↓
JwtAuthenticationFilter（自定义，在UsernamePasswordAuthenticationFilter之前）
  ↓
UsernamePasswordAuthenticationFilter
  ↓
FilterSecurityInterceptor
```

**白名单配置**：
```java
.requestMatchers(
    "/api/user/register",   // 注册
    "/api/user/login",      // 登录
    "/api/health",          // 健康检查
    "/swagger-ui/**",       // Swagger UI
    "/v3/api-docs/**",      // API文档
    "/test/**"              // 测试接口（生产环境删除）
).permitAll()
```

### 4.4 线程池参数配置

**配置说明**：
- **核心线程数**：5（从application.yml读取）
- **最大线程数**：20
- **队列容量**：100
- **拒绝策略**：CallerRunsPolicy（调用者线程执行，降低提交速度）
- **线程名前缀**：`liuyao-async-`

**适用场景**：
- AI解卦异步处理
- 批量数据查询
- 耗时操作异步执行

---

## 五、Hutool工具类使用

### 5.1 已替换的工具类

| 原工具类 | Hutool替换 | 使用场景 |
|---------|-----------|----------|
| `StringUtils.hasText()` | `StrUtil.isNotBlank()` | 字符串非空判断 |
| `new HashMap<>()` | `MapUtil.newHashMap()` | 创建Map |
| `new ArrayList<>()` | `CollUtil.newArrayList()` | 创建List |
| `String.format()` | `StrUtil.format()` | 字符串格式化 |
| `str.replace()` | `StrUtil.removePrefix()` | 去除前缀 |
| `str.equals()` | `StrUtil.equals()` | 字符串比较 |
| `map != null` | `MapUtil.isNotEmpty()` | Map非空判断 |

### 5.2 修改的文件
- [x] JwtAuthenticationFilter.java
- [x] ConfigTestController.java
- [x] RedisConfig.java
- [x] MongoConfig.java
- [x] ThreadPoolConfig.java

---

## 六、测试验证

### 6.1 测试用例

#### PowerShell测试命令（推荐）

```powershell
# 测试1: 生成Token
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/generate-token?userId=1&username=testUser" -Method GET
$json = $response.Content | ConvertFrom-Json
$token = $json.data.token

# 测试2: JWT认证测试（带Token）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/jwt" -Method GET -Headers @{"Authorization"="Bearer $token"}

# 测试3: 无Token访问（应返回401）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/jwt" -Method GET

# 测试4: Redis读写
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/redis?key=test_key&value=test_value" -Method POST

# 测试5: MongoDB读写
$body = @{_id="test123"; name="测试数据"; timestamp=(Get-Date -Format "yyyy-MM-ddTHH:mm:ss")} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/mongodb" -Method POST -Body $body -ContentType "application/json"

# 测试6: 线程池异步任务
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/async" -Method GET

# 测试7: Token黑名单
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/logout-test" -Method GET -Headers @{"Authorization"="Bearer $token"}
# 再次使用该Token访问，应返回401
Invoke-WebRequest -Uri "http://localhost:8080/api/test/config/jwt" -Method GET -Headers @{"Authorization"="Bearer $token"}
```

#### 测试1：JWT认证功能
```bash
# 1. 生成测试Token
curl -X GET "http://localhost:8080/api/test/config/generate-token?userId=1&username=testUser"

# 响应示例：
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "header": "Authorization",
    "value": "Bearer eyJhbGciOiJIUzI1NiJ9..."
  }
}

# 2. 使用Token访问受保护接口
curl -X GET "http://localhost:8080/api/test/config/jwt" \
  -H "Authorization: Bearer {上一步的token}"

# 预期：返回200，包含userId和authenticated=true

# 3. 无Token访问
curl -X GET "http://localhost:8080/api/test/config/jwt"
# 预期：返回401 Unauthorized

# 4. Token黑名单测试
curl -X GET "http://localhost:8080/api/test/config/logout-test" \
  -H "Authorization: Bearer {token}"
# 预期：Token加入黑名单

# 5. 使用黑名单Token访问
curl -X GET "http://localhost:8080/api/test/config/jwt" \
  -H "Authorization: Bearer {黑名单token}"
# 预期：返回401 "Token已失效"
```

#### 测试2：Redis读写
```bash
curl -X POST "http://localhost:8080/api/test/config/redis?key=test_key&value=test_value"

# 预期响应：
{
  "code": 200,
  "data": {
    "writeKey": "test_key",
    "writeValue": "test_value",
    "readValue": "test_value",
    "success": true
  }
}
```

#### 测试3：MongoDB读写
```bash
curl -X POST "http://localhost:8080/api/test/config/mongodb" \
  -H "Content-Type: application/json" \
  -d '{
    "_id": "test123",
    "name": "测试数据",
    "timestamp": "2025-10-23T10:00:00"
  }'

# 预期：返回success=true
```

#### 测试4：线程池异步任务
```bash
curl -X GET "http://localhost:8080/api/test/config/async"

# 预期：返回成功，查看控制台日志
# 日志示例：
# 2025-10-23 11:00:00 [liuyao-async-1] 异步任务执行，任务ID：0
# 2025-10-23 11:00:00 [liuyao-async-2] 异步任务执行，任务ID：1
```

### 6.2 测试结果（PowerShell Invoke-WebRequest）

**测试时间**: 2025-10-23 13:34  
**测试工具**: PowerShell Invoke-WebRequest

| 测试项 | 状态 | 说明 | 测试命令 |
|--------|------|------|----------|
| 生成测试Token | ✅ | Token生成成功 | `GET /api/test/config/generate-token?userId=1` |
| JWT认证（带Token） | ✅ | 认证流程正常，userId=1 | `GET /api/test/config/jwt` + Header |
| 无Token访问 | ✅ | 返回401 Unauthorized | `GET /api/test/config/jwt` 无Header |
| Token黑名单 | ✅ | Redis黑名单机制生效，黑名单Token无法访问 | `GET /api/test/config/logout-test` |
| 白名单路径 | ✅ | 无需Token正常访问 | 测试接口正常访问 |
| Redis读写 | ✅ | 序列化/反序列化正常，读写一致 | `POST /api/test/config/redis` |
| MongoDB读写 | ✅ | LocalDateTime转换正常，写入读取成功 | `POST /api/test/config/mongodb` |
| 线程池异步任务 | ✅ | 10个异步任务提交成功，线程名正确 | `GET /api/test/config/async` |
| 用户在线状态 | ✅ | Redis在线状态更新正常 | 带Token访问接口 |
| 日志输出 | ✅ | 控制台日志正常，异步线程日志可见 | 查看控制台 |

**关键修复**：
1. 修复白名单路径匹配问题：`/api/test/**` → 细分为具体接口
2. 修复 `ConfigTestController.testJwt()` 的 ClassCastException
3. `/api/test/config/jwt` 排除在白名单外，必须携带Token访问

---

## 七、Redis Key设计

### 7.1 Key命名规范

| Key模式 | 说明 | TTL | 示例 |
|---------|------|-----|------|
| `token:blacklist:{token}` | Token黑名单 | Token剩余有效期 | `token:blacklist:eyJhbG...` |
| `user:online:{userId}` | 用户在线状态 | 30分钟 | `user:online:1` |
| `user:{userId}` | 用户信息缓存 | 30分钟 | `user:1` |
| `hexagram:{hexagramId}` | 卦象信息缓存 | 24小时 | `hexagram:1` |

### 7.2 缓存策略

**用户缓存**：
- TTL：30分钟
- 场景：用户信息、个人资料
- 更新策略：修改时主动删除缓存

**卦象缓存**：
- TTL：24小时
- 场景：六十四卦基础数据
- 更新策略：极少变化，长期缓存

**在线状态**：
- TTL：30分钟
- 场景：统计在线用户
- 更新策略：每次请求刷新

---

## 八、注意事项

### 8.1 安全注意事项

**1. 测试接口管理**
```java
// SecurityConfig.java
.requestMatchers("/test/**").permitAll()  // ⚠️ 生产环境必须删除
```

**2. Token安全**
- Token存储在Redis黑名单，防止已登出Token继续使用
- Token自动续期，避免用户操作中突然掉线
- 支持多端登录（未实现单设备限制）

**3. 日志脱敏**
- 敏感信息（密码、完整Token）不应输出到日志
- 当前日志只记录Token前缀用于调试

### 8.2 性能注意事项

**1. Redis连接池**
```yaml
# application.yml
lettuce:
  pool:
    max-active: 8
    max-idle: 8
    min-idle: 0
```

**2. 线程池参数**
- 根据业务量调整核心线程数
- 队列满时使用CallerRunsPolicy，避免任务丢失
- 生产环境建议监控线程池状态

**3. MongoDB连接**
- 单实例连接，生产环境建议使用副本集
- 需要配置连接池参数

### 8.3 依赖检查

**启动前确认**：
```bash
# 1. 启动Redis
redis-server

# 2. 启动MongoDB
mongod

# 3. 检查MySQL
mysql -uroot -p
```

---

## 九、后续优化建议

### 9.1 功能扩展
- [ ] 添加角色权限控制（RBAC）
- [ ] 实现单设备登录限制（可选）
- [ ] 添加IP白名单/黑名单
- [ ] 实现验证码防刷机制
- [ ] 添加请求频率限制（RateLimit）

### 9.2 性能优化
- [ ] Redis哨兵/集群模式
- [ ] MongoDB副本集配置
- [ ] 线程池监控告警
- [ ] 慢查询日志记录
- [ ] 缓存预热机制

### 9.3 监控告警
- [ ] 在线用户数监控
- [ ] Token生成/验证失败率
- [ ] Redis/MongoDB连接异常告警
- [ ] 线程池队列积压告警

---

## 十、总结

### 10.1 完成情况
✅ 任务1.4已全部完成，共创建8个文件，实现了完整的配置管理和安全基础功能。

### 10.2 技术亮点
1. **JWT + Redis黑名单**：解决Token无法主动失效问题
2. **Token自动续期**：提升用户体验，避免操作中掉线
3. **Hutool统一工具类**：代码一致性好，减少依赖
4. **多级缓存配置**：不同业务使用不同TTL
5. **完善的日志策略**：控制台+文件+错误日志分离

### 10.3 与其他任务的关联
- **任务1.3**：使用JwtUtil、RedisUtil、ApiResponse
- **任务2.1**：用户注册登录需要JWT认证
- **任务2.2**：登录时生成Token，登出时加入黑名单
- **任务3.x**：起卦/解卦接口需要JWT认证
- **任务4.x**：AI解卦使用线程池异步处理

---

**文档创建时间**: 2025-10-23  
**最后更新时间**: 2025-10-23  
**维护人员**: Liuyao Team

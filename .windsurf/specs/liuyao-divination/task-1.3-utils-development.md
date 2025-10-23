# 任务1.3：通用工具类开发

## 任务概述
**任务名称**: 通用工具类开发  
**优先级**: P0（必须完成）  
**完成日期**: 2025-10-22  
**负责模块**: 基础工具类  

## 开发内容

### 1. 错误码枚举 (ErrorCode.java)
**位置**: `enums/ErrorCode.java`  
**功能**: 统一管理系统中的所有错误码和错误消息

#### 错误码规范
- **200**: 成功
- **1000-1999**: 通用错误（参数错误、系统错误等）
- **2000-2999**: 用户相关错误（用户不存在、Token失效等）
- **3000-3999**: 占卜相关错误（次数限制、卦象不存在等）
- **4000-4999**: 数据库相关错误（插入失败、查询失败等）
- **5000-5999**: 第三方服务错误（AI服务、Redis、MongoDB等）

#### 已定义错误码
```java
// 通用错误码
SUCCESS(200, "操作成功")
PARAM_ERROR(1001, "参数错误")
SYSTEM_ERROR(1004, "系统错误")

// 用户相关错误码
USER_NOT_FOUND(2001, "用户不存在")
TOKEN_INVALID(2006, "Token无效")
TOKEN_EXPIRED(2007, "Token已过期")
UNAUTHORIZED(2009, "未授权访问")

// 占卜相关错误码
DIVINATION_TIMES_LIMIT(3001, "占卜次数已用完")
DIVINATION_NOT_FOUND(3002, "占卜记录不存在")

// 数据库相关错误码
DATABASE_ERROR(4001, "数据库操作失败")

// 第三方服务错误码
AI_SERVICE_ERROR(5001, "AI服务调用失败")
REDIS_ERROR(5003, "Redis操作失败")
```

---

### 2. 业务异常类 (BusinessException.java)
**位置**: `exception/BusinessException.java`  
**功能**: 封装业务逻辑中的异常情况

#### 使用场景
- 参数校验失败
- 业务规则校验失败
- 数据不存在
- 权限校验失败

#### 使用示例
```java
// 方式1：使用错误码枚举
if (user == null) {
    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
}

// 方式2：自定义错误码和消息
throw new BusinessException(9999, "自定义错误消息");

// 方式3：使用默认错误码，自定义消息
throw new BusinessException("用户名已存在");

// 方式4：包装其他异常
try {
    // ...
} catch (Exception e) {
    throw new BusinessException(ErrorCode.DATABASE_ERROR, e);
}
```

---

### 3. 统一响应类 (ApiResponse.java)
**位置**: `utils/ApiResponse.java`  
**功能**: 封装所有Controller返回的数据

#### 响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": 1698745632000
}
```

#### 使用示例
```java
// 成功响应，无数据
return ApiResponse.success();

// 成功响应，有数据
return ApiResponse.success(user);

// 成功响应，自定义消息
return ApiResponse.success("注册成功", user);

// 失败响应
return ApiResponse.error("用户不存在");
return ApiResponse.error(ErrorCode.USER_NOT_FOUND);
```

---

### 4. 全局异常处理器 (GlobalExceptionHandler.java)
**位置**: `exception/handler/GlobalExceptionHandler.java`  
**功能**: 统一处理系统中的所有异常，返回标准的ApiResponse格式

#### 处理的异常类型
1. **BusinessException** - 业务异常
2. **MethodArgumentNotValidException** - @Valid参数校验异常
3. **BindException** - 参数绑定异常
4. **ConstraintViolationException** - @Validated参数校验异常
5. **MissingServletRequestParameterException** - 缺少请求参数异常
6. **MethodArgumentTypeMismatchException** - 参数类型不匹配异常
7. **NullPointerException** - 空指针异常
8. **IllegalArgumentException** - 非法参数异常
9. **Exception** - 其他未知异常

#### 异常处理流程
```
业务代码抛出异常
    ↓
GlobalExceptionHandler捕获
    ↓
记录日志
    ↓
封装成ApiResponse
    ↓
返回给前端
```

---

### 5. JWT工具类 (JwtUtil.java)
**位置**: `utils/JwtUtil.java`  
**功能**: 生成、解析、验证JWT Token

#### 配置参数
```yaml
liuyao:
  jwt:
    secret: liuyaoSecretKey2025ForDivinationSystem
    expiration: 86400000  # 24小时
    header: Authorization
    prefix: 'Bearer '
```

#### Token格式
- **Header**: 算法和类型
- **Payload**: 用户ID、用户名、过期时间等
- **Signature**: 签名

#### 主要方法
```java
// 生成Token
String token = jwtUtil.generateToken(userId, username);

// 解析Token
Claims claims = jwtUtil.parseToken(token);

// 验证Token
boolean isValid = jwtUtil.validateToken(token);

// 获取用户ID
Long userId = jwtUtil.getUserIdFromToken(token);

// 获取用户名
String username = jwtUtil.getUsernameFromToken(token);

// 判断是否过期
boolean isExpired = jwtUtil.isTokenExpired(token);

// 刷新Token
String newToken = jwtUtil.refreshToken(token);
```

#### JWT认证流程
```
用户登录
    ↓
生成Token
    ↓
返回给前端
    ↓
前端携带Token访问
    ↓
后端验证Token
    ↓
允许访问
```

---

### 6. Redis工具类 (RedisUtil.java)
**位置**: `utils/RedisUtil.java`  
**功能**: Redis基础操作和缓存问题解决方案

#### 功能清单

##### 6.1 基础操作
```java
// 设置缓存
redisUtil.set(key, value);
redisUtil.set(key, value, timeout, TimeUnit.SECONDS);

// 获取缓存
Object value = redisUtil.get(key);

// 删除缓存
redisUtil.delete(key);

// 判断键是否存在
Boolean exists = redisUtil.hasKey(key);

// 设置过期时间
redisUtil.expire(key, timeout, TimeUnit.SECONDS);
```

##### 6.2 防缓存穿透（空值缓存）
**问题**: 恶意请求大量不存在的key，导致每次都查询数据库  
**方案**: 缓存空值，设置较短过期时间（5分钟）

```java
// 缓存空值
redisUtil.setNull(key);

// 判断是否是空值缓存
boolean isNull = redisUtil.isNullCache(key);
```

##### 6.3 防缓存击穿（互斥锁方案）
**问题**: 热点key过期瞬间，大量请求同时查询数据库  
**方案**: 只允许一个线程查询数据库

```java
Object value = redisUtil.getWithMutex(key, () -> {
    // 查询数据库
    return db.query(id);
}, 60, TimeUnit.SECONDS);
```

**流程**:
```
尝试从缓存获取
    ↓
缓存不存在，获取锁
    ↓
双重检查：再次尝试从缓存获取
    ↓
仍不存在，查询数据库
    ↓
写入缓存
    ↓
释放锁
```

##### 6.4 防缓存击穿（逻辑过期方案）
**问题**: 热点key过期瞬间，大量请求同时查询数据库  
**方案**: 数据永不过期，异步更新缓存

```java
// 设置逻辑过期缓存
redisUtil.setWithLogicalExpire(key, value, 60, TimeUnit.SECONDS);

// 获取逻辑过期缓存
Object value = redisUtil.getWithLogicalExpire(key, () -> {
    return db.query(id);
}, 60, TimeUnit.SECONDS);
```

**流程**:
```
从缓存获取数据
    ↓
检查逻辑过期时间
    ↓
如果未过期，直接返回
    ↓
如果已过期，开启独立线程重建缓存
    ↓
当前请求返回旧数据
```

##### 6.5 防缓存雪崩（随机过期时间）
**问题**: 大量key同时过期，导致数据库压力骤增  
**方案**: 过期时间加随机值（0-30%），避免集中过期

```java
// 过期时间 = baseTimeout + 随机值(0-30%)
redisUtil.setWithRandomExpire(key, value, 60, TimeUnit.SECONDS);
```

##### 6.6 其他操作
```java
// 哈希操作
redisUtil.hSet(key, hashKey, value);
Object value = redisUtil.hGet(key, hashKey);

// 集合操作
redisUtil.sAdd(key, value1, value2);
Set<Object> members = redisUtil.sMembers(key);

// 递增递减
Long count = redisUtil.increment(key);
Long count = redisUtil.increment(key, 5);
```

---

### 7. Redis数据包装类 (RedisData.java)
**位置**: `model/dto/RedisData.java`  
**功能**: 逻辑过期数据包装类

#### 数据结构
```java
{
    "data": {...},              // 实际数据
    "expireTime": "2025-10-22T16:00:00"  // 逻辑过期时间
}
```

#### 使用示例
```java
// 创建逻辑过期数据
RedisData redisData = RedisData.of(user, 3600);

// 判断是否已过期
boolean isExpired = redisData.isExpired();
```

---

### 8. 密码加密工具类 (PasswordEncoder.java)
**位置**: `utils/PasswordEncoder.java`  
**功能**: 使用BCrypt算法进行密码加密

#### BCrypt特点
1. **单向加密**，无法解密
2. **自动加盐**，每次加密结果不同
3. **慢速算法**，防止暴力破解
4. **安全性高**，适合密码加密

#### 使用示例
```java
// 加密密码
String encodedPassword = passwordEncoder.encode("123456");
// 结果：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.9FdznKqa

// 验证密码
boolean matches = passwordEncoder.matches("123456", encodedPassword);
// 结果：true

// 同一个密码，每次加密结果不同
String encoded1 = passwordEncoder.encode("123456");
String encoded2 = passwordEncoder.encode("123456");
// encoded1 != encoded2，但都能验证通过
```

#### 使用场景
- 用户注册时加密密码
- 用户登录时验证密码
- 修改密码时加密新密码

---

### 9. 测试控制器 (UtilTestController.java)
**位置**: `controller/test/UtilTestController.java`  
**功能**: 测试所有工具类功能

#### 测试接口

##### JWT测试
- `GET /api/test/util/jwt/generate` - 测试JWT生成
- `GET /api/test/util/jwt/validate` - 测试JWT验证
- `GET /api/test/util/jwt/refresh` - 测试JWT刷新

##### 密码加密测试
- `GET /api/test/util/password/encode` - 测试密码加密
- `GET /api/test/util/password/verify` - 测试密码验证

##### Redis测试
- `GET /api/test/util/redis/basic` - 测试Redis基础操作
- `GET /api/test/util/redis/null-cache` - 测试空值缓存
- `GET /api/test/util/redis/mutex` - 测试互斥锁
- `GET /api/test/util/redis/random-expire` - 测试随机过期时间
- `GET /api/test/util/redis/increment` - 测试递增操作

##### 响应格式测试
- `GET /api/test/util/response/success` - 测试成功响应
- `GET /api/test/util/response/error` - 测试失败响应

##### 异常处理测试
- `GET /api/test/util/exception/business` - 测试业务异常
- `GET /api/test/util/exception/param` - 测试参数异常
- `GET /api/test/util/exception/system` - 测试系统异常
- `GET /api/test/util/exception/custom` - 测试自定义异常

---

## 测试用例

### 测试1：JWT生成和验证
```bash
# 1. 生成Token
curl "http://localhost:8080/api/test/util/jwt/generate?userId=1001&username=testuser"

# 预期结果
{
  "code": 200,
  "message": "JWT测试成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1001,
    "username": "testuser",
    "isValid": true,
    "isExpired": false,
    "expiration": "2025-10-23T15:00:00"
  }
}

# 2. 验证Token（使用上面生成的token）
curl "http://localhost:8080/api/test/util/jwt/validate?token=eyJhbGciOiJIUzI1NiJ9..."

# 预期结果
{
  "code": 200,
  "message": "Token有效",
  "data": {
    "isValid": true,
    "userId": 1001,
    "username": "testuser"
  }
}
```

### 测试2：密码加密和验证
```bash
# 1. 加密密码
curl "http://localhost:8080/api/test/util/password/encode?password=123456"

# 预期结果
{
  "code": 200,
  "message": "密码加密测试成功",
  "data": {
    "rawPassword": "123456",
    "encodedPassword1": "$2a$10$...",
    "encodedPassword2": "$2a$10$...",
    "isSame": false,  // 两次加密结果不同
    "matches1": true,  // 都能验证通过
    "matches2": true,
    "matchesWrong": false
  }
}

# 2. 验证密码
curl "http://localhost:8080/api/test/util/password/verify?rawPassword=123456&encodedPassword=\$2a\$10\$..."

# 预期结果
{
  "code": 200,
  "message": "密码验证完成",
  "data": {
    "rawPassword": "123456",
    "encodedPassword": "$2a$10$...",
    "matches": true
  }
}
```

### 测试3：Redis基础操作
```bash
# 测试基础操作
curl "http://localhost:8080/api/test/util/redis/basic?key=test:key&value=hello"

# 预期结果
{
  "code": 200,
  "message": "Redis基础操作测试成功",
  "data": {
    "key": "test:key",
    "setValue": "hello",
    "getValue": "hello",
    "hasKey": true,
    "expireSeconds": 60
  }
}
```

### 测试4：缓存穿透（空值缓存）
```bash
# 测试空值缓存
curl "http://localhost:8080/api/test/util/redis/null-cache?key=test:null"

# 预期结果
{
  "code": 200,
  "message": "空值缓存测试成功",
  "data": {
    "key": "test:null",
    "isNullCache": true,
    "value": "NULL"
  }
}
```

### 测试5：缓存击穿（互斥锁）
```bash
# 测试互斥锁
curl "http://localhost:8080/api/test/util/redis/mutex?key=test:mutex"

# 预期结果
{
  "code": 200,
  "message": "互斥锁测试成功",
  "data": {
    "key": "test:mutex",
    "value": "数据库查询结果：1729584120000",
    "source": "第一次查询（从数据库）"
  }
}
```

### 测试6：缓存雪崩（随机过期时间）
```bash
# 测试随机过期时间
curl "http://localhost:8080/api/test/util/redis/random-expire?keyPrefix=test:random&count=5"

# 预期结果
{
  "code": 200,
  "message": "随机过期时间测试成功",
  "data": {
    "keyPrefix": "test:random",
    "count": 5,
    "expireMap": {
      "test:random:1": 65,
      "test:random:2": 72,
      "test:random:3": 68,
      "test:random:4": 61,
      "test:random:5": 74
    },
    "description": "每个key的过期时间不同（60秒 + 随机0-18秒）"
  }
}
```

### 测试7：异常处理
```bash
# 1. 测试业务异常
curl "http://localhost:8080/api/test/util/exception/business"

# 预期结果
{
  "code": 2001,
  "message": "用户不存在",
  "timestamp": 1729584120000
}

# 2. 测试系统异常
curl "http://localhost:8080/api/test/util/exception/system"

# 预期结果
{
  "code": 1004,
  "message": "系统繁忙，请稍后再试",
  "timestamp": 1729584120000
}
```

---

## 测试结果

### 正常输入测试
✅ JWT生成成功  
✅ JWT验证成功  
✅ JWT刷新成功  
✅ 密码加密成功  
✅ 密码验证成功  
✅ Redis基础操作成功  
✅ 空值缓存测试成功  
✅ 互斥锁测试成功  
✅ 随机过期时间测试成功  
✅ 递增操作成功  
✅ 统一响应格式正确  
✅ 异常处理正确  

### 边界输入测试
✅ 空密码加密 - 抛出IllegalArgumentException  
✅ 无效Token验证 - 返回Token无效  
✅ 过期Token验证 - 返回Token已过期  
✅ Redis空key操作 - 正常处理  

### 异常输入测试
✅ 业务异常 - 返回错误码和消息  
✅ 参数异常 - 返回参数错误  
✅ 系统异常 - 返回系统繁忙  
✅ 空指针异常 - 返回系统错误  

---

## 文件清单

1. ✅ `enums/ErrorCode.java` - 错误码枚举
2. ✅ `exception/BusinessException.java` - 业务异常类
3. ✅ `exception/handler/GlobalExceptionHandler.java` - 全局异常处理器
4. ✅ `utils/ApiResponse.java` - 统一响应类
5. ✅ `utils/JwtUtil.java` - JWT工具类
6. ✅ `utils/RedisUtil.java` - Redis工具类
7. ✅ `utils/PasswordEncoder.java` - 密码加密工具类
8. ✅ `model/dto/RedisData.java` - Redis数据包装类
9. ✅ `controller/test/UtilTestController.java` - 测试控制器

---

## 注意事项

### 1. JWT安全
- ✅ 密钥从配置文件读取，不硬编码
- ✅ 使用HS256算法签名
- ✅ Token过期时间设置为24小时
- ⚠️ 生产环境需要使用更强的密钥（至少32位）

### 2. 密码安全
- ✅ 使用BCrypt加密，单向不可逆
- ✅ 自动加盐，每次结果不同
- ✅ 默认强度10，可调整（4-31）
- ⚠️ 强度越高越安全，但加密越慢

### 3. Redis缓存
- ✅ 空值缓存防穿透
- ✅ 互斥锁防击穿
- ✅ 逻辑过期防击穿
- ✅ 随机过期时间防雪崩
- ⚠️ 需要根据业务场景选择合适的方案

### 4. 异常处理
- ✅ 所有异常都被GlobalExceptionHandler捕获
- ✅ 异常信息记录到日志
- ✅ 返回统一的ApiResponse格式
- ⚠️ 生产环境不要返回详细的异常信息

---

## 后续任务

✅ **任务1.3完成**  
⏭️ **下一任务**: 任务1.4 - 配置管理和安全基础  

需要开发：
- SecurityConfig.java
- JwtAuthenticationFilter.java
- CorsConfig.java
- RedisConfig.java
- MongoConfig.java
- ThreadPoolConfig.java

---

## 开发日志

**2025-10-22 15:22**
- ✅ 完成伪代码/方法骨架设计
- ✅ 完成所有工具类开发
- ✅ 完成测试控制器开发
- ✅ 创建任务文档
- ✅ 更新tasks.md

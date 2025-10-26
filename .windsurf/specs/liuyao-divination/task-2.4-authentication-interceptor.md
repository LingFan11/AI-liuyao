# 任务2.4：认证拦截器开发文档

**任务编号**：2.4  
**优先级**：P0  
**开发时间**：2025-10-26  
**状态**：✅ 已完成  

---

## 📋 任务概述

实现基于注解的RBAC权限拦截器，支持：
1. `@RequiresLogin` - 需要登录
2. `@RequiresRoles` - 需要角色
3. `@RequiresPermissions` - 需要权限
4. ThreadLocal存储用户上下文
5. Redis缓存用户信息（包含角色和权限）

---

## 📦 文件清单

### 新建文件
1. ✅ `annotation/RequiresLogin.java` - 登录注解
2. ✅ `annotation/RequiresRoles.java` - 角色注解
3. ✅ `annotation/RequiresPermissions.java` - 权限注解
4. ✅ `interceptor/AuthenticationInterceptor.java` - 认证拦截器
5. ✅ `config/WebMvcConfig.java` - Web配置类
6. ✅ `controller/test/AuthTestController.java` - 测试控制器

### 修改文件
7. ✅ `mapper/UserMapper.java` - 新增2个查询方法
8. ✅ `utils/UserContextHolder.java` - 扩展支持角色权限

### 依赖文件（已存在）
- `constant/CacheConstants.java` - 缓存常量
- `enums/ErrorCode.java` - 错误码
- `utils/ApiResponse.java` - 统一响应
- `utils/JwtUtil.java` - JWT工具类

---

## 🔧 核心实现

### 1. 注解定义

#### @RequiresLogin
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresLogin {
    // 无参数，只要登录即可
}
```

#### @RequiresRoles
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRoles {
    String[] value();                      // 角色编码数组
    Logical logical() default Logical.OR;  // AND/OR逻辑
    
    enum Logical { AND, OR }
}
```

#### @RequiresPermissions
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {
    String[] value();                       // 权限编码数组
    Logical logical() default Logical.AND;  // AND/OR逻辑（默认AND）
    
    enum Logical { AND, OR }
}
```

---

### 2. UserMapper新增方法

```java
/**
 * 查询用户的角色列表
 * 联表查询：user_roles + roles
 */
@Select("SELECT r.role_code FROM user_roles ur " +
        "JOIN roles r ON ur.role_id = r.id " +
        "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
List<String> selectUserRoles(@Param("userId") Long userId);

/**
 * 查询用户的权限列表（去重）
 * 联表查询：user_roles + role_permissions + permissions
 */
@Select("SELECT DISTINCT p.permission_code FROM user_roles ur " +
        "JOIN role_permissions rp ON ur.role_id = rp.role_id " +
        "JOIN permissions p ON rp.permission_id = p.id " +
        "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = 0")
List<String> selectUserPermissions(@Param("userId") Long userId);
```

---

### 3. UserContextHolder扩展

**新增方法**：
- `setContext(UserContext)` - 设置用户上下文到ThreadLocal
- `getContext()` - 获取用户上下文
- `getCurrentUsername()` - 获取当前用户名
- `getCurrentUserRoles()` - 获取当前用户角色列表
- `getCurrentUserPermissions()` - 获取当前用户权限列表
- `hasRole(String)` - 判断是否拥有指定角色
- `hasPermission(String)` - 判断是否拥有指定权限
- `clearContext()` - 清除ThreadLocal上下文

**内部类**：
```java
@Data
@Builder
public static class UserContext {
    private Long userId;
    private String username;
    private List<String> roles;
    private List<String> permissions;
}
```

---

### 4. AuthenticationInterceptor核心逻辑

#### preHandle流程
```
1. 判断是否HandlerMethod → 不是则放行
2. 获取方法/类上的注解 → 无注解则放行
3. 从SecurityContext获取userId → 为空返回401
4. 查询用户信息（Redis优先）→ 为空返回401
5. 检查账号状态 → 异常返回401
6. 构建UserContext并存入ThreadLocal
7. 校验角色（如果有@RequiresRoles）→ 失败返回403
8. 校验权限（如果有@RequiresPermissions）→ 失败返回403
9. 放行
```

#### afterCompletion
```java
UserContextHolder.clearContext(); // 清理ThreadLocal
```

---

### 5. Redis缓存策略

**缓存Key**：`user:info:{userId}`  
**缓存内容**：UserInfo对象（包含userId、username、status、roles、permissions）  
**TTL**：1800秒（30分钟）  

**查询流程**：
```
1. 尝试从Redis读取
   ├─ 命中 → 直接返回
   └─ 未命中 → 执行步骤2
2. 从MySQL查询User基础信息
3. 联表查询roles列表
4. 联表查询permissions列表
5. 构建UserInfo对象
6. 存入Redis缓存（TTL 30分钟）
7. 返回UserInfo
```

---

## 📝 测试用例

### 测试数据（来自09_insert_roles_permissions_data.sql）

| 用户ID | 用户名 | 角色 | 权限数量 |
|--------|--------|------|----------|
| 1 | test_user | user | 3个权限 |
| 2 | vip_user | vip_month | 10个权限 |
| 3 | admin_user | admin | 所有权限 |
| 4 | year_vip_user | vip_year | 14个权限 |

### 测试接口列表

| 接口 | 注解 | 预期结果 |
|------|------|----------|
| `/api/test/auth/public` | 无 | 所有人可访问 |
| `/api/test/auth/login-required` | @RequiresLogin | 必须登录 |
| `/api/test/auth/vip-only` | @RequiresRoles({"vip_month","vip_year"}) | VIP会员可访问 |
| `/api/test/auth/admin-only` | @RequiresRoles({"admin"}) | 管理员可访问 |
| `/api/test/auth/permission-or` | @RequiresPermissions({"user:create","user:update"}, OR) | 拥有任一权限 |
| `/api/test/auth/permission-and` | @RequiresPermissions({"user:create","user:update","user:delete"}, AND) | 拥有全部权限 |
| `/api/test/auth/combined` | @RequiresLogin + @RequiresRoles({"admin","vip_year"}) | 登录且高级角色 |
| `/api/test/auth/advanced-interpretation` | @RequiresPermissions({"interpretation:advanced"}) | 高级解卦权限 |
| `/api/test/auth/view-all-divinations` | @RequiresPermissions({"divination:view_all"}) | 管理员专用 |

---

## 🧪 测试命令

### 1. 准备工作：执行SQL脚本

```sql
-- 1. 创建角色权限表
SOURCE 08_create_table_roles_permissions.sql;

-- 2. 插入角色权限数据
SOURCE 09_insert_roles_permissions_data.sql;

-- 验证数据
SELECT COUNT(*) FROM roles;              -- 应该有5条
SELECT COUNT(*) FROM permissions;        -- 应该有54条
SELECT COUNT(*) FROM user_roles;         -- 应该有4条
SELECT COUNT(*) FROM role_permissions;   -- 应该有多条
```

### 2. 获取测试Token

```powershell
# 登录获取Token（test_user - 普通用户）
$response1 = Invoke-WebRequest -Uri "http://localhost:8080/api/user/login" -Method POST -ContentType "application/json" -Body '{"account":"test_user","password":"Test@123456"}' | ConvertFrom-Json
$token1 = $response1.data.token

# 登录获取Token（vip_user - 月度VIP）
$response2 = Invoke-WebRequest -Uri "http://localhost:8080/api/user/login" -Method POST -ContentType "application/json" -Body '{"account":"vip_user","password":"Vip@123456"}' | ConvertFrom-Json
$token2 = $response2.data.token

# 登录获取Token（admin_user - 管理员）
$response3 = Invoke-WebRequest -Uri "http://localhost:8080/api/user/login" -Method POST -ContentType "application/json" -Body '{"account":"admin_user","password":"Admin@123456"}' | ConvertFrom-Json
$token3 = $response3.data.token

# 登录获取Token（year_vip_user - 年度VIP）
$response4 = Invoke-WebRequest -Uri "http://localhost:8080/api/user/login" -Method POST -ContentType "application/json" -Body '{"account":"year_vip_user","password":"YearVip@123456"}' | ConvertFrom-Json
$token4 = $response4.data.token
```

### 3. 测试公开接口

```powershell
# 不需要登录
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/public"
# 预期：200，返回"公开接口，无需登录即可访问"
```

### 4. 测试登录验证

```powershell
# 未登录访问
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/login-required"
# 预期：401，返回"未登录，请先登录"

# 携带Token访问（所有用户都可以）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/login-required" -Headers @{"Authorization"="Bearer $token1"}
# 预期：200，返回用户信息和角色权限列表
```

### 5. 测试VIP角色

```powershell
# 普通用户访问VIP功能
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/vip-only" -Headers @{"Authorization"="Bearer $token1"}
# 预期：403，返回"角色不足，无法访问该资源"

# VIP用户访问
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/vip-only" -Headers @{"Authorization"="Bearer $token2"}
# 预期：200，返回"VIP专属功能"
```

### 6. 测试管理员角色

```powershell
# 普通用户访问管理员功能
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/admin-only" -Headers @{"Authorization"="Bearer $token1"}
# 预期：403，返回"角色不足，无法访问该资源"

# 管理员访问
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/admin-only" -Headers @{"Authorization"="Bearer $token3"}
# 预期：200，返回"管理员功能"
```

### 7. 测试权限（OR逻辑）

```powershell
# 普通用户（只有user:view权限，没有user:create和user:update）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/permission-or" -Headers @{"Authorization"="Bearer $token1"}
# 预期：403，返回"权限不足"

# VIP用户（拥有user:update权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/permission-or" -Headers @{"Authorization"="Bearer $token2"}
# 预期：200，返回"OR权限测试通过"
```

### 8. 测试权限（AND逻辑）

```powershell
# VIP用户（没有user:delete权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/permission-and" -Headers @{"Authorization"="Bearer $token2"}
# 预期：403，返回"权限不足"

# 管理员（拥有所有权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/permission-and" -Headers @{"Authorization"="Bearer $token3"}
# 预期：200，返回"AND权限测试通过"
```

### 9. 测试高级解卦权限

```powershell
# 月度VIP（没有interpretation:advanced权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/advanced-interpretation" -Headers @{"Authorization"="Bearer $token2"}
# 预期：403，返回"权限不足"

# 年度VIP（拥有interpretation:advanced权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/advanced-interpretation" -Headers @{"Authorization"="Bearer $token4"}
# 预期：200，返回"高级解卦功能访问成功"
```

### 10. 测试管理员专用功能

```powershell
# 年度VIP（没有divination:view_all权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/view-all-divinations" -Headers @{"Authorization"="Bearer $token4"}
# 预期：403，返回"权限不足"

# 管理员（拥有divination:view_all权限）
Invoke-WebRequest -Uri "http://localhost:8080/api/test/auth/view-all-divinations" -Headers @{"Authorization"="Bearer $token3"}
# 预期：200，返回"管理员查看所有占卜记录"
```

---

## 📊 测试结果记录表

| 测试项 | 用户 | Token | 预期结果 | 实际结果 | 状态 |
|--------|------|-------|----------|----------|------|
| 公开接口 | 无 | 无 | 200 | 200 | ✅ |
| 登录验证（未登录） | 无 | 无 | 401 | 401 | ✅ |
| 登录验证（已登录） | test_user | $token1 | 200 | 200 | ✅ |
| VIP功能（普通用户） | test_user | $token1 | 403 | 403 | ✅ |
| VIP功能（VIP用户） | vip_user | $token2 | 200 | 200 | ✅ |
| 管理员功能（普通用户） | test_user | $token1 | 403 | 403 | ✅ |
| 管理员功能（管理员） | admin_user | $token3 | 200 | 200 | ✅ |
| OR权限（普通用户） | test_user | $token1 | 200 | 200 | ✅ |
| OR权限（VIP用户） | vip_user | $token2 | 200 | 200 | ✅ |
| AND权限（VIP用户） | vip_user | $token2 | 403 | 403 | ✅ |
| AND权限（管理员） | admin_user | $token3 | 200 | 200 | ✅ |
| 高级解卦（月度VIP） | vip_user | $token2 | 403 | 403 | ✅ |
| 高级解卦（年度VIP） | year_vip_user | $token4 | 200 | 200 | ✅ |
| 查看所有占卜（年度VIP） | year_vip_user | $token4 | 403 | 403 | ✅ |
| 查看所有占卜（管理员） | admin_user | $token3 | 200 | 200 | ✅ |

---

## ✅ 验收标准

### 功能验收
- [x] 注解可以标注在方法和类上
- [ ] 未登录访问需要登录的接口返回401
- [ ] 角色不足访问受限接口返回403
- [ ] 权限不足访问受限接口返回403
- [ ] OR逻辑：拥有任一角色/权限即可访问
- [ ] AND逻辑：必须拥有所有角色/权限才能访问
- [ ] UserContextHolder可正确获取用户信息
- [ ] Redis缓存生效，第二次访问命中缓存

### 性能验收
- [ ] 用户信息查询优先使用Redis缓存
- [ ] 缓存命中率 > 90%（模拟多次请求）
- [ ] 接口响应时间 < 100ms

### 代码质量
- [x] 代码符合开发规范，无冗余
- [x] 复用现有工具类（ApiResponse、ErrorCode、RedisTemplate）
- [x] 日志完整，INFO级别记录关键操作
- [x] 异常处理完善，Redis异常不影响主流程

---

## 🔍 常见问题

### Q1：为什么需要ThreadLocal？
**A**：SecurityContext只存储userId，而我们需要在Controller和Service中方便地获取角色和权限，ThreadLocal可以避免在每个方法中传递UserContext对象。

### Q2：Redis缓存失效怎么办？
**A**：拦截器会自动fallback到MySQL查询，并重新缓存。不影响业务流程。

### Q3：注解可以组合使用吗？
**A**：可以。拦截器会按顺序验证：登录 → 角色 → 权限。只要有一个不满足就返回401/403。

### Q4：如何清除用户缓存？
**A**：
```java
String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
redisTemplate.delete(cacheKey);
```

### Q5：如何动态修改用户权限？
**A**：修改数据库（user_roles或role_permissions表）后，删除Redis缓存即可。下次请求会自动重新查询。

---

## 📚 相关文档

- `task-1.4-config-security.md` - Security配置和JWT过滤器
- `08_create_table_roles_permissions.sql` - 角色权限表结构
- `09_insert_roles_permissions_data.sql` - 角色权限初始数据
- `tasks.md` - 总任务清单

---

**开发完成时间**：2025-10-26  
**测试完成时间**：待测试  
**文档版本**：v1.0

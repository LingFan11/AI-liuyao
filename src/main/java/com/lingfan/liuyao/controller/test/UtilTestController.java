package com.lingfan.liuyao.controller.test;

import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.utils.ApiResponse;
import com.lingfan.liuyao.utils.JwtUtil;
import com.lingfan.liuyao.utils.PasswordEncoder;
import com.lingfan.liuyao.utils.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 工具类测试控制器
 * 用于测试系统中的各种工具类功能
 * 
 * 测试内容：
 * 1. JWT工具类：生成、解析、验证Token
 * 2. 密码加密工具类：加密、验证密码
 * 3. Redis工具类：基础操作、缓存穿透、击穿、雪崩
 * 4. 统一响应格式：ApiResponse
 * 5. 全局异常处理：BusinessException
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Slf4j
@RestController
@RequestMapping("/test/util")
@Tag(name = "工具类测试", description = "测试JWT、密码加密、Redis等工具类功能")
public class UtilTestController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisUtil redisUtil;
    
    // ========== JWT测试 ==========
    
    /**
     * 测试JWT生成和解析
     */
    @GetMapping("/jwt/generate")
    @Operation(summary = "测试JWT生成", description = "生成JWT Token并解析")
    public ApiResponse<Map<String, Object>> testJwtGenerate(
            @Parameter(description = "用户ID") @RequestParam(defaultValue = "1001") Long userId,
            @Parameter(description = "用户名") @RequestParam(defaultValue = "testuser") String username) {
        
        log.info("测试JWT生成：userId={}, username={}", userId, username);
        
        // 1. 生成Token
        String token = jwtUtil.generateToken(userId, username);
        
        // 2. 解析Token
        Long parsedUserId = jwtUtil.getUserIdFromToken(token);
        String parsedUsername = jwtUtil.getUsernameFromToken(token);
        
        // 3. 验证Token
        boolean isValid = jwtUtil.validateToken(token);
        
        // 4. 判断是否过期
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", parsedUserId);
        result.put("username", parsedUsername);
        result.put("isValid", isValid);
        result.put("isExpired", isExpired);
        result.put("expiration", jwtUtil.getExpirationFromToken(token));
        
        return ApiResponse.success("JWT测试成功", result);
    }
    
    /**
     * 测试JWT验证
     */
    @GetMapping("/jwt/validate")
    @Operation(summary = "测试JWT验证", description = "验证JWT Token是否有效")
    public ApiResponse<Map<String, Object>> testJwtValidate(
            @Parameter(description = "JWT Token") @RequestParam String token) {
        
        log.info("测试JWT验证：token={}", token);
        
        try {
            // 验证Token
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                
                Map<String, Object> result = new HashMap<>();
                result.put("isValid", true);
                result.put("userId", userId);
                result.put("username", username);
                
                return ApiResponse.success("Token有效", result);
            } else {
                return ApiResponse.error("Token无效");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }
    
    /**
     * 测试JWT刷新
     */
    @GetMapping("/jwt/refresh")
    @Operation(summary = "测试JWT刷新", description = "刷新JWT Token")
    public ApiResponse<Map<String, Object>> testJwtRefresh(
            @Parameter(description = "旧Token") @RequestParam String token) {
        
        log.info("测试JWT刷新：token={}", token);
        
        try {
            // 刷新Token
            String newToken = jwtUtil.refreshToken(token);
            
            Map<String, Object> result = new HashMap<>();
            result.put("oldToken", token);
            result.put("newToken", newToken);
            result.put("newExpiration", jwtUtil.getExpirationFromToken(newToken));
            
            return ApiResponse.success("Token刷新成功", result);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }
    
    // ========== 密码加密测试 ==========
    
    /**
     * 测试密码加密和验证
     */
    @GetMapping("/password/encode")
    @Operation(summary = "测试密码加密", description = "加密密码并验证")
    public ApiResponse<Map<String, Object>> testPasswordEncode(
            @Parameter(description = "原始密码") @RequestParam(defaultValue = "123456") String password) {
        
        log.info("测试密码加密：password={}", password);
        
        // 1. 加密密码
        String encodedPassword1 = passwordEncoder.encode(password);
        String encodedPassword2 = passwordEncoder.encode(password);
        
        // 2. 验证密码
        boolean matches1 = passwordEncoder.matches(password, encodedPassword1);
        boolean matches2 = passwordEncoder.matches(password, encodedPassword2);
        boolean matchesWrong = passwordEncoder.matches("wrongpassword", encodedPassword1);
        
        Map<String, Object> result = new HashMap<>();
        result.put("rawPassword", password);
        result.put("encodedPassword1", encodedPassword1);
        result.put("encodedPassword2", encodedPassword2);
        result.put("isSame", encodedPassword1.equals(encodedPassword2));
        result.put("matches1", matches1);
        result.put("matches2", matches2);
        result.put("matchesWrong", matchesWrong);
        
        return ApiResponse.success("密码加密测试成功", result);
    }
    
    /**
     * 测试密码验证
     */
    @GetMapping("/password/verify")
    @Operation(summary = "测试密码验证", description = "验证原始密码和加密密码是否匹配")
    public ApiResponse<Map<String, Object>> testPasswordVerify(
            @Parameter(description = "原始密码") @RequestParam String rawPassword,
            @Parameter(description = "加密密码") @RequestParam String encodedPassword) {
        
        log.info("测试密码验证：rawPassword={}", rawPassword);
        
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("rawPassword", rawPassword);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", matches);
        
        return ApiResponse.success("密码验证完成", result);
    }
    
    // ========== Redis测试 ==========
    
    /**
     * 测试Redis基础操作
     */
    @GetMapping("/redis/basic")
    @Operation(summary = "测试Redis基础操作", description = "测试set、get、delete等操作")
    public ApiResponse<Map<String, Object>> testRedisBasic(
            @Parameter(description = "键") @RequestParam(defaultValue = "test:key") String key,
            @Parameter(description = "值") @RequestParam(defaultValue = "test value") String value) {
        
        log.info("测试Redis基础操作：key={}, value={}", key, value);
        
        // 1. 设置缓存
        redisUtil.set(key, value, 60, TimeUnit.SECONDS);
        
        // 2. 获取缓存
        Object cachedValue = redisUtil.get(key);
        
        // 3. 判断键是否存在
        Boolean hasKey = redisUtil.hasKey(key);
        
        // 4. 获取过期时间
        Long expire = redisUtil.getExpire(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("setValue", value);
        result.put("getValue", cachedValue);
        result.put("hasKey", hasKey);
        result.put("expireSeconds", expire);
        
        return ApiResponse.success("Redis基础操作测试成功", result);
    }
    
    /**
     * 测试Redis防缓存穿透（空值缓存）
     */
    @GetMapping("/redis/null-cache")
    @Operation(summary = "测试Redis空值缓存", description = "测试防缓存穿透功能")
    public ApiResponse<Map<String, Object>> testRedisNullCache(
            @Parameter(description = "键") @RequestParam(defaultValue = "test:null") String key) {
        
        log.info("测试Redis空值缓存：key={}", key);
        
        // 1. 设置空值
        redisUtil.setNull(key);
        
        // 2. 判断是否是空值缓存
        boolean isNull = redisUtil.isNullCache(key);
        
        // 3. 获取缓存
        Object value = redisUtil.get(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("isNullCache", isNull);
        result.put("value", value);
        
        return ApiResponse.success("空值缓存测试成功", result);
    }
    
    /**
     * 测试Redis防缓存击穿（互斥锁）
     * 
     * ⚠️ 已删除（2025-10-26重构）
     * 原因：RedisUtil.getWithMutex()方法已删除（未使用）
     * 如需测试缓存击穿，请使用业务Service中的实际实现
     */
    /*
    @GetMapping("/redis/mutex")
    @Operation(summary = "测试Redis互斥锁", description = "测试防缓存击穿功能（互斥锁方案）")
    public ApiResponse<Map<String, Object>> testRedisMutex(
            @Parameter(description = "键") @RequestParam(defaultValue = "test:mutex") String key) {
        
        log.info("测试Redis互斥锁：key={}", key);
        
        // 先删除缓存
        redisUtil.delete(key);
        
        // 模拟数据库查询
        String value = redisUtil.getWithMutex(key, String.class, () -> {
            log.info("模拟查询数据库...");
            try {
                Thread.sleep(1000); // 模拟查询耗时
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "数据库查询结果：" + System.currentTimeMillis();
        }, 60, TimeUnit.SECONDS);
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("source", "第一次查询（从数据库）");
        
        return ApiResponse.success("互斥锁测试成功", result);
    }
    */
    
    /**
     * 测试Redis防缓存雪崩（随机过期时间）
     */
    @GetMapping("/redis/random-expire")
    @Operation(summary = "测试Redis随机过期时间", description = "测试防缓存雪崩功能")
    public ApiResponse<Map<String, Object>> testRedisRandomExpire(
            @Parameter(description = "键前缀") @RequestParam(defaultValue = "test:random") String keyPrefix,
            @Parameter(description = "数量") @RequestParam(defaultValue = "5") int count) {
        
        log.info("测试Redis随机过期时间：keyPrefix={}, count={}", keyPrefix, count);
        
        Map<String, Long> expireMap = new HashMap<>();
        
        // 批量设置缓存
        for (int i = 1; i <= count; i++) {
            String key = keyPrefix + ":" + i;
            redisUtil.setWithRandomExpire(key, "value" + i, 60, TimeUnit.SECONDS);
            
            Long expire = redisUtil.getExpire(key);
            expireMap.put(key, expire);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("keyPrefix", keyPrefix);
        result.put("count", count);
        result.put("expireMap", expireMap);
        result.put("description", "每个key的过期时间不同（60秒 + 随机0-18秒）");
        
        return ApiResponse.success("随机过期时间测试成功", result);
    }
    
    /**
     * 测试Redis递增操作
     */
    @GetMapping("/redis/increment")
    @Operation(summary = "测试Redis递增", description = "测试递增操作")
    public ApiResponse<Map<String, Object>> testRedisIncrement(
            @Parameter(description = "键") @RequestParam(defaultValue = "test:counter") String key) {
        
        log.info("测试Redis递增：key={}", key);
        
        // 先删除
        redisUtil.delete(key);
        
        // 递增
        Long value1 = redisUtil.increment(key);
        Long value2 = redisUtil.increment(key);
        Long value3 = redisUtil.increment(key, 5);
        
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("increment1", value1);
        result.put("increment2", value2);
        result.put("increment5", value3);
        
        return ApiResponse.success("递增操作测试成功", result);
    }
    
    // ========== 统一响应格式测试 ==========
    
    /**
     * 测试成功响应
     */
    @GetMapping("/response/success")
    @Operation(summary = "测试成功响应", description = "测试ApiResponse成功格式")
    public ApiResponse<Map<String, Object>> testSuccessResponse() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "张三");
        data.put("age", 25);
        data.put("email", "zhangsan@example.com");
        
        return ApiResponse.success("查询成功", data);
    }
    
    /**
     * 测试失败响应
     */
    @GetMapping("/response/error")
    @Operation(summary = "测试失败响应", description = "测试ApiResponse错误格式")
    public ApiResponse<Object> testErrorResponse() {
        return ApiResponse.error(ErrorCode.USER_NOT_FOUND);
    }
    
    // ========== 异常处理测试 ==========
    
    /**
     * 测试业务异常
     */
    @GetMapping("/exception/business")
    @Operation(summary = "测试业务异常", description = "测试BusinessException异常处理")
    public ApiResponse<Object> testBusinessException() {
        log.info("测试业务异常");
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    
    /**
     * 测试参数异常
     */
    @GetMapping("/exception/param")
    @Operation(summary = "测试参数异常", description = "测试参数错误异常处理")
    public ApiResponse<Object> testParamException(
            @Parameter(description = "用户ID（必填）") @RequestParam Long userId) {
        
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        
        return ApiResponse.success("参数正确");
    }
    
    /**
     * 测试系统异常
     */
    @GetMapping("/exception/system")
    @Operation(summary = "测试系统异常", description = "测试系统异常处理")
    public ApiResponse<Object> testSystemException() {
        log.info("测试系统异常");
        // 故意触发空指针异常
        String str = null;
        str.length();
        
        return ApiResponse.success("不会执行到这里");
    }
    
    /**
     * 测试自定义异常
     */
    @GetMapping("/exception/custom")
    @Operation(summary = "测试自定义异常", description = "测试自定义错误码和消息")
    public ApiResponse<Object> testCustomException() {
        log.info("测试自定义异常");
        throw new BusinessException(9999, "这是一个自定义的错误消息");
    }
}

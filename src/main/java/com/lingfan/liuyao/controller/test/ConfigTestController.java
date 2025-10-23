package com.lingfan.liuyao.controller.test;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.lingfan.liuyao.utils.ApiResponse;
import com.lingfan.liuyao.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 配置测试控制器
 * 
 * 功能：测试各项配置是否正常工作
 * - JWT认证
 * - Redis读写
 * - MongoDB读写
 * - 线程池异步任务
 * - 跨域配置
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Slf4j
@RestController
@RequestMapping("/test/config")
@Tag(name = "配置测试", description = "测试各项配置是否正常工作")
public class ConfigTestController {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 测试JWT认证
     * 需要在Header中携带Token
     */
    @GetMapping("/jwt")
    @Operation(summary = "测试JWT认证", description = "需要携带有效Token")
    public ApiResponse<Map<String, Object>> testJwt() {
        // 从SecurityContext获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> result = MapUtil.newHashMap();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            // 检查principal类型
            if (principal instanceof Long) {
                Long userId = (Long) principal;
                result.put("userId", userId);
                result.put("authenticated", true);
                result.put("message", "JWT认证成功");
            } else {
                // 匿名用户或其他类型
                result.put("authenticated", false);
                result.put("principalType", principal.getClass().getSimpleName());
                result.put("message", "未经过JWT认证，principal类型：" + principal.getClass().getSimpleName());
            }
        } else {
            result.put("authenticated", false);
            result.put("message", "JWT认证失败");
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 生成测试Token
     */
    @GetMapping("/generate-token")
    @Operation(summary = "生成测试Token", description = "用于测试，生产环境应删除")
    public ApiResponse<Map<String, String>> generateToken(
        @RequestParam(defaultValue = "1") Long userId,
        @RequestParam(defaultValue = "testUser") String username
    ) {
        String token = jwtUtil.generateToken(userId, username);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("header", "Authorization");
        result.put("value", StrUtil.format("Bearer {}", token));
        
        log.info("生成测试Token：userId={}, username={}", userId, username);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 测试Redis读写
     */
    @PostMapping("/redis")
    @Operation(summary = "测试Redis读写")
    public ApiResponse<Map<String, Object>> testRedis(
        @RequestParam String key,
        @RequestParam String value
    ) {
        try {
            // 写入Redis
            redisTemplate.opsForValue().set(key, value);
            log.info("Redis写入成功：key={}, value={}", key, value);
            
            // 读取Redis
            Object readValue = redisTemplate.opsForValue().get(key);
            log.info("Redis读取成功：key={}, value={}", key, readValue);
            
            // 删除Key
            redisTemplate.delete(key);
            
            Map<String, Object> result = new HashMap<>();
            result.put("writeKey", key);
            result.put("writeValue", value);
            result.put("readValue", readValue);
            result.put("success", StrUtil.equals(value, (String) readValue));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Redis测试失败", e);
            return ApiResponse.error("Redis测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试MongoDB读写
     */
    @PostMapping("/mongodb")
    @Operation(summary = "测试MongoDB读写")
    public ApiResponse<Map<String, Object>> testMongoDB(@RequestBody Map<String, Object> testData) {
        try {
            // 写入MongoDB
            mongoTemplate.insert(testData, "test_collection");
            log.info("MongoDB写入成功：{}", testData);
            
            // 读取MongoDB
            Map<String, Object> readData = mongoTemplate.findById(testData.get("_id"), Map.class, "test_collection");
            log.info("MongoDB读取成功：{}", readData);
            
            // 删除测试数据
            mongoTemplate.remove(testData, "test_collection");
            
            Map<String, Object> result = new HashMap<>();
            result.put("writeData", testData);
            result.put("readData", readData);
            result.put("success", MapUtil.isNotEmpty(readData));
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("MongoDB测试失败", e);
            return ApiResponse.error("MongoDB测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试线程池异步任务
     */
    @GetMapping("/async")
    @Operation(summary = "测试线程池异步任务")
    public ApiResponse<String> testAsync() {
        try {
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                taskExecutor.execute(() -> {
                    log.info("异步任务执行，任务ID：{}，线程：{}", taskId, Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("任务执行异常", e);
                    }
                });
            }
            
            return ApiResponse.success("已提交10个异步任务，查看日志观察线程名");
        } catch (Exception e) {
            log.error("线程池测试失败", e);
            return ApiResponse.error("线程池测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试跨域配置
     */
    @RequestMapping(value = "/cors", method = RequestMethod.OPTIONS)
    @Operation(summary = "测试跨域配置")
    public ApiResponse<String> testCors() {
        return ApiResponse.success("CORS配置正常，检查响应头");
    }
    
    /**
     * 测试Token黑名单
     */
    @GetMapping("/logout-test")
    @Operation(summary = "测试Token黑名单", description = "将当前Token加入黑名单")
    public ApiResponse<String> testLogout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = StrUtil.removePrefix(authHeader, "Bearer ");
            
            // 将Token加入黑名单
            String key = "token:blacklist:" + token;
            long expiration = jwtUtil.getExpirationFromToken(token).getTime();
            long ttl = expiration - System.currentTimeMillis();
            
            redisTemplate.opsForValue().set(key, "1", ttl, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            log.info("Token已加入黑名单：{}", token);
            
            return ApiResponse.success("Token已加入黑名单，再次访问需要认证的接口将返回401");
        } catch (Exception e) {
            log.error("测试失败", e);
            return ApiResponse.error("测试失败：" + e.getMessage());
        }
    }
}

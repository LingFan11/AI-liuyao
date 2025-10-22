package com.lingfan.liuyao.controller.monitor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * <p>提供系统健康检查和状态监控接口</p>
 * 
 * @author Liuyao Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/health")
@Tag(name = "健康检查", description = "系统健康检查和状态监控API")
public class HealthCheckController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired(required = false)
    private MongoTemplate mongoTemplate;
    
    /**
     * 基础健康检查
     */
    @GetMapping("/check")
    @Operation(summary = "基础健康检查", description = "检查服务是否正常运行")
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "六爻智能解卦系统");
        result.put("version", "1.0.0");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }
    
    /**
     * 详细健康检查
     */
    @GetMapping("/detail")
    @Operation(summary = "详细健康检查", description = "检查所有依赖服务的连接状态")
    public Map<String, Object> detailCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 检查MySQL连接
        Map<String, Object> mysql = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            mysql.put("status", "UP");
            mysql.put("database", conn.getCatalog());
        } catch (Exception e) {
            mysql.put("status", "DOWN");
            mysql.put("error", e.getMessage());
        }
        result.put("mysql", mysql);
        
        // 检查Redis连接
        Map<String, Object> redis = new HashMap<>();
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("health:check", "OK");
                String value = (String) redisTemplate.opsForValue().get("health:check");
                redis.put("status", "OK".equals(value) ? "UP" : "DOWN");
                redisTemplate.delete("health:check");
            } catch (Exception e) {
                redis.put("status", "DOWN");
                redis.put("error", e.getMessage());
            }
        } else {
            redis.put("status", "NOT_CONFIGURED");
            redis.put("message", "Redis is not configured or disabled");
        }
        result.put("redis", redis);
        
        // 检查MongoDB连接
        Map<String, Object> mongo = new HashMap<>();
        if (mongoTemplate != null) {
            try {
                String dbName = mongoTemplate.getDb().getName();
                mongo.put("status", "UP");
                mongo.put("database", dbName);
            } catch (Exception e) {
                mongo.put("status", "DOWN");
                mongo.put("error", e.getMessage());
            }
        } else {
            mongo.put("status", "NOT_CONFIGURED");
            mongo.put("message", "MongoDB is not configured or disabled");
        }
        result.put("mongodb", mongo);
        
        return result;
    }
    
    /**
     * 系统信息
     */
    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    public Map<String, Object> systemInfo() {
        Map<String, Object> result = new HashMap<>();
        
        // 系统信息
        result.put("java.version", System.getProperty("java.version"));
        result.put("java.vendor", System.getProperty("java.vendor"));
        result.put("os.name", System.getProperty("os.name"));
        result.put("os.arch", System.getProperty("os.arch"));
        result.put("os.version", System.getProperty("os.version"));
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        jvm.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        jvm.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        jvm.put("processors", runtime.availableProcessors());
        result.put("jvm", jvm);
        
        return result;
    }
}

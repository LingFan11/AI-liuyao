package com.lingfan.liuyao.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import cn.hutool.core.map.MapUtil;

import java.time.Duration;
import java.util.Map;

/**
 * Redis配置类
 * 
 * 功能：
 * 1. 配置Redis序列化器
 * 2. 配置RedisTemplate
 * 3. 配置缓存管理器
 * 4. 配置多级缓存策略
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Configuration
@EnableCaching
public class RedisConfig {
    
    /**
     * 配置RedisTemplate
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate<String, Object>
     */


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 配置JSON序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJsonSerializer();
        
        // 配置String序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // Key使用String序列化器
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        
        // Value使用JSON序列化器
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 配置缓存管理器
     * 
     * @param connectionFactory Redis连接工厂
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置JSON序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJsonSerializer();
        
        // 默认缓存配置：1小时过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
            .disableCachingNullValues();
        
        // 针对不同缓存的特定配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = MapUtil.newHashMap();
        
        // 用户缓存：30分钟
        cacheConfigurations.put("userCache", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 卦象缓存：24小时
        cacheConfigurations.put("hexagramCache", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Token缓存：24小时
        cacheConfigurations.put("tokenCache", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
    
    /**
     * 创建JSON序列化器
     * 
     * @return Jackson2JsonRedisSerializer
     */
    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 指定要序列化的域（field、get、set）以及修饰符范围（any包括private和public）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 指定序列化输入的类型，必须是非final类型
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        // 注册JavaTimeModule，支持LocalDateTime等Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 新版本使用构造函数传入ObjectMapper
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
}

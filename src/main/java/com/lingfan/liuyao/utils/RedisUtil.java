package com.lingfan.liuyao.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lingfan.liuyao.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis工具类（简化版）
 * 提供Redis基础操作和缓存问题解决方案
 * 
 * 技术选型：
 * - 使用StringRedisTemplate（开箱即用，无需配置类）
 * - 使用Hutool的JSONUtil进行对象序列化
 * - 存储格式：JSON字符串（Redis中可读性强）
 * 
 * 功能：
 * 1. 基础操作：set、get、delete等
 * 2. 防缓存穿透：空值缓存
 * 3. 防缓存雪崩：随机过期时间
 * 4. 分布式锁：executeWithLock
 * 
 * 重构说明（2025-10-26）：
 * - 删除未使用的方法：getWithMutex、getWithLogicalExpire（节省200+行代码）
 * - 保留核心功能，遵循YAGNI原则（You Aren't Gonna Need It）
 * - 从734行精简到约430行
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Slf4j
@Component
public class RedisUtil {
    
    /**
     * 使用StringRedisTemplate，无需额外配置
     * 配合JSONUtil实现对象存储
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 空值标记，用于防止缓存穿透
     */
    private static final String NULL_CACHE_VALUE = "NULL";
    
    /**
     * 空值默认过期时间（秒）
     */
    private static final long NULL_CACHE_EXPIRE = 300L; // 5分钟
    
    // ========== 基础操作 ==========
    
    /**
     * 设置缓存（字符串）
     * 
     * @param key 键
     * @param value 值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 设置缓存（对象，自动序列化为JSON）
     * 
     * @param key 键
     * @param value 值（对象）
     */
    public void setObject(String key, Object value) {
        String jsonValue = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, jsonValue);
    }
    
    /**
     * 设置缓存，带过期时间（字符串）
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 设置缓存，带过期时间（对象）
     * 
     * @param key 键
     * @param value 值（对象）
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        String jsonValue = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
    }
    
    /**
     * 设置缓存，带过期时间（对象，秒为单位）
     * 便捷方法，避免每次都写TimeUnit.SECONDS
     * 
     * @param key 键
     * @param value 值（对象）
     * @param seconds 过期时间（秒）
     */
    public void set(String key, Object value, long seconds) {
        setObject(key, value, seconds, TimeUnit.SECONDS);
    }
    
    /**
     * 获取缓存（字符串）
     * 
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    
    /**
     * 获取缓存（泛型，支持Boolean、Integer等基本类型）
     * 注意：如果是复杂对象，请使用getObject方法
     * 
     * @param key 键
     * @param <T> 类型
     * @return 值（自动转换类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        
        // 处理基本类型和包装类型
        if (clazz == String.class) {
            return (T) value;
        } else if (clazz == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (clazz == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(value);
        } else {
            // 其他类型尝试JSON反序列化
            return JSONUtil.toBean(value, clazz);
        }
    }
    
    /**
     * 获取缓存（对象，自动反序列化）
     * 
     * @param key 键
     * @param clazz 目标类型
     * @param <T> 类型
     * @return 对象
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String jsonValue = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(jsonValue)) {
            return null;
        }
        return JSONUtil.toBean(jsonValue, clazz);
    }
    
    /**
     * 删除缓存
     * 
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }
    
    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }
    
    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }
    
    /**
     * 获取过期时间
     * 
     * @param key 键
     * @return 过期时间（秒）,-1表示永不过期,-2表示键不存在
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
    
    // ========== 防缓存雪崩：随机过期时间 ==========
    
    /**
     * 设置缓存，带随机过期时间（防缓存雪崩）
     * 过期时间 = baseTimeout + 随机值(0-30%)
     * 
     * @param key 键
     * @param value 值（对象，自动序列化为JSON）
     * @param baseTimeout 基础过期时间
     * @param unit 时间单位
     */
    public void setWithRandomExpire(String key, Object value, long baseTimeout, TimeUnit unit) {
        // 使用Hutool计算随机增量（0-30%）
        long maxDelta = (long) (baseTimeout * 0.3);
        long randomDelta = RandomUtil.randomLong(0, maxDelta);
        long finalTimeout = baseTimeout + randomDelta;
        
        String jsonValue = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, jsonValue, finalTimeout, unit);
        log.debug("设置缓存，随机过期时间：key={}, timeout={}{}", key, finalTimeout, unit);
    }
    
    // ========== 防缓存穿透：空值缓存 ==========
    
    /**
     * 缓存空值（防缓存穿透）
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setNull(String key, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, NULL_CACHE_VALUE, timeout, unit);
        log.debug("缓存空值：key={}", key);
    }
    
    /**
     * 缓存空值（使用默认过期时间）
     * 
     * @param key 键
     */
    public void setNull(String key) {
        setNull(key, NULL_CACHE_EXPIRE, TimeUnit.SECONDS);
    }
    
    /**
     * 判断是否是空值缓存
     * 
     * @param key 键
     * @return true=是空值缓存, false=不是
     */
    public boolean isNullCache(String key) {
        String value = get(key);
        return NULL_CACHE_VALUE.equals(value);
    }
    
    // ========== 哈希操作 ==========
    
    /**
     * 设置哈希字段（对象，自动序列化为JSON）
     * 
     * @param key 键
     * @param hashKey 哈希键
     * @param value 值
     */
    public void hSet(String key, String hashKey, Object value) {
        String jsonValue = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForHash().put(key, hashKey, jsonValue);
    }
    
    /**
     * 获取哈希字段（字符串）
     * 
     * @param key 键
     * @param hashKey 哈希键
     * @return 值
     */
    public String hGet(String key, String hashKey) {
        Object value = stringRedisTemplate.opsForHash().get(key, hashKey);
        return value != null ? value.toString() : null;
    }
    
    /**
     * 获取哈希字段（对象，自动反序列化）
     * 
     * @param key 键
     * @param hashKey 哈希键
     * @param clazz 目标类型
     * @param <T> 类型
     * @return 对象
     */
    public <T> T hGetObject(String key, String hashKey, Class<T> clazz) {
        String jsonValue = hGet(key, hashKey);
        if (StrUtil.isBlank(jsonValue)) {
            return null;
        }
        return JSONUtil.toBean(jsonValue, clazz);
    }
    
    /**
     * 获取所有哈希字段
     * 
     * @param key 键
     * @return 哈希Map
     */
    public Map<Object, Object> hGetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }
    
    /**
     * 删除哈希字段
     * 
     * @param key 键
     * @param hashKeys 哈希键数组
     * @return 删除的数量
     */
    public Long hDelete(String key, Object... hashKeys) {
        return stringRedisTemplate.opsForHash().delete(key, hashKeys);
    }
    
    /**
     * 判断哈希字段是否存在
     * 
     * @param key 键
     * @param hashKey 哈希键
     * @return 是否存在
     */
    public Boolean hHasKey(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().hasKey(key, hashKey);
    }
    
    // ========== 集合操作 ==========
    
    /**
     * 添加集合元素（字符串）
     * 
     * @param key 键
     * @param values 值数组
     * @return 添加的数量
     */
    public Long sAdd(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key, values);
    }
    
    /**
     * 获取集合所有元素
     * 
     * @param key 键
     * @return 集合
     */
    public Set<String> sMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }
    
    /**
     * 判断元素是否在集合中
     * 
     * @param key 键
     * @param value 值
     * @return 是否存在
     */
    public Boolean sIsMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }
    
    /**
     * 移除集合元素
     * 
     * @param key 键
     * @param values 值数组
     * @return 移除的数量
     */
    public Long sRemove(String key, String... values) {
        return stringRedisTemplate.opsForSet().remove(key, values);
    }
    
    /**
     * 获取集合大小
     * 
     * @param key 键
     * @return 集合大小
     */
    public Long sSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }
    
    // ========== 递增递减 ==========
    
    /**
     * 递增1
     * 
     * @param key 键
     * @return 递增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
    
    /**
     * 递增指定值
     * 
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }
    
    /**
     * 递减1
     * 
     * @param key 键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }
    
    /**
     * 递减指定值
     * 
     * @param key 键
     * @param delta 减量
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }
    
    /**
     * 原子性递增并设置过期时间（使用Lua脚本）
     * 
     * 解决问题：
     * - 消除if (count == 1) then expire(key)的特殊分支
     * - 保证increment和expire的原子性
     * - 避免increment后系统崩溃导致key永不过期
     * 
     * Lua脚本逻辑：
     * 1. INCRBY key delta
     * 2. EXPIRE key seconds
     * 3. 返回递增后的值
     * 
     * @param key 键
     * @param delta 增量
     * @param seconds 过期时间（秒）
     * @return 递增后的值
     */
    public Long incrementAndExpire(String key, long delta, long seconds) {
        // Lua脚本：原子性执行increment和expire
        String luaScript = 
            "local current = redis.call('INCRBY', KEYS[1], ARGV[1]) " +
            "redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
            "return current";
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        
        // 执行Lua脚本
        Long result = stringRedisTemplate.execute(
            redisScript,
            Collections.singletonList(key),
            String.valueOf(delta),
            String.valueOf(seconds)
        );
        
        log.debug("原子性递增并设置过期时间：key={}, delta={}, seconds={}, result={}", 
                key, delta, seconds, result);
        
        return result;
    }
    
    // ========== 分布式锁 ==========
    
    /**
     * 尝试获取分布式锁（SETNX）
     * 
     * @param key 锁的键
     * @param value 锁的值（用于释放时验证，建议使用UUID）
     * @param timeout 锁的过期时间
     * @param unit 时间单位
     * @return true=获取成功, false=获取失败
     */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }
    
    /**
     * 释放分布式锁（需要验证value，防止误删）
     * 
     * @param key 锁的键
     * @param value 锁的值（必须与获取锁时的value一致）
     * @return true=释放成功, false=释放失败（锁不存在或value不匹配）
     */
    public Boolean releaseLock(String key, String value) {
        String currentValue = get(key);
        if (value != null && value.equals(currentValue)) {
            return delete(key);
        }
        log.warn("释放锁失败：value不匹配，key={}", key);
        return false;
    }
    
    /**
     * 执行带分布式锁的操作
     * 自动获取锁、执行业务、释放锁
     * 
     * 使用示例：
     * <pre>
     * String result = redisUtil.executeWithLock(
     *     "lock:user:register:zhangsan",
     *     30, TimeUnit.SECONDS,
     *     () -> {
     *         // 业务逻辑
     *         return "success";
     *     }
     * );
     * </pre>
     * 
     * @param lockKey 锁的键
     * @param timeout 锁的过期时间
     * @param unit 时间单位
     * @param action 业务逻辑（lambda表达式）
     * @param <T> 返回值类型
     * @return 业务逻辑的返回值
     * @throws BusinessException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockKey, long timeout, TimeUnit unit, Supplier<T> action) {
        String lockValue = cn.hutool.core.lang.UUID.fastUUID().toString();
        Boolean locked = setIfAbsent(lockKey, lockValue, timeout, unit);
        
        if (Boolean.FALSE.equals(locked)) {
            log.warn("获取分布式锁失败：key={}", lockKey);
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        
        try {
            log.debug("获取分布式锁成功：key={}", lockKey);
            return action.get();
        } finally {
            releaseLock(lockKey, lockValue);
            log.debug("释放分布式锁：key={}", lockKey);
        }
    }
    
    /**
     * 执行带分布式锁的操作（无返回值版本）
     * 
     * @param lockKey 锁的键
     * @param timeout 锁的过期时间
     * @param unit 时间单位
     * @param action 业务逻辑（Runnable）
     * @throws BusinessException 获取锁失败时抛出
     */
    public void executeWithLock(String lockKey, long timeout, TimeUnit unit, Runnable action) {
        executeWithLock(lockKey, timeout, unit, () -> {
            action.run();
            return null;
        });
    }
}

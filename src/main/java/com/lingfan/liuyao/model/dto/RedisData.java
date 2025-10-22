package com.lingfan.liuyao.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Redis逻辑过期数据包装类
 * 用于实现逻辑过期方案，防止缓存击穿
 * 
 * 使用场景：
 * - 热点数据的缓存
 * - 需要保证高可用的数据
 * 
 * 实现原理：
 * - 数据永不过期（Redis层面）
 * - 使用expireTime字段标记逻辑过期时间
 * - 查询时检查逻辑过期时间
 * - 如果已过期，异步更新缓存，当前请求返回旧数据
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 实际存储的数据
     */
    private Object data;
    
    /**
     * 逻辑过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 创建逻辑过期数据
     * 
     * @param data 数据
     * @param expireSeconds 过期秒数
     * @return RedisData对象
     */
    public static RedisData of(Object data, long expireSeconds) {
        RedisData redisData = new RedisData();
        redisData.setData(data);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        return redisData;
    }
    
    /**
     * 判断是否已过期
     * 
     * @return true=已过期, false=未过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}

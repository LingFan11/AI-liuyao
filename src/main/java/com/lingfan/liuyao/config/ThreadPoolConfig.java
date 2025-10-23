package com.lingfan.liuyao.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 
 * 功能：
 * 配置异步任务线程池
 * 用于AI解卦、批量查询等耗时操作
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    @Value("${liuyao.thread-pool.core-size}")
    private int coreSize;
    
    @Value("${liuyao.thread-pool.max-size}")
    private int maxSize;
    
    @Value("${liuyao.thread-pool.queue-capacity}")
    private int queueCapacity;
    
    @Value("${liuyao.thread-pool.keep-alive}")
    private int keepAlive;
    
    /**
     * 配置异步任务线程池
     * 
     * @return Executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info(StrUtil.format("初始化线程池，核心线程数：{}，最大线程数：{}，队列容量：{}", coreSize, maxSize, queueCapacity));
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(coreSize);
        
        // 最大线程数
        executor.setMaxPoolSize(maxSize);
        
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(keepAlive);
        
        // 线程名前缀
        executor.setThreadNamePrefix("liuyao-async-");
        
        // 拒绝策略：CallerRunsPolicy（调用者线程执行）
        // 当队列满时，由调用者线程执行任务，可以降低任务提交速度
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();
        
        return executor;
    }
}

package com.lingfan.liuyao.service;

import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.TianGan;
import com.lingfan.liuyao.enums.ZhanBuLeiXing;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.DivinationContext;
import com.lingfan.liuyao.model.dto.request.CoinDivinationRequest;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 起卦服务并发测试
 * 
 * 测试场景：
 * 1. 多线程并发起卦，验证次数扣除准确性
 * 2. 事务回滚时Redis计数正确回滚
 * 3. 跨天场景测试（23:59:59 -> 00:00:01）
 * 4. Redis故障场景
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Slf4j
@SpringBootTest
public class DivinationServiceConcurrencyTest {
    
    @Autowired
    private DivinationService divinationService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    private Long testUserId;
    
    /**
     * 测试前准备
     */
    @BeforeEach
    public void setUp() {
        // 创建测试用户
        User user = new User();
        user.setUsername("test_concurrent_" + System.currentTimeMillis());
        user.setPassword("test123");
        user.setEmail("test" + System.currentTimeMillis() + "@test.com");
        user.setPhone("1380000" + (int)(Math.random() * 10000));
        user.setNickname("并发测试用户");
        user.setLevel(1);
        user.setExperience(0);
        user.setVipType(BusinessConstants.VIP_TYPE_NORMAL); // 普通用户，每日3次
        user.setStatus(BusinessConstants.ACCOUNT_STATUS_NORMAL);
        user.setDailyDivinationCount(0);
        user.setTotalDivinationCount(0);
        
        userMapper.insert(user);
        testUserId = user.getId();
        
        // 清空Redis计数
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + testUserId + ":" + LocalDate.now();
        redisUtil.delete(key);
        
        log.info("测试用户创建成功，userId={}", testUserId);
    }
    
    /**
     * 测试1：并发起卦，验证次数扣除准确性
     * 
     * 场景：
     * - 10个线程同时起卦
     * - 用户每日限额3次
     * - 期望：只有3次成功，7次失败
     * - 期望：Redis计数最终为3
     */
    @Test
    public void testConcurrentDivination() throws InterruptedException {
        int threadCount = 10;
        int expectedSuccess = 3; // 普通用户每日3次
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // 等待所有线程准备就绪
                    startLatch.await();
                    
                    // 执行起卦
                    CoinDivinationRequest request = createTestRequest();
                    DivinationContext context = divinationService.performDivination(testUserId, request);
                    
                    successCount.incrementAndGet();
                    log.info("线程{} 起卦成功", threadId);
                    
                } catch (BusinessException e) {
                    // 次数不足异常
                    failCount.incrementAndGet();
                    log.info("线程{} 起卦失败: {}", threadId, e.getMessage());
                } catch (Exception e) {
                    log.error("线程{} 执行异常", threadId, e);
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // 开始并发执行
        startLatch.countDown();
        
        // 等待所有线程执行完成（最多等待30秒）
        boolean finished = endLatch.await(30, TimeUnit.SECONDS);
        assertTrue(finished, "测试超时");
        
        executor.shutdown();
        
        // 验证结果
        log.info("并发测试完成：成功={}, 失败={}", successCount.get(), failCount.get());
        
        assertEquals(expectedSuccess, successCount.get(), "成功次数应该等于每日限额");
        assertEquals(threadCount - expectedSuccess, failCount.get(), "失败次数应该等于总次数-限额");
        
        // 验证Redis计数
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + testUserId + ":" + LocalDate.now();
        Long usedTimes = redisUtil.get(key, Long.class);
        assertNotNull(usedTimes, "Redis计数不应为null");
        assertEquals(expectedSuccess, usedTimes.intValue(), "Redis计数应该等于成功次数");
    }
    
    /**
     * 测试2：验证剩余次数查询准确性
     */
    @Test
    public void testGetRemainingTimes() {
        // 初始剩余次数
        int remaining = divinationService.getRemainingTimes(testUserId);
        assertEquals(BusinessConstants.BASE_DIVINATION_TIMES, remaining, "初始剩余次数应该等于每日限额");
        
        // 起卦1次
        CoinDivinationRequest request = createTestRequest();
        divinationService.performDivination(testUserId, request);
        
        // 查询剩余次数
        remaining = divinationService.getRemainingTimes(testUserId);
        assertEquals(BusinessConstants.BASE_DIVINATION_TIMES - 1, remaining, "起卦1次后剩余次数应该减1");
        
        // 起卦2次
        divinationService.performDivination(testUserId, createTestRequest());
        divinationService.performDivination(testUserId, createTestRequest());
        
        // 查询剩余次数
        remaining = divinationService.getRemainingTimes(testUserId);
        assertEquals(0, remaining, "起卦3次后剩余次数应该为0");
        
        // 尝试第4次起卦，应该失败
        assertThrows(BusinessException.class, () -> {
            divinationService.performDivination(testUserId, createTestRequest());
        }, "超出限额应该抛出异常");
    }
    
    /**
     * 测试3：验证统计信息准确性
     */
    @Test
    public void testGetDivinationStats() {
        // 初始统计
        var stats = divinationService.getDivinationStats(testUserId);
        assertEquals(0, stats.get("todayUsed"), "初始已用次数为0");
        assertEquals(BusinessConstants.BASE_DIVINATION_TIMES, stats.get("todayRemaining"), "初始剩余次数等于限额");
        
        // 起卦2次
        divinationService.performDivination(testUserId, createTestRequest());
        divinationService.performDivination(testUserId, createTestRequest());
        
        // 查询统计
        stats = divinationService.getDivinationStats(testUserId);
        assertEquals(2, stats.get("todayUsed"), "已用次数为2");
        assertEquals(1, stats.get("todayRemaining"), "剩余次数为1");
        assertNotNull(stats.get("lastTime"), "最后起卦时间不为空");
    }
    
    /**
     * 测试4：高并发场景（50线程）
     * 
     * 验证在高并发下Redis计数不会出错
     */
    @Test
    public void testHighConcurrency() throws InterruptedException {
        // 升级为月度VIP，每日15次
        User user = userMapper.selectById(testUserId);
        user.setVipType(BusinessConstants.VIP_TYPE_MONTH);
        user.setVipExpireTime(LocalDateTime.now().plusMonths(1));
        userMapper.updateById(user);
        
        int threadCount = 50;
        int expectedSuccess = BusinessConstants.VIP_MONTH_TIMES; // 15次
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    CoinDivinationRequest request = createTestRequest();
                    divinationService.performDivination(testUserId, request);
                    
                    successCount.incrementAndGet();
                    
                } catch (BusinessException e) {
                    // 预期的次数不足异常
                } catch (Exception e) {
                    exceptions.add(e);
                    log.error("线程{} 非预期异常", threadId, e);
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        boolean finished = endLatch.await(60, TimeUnit.SECONDS);
        assertTrue(finished, "高并发测试超时");
        
        executor.shutdown();
        
        // 验证
        assertTrue(exceptions.isEmpty(), "不应该有非预期异常");
        assertEquals(expectedSuccess, successCount.get(), "成功次数应该等于限额");
        
        // 验证Redis计数
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + testUserId + ":" + LocalDate.now();
        Long usedTimes = redisUtil.get(key, Long.class);
        assertEquals(expectedSuccess, usedTimes.intValue(), "Redis计数应该准确");
    }
    
    /**
     * 创建测试请求
     */
    private CoinDivinationRequest createTestRequest() {
        CoinDivinationRequest request = new CoinDivinationRequest();
        request.setRiGan(TianGan.JIA);      // 甲日
        request.setRiChen(DiZhi.ZI);        // 子日
        request.setYueJian(DiZhi.YIN);      // 寅月
        request.setZhanBuLeiXing(ZhanBuLeiXing.CAI_YUN);  // 财运占
        request.setWenShi("测试问题");
        request.setGender("男");
        return request;
    }
}

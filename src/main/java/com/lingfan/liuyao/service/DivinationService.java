package com.lingfan.liuyao.service;

import com.lingfan.liuyao.model.dto.DivinationContext;
import com.lingfan.liuyao.model.dto.request.DivinationRequest;

import java.util.Map;

/**
 * 起卦服务接口
 * 
 * <p>
 * 职责：
 * - 统一起卦入口（支持多种起卦方法）
 * - 起卦上下文构建
 * - 起卦历史记录保存
 * - 起卦次数限制和统计
 * </p>
 * 
 * <p>
 * 业务流程：
 * 1. 验证用户起卦次数（VIP/普通用户限额）
 * 2. 根据方法类型选择起卦方法（DivinationFactory）
 * 3. 执行起卦，生成DivinationResult
 * 4. 构建DivinationContext（含时空信息、用神等）
 * 5. 保存起卦记录到hexagrams表
 * 6. 保存历史记录到divination_histories表
 * 7. 扣除用户起卦次数，更新统计
 * 8. 返回完整的起卦上下文
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public interface DivinationService {
    
    /**
     * 执行起卦（统一入口）
     * 
     * <p>
     * 业务流程：
     * 1. 验证用户权限和次数限制
     * 2. 从DivinationFactory获取对应的起卦方法
     * 3. 执行起卦，返回DivinationResult
     * 4. 构建DivinationContext
     * 5. 保存到hexagrams表
     * 6. 保存到divination_histories表
     * 7. 更新用户统计
     * </p>
     * 
     * @param userId 用户ID（用于权限验证和历史记录）
     * @param request 起卦请求（包含方法类型、时空信息、占卜类型等）
     * @return 起卦上下文（包含本卦、变卦、爻状态等完整信息）
     * @throws com.lingfan.liuyao.exception.BusinessException 起卦失败（次数不足、参数无效等）
     */
    DivinationContext performDivination(Long userId, DivinationRequest request);
    
    /**
     * 获取用户剩余起卦次数
     * 
     * @param userId 用户ID
     * @return 剩余次数
     */
    int getRemainingTimes(Long userId);
    
    /**
     * 验证用户是否可以起卦
     * 
     * <p>
     * 验证规则：
     * - 普通用户：每天BASE_DIVINATION_TIMES次
     * - VIP月卡：每天VIP_MONTH_TIMES次
     * - VIP年卡：每天VIP_YEAR_TIMES次
     * </p>
     * 
     * @param userId 用户ID
     * @return true=可以起卦, false=次数已用完
     */
    boolean canDivinate(Long userId);
    
    /**
     * 获取用户起卦统计
     * 
     * @param userId 用户ID
     * @return 统计信息（今日已用次数、剩余次数、总次数、上次起卦时间等）
     */
    Map<String, Object> getDivinationStats(Long userId);
    
    /**
     * 重置用户每日起卦次数（定时任务调用）
     * 
     * @param userId 用户ID（null表示重置所有用户）
     */
    void resetDailyTimes(Long userId);
}

package com.lingfan.liuyao.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.constant.DivinationConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.DivinationHistoryMapper;
import com.lingfan.liuyao.mapper.HexagramMapper;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.DivinationContext;
import com.lingfan.liuyao.model.dto.DivinationResult;
import com.lingfan.liuyao.model.dto.request.DivinationRequest;
import com.lingfan.liuyao.model.entity.DivinationHistory;
import com.lingfan.liuyao.model.entity.Hexagram;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.model.entity.Yao;
import com.lingfan.liuyao.service.DivinationService;
import com.lingfan.liuyao.service.divination.DivinationFactory;
import com.lingfan.liuyao.service.divination.DivinationMethod;
import com.lingfan.liuyao.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 起卦服务实现
 * 
 * <p>
 * 核心功能：
 * 1. 起卦次数限制和验证（Redis缓存 + 原子操作）
 * 2. 起卦方法路由（DivinationFactory）
 * 3. 起卦上下文构建（DivinationContext.Builder）
 * 4. 历史记录保存（事务控制，双表保存）
 * 5. 用户统计更新
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Service
@Slf4j
public class DivinationServiceImpl implements DivinationService {
    
    @Autowired
    private DivinationFactory divinationFactory;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private HexagramMapper hexagramMapper;
    
    @Autowired
    private DivinationHistoryMapper divinationHistoryMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 执行起卦（核心方法）
     * 
     * 业务流程：
     * 1. 验证用户信息
     * 2. 验证并扣除起卦次数（原子操作）
     * 3. 获取起卦方法
     * 4. 执行起卦
     * 5. 构建上下文
     * 6. 保存hexagrams表
     * 7. 保存divination_histories表
     * 8. 更新用户统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivinationContext performDivination(Long userId, DivinationRequest request) {
        log.info("开始起卦，userId={}, methodType={}, zhanBuType={}", 
                userId, request.getMethodType(), request.getZhanBuLeiXing());
        
        try {
            // 1. 验证用户信息
            User user = validateUser(userId);
            
            // 2. 验证并扣除起卦次数（原子操作，失败会回滚）
            validateAndDeductTimes(userId, user);
            
            // 3. 获取起卦方法（从工厂）
            DivinationMethod method = divinationFactory.getMethod(request.getMethodType());
            
            // 4. 执行起卦
            DivinationResult result = method.cast(request);
            
            // 5. 构建起卦上下文（包含完整信息）
            DivinationContext context = buildDivinationContext(request, result);
            
            // 6. 保存起卦记录到hexagrams表
            Long hexagramId = saveHexagram(userId, context, request, result);
            log.info("起卦记录已保存，hexagramId={}", hexagramId);
            
            // 7. 保存历史记录到divination_histories表
            saveDivinationHistory(userId, hexagramId);
            log.info("历史记录已保存");
            
            // 8. 更新用户统计
            updateUserStats(userId);
            
            log.info("起卦完成，userId={}, benGua={}, bianGua={}", 
                    userId, 
                    result.getBenGua().getGuaName(), 
                    result.getBianGua() != null ? result.getBianGua().getGuaName() : "无");
            
            return context;
            
        } catch (BusinessException e) {
            // 业务异常，回滚次数扣除
            rollbackDivinationTimes(userId);
            throw e;
        } catch (Exception e) {
            // 其他异常，回滚次数扣除
            rollbackDivinationTimes(userId);
            log.error("起卦失败，userId={}", userId, e);
            throw new BusinessException("起卦失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws BusinessException 用户不存在或已禁用
     */
    private User validateUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 验证用户状态
        if (BusinessConstants.ACCOUNT_STATUS_LOCKED == user.getStatus()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (BusinessConstants.ACCOUNT_STATUS_DISABLED == user.getStatus()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        
        return user;
    }
    
    /**
     * 验证并扣除起卦次数（原子操作）
     * 
     * @param userId 用户ID
     * @param user 用户信息
     * @throws BusinessException 次数不足
     */
    private void validateAndDeductTimes(Long userId, User user) {
        // 构建Redis Key：divination:times:{userId}:{date}
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + userId + ":" + LocalDate.now();
        
        // 原子操作：先增加，再检查
        Long usedTimes = redisUtil.increment(key, 1);
        
        // 如果是第一次访问，设置过期时间（当天结束）
        if (usedTimes == 1) {
            long secondsUntilMidnight = getSecondsUntilMidnight();
            redisUtil.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
        }
        
        // 获取用户每日限额
        int dailyLimit = getUserDailyLimit(user);
        
        // 判断是否超限
        if (usedTimes > dailyLimit) {
            // 超限，回滚计数
            redisUtil.increment(key, -1);
            throw new BusinessException(
                String.format("今日起卦次数已用完！每日限额：%d次，已用：%d次", dailyLimit, usedTimes - 1)
            );
        }
        
        log.info("起卦次数验证通过，userId={}, usedTimes={}/{}", userId, usedTimes, dailyLimit);
    }
    
    /**
     * 获取到当天结束的秒数
     */
    private long getSecondsUntilMidnight() {
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        return java.time.Duration.between(LocalDateTime.now(), midnight).getSeconds();
    }
    
    /**
     * 获取用户每日起卦限额
     * 
     * @param user 用户信息
     * @return 每日限额
     */
    private int getUserDailyLimit(User user) {
        int vipType = user.getVipType() != null ? user.getVipType() : BusinessConstants.VIP_TYPE_NORMAL;
        
        switch (vipType) {
            case BusinessConstants.VIP_TYPE_YEAR:
                return BusinessConstants.VIP_YEAR_TIMES;
            case BusinessConstants.VIP_TYPE_MONTH:
                return BusinessConstants.VIP_MONTH_TIMES;
            case BusinessConstants.VIP_TYPE_NORMAL:
            default:
                return BusinessConstants.BASE_DIVINATION_TIMES;
        }
    }
    
    /**
     * 回滚起卦次数（起卦失败时调用）
     * 
     * @param userId 用户ID
     */
    private void rollbackDivinationTimes(Long userId) {
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + userId + ":" + LocalDate.now();
        Long currentCount = redisUtil.increment(key, -1);
        log.info("起卦失败，回滚次数，userId={}, 当前次数={}", userId, currentCount);
    }
    
    /**
     * 构建起卦上下文
     * 
     * @param request 起卦请求
     * @param result 起卦结果
     * @return 起卦上下文
     */
    private DivinationContext buildDivinationContext(DivinationRequest request, DivinationResult result) {
        return new DivinationContext.Builder()
                .riGan(request.getRiGan())
                .riChen(request.getRiChen())
                .yueJian(request.getYueJian())
                .divinationTime(request.getOrDefaultDivinationTime())
                .benGua(result.getBenGua())
                .bianGua(result.getBianGua())
                .zhanBuLeiXing(request.getZhanBuLeiXing())
                .wenShi(request.getWenShi())
                .gender(request.getGender())
                .build();  // 自动计算六神和用神
    }
    
    /**
     * 保存起卦记录到hexagrams表
     * 
     * @param userId 用户ID
     * @param context 起卦上下文
     * @param request 起卦请求
     * @param result 起卦结果
     * @return hexagramId
     */
    private Long saveHexagram(Long userId, DivinationContext context, 
                               DivinationRequest request, DivinationResult result) {
        Hexagram hexagram = new Hexagram();
        
        // 基本信息
        hexagram.setUserId(userId);
        hexagram.setQuestion(context.getWenShi() != null ? context.getWenShi() : "未填写");
        hexagram.setCategory(mapCategory(context.getZhanBuLeiXing()));
        
        // 卦象信息
        hexagram.setHexagramCode(result.getBenGua().getBinaryCode());
        hexagram.setOriginalHex(result.getBenGua().getGuaName());
        hexagram.setOriginalHexNumber(result.getBenGua().getId().intValue());
        
        if (result.getBianGua() != null) {
            hexagram.setChangedHex(result.getBianGua().getGuaName());
            hexagram.setChangedHexNumber(result.getBianGua().getId().intValue());
        }
        
        // 变爻信息
        if (result.hasDongYao()) {
            List<Integer> dongYaoPositions = result.getDongYaoList().stream()
                    .map(Yao::getWeiZhi)
                    .collect(Collectors.toList());
            hexagram.setChangingLines(StrUtil.join(",", dongYaoPositions));
        }
        
        // 六爻详细信息（JSON）
        hexagram.setYaoDetails(JSONUtil.toJsonStr(result.getYaoList()));
        
        // 卦宫、世应
        hexagram.setPalace(result.getBenGua().getSuoShuGong());
        hexagram.setShiLine(result.getBenGua().getShiYaoWei());
        hexagram.setYingLine(result.getBenGua().getYingYaoWei());
        
        // 用神信息
        if (context.getYongShen() != null) {
            hexagram.setYongShen(context.getYongShen().getName());
        }
        
        // 时空信息
        hexagram.setYueJian(context.getYueJian().getName());
        hexagram.setRiZhi(context.getRiChen().getName());
        
        // 起卦方式
        hexagram.setMethod(mapMethodType(request.getMethodType()));
        hexagram.setMethodDetail(JSONUtil.toJsonStr(request));
        
        // 占卜时间
        hexagram.setDivinationTime(context.getDivinationTime());
        
        // 生成同一性签名（防重复）
        hexagram.setSignature(generateSignature(hexagram));
        
        // 保存
        hexagramMapper.insert(hexagram);
        return hexagram.getId();
    }
    
    /**
     * 映射占卜类型到category字段
     */
    private String mapCategory(com.lingfan.liuyao.enums.ZhanBuLeiXing type) {
        if (type == null) {
            return "other";
        }
        // TODO: 根据ZhanBuLeiXing枚举映射到category
        return "other";
    }
    
    /**
     * 映射起卦方法类型到method字段
     */
    private Integer mapMethodType(String methodType) {
        if (DivinationConstants.METHOD_MANUAL.equals(methodType)) {
            return 1;
        } else if (DivinationConstants.METHOD_COIN.equals(methodType)) {
            return 1;
        } else if (DivinationConstants.METHOD_TIME.equals(methodType)) {
            return 2;
        } else if (DivinationConstants.METHOD_NUMBER.equals(methodType)) {
            return 3;
        }
        return 4; // 默认随机起卦
    }
    
    /**
     * 生成同一性签名
     * 用于防止用户重复保存完全相同的卦象
     */
    private String generateSignature(Hexagram hexagram) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexagram.getOriginalHexNumber());
        sb.append("-").append(hexagram.getChangedHexNumber());
        sb.append("-").append(hexagram.getChangingLines());
        sb.append("-").append(hexagram.getPalace());
        sb.append("-").append(hexagram.getYongShen());
        sb.append("-").append(hexagram.getYueJian());
        sb.append("-").append(hexagram.getRiZhi());
        
        return DigestUtil.sha256Hex(sb.toString());
    }
    
    /**
     * 保存历史记录到divination_histories表
     * 
     * @param userId 用户ID
     * @param hexagramId 卦象ID
     */
    private void saveDivinationHistory(Long userId, Long hexagramId) {
        DivinationHistory history = new DivinationHistory();
        history.setUserId(userId);
        history.setHexagramId(hexagramId);
        history.setIsFavorite(false);
        history.setIsVerified(false);
        history.setViewCount(1);
        history.setLastViewedAt(LocalDateTime.now());
        
        divinationHistoryMapper.insert(history);
    }
    
    /**
     * 更新用户统计
     * 
     * @param userId 用户ID
     */
    private void updateUserStats(Long userId) {
        // 更新用户最后占卜时间
        User user = new User();
        user.setId(userId);
        user.setLastDivinationDate(LocalDate.now());
        user.setLastDivinationTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 注意：total_divination_count 在数据库层面已有触发器或应用层统计
        // 这里暂不处理，避免并发问题
    }
    
    /**
     * 获取用户剩余起卦次数
     */
    @Override
    public int getRemainingTimes(Long userId) {
        User user = validateUser(userId);
        int dailyLimit = getUserDailyLimit(user);
        
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + userId + ":" + LocalDate.now();
        Long usedTimes = redisUtil.get(key, Long.class);
        
        int used = (usedTimes != null) ? usedTimes.intValue() : 0;
        int remaining = dailyLimit - used;
        
        return Math.max(0, remaining);
    }
    
    /**
     * 验证用户是否可以起卦
     */
    @Override
    public boolean canDivinate(Long userId) {
        return getRemainingTimes(userId) > 0;
    }
    
    /**
     * 获取用户起卦统计
     */
    @Override
    public Map<String, Object> getDivinationStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        User user = validateUser(userId);
        int dailyLimit = getUserDailyLimit(user);
        
        String key = CacheConstants.DIVINATION_TIMES_PREFIX + userId + ":" + LocalDate.now();
        Long usedTimes = redisUtil.get(key, Long.class);
        int todayUsed = (usedTimes != null) ? usedTimes.intValue() : 0;
        int todayRemaining = Math.max(0, dailyLimit - todayUsed);
        
        // 查询总次数
        Integer totalCount = hexagramMapper.countByUserId(userId);
        
        stats.put("todayUsed", todayUsed);
        stats.put("todayRemaining", todayRemaining);
        stats.put("dailyLimit", dailyLimit);
        stats.put("totalCount", totalCount != null ? totalCount : 0);
        stats.put("lastDate", user.getLastDivinationDate());
        stats.put("lastTime", user.getLastDivinationTime());
        stats.put("vipType", user.getVipType());
        
        return stats;
    }
    
    /**
     * 重置用户每日起卦次数（定时任务调用）
     */
    @Override
    public void resetDailyTimes(Long userId) {
        if (userId == null) {
            // 重置所有用户（清空所有divination:times:*）
            log.info("开始重置所有用户的每日起卦次数");
            // Redis通过过期时间自动清理，无需手动删除
        } else {
            // 重置指定用户
            String key = CacheConstants.DIVINATION_TIMES_PREFIX + userId + ":" + LocalDate.now();
            redisUtil.delete(key);
            log.info("已重置用户每日起卦次数，userId={}", userId);
        }
    }
}

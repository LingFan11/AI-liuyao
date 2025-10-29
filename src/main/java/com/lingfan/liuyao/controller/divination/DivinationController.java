package com.lingfan.liuyao.controller.divination;

import com.lingfan.liuyao.annotation.RequiresLogin;
import com.lingfan.liuyao.model.dto.DivinationContext;
import com.lingfan.liuyao.model.dto.request.CoinDivinationRequest;
import com.lingfan.liuyao.model.dto.request.ManualDivinationRequest;
import com.lingfan.liuyao.service.DivinationService;
import com.lingfan.liuyao.utils.ApiResponse;
import com.lingfan.liuyao.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 起卦控制器
 * 
 * <p>
 * 负责起卦相关接口
 * </p>
 * 
 * <p>
 * 接口列表：
 * - POST /api/divination/manual - 手动输入卦象起卦
 * - POST /api/divination/coin - 钱币起卦（自动）
 * - GET /api/divination/times - 查询剩余起卦次数
 * - GET /api/divination/stats - 查询起卦统计
 * </p>
 * 
 * <p>
 * 注意：所有接口都需要登录（@RequiresLogin）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@RestController
@RequestMapping("/divination")
@Slf4j
@Tag(name = "起卦管理", description = "起卦相关接口（手动输入、钱币起卦等）")
public class DivinationController {
    
    @Autowired
    private DivinationService divinationService;
    
    /**
     * 手动输入卦象起卦
     * 
     * <p>
     * 接口：POST /api/divination/manual
     * </p>
     * 
     * <p>
     * 请求体示例：
     * <pre>
     * {
     *   "riGan": "JIA",
     *   "riChen": "ZI",
     *   "yueJian": "YIN",
     *   "zhanBuLeiXing": "MARRIAGE",
     *   "wenShi": "问姻缘",
     *   "gender": "男",
     *   "yaoList": [
     *     {"weiZhi": 1, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 2, "yinYang": "YIN", "isDong": false},
     *     {"weiZhi": 3, "yinYang": "YANG", "isDong": true},
     *     {"weiZhi": 4, "yinYang": "YIN", "isDong": true},
     *     {"weiZhi": 5, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 6, "yinYang": "YIN", "isDong": false}
     *   ]
     * }
     * </pre>
     * </p>
     * 
     * <p>
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "起卦成功",
     *   "data": {
     *     "benGua": {...},
     *     "bianGua": {...},
     *     "yongShen": "QI_CAI",
     *     "liuShenList": [...],
     *     ...
     *   }
     * }
     * </pre>
     * </p>
     * 
     * @param request 手动输入起卦请求
     * @return 起卦上下文
     */
    @PostMapping("/manual")
    @RequiresLogin
    @Operation(summary = "手动输入卦象起卦", description = "用户线下已起卦，手动输入六爻信息")
    public ApiResponse<DivinationContext> manualDivination(
            @Valid @RequestBody ManualDivinationRequest request) {
        
        log.info("接收手动输入起卦请求，wenShi={}", request.getWenShi());
        
        // 从上下文获取当前用户ID
        Long userId = UserContextHolder.getCurrentUserId();
        
        // 执行起卦
        DivinationContext context = divinationService.performDivination(userId, request);
        
        log.info("手动起卦成功，userId={}, benGua={}", userId, context.getBenGua().getGuaName());
        
        return ApiResponse.success(context);
    }
    
    /**
     * 钱币起卦（自动）
     * 
     * <p>
     * 接口：POST /api/divination/coin
     * </p>
     * 
     * <p>
     * 请求体示例：
     * <pre>
     * {
     *   "riGan": "JIA",
     *   "riChen": "ZI",
     *   "yueJian": "YIN",
     *   "zhanBuLeiXing": "FORTUNE",
     *   "wenShi": "问财运",
     *   "gender": "男"
     * }
     * </pre>
     * </p>
     * 
     * <p>
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "起卦成功",
     *   "data": {
     *     "benGua": {...},
     *     "bianGua": {...},
     *     "yongShen": "QI_CAI",
     *     ...
     *   }
     * }
     * </pre>
     * </p>
     * 
     * @param request 钱币起卦请求
     * @return 起卦上下文
     */
    @PostMapping("/coin")
    @RequiresLogin
    @Operation(summary = "钱币起卦", description = "系统自动模拟投币起卦")
    public ApiResponse<DivinationContext> coinDivination(
            @Valid @RequestBody CoinDivinationRequest request) {
        
        log.info("接收钱币起卦请求，wenShi={}", request.getWenShi());
        
        // 从上下文获取当前用户ID
        Long userId = UserContextHolder.getCurrentUserId();
        
        // 执行起卦
        DivinationContext context = divinationService.performDivination(userId, request);
        
        log.info("钱币起卦成功，userId={}, benGua={}", userId, context.getBenGua().getGuaName());
        
        return ApiResponse.success(context);
    }
    
    /**
     * 查询剩余起卦次数
     * 
     * <p>
     * 接口：GET /api/divination/times
     * </p>
     * 
     * <p>
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "查询成功",
     *   "data": {
     *     "remaining": 5,
     *     "total": 10,
     *     "vipLevel": "MONTH"
     *   }
     * }
     * </pre>
     * </p>
     * 
     * @return 剩余次数信息
     */
    @GetMapping("/times")
    @RequiresLogin
    @Operation(summary = "查询剩余起卦次数", description = "查询用户今日剩余起卦次数")
    public ApiResponse<Map<String, Object>> getRemainingTimes() {
        
        // 从上下文获取当前用户ID
        Long userId = UserContextHolder.getCurrentUserId();
        
        // 获取剩余次数
        int remaining = divinationService.getRemainingTimes(userId);
        
        // 获取详细统计
        Map<String, Object> stats = divinationService.getDivinationStats(userId);
        
        log.info("查询剩余次数，userId={}, remaining={}", userId, remaining);
        
        return ApiResponse.success("查询成功", stats);
    }
    
    /**
     * 查询起卦统计
     * 
     * <p>
     * 接口：GET /api/divination/stats
     * </p>
     * 
     * <p>
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "查询成功",
     *   "data": {
     *     "todayUsed": 3,
     *     "todayRemaining": 7,
     *     "totalCount": 156,
     *     "lastTime": "2025-10-29T15:30:00"
     *   }
     * }
     * </pre>
     * </p>
     * 
     * @return 起卦统计信息
     */
    @GetMapping("/stats")
    @RequiresLogin
    @Operation(summary = "查询起卦统计", description = "查询用户起卦统计信息")
    public ApiResponse<Map<String, Object>> getDivinationStats() {
        
        // 从上下文获取当前用户ID
        Long userId = UserContextHolder.getCurrentUserId();
        
        // 获取统计信息
        Map<String, Object> stats = divinationService.getDivinationStats(userId);
        
        log.info("查询起卦统计，userId={}, stats={}", userId, stats);
        
        return ApiResponse.success("查询成功", stats);
    }
}

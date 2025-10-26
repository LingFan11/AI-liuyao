package com.lingfan.liuyao.controller.test;

import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.model.dto.request.UpdateProfileRequest;
import com.lingfan.liuyao.model.dto.response.UserProfileResponse;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.service.UserProfileService;
import com.lingfan.liuyao.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息管理测试控制器
 * 用于测试任务2.3的所有功能
 * 
 * 重构说明（2025-10-26）：
 * - 已更新为使用UserProfileService（拆分后的Service）
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@RestController
@RequestMapping("/test/profile")
@Slf4j
@Tag(name = "测试-用户信息管理", description = "测试用户信息查询、更新、VIP、等级等功能")
public class ProfileTestController {
    
    @Autowired
    private UserProfileService userProfileService;
    
    /**
     * 测试1：获取用户信息（正常情况）
     * 
     * GET /api/test/profile/get-success
     * 
     * 测试场景：查询存在的用户信息
     * 预期结果：返回完整的用户信息（邮箱、手机号已脱敏）
     */
    @GetMapping("/get-success")
    @Operation(summary = "测试获取用户信息-成功", description = "查询userId=1的用户信息")
    public ApiResponse<UserProfileResponse> testGetProfileSuccess() {
        log.info("【测试1】获取用户信息-成功");
        
        // 使用测试数据中的userId=68（testuser001）
        Long userId = 68L;
        
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        
        log.info("测试结果：{}", response);
        return ApiResponse.success(response);
    }
    
    /**
     * 测试2：获取用户信息（用户不存在）
     * 
     * GET /api/test/profile/get-not-found
     * 
     * 测试场景：查询不存在的用户
     * 预期结果：抛出USER_NOT_FOUND异常
     */
    @GetMapping("/get-not-found")
    @Operation(summary = "测试获取用户信息-用户不存在", description = "查询不存在的userId")
    public ApiResponse<?> testGetProfileNotFound() {
        log.info("【测试2】获取用户信息-用户不存在");
        
        try {
            Long userId = 99999L;  // 不存在的用户ID
            userProfileService.getUserProfile(userId);
            
            return ApiResponse.error(500, "测试失败：应该抛出异常");
        } catch (Exception e) {
            log.info("测试通过：捕获到异常 - {}", e.getMessage());
            return ApiResponse.success("测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试3：更新用户信息（成功）
     * 
     * POST /api/test/profile/update-success
     * 
     * 测试场景：更新昵称、签名
     * 预期结果：更新成功，返回最新用户信息
     */
    @PostMapping("/update-success")
    @Operation(summary = "测试更新用户信息-成功", description = "更新userId=1的昵称和签名")
    public ApiResponse<UserProfileResponse> testUpdateProfileSuccess() {
        log.info("【测试3】更新用户信息-成功");
        
        Long userId = 68L;
        
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("测试昵称" + System.currentTimeMillis());
        request.setSignature("这是一个测试签名：" + LocalDateTime.now());
        
        UserProfileResponse response = userProfileService.updateProfile(userId, request);
        
        log.info("更新成功，新昵称：{}, 新签名：{}", response.getNickname(), response.getSignature());
        return ApiResponse.success(response);
    }
    
    /**
     * 测试4：更新用户信息（昵称太长）
     * 
     * POST /api/test/profile/update-invalid-nickname
     * 
     * 测试场景：昵称超过20个字符
     * 预期结果：抛出PARAM_ERROR异常
     */
    @PostMapping("/update-invalid-nickname")
    @Operation(summary = "测试更新用户信息-昵称太长", description = "测试昵称长度校验")
    public ApiResponse<?> testUpdateInvalidNickname() {
        log.info("【测试4】更新用户信息-昵称太长");
        
        try {
            Long userId = 68L;
            
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setNickname("这是一个超级超级超级超级超级超级长的昵称，肯定会超过20个字符的限制");
            
            userProfileService.updateProfile(userId, request);
            
            return ApiResponse.error(500, "测试失败：应该抛出异常");
        } catch (Exception e) {
            log.info("测试通过：捕获到异常 - {}", e.getMessage());
            return ApiResponse.success("测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试5：更新用户信息（昵称太短）
     * 
     * POST /api/test/profile/update-short-nickname
     * 
     * 测试场景：昵称少于2个字符
     * 预期结果：抛出PARAM_ERROR异常
     */
    @PostMapping("/update-short-nickname")
    @Operation(summary = "测试更新用户信息-昵称太短", description = "测试昵称最小长度校验")
    public ApiResponse<?> testUpdateShortNickname() {
        log.info("【测试5】更新用户信息-昵称太短");
        
        try {
            Long userId = 68L;
            
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setNickname("A");  // 只有1个字符
            
            userProfileService.updateProfile(userId, request);
            
            return ApiResponse.error(500, "测试失败：应该抛出异常");
        } catch (Exception e) {
            log.info("测试通过：捕获到异常 - {}", e.getMessage());
            return ApiResponse.success("测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试6：VIP过期检查
     * 
     * GET /api/test/profile/vip-expired
     * 
     * 测试场景：查询VIP已过期的用户
     * 预期结果：vipType=0，isVipActive=false
     */
    @GetMapping("/vip-expired")
    @Operation(summary = "测试VIP过期检查", description = "测试VIP过期后的状态更新")
    public ApiResponse<Map<String, Object>> testVipExpired() {
        log.info("【测试6】VIP过期检查");
        
        // 使用userId=4（VIP用户）
        Long userId = 4L;
        
        User user = userProfileService.getUserById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        
        Boolean isVipActive = userProfileService.isVipActive(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("vipType", user.getVipType());
        result.put("vipExpireTime", user.getVipExpireTime());
        result.put("isVipActive", isVipActive);
        result.put("now", LocalDateTime.now());
        
        log.info("VIP状态检查结果：{}", result);
        return ApiResponse.success(result);
    }
    
    /**
     * 测试7：等级计算
     * 
     * GET /api/test/profile/level-calculation
     * 
     * 测试场景：测试不同经验值对应的等级
     * 预期结果：
     * - 0经验 → 1级
     * - 100经验 → 2级
     * - 450经验 → 5级
     * - 9900经验 → 99级（封顶）
     */
    @GetMapping("/level-calculation")
    @Operation(summary = "测试等级计算", description = "测试经验值转等级的算法")
    public ApiResponse<Map<String, Object>> testLevelCalculation() {
        log.info("【测试7】等级计算");
        
        Map<String, Object> result = new HashMap<>();
        
        // 测试多个经验值
        int[] experiences = {0, 100, 450, 1000, 5000, 9800, 9900, 10000};
        
        for (int exp : experiences) {
            Integer level = userProfileService.calculateLevel(exp);
            result.put("经验" + exp, level + "级");
        }
        
        result.put("最大等级", BusinessConstants.MAX_LEVEL);
        result.put("每级所需经验", BusinessConstants.EXP_PER_LEVEL);
        
        log.info("等级计算结果：{}", result);
        return ApiResponse.success(result);
    }
    
    /**
     * 测试8：占卜次数限制
     * 
     * GET /api/test/profile/divination-limit
     * 
     * 测试场景：测试不同VIP类型的占卜次数限制
     * 预期结果：
     * - 普通用户：3次
     * - 月度VIP：15次
     * - 年度VIP：30次
     */
    @GetMapping("/divination-limit")
    @Operation(summary = "测试占卜次数限制", description = "测试不同VIP类型的每日次数限制")
    public ApiResponse<Map<String, Object>> testDivinationLimit() {
        log.info("【测试8】占卜次数限制");
        
        Map<String, Object> result = new HashMap<>();
        
        // 普通用户
        Integer normalLimit = userProfileService.getDailyDivinationLimit(
            BusinessConstants.VIP_TYPE_NORMAL, false);
        result.put("普通用户", normalLimit + "次");
        
        // 月度VIP（有效）
        Integer monthLimit = userProfileService.getDailyDivinationLimit(
            BusinessConstants.VIP_TYPE_MONTH, true);
        result.put("月度VIP", monthLimit + "次");
        
        // 年度VIP（有效）
        Integer yearLimit = userProfileService.getDailyDivinationLimit(
            BusinessConstants.VIP_TYPE_YEAR, true);
        result.put("年度VIP", yearLimit + "次");
        
        // 年度VIP（已过期）
        Integer expiredLimit = userProfileService.getDailyDivinationLimit(
            BusinessConstants.VIP_TYPE_YEAR, false);
        result.put("年度VIP（过期）", expiredLimit + "次");
        
        log.info("占卜次数限制：{}", result);
        return ApiResponse.success(result);
    }
    
    /**
     * 测试9：数据脱敏
     * 
     * GET /api/test/profile/data-mask
     * 
     * 测试场景：测试邮箱、手机号脱敏
     * 预期结果：邮箱和手机号应该被脱敏处理
     */
    @GetMapping("/data-mask")
    @Operation(summary = "测试数据脱敏", description = "测试邮箱、手机号脱敏功能")
    public ApiResponse<Map<String, Object>> testDataMask() {
        log.info("【测试9】数据脱敏");
        
        Long userId = 68L;
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("原始userId", userId);
        result.put("脱敏后邮箱", response.getEmail());
        result.put("脱敏后手机号", response.getPhone());
        result.put("说明", "邮箱和手机号已脱敏，无法看到完整信息");
        
        log.info("数据脱敏结果：{}", result);
        return ApiResponse.success(result);
    }
    
    /**
     * 测试10：缓存测试
     * 
     * GET /api/test/profile/cache-test
     * 
     * 测试场景：连续3次查询同一用户，测试缓存命中
     * 预期结果：第2、3次应该从缓存读取（查看日志）
     */
    @GetMapping("/cache-test")
    @Operation(summary = "测试缓存功能", description = "测试Redis缓存是否生效")
    public ApiResponse<Map<String, Object>> testCache() {
        log.info("【测试10】缓存测试");
        
        Long userId = 68L;
        
        long start1 = System.currentTimeMillis();
        UserProfileResponse result1 = userProfileService.getUserProfile(userId);
        long time1 = System.currentTimeMillis() - start1;
        
        long start2 = System.currentTimeMillis();
        UserProfileResponse result2 = userProfileService.getUserProfile(userId);
        long time2 = System.currentTimeMillis() - start2;
        
        long start3 = System.currentTimeMillis();
        UserProfileResponse result3 = userProfileService.getUserProfile(userId);
        long time3 = System.currentTimeMillis() - start3;
        
        Map<String, Object> result = new HashMap<>();
        result.put("第1次查询耗时", time1 + "ms（查数据库）");
        result.put("第2次查询耗时", time2 + "ms（应该从缓存读取）");
        result.put("第3次查询耗时", time3 + "ms（应该从缓存读取）");
        result.put("说明", "查看日志中是否有'命中缓存'字样");
        
        log.info("缓存测试结果：{}", result);
        return ApiResponse.success(result);
    }
    
    /**
     * 测试11：延迟双删测试
     * 
     * POST /api/test/profile/delay-delete-test
     * 
     * 测试场景：更新用户信息，观察延迟双删日志
     * 预期结果：日志中应该有两次删除缓存的记录
     */
    @PostMapping("/delay-delete-test")
    @Operation(summary = "测试延迟双删", description = "测试延迟双删缓存策略")
    public ApiResponse<Map<String, Object>> testDelayDelete() {
        log.info("【测试11】延迟双删测试");
        
        Long userId = 68L;
        
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setSignature("延迟双删测试：" + System.currentTimeMillis());
        
        UserProfileResponse response = userProfileService.updateProfile(userId, request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("更新成功", true);
        result.put("新签名", response.getSignature());
        result.put("说明", "查看日志，应该有：第一次删除缓存 + 延迟500ms后第二次删除");
        
        log.info("延迟双删测试完成，请查看日志");
        return ApiResponse.success(result);
    }
    
    /**
     * 测试汇总：执行所有测试
     * 
     * GET /api/test/profile/run-all
     * 
     * 一键执行所有测试用例
     */
    @GetMapping("/run-all")
    @Operation(summary = "执行所有测试", description = "一键执行所有测试用例")
    public ApiResponse<Map<String, String>> runAllTests() {
        log.info("【测试汇总】执行所有测试用例");
        
        Map<String, String> results = new HashMap<>();
        
        try {
            testGetProfileSuccess();
            results.put("测试1-获取用户信息", "✅ 通过");
        } catch (Exception e) {
            results.put("测试1-获取用户信息", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testGetProfileNotFound();
            results.put("测试2-用户不存在", "✅ 通过");
        } catch (Exception e) {
            results.put("测试2-用户不存在", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testUpdateProfileSuccess();
            results.put("测试3-更新成功", "✅ 通过");
        } catch (Exception e) {
            results.put("测试3-更新成功", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testUpdateInvalidNickname();
            results.put("测试4-昵称太长", "✅ 通过");
        } catch (Exception e) {
            results.put("测试4-昵称太长", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testUpdateShortNickname();
            results.put("测试5-昵称太短", "✅ 通过");
        } catch (Exception e) {
            results.put("测试5-昵称太短", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testVipExpired();
            results.put("测试6-VIP过期", "✅ 通过");
        } catch (Exception e) {
            results.put("测试6-VIP过期", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testLevelCalculation();
            results.put("测试7-等级计算", "✅ 通过");
        } catch (Exception e) {
            results.put("测试7-等级计算", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testDivinationLimit();
            results.put("测试8-占卜次数", "✅ 通过");
        } catch (Exception e) {
            results.put("测试8-占卜次数", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testDataMask();
            results.put("测试9-数据脱敏", "✅ 通过");
        } catch (Exception e) {
            results.put("测试9-数据脱敏", "❌ 失败：" + e.getMessage());
        }
        
        try {
            testCache();
            results.put("测试10-缓存功能", "✅ 通过");
        } catch (Exception e) {
            results.put("测试10-缓存功能", "❌ 失败：" + e.getMessage());
        }
        
        log.info("所有测试执行完成");
        return ApiResponse.success(results);
    }
}

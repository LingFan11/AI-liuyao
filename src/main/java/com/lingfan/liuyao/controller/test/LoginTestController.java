package com.lingfan.liuyao.controller.test;

import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.service.UserService;
import com.lingfan.liuyao.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录测试控制器
 * 
 * 注意：仅用于开发和测试环境，生产环境需删除或禁用
 * 
 * @author Liuyao Team
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/test/login")
@Slf4j
@Tag(name = "登录测试接口", description = "登录功能测试接口（仅开发环境）")
public class LoginTestController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 测试1：正常登录（用户名）
     * 使用testuser001 + 123456登录
     */
    @PostMapping("/test1-normal-username")
    @Operation(summary = "测试1：正常登录（用户名）", description = "使用testuser001登录")
    public ApiResponse<LoginResponse> test1NormalUsername() {
        log.info("========== 测试1：正常登录（用户名） ==========");
        
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser001");  // 注册时创建的测试用户
        request.setPassword("123456");
        request.setLoginIp("127.0.0.1");
        
        LoginResponse response = userService.login(request);
        log.info("测试1完成：Token={}", response.getToken().substring(0, 20) + "...");
        
        return ApiResponse.success(response);
    }
    
    /**
     * 测试2：正常登录（邮箱）
     * 使用邮箱testuser001@test.com + 123456登录
     */
    @PostMapping("/test2-normal-email")
    @Operation(summary = "测试2：正常登录（邮箱）", description = "使用testuser001@test.com登录")
    public ApiResponse<LoginResponse> test2NormalEmail() {
        log.info("========== 测试2：正常登录（邮箱） ==========");
        
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser001@test.com");
        request.setPassword("123456");
        request.setLoginIp("127.0.0.1");
        
        LoginResponse response = userService.login(request);
        log.info("测试2完成：Token={}", response.getToken().substring(0, 20) + "...");
        
        return ApiResponse.success(response);
    }
    
    /**
     * 测试3：正常登录（手机号）
     * 使用手机号13800138001 + 123456登录
     */
    @PostMapping("/test3-normal-phone")
    @Operation(summary = "测试3：正常登录（手机号）", description = "使用13800138001登录")
    public ApiResponse<LoginResponse> test3NormalPhone() {
        log.info("========== 测试3：正常登录（手机号） ==========");
        
        LoginRequest request = new LoginRequest();
        request.setAccount("13800138001");
        request.setPassword("123456");
        request.setLoginIp("127.0.0.1");
        
        LoginResponse response = userService.login(request);
        log.info("测试3完成：Token={}", response.getToken().substring(0, 20) + "...");
        
        return ApiResponse.success(response);
    }
    
    /**
     * 测试4：用户不存在
     * 使用不存在的用户名登录
     */
    @PostMapping("/test4-user-not-exist")
    @Operation(summary = "测试4：用户不存在", description = "使用不存在的用户名登录，应返回2001错误")
    public ApiResponse<String> test4UserNotExist() {
        log.info("========== 测试4：用户不存在 ==========");
        
        LoginRequest request = new LoginRequest();
        request.setAccount("notexistuser999");
        request.setPassword("123456");
        request.setLoginIp("127.0.0.1");
        
        try {
            userService.login(request);
            return ApiResponse.success("测试失败：应该抛出异常");
        } catch (BusinessException e) {
            log.info("测试4完成：捕获到异常 code={}, message={}", e.getCode(), e.getMessage());
            return ApiResponse.error(e.getCode(), "测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试5：密码错误（触发失败次数累加）
     * 连续5次错误密码，触发账号锁定
     */
    @PostMapping("/test5-wrong-password")
    @Operation(summary = "测试5：密码错误", description = "连续5次错误密码，触发账号锁定")
    public ApiResponse<Map<String, Object>> test5WrongPassword() {
        log.info("========== 测试5：密码错误（触发锁定） ==========");
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("testAccount", "testuser002");
        result.put("attempts", 5);
        
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser002");  // 使用testuser002测试
        request.setPassword("wrongpassword123");
        request.setLoginIp("127.0.0.1");
        
        // 连续5次错误密码
        for (int i = 1; i <= 5; i++) {
            try {
                userService.login(request);
            } catch (BusinessException e) {
                log.info("第{}次登录失败：{}", i, e.getMessage());
                result.put("attempt" + i, "失败：" + e.getMessage());
            }
        }
        
        result.put("status", "账号应已被锁定30分钟");
        log.info("测试5完成：已连续5次密码错误");
        
        return ApiResponse.success(result);
    }
    
    /**
     * 测试6：账号锁定状态
     * 验证测试5执行后账号是否被锁定
     */
    @PostMapping("/test6-account-locked")
    @Operation(summary = "测试6：账号锁定", description = "验证账号锁定状态（需先执行测试5）")
    public ApiResponse<String> test6AccountLocked() {
        log.info("========== 测试6：账号锁定状态 ==========");
        
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser002");
        request.setPassword("123456");  // 正确密码
        request.setLoginIp("127.0.0.1");
        
        try {
            userService.login(request);
            return ApiResponse.success("测试失败：账号应该被锁定");
        } catch (BusinessException e) {
            log.info("测试6完成：捕获到锁定异常 code={}, message={}", e.getCode(), e.getMessage());
            return ApiResponse.error(e.getCode(), "测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试7：VIP用户登录
     * 使用VIP用户登录，验证VIP信息返回
     */
    @PostMapping("/test7-vip-user")
    @Operation(summary = "测试7：VIP用户登录", description = "使用vipuser登录，验证VIP信息")
    public ApiResponse<LoginResponse> test7VipUser() {
        log.info("========== 测试7：VIP用户登录 ==========");
        
        LoginRequest request = new LoginRequest();
        request.setAccount("vipuser");
        request.setPassword("123456");
        request.setLoginIp("127.0.0.1");
        
        LoginResponse response = userService.login(request);
        log.info("测试7完成：VIP类型={}, 等级={}", response.getVipType(), response.getLevel());
        
        return ApiResponse.success(response);
    }
    
    /**
     * 批量测试（测试1-4、7）
     * 注意：测试5-6涉及锁定机制，需单独测试
     */
    @PostMapping("/run-batch-tests")
    @Operation(summary = "批量测试", description = "执行测试1-4、7（不含锁定测试）")
    public ApiResponse<Map<String, Object>> runBatchTests() {
        log.info("========== 批量测试开始 ==========");
        
        Map<String, Object> results = new LinkedHashMap<>();
        
        // 测试1：用户名登录
        try {
            LoginResponse r1 = test1NormalUsername().getData();
            results.put("test1_username", "✅ 通过 - userId=" + r1.getUserId());
        } catch (Exception e) {
            results.put("test1_username", "❌ 失败 - " + e.getMessage());
        }
        
        // 测试2：邮箱登录
        try {
            LoginResponse r2 = test2NormalEmail().getData();
            results.put("test2_email", "✅ 通过 - userId=" + r2.getUserId());
        } catch (Exception e) {
            results.put("test2_email", "❌ 失败 - " + e.getMessage());
        }
        
        // 测试3：手机号登录
        try {
            LoginResponse r3 = test3NormalPhone().getData();
            results.put("test3_phone", "✅ 通过 - userId=" + r3.getUserId());
        } catch (Exception e) {
            results.put("test3_phone", "❌ 失败 - " + e.getMessage());
        }
        
        // 测试4：用户不存在
        try {
            test4UserNotExist();
            results.put("test4_not_exist", "✅ 通过 - 正确抛出异常");
        } catch (Exception e) {
            results.put("test4_not_exist", "✅ 通过 - " + e.getMessage());
        }
        
        // 测试7：VIP用户
        try {
            LoginResponse r7 = test7VipUser().getData();
            results.put("test7_vip", "✅ 通过 - VIP类型=" + r7.getVipType());
        } catch (Exception e) {
            results.put("test7_vip", "❌ 失败 - " + e.getMessage());
        }
        
        results.put("summary", "批量测试完成");
        results.put("note", "测试5-6（锁定机制）需单独执行");
        
        log.info("========== 批量测试完成 ==========");
        return ApiResponse.success(results);
    }
}

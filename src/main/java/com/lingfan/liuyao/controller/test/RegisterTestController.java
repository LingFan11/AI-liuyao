package com.lingfan.liuyao.controller.test;

import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.vo.UserVO;
import com.lingfan.liuyao.service.UserService;
import com.lingfan.liuyao.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注册功能测试控制器
 * 开发阶段使用，生产环境需删除或禁用
 * 
 * 访问路径：http://localhost:8080/api/test/register/*
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@RestController
@RequestMapping("/test/register")
@Slf4j
@Tag(name = "测试-注册", description = "注册功能测试接口（开发阶段）")
public class RegisterTestController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 测试1：正常注册（所有字段）
     * 
     * URL: POST http://localhost:8080/api/test/register/test1-normal-full
     * 
     * 预期结果：注册成功，返回用户信息
     */
    @PostMapping("/test1-normal-full")
    @Operation(summary = "测试1：正常注册（所有字段）")
    public ApiResponse<UserVO> test1NormalFull() {
        log.info("====== 测试1：正常注册（所有字段） ======");
        
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser001");
        request.setPassword("123456");
        request.setEmail("testuser001@test.com");
        request.setPhone("13800138001");
        request.setNickname("测试用户001");
        
        UserVO userVO = userService.register(request);
        log.info("测试1通过：userId={}", userVO.getId());
        
        return ApiResponse.success(userVO);
    }
    
    /**
     * 测试2：正常注册（必填字段，nickname留空）
     * 
     * URL: POST http://localhost:8080/api/test/register/test2-normal-required
     * 
     * 预期结果：注册成功，nickname默认使用username
     */
    @PostMapping("/test2-normal-required")
    @Operation(summary = "测试2：正常注册（必填字段）")
    public ApiResponse<UserVO> test2NormalRequired() {
        log.info("====== 测试2：正常注册（必填字段） ======");
        
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser002");
        request.setPassword("123456");
        request.setEmail("testuser002@test.com");
        request.setPhone("13800138002");
        // nickname为空，应默认使用username
        
        UserVO userVO = userService.register(request);
        log.info("测试2通过：userId={}, nickname={}", userVO.getId(), userVO.getNickname());
        
        return ApiResponse.success(userVO);
    }
    
    /**
     * 测试3：边界输入 - 最短用户名和密码
     * 
     * URL: POST http://localhost:8080/api/test/register/test3-boundary-min
     * 
     * 预期结果：注册成功
     */
    @PostMapping("/test3-boundary-min")
    @Operation(summary = "测试3：边界输入（最短）")
    public ApiResponse<UserVO> test3BoundaryMin() {
        log.info("====== 测试3：边界输入（最短） ======");
        
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abc");  // 3个字符（最短）
        request.setPassword("123456");  // 6个字符（最短）
        request.setEmail("abc@test.com");
        request.setPhone("13800138003");
        
        UserVO userVO = userService.register(request);
        log.info("测试3通过：userId={}, username={}", userVO.getId(), userVO.getUsername());
        
        return ApiResponse.success(userVO);
    }
    
    /**
     * 测试4：边界输入 - 最长用户名和密码
     * 
     * URL: POST http://localhost:8080/api/test/register/test4-boundary-max
     * 
     * 预期结果：注册成功
     */
    @PostMapping("/test4-boundary-max")
    @Operation(summary = "测试4：边界输入（最长）")
    public ApiResponse<UserVO> test4BoundaryMax() {
        log.info("====== 测试4：边界输入（最长） ======");
        
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(50));  // 50个字符（最长）
        request.setPassword("a".repeat(20));  // 20个字符（最长）
        request.setEmail("longusername@test.com");
        request.setPhone("13800138004");
        
        UserVO userVO = userService.register(request);
        log.info("测试4通过：userId={}, usernameLength={}", 
            userVO.getId(), userVO.getUsername().length());
        
        return ApiResponse.success(userVO);
    }
    
    /**
     * 测试5：异常输入 - 用户名重复
     * 
     * URL: POST http://localhost:8080/api/test/register/test5-duplicate-username
     * 
     * 预期结果：抛出异常USER_ALREADY_EXISTS (2002)
     */
    @PostMapping("/test5-duplicate-username")
    @Operation(summary = "测试5：异常输入（用户名重复）")
    public ApiResponse<String> test5DuplicateUsername() {
        log.info("====== 测试5：异常输入（用户名重复） ======");
        
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser001");  // 与test1重复
            request.setPassword("123456");
            request.setEmail("duplicate1@test.com");
            request.setPhone("13800138005");
            
            userService.register(request);
            
            log.error("测试5失败：应该抛出异常但没有");
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "测试失败：应该抛出异常但没有");
            
        } catch (BusinessException e) {
            log.info("测试5通过：正确抛出异常，code={}, message={}", 
                e.getCode(), e.getMessage());
            return ApiResponse.success("测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试6：异常输入 - 邮箱重复
     * 
     * URL: POST http://localhost:8080/api/test/register/test6-duplicate-email
     * 
     * 预期结果：抛出异常EMAIL_ALREADY_EXISTS (2015)
     */
    @PostMapping("/test6-duplicate-email")
    @Operation(summary = "测试6：异常输入（邮箱重复）")
    public ApiResponse<String> test6DuplicateEmail() {
        log.info("====== 测试6：异常输入（邮箱重复） ======");
        
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser_new1");
            request.setPassword("123456");
            request.setEmail("testuser001@test.com");  // 与test1重复
            request.setPhone("13800138006");
            
            userService.register(request);
            
            log.error("测试6失败：应该抛出异常但没有");
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "测试失败：应该抛出异常但没有");
            
        } catch (BusinessException e) {
            log.info("测试6通过：正确抛出异常，code={}, message={}", 
                e.getCode(), e.getMessage());
            return ApiResponse.success("测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 测试7：异常输入 - 手机号重复
     * 
     * URL: POST http://localhost:8080/api/test/register/test7-duplicate-phone
     * 
     * 预期结果：抛出异常PHONE_ALREADY_EXISTS (2016)
     */
    @PostMapping("/test7-duplicate-phone")
    @Operation(summary = "测试7：异常输入（手机号重复）")
    public ApiResponse<String> test7DuplicatePhone() {
        log.info("====== 测试7：异常输入（手机号重复） ======");
        
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser_new2");
            request.setPassword("123456");
            request.setEmail("testnew2@test.com");
            request.setPhone("13800138001");  // 与test1重复
            
            userService.register(request);
            
            log.error("测试7失败：应该抛出异常但没有");
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "测试失败：应该抛出异常但没有");
            
        } catch (BusinessException e) {
            log.info("测试7通过：正确抛出异常，code={}, message={}", 
                e.getCode(), e.getMessage());
            return ApiResponse.success("测试通过：" + e.getMessage());
        }
    }
    
    /**
     * 批量测试：执行所有测试用例
     * 
     * URL: POST http://localhost:8080/api/test/register/run-all-tests
     * 
     * 预期结果：返回所有测试结果
     */
    @PostMapping("/run-all-tests")
    @Operation(summary = "批量测试：执行所有测试用例")
    public ApiResponse<Map<String, String>> runAllTests() {
        log.info("====== 批量测试：执行所有测试用例 ======");
        
        Map<String, String> results = new LinkedHashMap<>();
        
        // 测试1
        try {
            test1NormalFull();
            results.put("test1-normal-full", "✅ 通过");
        } catch (Exception e) {
            results.put("test1-normal-full", "❌ 失败：" + e.getMessage());
        }
        
        // 测试2
        try {
            test2NormalRequired();
            results.put("test2-normal-required", "✅ 通过");
        } catch (Exception e) {
            results.put("test2-normal-required", "❌ 失败：" + e.getMessage());
        }
        
        // 测试3
        try {
            test3BoundaryMin();
            results.put("test3-boundary-min", "✅ 通过");
        } catch (Exception e) {
            results.put("test3-boundary-min", "❌ 失败：" + e.getMessage());
        }
        
        // 测试4
        try {
            test4BoundaryMax();
            results.put("test4-boundary-max", "✅ 通过");
        } catch (Exception e) {
            results.put("test4-boundary-max", "❌ 失败：" + e.getMessage());
        }
        
        // 测试5
        try {
            ApiResponse<String> response = test5DuplicateUsername();
            if ((response.getData() != null && response.getData().contains("测试通过")) || 
                (response.getMessage() != null && response.getMessage().contains("测试通过"))) {
                results.put("test5-duplicate-username", "✅ 通过");
            } else {
                results.put("test5-duplicate-username", "❌ 失败");
            }
        } catch (Exception e) {
            results.put("test5-duplicate-username", "❌ 失败：" + e.getMessage());
        }
        
        // 测试6
        try {
            ApiResponse<String> response = test6DuplicateEmail();
            if ((response.getData() != null && response.getData().contains("测试通过")) || 
                (response.getMessage() != null && response.getMessage().contains("测试通过"))) {
                results.put("test6-duplicate-email", "✅ 通过");
            } else {
                results.put("test6-duplicate-email", "❌ 失败");
            }
        } catch (Exception e) {
            results.put("test6-duplicate-email", "❌ 失败：" + e.getMessage());
        }
        
        // 测试7
        try {
            ApiResponse<String> response = test7DuplicatePhone();
            if ((response.getData() != null && response.getData().contains("测试通过")) || 
                (response.getMessage() != null && response.getMessage().contains("测试通过"))) {
                results.put("test7-duplicate-phone", "✅ 通过");
            } else {
                results.put("test7-duplicate-phone", "❌ 失败");
            }
        } catch (Exception e) {
            results.put("test7-duplicate-phone", "❌ 失败：" + e.getMessage());
        }
        
        log.info("批量测试完成，results={}", results);
        return ApiResponse.success(results);
    }
}

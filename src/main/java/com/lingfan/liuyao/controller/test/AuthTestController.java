package com.lingfan.liuyao.controller.test;

import com.lingfan.liuyao.annotation.RequiresLogin;
import com.lingfan.liuyao.annotation.RequiresPermissions;
import com.lingfan.liuyao.annotation.RequiresRoles;
import com.lingfan.liuyao.utils.ApiResponse;
import com.lingfan.liuyao.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证拦截器测试控制器
 * 
 * 测试场景：
 * 1. 公开接口（无注解）
 * 2. 需要登录（@RequiresLogin）
 * 3. 需要角色（@RequiresRoles）
 * 4. 需要权限（@RequiresPermissions）
 * 5. 组合注解
 * 
 * 测试方法：
 * 1. 不携带Token访问 → 401
 * 2. 携带Token访问 → 根据角色/权限返回200或403
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@RestController
@RequestMapping("/test/auth")
@Slf4j
public class AuthTestController {
    
    /**
     * 测试1：公开接口（无需登录）
     * 预期：任何人都可以访问
     * 
     * 测试命令：
     * curl http://localhost:8080/api/test/auth/public
     */
    @GetMapping("/public")
    public ApiResponse<String> publicEndpoint() {
        log.info("公开接口被访问");
        return ApiResponse.success("公开接口，无需登录即可访问");
    }
    
    /**
     * 测试2：需要登录
     * 预期：必须携带有效Token，返回当前用户信息
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/login-required
     */
    @GetMapping("/login-required")
    @RequiresLogin
    public ApiResponse<Map<String, Object>> loginRequired() {
        Long userId = UserContextHolder.getCurrentUserId();
        String username = UserContextHolder.getCurrentUsername();
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "登录验证通过");
        data.put("userId", userId);
        data.put("username", username);
        data.put("roles", UserContextHolder.getCurrentUserRoles());
        data.put("permissions", UserContextHolder.getCurrentUserPermissions());
        
        log.info("登录验证通过：userId={}, username={}", userId, username);
        return ApiResponse.success(data);
    }
    
    /**
     * 测试3：需要VIP会员角色
     * 预期：普通用户返回403，VIP会员返回200
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/vip-only
     */
    @GetMapping("/vip-only")
    @RequiresRoles({"vip_month", "vip_year"})
    public ApiResponse<Map<String, Object>> vipOnly() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "VIP专属功能");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("roles", UserContextHolder.getCurrentUserRoles());
        
        log.info("VIP功能访问成功：userId={}", UserContextHolder.getCurrentUserId());
        return ApiResponse.success(data);
    }
    
    /**
     * 测试4：需要管理员角色
     * 预期：非管理员返回403，管理员返回200
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/admin-only
     */
    @GetMapping("/admin-only")
    @RequiresRoles({"admin"})
    public ApiResponse<Map<String, Object>> adminOnly() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "管理员功能");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("username", UserContextHolder.getCurrentUsername());
        data.put("roles", UserContextHolder.getCurrentUserRoles());
        
        log.info("管理员功能访问成功：userId={}", UserContextHolder.getCurrentUserId());
        return ApiResponse.success(data);
    }
    
    /**
     * 测试5：需要权限（OR逻辑）
     * 预期：拥有user:create或user:update权限之一即可访问
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/permission-or
     */
    @GetMapping("/permission-or")
    @RequiresPermissions(
        value = {"user:create", "user:update"},
        logical = RequiresPermissions.Logical.OR
    )
    public ApiResponse<Map<String, Object>> permissionOr() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "OR权限测试通过");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("requiredPermissions", new String[]{"user:create", "user:update"});
        data.put("userPermissions", UserContextHolder.getCurrentUserPermissions());
        
        log.info("OR权限测试通过：userId={}", UserContextHolder.getCurrentUserId());
        return ApiResponse.success(data);
    }
    
    /**
     * 测试6：需要权限（AND逻辑）
     * 预期：必须同时拥有user:create、user:update、user:delete权限
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/permission-and
     */
    @GetMapping("/permission-and")
    @RequiresPermissions(
        value = {"user:create", "user:update", "user:delete"},
        logical = RequiresPermissions.Logical.AND
    )
    public ApiResponse<Map<String, Object>> permissionAnd() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "AND权限测试通过");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("requiredPermissions", new String[]{"user:create", "user:update", "user:delete"});
        data.put("userPermissions", UserContextHolder.getCurrentUserPermissions());
        
        log.info("AND权限测试通过：userId={}", UserContextHolder.getCurrentUserId());
        return ApiResponse.success(data);
    }
    
    /**
     * 测试7：组合注解（登录+角色）
     * 预期：必须登录且拥有admin或vip_year角色
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/combined
     */
    @GetMapping("/combined")
    @RequiresLogin
    @RequiresRoles({"admin", "vip_year"})
    public ApiResponse<Map<String, Object>> combined() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "组合权限测试通过");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("username", UserContextHolder.getCurrentUsername());
        data.put("roles", UserContextHolder.getCurrentUserRoles());
        data.put("permissions", UserContextHolder.getCurrentUserPermissions());
        
        log.info("组合权限测试通过：userId={}, roles={}", 
            UserContextHolder.getCurrentUserId(),
            UserContextHolder.getCurrentUserRoles());
        return ApiResponse.success(data);
    }
    
    /**
     * 测试8：高级解卦权限
     * 预期：必须拥有interpretation:advanced权限（仅年度VIP）
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/advanced-interpretation
     */
    @GetMapping("/advanced-interpretation")
    @RequiresPermissions({"interpretation:advanced"})
    public ApiResponse<Map<String, Object>> advancedInterpretation() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "高级解卦功能访问成功");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("username", UserContextHolder.getCurrentUsername());
        data.put("hasAdvancedPermission", UserContextHolder.hasPermission("interpretation:advanced"));
        
        log.info("高级解卦功能访问成功：userId={}", UserContextHolder.getCurrentUserId());
        return ApiResponse.success(data);
    }
    
    /**
     * 测试9：查看所有占卜记录（管理员专用）
     * 预期：必须拥有divination:view_all权限（仅管理员）
     * 
     * 测试命令：
     * curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/test/auth/view-all-divinations
     */
    @GetMapping("/view-all-divinations")
    @RequiresPermissions({"divination:view_all"})
    public ApiResponse<Map<String, Object>> viewAllDivinations() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "管理员查看所有占卜记录");
        data.put("userId", UserContextHolder.getCurrentUserId());
        data.put("isAdmin", UserContextHolder.hasRole("admin"));
        
        log.info("管理员查看所有占卜记录：userId={}", UserContextHolder.getCurrentUserId());
        return ApiResponse.success(data);
    }
}

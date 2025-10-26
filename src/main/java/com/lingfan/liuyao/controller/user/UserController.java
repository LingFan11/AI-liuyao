package com.lingfan.liuyao.controller.user;

import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.dto.request.UpdateProfileRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.model.dto.response.UserProfileResponse;
import com.lingfan.liuyao.model.vo.UserVO;
import com.lingfan.liuyao.service.UserService;
import com.lingfan.liuyao.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 * 负责用户相关接口（注册、登录、信息管理等）
 * 
 * 注意：URL已配置context-path: /api，这里不需要重复
 * 实际访问路径：http://localhost:8080/api/user/register
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 验证码开关配置
    @Value("${liuyao.user.email-verify-enabled:false}")
    private boolean emailVerifyEnabled;
    
    @Value("${liuyao.user.phone-verify-enabled:false}")
    private boolean phoneVerifyEnabled;
    
    /**
     * 用户注册
     * 
     * 接口：POST /api/user/register
     * 
     * 请求体示例：
     * {
     *   "username": "zhangsan",
     *   "password": "123456",
     *   "email": "zhangsan@test.com",
     *   "phone": "13800138000",
     *   "nickname": "张三"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "id": 1,
     *     "username": "zhangsan",
     *     "nickname": "张三",
     *     "level": 1,
     *     "experience": 0,
     *     ...
     *   }
     * }
     * 
     * @param request 注册请求（自动校验）
     * @return UserVO（不包含敏感信息）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        log.info("接收注册请求，username={}, email={}, phone={}", 
            request.getUsername(), request.getEmail(), request.getPhone());
        
        // 调用Service进行注册
        UserVO userVO = userService.register(request);
        
        log.info("注册成功，userId={}, username={}", userVO.getId(), userVO.getUsername());
        return ApiResponse.success(userVO);
    }
    
    /**
     * 检查用户名是否可用
     * 
     * 接口：GET /api/user/check-username?username=zhangsan
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": true   // true=可用，false=已存在
     * }
     * 
     * @param username 用户名
     * @return true=可用, false=已存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        log.debug("检查用户名是否可用，username={}", username);
        
        boolean exists = userService.isUsernameExists(username);
        boolean available = !exists;  // 返回是否可用（取反）
        
        log.debug("用户名检查结果，username={}, available={}", username, available);
        return ApiResponse.success(available);
    }
    
    /**
     * 检查邮箱是否可用
     * 
     * 接口：GET /api/user/check-email?email=test@test.com
     * 
     * @param email 邮箱
     * @return true=可用, false=已存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        log.debug("检查邮箱是否可用，email={}", email);
        
        boolean exists = userService.isEmailExists(email);
        boolean available = !exists;
        
        log.debug("邮箱检查结果，email={}, available={}", email, available);
        return ApiResponse.success(available);
    }
    
    /**
     * 检查手机号是否可用
     * 
     * 接口：GET /api/user/check-phone?phone=13800138000
     * 
     * @param phone 手机号
     * @return true=可用, false=已存在
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已存在")
    public ApiResponse<Boolean> checkPhone(@RequestParam String phone) {
        log.debug("检查手机号是否可用，phone={}", phone);
        
        boolean exists = userService.isPhoneExists(phone);
        boolean available = !exists;
        
        log.debug("手机号检查结果，phone={}, available={}", phone, available);
        return ApiResponse.success(available);
    }
    
    // ==================== 登录功能 ====================
    
    /**
     * 用户登录
     * 
     * 接口：POST /api/user/login
     * 
     * 请求体示例：
     * {
     *   "account": "testuser001",     // 支持用户名/邮箱/手机号
     *   "password": "123456"
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "userId": 1,
     *     "username": "testuser001",
     *     "nickname": "测试用户",
     *     "avatar": null,
     *     "level": 1,
     *     "experience": 0,
     *     "vipType": 0,
     *     "vipExpireTime": null,
     *     "loginTime": "2025-10-24 08:30:00"
     *   }
     * }
     * 
     * 功能说明：
     * 1. 支持三种登录方式：用户名/邮箱/手机号
     * 2. 登录失败5次锁定账号30分钟
     * 3. 返回JWT Token用于后续请求认证
     * 4. 记录登录时间和IP
     * 
     * @param request 登录请求（account、password）
     * @param httpRequest HttpServletRequest（获取真实IP）
     * @return LoginResponse（Token + 用户基本信息）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名/邮箱/手机号登录，返回JWT Token")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("接收登录请求，account={}", request.getAccount());
        
        // 获取真实IP（支持代理）
        String realIp = getRealIp(httpRequest);
        request.setLoginIp(realIp);
        
        // 调用Service进行登录
        LoginResponse response = userService.login(request);
        
        log.info("登录成功，userId={}, username={}", response.getUserId(), response.getUsername());
        return ApiResponse.success(response);
    }
    
    /**
     * 获取真实IP地址
     * 支持Nginx等反向代理
     * 
     * @param request HttpServletRequest
     * @return 真实IP地址
     */
    private String getRealIp(HttpServletRequest request) {
        // 尝试从X-Real-IP获取
        String ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        // 尝试从X-Forwarded-For获取
        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }
        
        // 直接获取RemoteAddr
        ip = request.getRemoteAddr();
        return ip != null ? ip : "unknown";
    }
    
    // ==================== 预留接口：发送验证码（后期实现） ====================
    
    /**
     * 发送邮箱验证码
     * TODO: 后期实现
     * 
     * 接口：POST /api/user/send-email-code
     * 请求参数：email=test@test.com
     * 
     * @param email 邮箱
     * @return 成功消息
     */
    @PostMapping("/send-email-code")
    @Operation(summary = "发送邮箱验证码", description = "注册时发送邮箱验证码（后期启用）")
    public ApiResponse<String> sendEmailCode(@RequestParam String email) {
        if (!emailVerifyEnabled) {
            log.warn("邮箱验证功能未启用，email={}", email);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "邮箱验证功能未启用");
        }
        
        log.info("发送邮箱验证码，email={}", email);
        
        // TODO: 调用Service发送邮箱验证码
        // userService.sendEmailCode(email);
        
        return ApiResponse.success("验证码已发送到邮箱（功能待实现）");
    }
    
    /**
     * 发送手机验证码
     * TODO: 后期实现
     * 
     * 接口：POST /api/user/send-phone-code
     * 请求参数：phone=13800138000
     * 
     * @param phone 手机号
     * @return 成功消息
     */
    @PostMapping("/send-phone-code")
    @Operation(summary = "发送手机验证码", description = "注册时发送手机验证码（后期启用）")
    public ApiResponse<String> sendPhoneCode(@RequestParam String phone) {
        if (!phoneVerifyEnabled) {
            log.warn("手机验证功能未启用，phone={}", phone);
            return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "手机验证功能未启用");
        }
        
        log.info("发送手机验证码，phone={}", phone);
        
        // TODO: 调用Service发送手机验证码
        // userService.sendPhoneCode(phone);
        
        return ApiResponse.success("验证码已发送到手机（功能待实现）");
    }
    
    // ==================== 用户信息管理 ====================
    
    /**
     * 获取当前用户信息
     * 
     * 接口：GET /api/user/profile
     * Header: Authorization: Bearer {token}
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "userId": 1,
     *     "username": "testuser001",
     *     "email": "tes***@qq.com",          // 脱敏
     *     "phone": "138****5678",            // 脱敏
     *     "nickname": "测试用户",
     *     "avatar": "/files/avatars/1/xxx.jpg",
     *     "signature": "万事皆可问卦",
     *     "level": 5,
     *     "experience": 450,
     *     "nextLevelExp": 600,
     *     "vipType": 1,
     *     "vipExpireTime": "2025-11-26 00:00:00",
     *     "isVipActive": true,
     *     "dailyDivinationLimit": 15,
     *     "dailyDivinationCount": 3,
     *     "remainingDivinationCount": 12,
     *     "totalDivinationCount": 58,
     *     "status": 0,
     *     "lastLoginTime": "2025-10-26 18:00:00",
     *     "createdAt": "2025-10-20 10:00:00"
     *   }
     * }
     * 
     * @param request HttpServletRequest（从Header获取Token）
     * @return 用户详细信息（脱敏处理）
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserProfileResponse> getProfile() {
        // 从SecurityContext中获取userId（由JWT拦截器已验证并存入）
        Long userId = getCurrentUserId();
        
        log.info("获取用户信息，userId={}", userId);
        
        UserProfileResponse response = userService.getUserProfile(userId);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 更新用户信息
     * 
     * 接口：PUT /api/user/profile
     * Header: Authorization: Bearer {token}
     * 
     * 请求体示例：
     * {
     *   "nickname": "新昵称",
     *   "avatar": "/files/avatars/1/avatar_123456.jpg",
     *   "signature": "新的个性签名"
     * }
     * 
     * 注意：所有字段都是可选的，只更新非空字段
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     // 返回更新后的完整用户信息
     *   }
     * }
     * 
     * @param updateRequest 更新请求
     * @param request HttpServletRequest
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新昵称、头像、个性签名")
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateRequest) {
        
        // 从SecurityContext中获取userId
        Long userId = getCurrentUserId();
        
        log.info("更新用户信息，userId={}, request={}", userId, updateRequest);
        
        UserProfileResponse response = userService.updateProfile(userId, updateRequest);
        
        log.info("用户信息更新成功，userId={}", userId);
        return ApiResponse.success(response);
    }
    
    /**
     * 上传头像
     * 
     * 接口：POST /api/user/avatar
     * Header: Authorization: Bearer {token}
     * Content-Type: multipart/form-data
     * 
     * 表单参数：
     * - file: 头像文件（jpg/png/gif，最大2MB）
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": "/files/avatars/1/1_1698765432_abc123.jpg"
     * }
     * 
     * @param file 头像文件
     * @param request HttpServletRequest
     * @return 头像URL
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像（支持jpg/png/gif，最大2MB）")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        
        // 从SecurityContext中获取userId
        Long userId = getCurrentUserId();
        
        log.info("上传头像，userId={}, fileName={}", userId, file.getOriginalFilename());
        
        String avatarUrl = userService.uploadAvatar(userId, file);
        
        log.info("头像上传成功，userId={}, avatarUrl={}", userId, avatarUrl);
        return ApiResponse.success(avatarUrl);
    }
    
    /**
     * 从SecurityContext中获取当前登录用户的userId
     * 
     * 说明：JWT拦截器已经验证Token并将userId存入SecurityContext
     * 这里直接读取即可，无需重复解析Token
     * 
     * @return userId
     * @throws com.lingfan.liuyao.exception.BusinessException 未登录或认证失败
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("SecurityContext中无认证信息，用户未登录");
            throw new com.lingfan.liuyao.exception.BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        
        // JWT拦截器存入的是userId（Long类型）
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        log.error("SecurityContext中的principal类型错误：{}", principal.getClass());
        throw new com.lingfan.liuyao.exception.BusinessException(ErrorCode.SYSTEM_ERROR, "认证信息异常");
    }
}

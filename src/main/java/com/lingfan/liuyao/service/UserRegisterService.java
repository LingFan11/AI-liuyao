package com.lingfan.liuyao.service;

import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.vo.UserVO;

/**
 * 用户注册服务接口
 * 
 * 职责：
 * - 用户注册
 * - 用户名/邮箱/手机号重复检查
 * - 验证码校验（后期启用）
 * 
 * 重构说明（2025-10-26）：
 * - 从UserService拆分出来，遵循单一职责原则
 * - 降低单个Service的复杂度
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
public interface UserRegisterService {
    
    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return UserVO（不包含敏感信息）
     */
    UserVO register(RegisterRequest request);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return true=已存在, false=不存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return true=已存在, false=不存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return true=已存在, false=不存在
     */
    boolean isPhoneExists(String phone);
}

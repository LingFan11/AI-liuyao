package com.lingfan.liuyao.service;

import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.model.entity.User;

/**
 * 用户认证服务接口
 * 
 * 职责：
 * - 用户登录
 * - 登录失败次数限制
 * - 账号锁定机制
 * - JWT Token生成
 * - 用户会话管理
 * 
 * 重构说明（2025-10-26）：
 * - 从UserService拆分出来，遵循单一职责原则
 * - 专注于认证相关功能
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
public interface UserAuthService {
    
    /**
     * 用户登录
     * 
     * @param request 登录请求（包含account、password、loginIp）
     * @return LoginResponse（Token + 用户基本信息）
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 根据账号查询用户（支持用户名/邮箱/手机号）
     * 
     * @param account 账号（用户名/邮箱/手机号）
     * @return User实体
     */
    User getUserByAccount(String account);
}

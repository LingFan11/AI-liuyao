package com.lingfan.liuyao.service;

import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.model.vo.UserVO;

/**
 * 用户服务接口
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
public interface UserService {
    
    /**
     * 用户注册
     * 
     * 业务流程：
     * 1. 校验验证码（如果启用）
     * 2. 检查用户名、邮箱、手机号是否已存在
     * 3. 加密密码（BCrypt）
     * 4. 初始化用户信息
     * 5. 保存到数据库
     * 6. 缓存用户信息到Redis
     * 7. 返回UserVO（不包含敏感信息）
     * 
     * @param request 注册请求
     * @return 用户VO（不包含敏感信息）
     * @throws BusinessException 用户名重复、邮箱重复、手机号重复、验证码错误等
     */
    UserVO register(RegisterRequest request);
    
    /**
     * 检查用户名是否存在
     * 
     * 缓存策略：先查Redis，未命中再查DB，结果缓存5分钟
     * 
     * @param username 用户名
     * @return true=存在, false=不存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * 缓存策略：先查Redis，未命中再查DB，结果缓存5分钟
     * 
     * @param email 邮箱
     * @return true=存在, false=不存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 检查手机号是否存在
     * 
     * 缓存策略：先查Redis，未命中再查DB，结果缓存5分钟
     * 
     * @param phone 手机号
     * @return true=存在, false=不存在
     */
    boolean isPhoneExists(String phone);
    
    /**
     * 用户登录
     * 
     * 业务流程：
     * 1. 检查Redis锁定状态（登录失败5次锁定30分钟）
     * 2. 根据账号查询用户（支持用户名/邮箱/手机号）
     * 3. 检查账号状态（正常/锁定/禁用）
     * 4. 验证密码（BCrypt）
     * 5. 密码错误：累加失败次数，5次后锁定账号
     * 6. 密码正确：
     *    - 生成JWT Token
     *    - 更新登录信息（lastLoginTime、lastLoginIp、loginFailedCount=0）
     *    - 缓存用户信息到Redis
     *    - 存储会话信息（用户在线状态）
     * 7. 返回LoginResponse（Token + 用户信息）
     * 
     * 异常处理：
     * - USER_NOT_FOUND: 用户不存在
     * - ACCOUNT_LOCKED: 账号已锁定（数据库或Redis）
     * - ACCOUNT_DISABLED: 账号已禁用
     * - USERNAME_PASSWORD_ERROR: 密码错误
     * 
     * @param request 登录请求（account、password、loginIp）
     * @return 登录响应（Token + 用户基本信息）
     * @throws BusinessException 各种登录异常
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 根据账号查询用户（支持用户名/邮箱/手机号）
     * 
     * 自动判断账号类型：
     * - 包含@符号 → 邮箱
     * - 符合手机号正则 → 手机号
     * - 其他 → 用户名
     * 
     * @param account 账号（用户名/邮箱/手机号）
     * @return 用户信息，不存在返回null
     */
    User getUserByAccount(String account);
}

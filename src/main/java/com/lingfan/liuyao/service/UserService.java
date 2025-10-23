package com.lingfan.liuyao.service;

import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
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
}

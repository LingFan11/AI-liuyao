package com.lingfan.liuyao.service;

import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.dto.request.UpdateProfileRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.model.dto.response.UserProfileResponse;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

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
    
    // ==================== 用户信息管理 ====================
    
    /**
     * 获取用户详细信息
     * 
     * 业务流程：
     * 1. 先查Redis缓存（user:info:userId）
     * 2. 缓存未命中则查数据库
     * 3. 检查VIP是否过期，过期则更新状态
     * 4. 计算用户等级和剩余次数
     * 5. 缓存到Redis（30分钟）
     * 6. 返回UserProfileResponse（脱敏处理）
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     * @throws BusinessException 用户不存在时抛出异常
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 更新用户信息
     * 
     * 业务流程：
     * 1. 校验参数（昵称长度、签名长度等）
     * 2. 只更新非空字段
     * 3. 延迟双删缓存（先删缓存 → 更新DB → 延迟500ms再删）
     * 4. 返回最新用户信息
     * 
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     * @throws BusinessException 参数不合法或用户不存在时抛出异常
     */
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * 上传头像
     * 
     * 业务流程：
     * 1. 校验文件类型（jpg/png/gif）
     * 2. 校验文件大小（最大2MB）
     * 3. 生成唯一文件名（userId_timestamp.ext）
     * 4. 保存到本地存储
     * 5. 返回访问URL
     * 6. 更新用户表avatar字段
     * 7. 删除旧头像文件（可选）
     * 
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像访问URL
     * @throws BusinessException 文件类型或大小不符合要求时抛出异常
     */
    String uploadAvatar(Long userId, MultipartFile file);
    
    /**
     * 根据ID查询用户（带缓存）
     * 
     * 缓存策略：先查Redis，未命中再查DB，结果缓存30分钟
     * 
     * @param userId 用户ID
     * @return 用户信息，不存在返回null
     */
    User getUserById(Long userId);
    
    /**
     * 计算用户等级
     * 
     * 算法：level = experience / 100 + 1，最大99级
     * 
     * @param experience 经验值
     * @return 等级（1-99）
     */
    Integer calculateLevel(Integer experience);
    
    /**
     * 获取每日占卜次数限制
     * 
     * - 普通用户：3次
     * - 月度VIP：15次
     * - 年度VIP：30次
     * - VIP过期：3次
     * 
     * @param vipType VIP类型
     * @param isVipActive VIP是否有效
     * @return 每日次数限制
     */
    Integer getDailyDivinationLimit(Integer vipType, Boolean isVipActive);
    
    /**
     * 检查VIP是否有效
     * 
     * 业务逻辑：
     * 1. vipType=0（普通用户）返回false
     * 2. vipExpireTime为null返回false
     * 3. vipExpireTime < now返回false并更新数据库
     * 
     * @param user 用户对象
     * @return true=VIP有效, false=已过期或非VIP
     */
    Boolean isVipActive(User user);
}

package com.lingfan.liuyao.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.dto.request.UpdateProfileRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.model.dto.response.UserProfileResponse;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.model.vo.UserVO;
import com.lingfan.liuyao.service.UserService;
import com.lingfan.liuyao.utils.JwtUtil;
import com.lingfan.liuyao.utils.PasswordEncoder;
import com.lingfan.liuyao.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 
 * Redis缓存策略：
 * 1. 分布式锁防并发注册（executeWithLock）
 * 2. 随机TTL防缓存雪崩（setWithRandomExpire）
 * 3. 空值缓存防缓存穿透（setNull）
 * 4. 事务提交后缓存（避免脏数据）
 * 5. 降级策略（Redis异常时仍可注册）
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // ==================== 配置项 ====================
    
    /**
     * 是否启用邮箱验证码（默认false）
     */
    @Value("${liuyao.user.email-verify-enabled:false}")
    private boolean emailVerifyEnabled;
    
    /**
     * 是否启用手机验证码（默认false）
     */
    @Value("${liuyao.user.phone-verify-enabled:false}")
    private boolean phoneVerifyEnabled;
    
    // ==================== 用户注册 ====================
    
    /**
     * 用户注册
     * 使用分布式锁防止并发注册同一用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterRequest request) {
        log.info("开始注册用户，username={}, email={}, phone={}", 
            request.getUsername(), request.getEmail(), request.getPhone());
        
        // 使用分布式锁防止并发注册（自动管理锁）
        String lockKey = CacheConstants.REGISTER_LOCK_PREFIX + request.getUsername();
        
        return redisUtil.executeWithLock(lockKey, CacheConstants.REGISTER_LOCK_TTL, TimeUnit.SECONDS, () -> {
            
            // ========== 预留：验证码校验（后期启用） ==========
            if (emailVerifyEnabled) {
                validateEmailCode(request.getEmail(), request.getEmailCode());
            } else {
                log.debug("邮箱验证码校验已禁用，跳过");
            }
            
            if (phoneVerifyEnabled) {
                validatePhoneCode(request.getPhone(), request.getPhoneCode());
            } else {
                log.debug("手机验证码校验已禁用，跳过");
            }
            // ====================================================
            
            // 1. 双重检查用户名是否存在
            if (isUsernameExists(request.getUsername())) {
                log.warn("用户名已存在，username={}", request.getUsername());
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, 
                    "用户名已存在：" + request.getUsername());
            }
            
            // 2. 检查邮箱是否存在
            if (isEmailExists(request.getEmail())) {
                log.warn("邮箱已存在，email={}", request.getEmail());
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, 
                    "邮箱已存在：" + request.getEmail());
            }
            
            // 3. 检查手机号是否存在
            if (isPhoneExists(request.getPhone())) {
                log.warn("手机号已存在，phone={}", request.getPhone());
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, 
                    "手机号已存在：" + request.getPhone());
            }
            
            // 4. 加密密码
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            log.debug("密码加密完成");
            
            // 5. 初始化用户信息
            User user = buildNewUser(request, encodedPassword);
            
            // 6. 保存到数据库（捕获唯一约束冲突）
            saveUserToDatabase(user, request);
            
            log.info("用户注册成功，userId={}, username={}", user.getId(), user.getUsername());
            
            // 7. 事务提交后缓存（避免事务回滚导致脏数据）
            cacheUserAfterCommit(user);
            
            // 8. 删除验证码（后期启用）
            if (emailVerifyEnabled) {
                deleteEmailCode(request.getEmail());
            }
            if (phoneVerifyEnabled) {
                deletePhoneCode(request.getPhone());
            }
            
            // 9. 返回UserVO（不包含敏感信息）
            return UserVO.fromEntity(user);
        });
    }
    
    /**
     * 构建新用户对象
     */
    private User buildNewUser(RegisterRequest request, String encodedPassword) {
        User user = new User();
        
        // 账号信息
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(StrUtil.isNotBlank(request.getNickname()) 
            ? request.getNickname() : request.getUsername());
        
        // 初始化等级和权限
        user.setLevel(1);
        user.setExperience(0);
        user.setVipType(0);
        user.setVipExpireTime(null);
        
        // 初始化占卜次数
        user.setDailyDivinationCount(0);
        user.setTotalDivinationCount(0);
        user.setLastDivinationDate(null);
        
        // 初始化账号状态
        user.setStatus(0);  // 0-正常
        user.setLoginFailedCount(0);
        user.setLastLoginTime(null);
        user.setLastLoginIp(null);
        
        return user;
    }
    
    /**
     * 保存用户到数据库
     * 捕获唯一约束冲突异常
     */
    private void saveUserToDatabase(User user, RegisterRequest request) {
        try {
            int result = userMapper.insert(user);
            if (result != 1) {
                throw new BusinessException(ErrorCode.INSERT_ERROR, "用户注册失败");
            }
        } catch (DuplicateKeyException e) {
            // 捕获MySQL唯一约束异常，给用户友好提示
            log.error("用户注册失败：唯一约束冲突，username={}, email={}, phone={}", 
                request.getUsername(), request.getEmail(), request.getPhone(), e);
            
            String errorMsg = e.getMessage();
            if (errorMsg.contains("username")) {
                throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在");
            } else if (errorMsg.contains("email")) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已存在");
            } else if (errorMsg.contains("phone")) {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, "手机号已存在");
            } else {
                throw new BusinessException(ErrorCode.INSERT_ERROR, "注册失败：数据重复");
            }
        }
    }
    
    /**
     * 事务提交后缓存用户信息
     * 使用Spring事务同步器，只有事务成功提交才缓存
     */
    private void cacheUserAfterCommit(User user) {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 缓存用户信息（30分钟，随机TTL防雪崩）
                        String userCacheKey = CacheConstants.USER_INFO_PREFIX + user.getId();
                        redisUtil.setWithRandomExpire(userCacheKey, user, 
                            CacheConstants.USER_INFO_TTL, TimeUnit.SECONDS);
                        
                        // 更新检查缓存（标记已存在）
                        redisUtil.set(CacheConstants.USER_CHECK_USERNAME_PREFIX + user.getUsername(), 
                            true, CacheConstants.USER_CHECK_TTL);
                        redisUtil.set(CacheConstants.USER_CHECK_EMAIL_PREFIX + user.getEmail(), 
                            true, CacheConstants.USER_CHECK_TTL);
                        redisUtil.set(CacheConstants.USER_CHECK_PHONE_PREFIX + user.getPhone(), 
                            true, CacheConstants.USER_CHECK_TTL);
                        
                        log.debug("用户信息已缓存到Redis，userId={}", user.getId());
                    } catch (Exception e) {
                        // 缓存失败不影响业务，仅记录日志
                        log.error("缓存用户信息失败，userId={}", user.getId(), e);
                    }
                }
            }
        );
    }
    
    // ==================== 重复检查（带Redis缓存） ====================
    
    /**
     * 检查用户名是否存在
     * 缓存策略：先查Redis，未命中再查DB，结果缓存5分钟
     */
    @Override
    public boolean isUsernameExists(String username) {
        if (StrUtil.isBlank(username)) {
            return false;
        }
        
        String cacheKey = CacheConstants.USER_CHECK_USERNAME_PREFIX + username;
        
        // 1. 尝试查Redis缓存
        try {
            Boolean cached = redisUtil.get(cacheKey, Boolean.class);
            if (cached != null) {
                log.debug("命中缓存：用户名检查，username={}, exists={}", username, cached);
                return cached;
            }
        } catch (Exception e) {
            // Redis异常时降级到查数据库
            log.warn("Redis查询失败，降级到数据库查询，username={}, error={}", 
                username, e.getMessage());
        }
        
        // 2. 缓存未命中，查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        boolean exists = (count > 0);
        
        // 3. 缓存结果（包括false，防止缓存穿透）
        try {
            // 不存在的结果缓存时间短一点（1分钟）
            long ttl = exists ? CacheConstants.USER_CHECK_TTL : 60;
            redisUtil.set(cacheKey, exists, ttl);
        } catch (Exception e) {
            log.warn("Redis缓存失败，username={}, error={}", username, e.getMessage());
        }
        
        log.debug("数据库查询：用户名检查，username={}, exists={}", username, exists);
        return exists;
    }
    
    /**
     * 检查邮箱是否存在
     * 缓存策略：先查Redis，未命中再查DB，结果缓存5分钟
     */
    @Override
    public boolean isEmailExists(String email) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        
        String cacheKey = CacheConstants.USER_CHECK_EMAIL_PREFIX + email;
        
        // 1. 尝试查Redis缓存
        try {
            Boolean cached = redisUtil.get(cacheKey, Boolean.class);
            if (cached != null) {
                log.debug("命中缓存：邮箱检查，email={}, exists={}", email, cached);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis查询失败，降级到数据库查询，email={}, error={}", 
                email, e.getMessage());
        }
        
        // 2. 查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        Long count = userMapper.selectCount(wrapper);
        boolean exists = (count > 0);
        
        // 3. 缓存结果
        try {
            long ttl = exists ? CacheConstants.USER_CHECK_TTL : 60;
            redisUtil.set(cacheKey, exists, ttl);
        } catch (Exception e) {
            log.warn("Redis缓存失败，email={}, error={}", email, e.getMessage());
        }
        
        log.debug("数据库查询：邮箱检查，email={}, exists={}", email, exists);
        return exists;
    }
    
    /**
     * 检查手机号是否存在
     * 缓存策略：先查Redis，未命中再查DB，结果缓存5分钟
     */
    @Override
    public boolean isPhoneExists(String phone) {
        if (StrUtil.isBlank(phone)) {
            return false;
        }
        
        String cacheKey = CacheConstants.USER_CHECK_PHONE_PREFIX + phone;
        
        // 1. 尝试查Redis缓存
        try {
            Boolean cached = redisUtil.get(cacheKey, Boolean.class);
            if (cached != null) {
                log.debug("命中缓存：手机号检查，phone={}, exists={}", phone, cached);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis查询失败，降级到数据库查询，phone={}, error={}", 
                phone, e.getMessage());
        }
        
        // 2. 查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        Long count = userMapper.selectCount(wrapper);
        boolean exists = (count > 0);
        
        // 3. 缓存结果
        try {
            long ttl = exists ? CacheConstants.USER_CHECK_TTL : 60;
            redisUtil.set(cacheKey, exists, ttl);
        } catch (Exception e) {
            log.warn("Redis缓存失败，phone={}, error={}", phone, e.getMessage());
        }
        
        log.debug("数据库查询：手机号检查，phone={}, exists={}", phone, exists);
        return exists;
    }
    
    // ==================== 预留方法：验证码相关（后期实现） ====================
    
    /**
     * 校验邮箱验证码
     * TODO: 后期实现
     */
    private void validateEmailCode(String email, String code) {
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR, "邮箱验证码不能为空");
        }
        
        String cacheKey = CacheConstants.VERIFY_EMAIL_PREFIX + email;
        String cachedCode = redisUtil.get(cacheKey);
        
        if (StrUtil.isBlank(cachedCode)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED, "邮箱验证码已过期");
        }
        
        if (!cachedCode.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR, "邮箱验证码错误");
        }
        
        log.info("邮箱验证码校验通过，email={}", email);
    }
    
    /**
     * 校验手机验证码
     * TODO: 后期实现
     */
    private void validatePhoneCode(String phone, String code) {
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR, "手机验证码不能为空");
        }
        
        String cacheKey = CacheConstants.VERIFY_PHONE_PREFIX + phone;
        String cachedCode = redisUtil.get(cacheKey);
        
        if (StrUtil.isBlank(cachedCode)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED, "手机验证码已过期");
        }
        
        if (!cachedCode.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR, "手机验证码错误");
        }
        
        log.info("手机验证码校验通过，phone={}", phone);
    }
    
    /**
     * 删除邮箱验证码
     * TODO: 后期实现
     */
    private void deleteEmailCode(String email) {
        String cacheKey = CacheConstants.VERIFY_EMAIL_PREFIX + email;
        redisUtil.delete(cacheKey);
        log.debug("邮箱验证码已删除，email={}", email);
    }
    
    /**
     * 删除手机验证码
     * TODO: 后期实现
     */
    private void deletePhoneCode(String phone) {
        String cacheKey = CacheConstants.VERIFY_PHONE_PREFIX + phone;
        redisUtil.delete(cacheKey);
        log.debug("手机验证码已删除，phone={}", phone);
    }
    
    // ==================== 登录功能实现 ====================
    
    /**
     * 用户登录
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        String loginIp = request.getLoginIp();
        
        log.info("用户登录请求：account={}, loginIp={}", account, loginIp);
        
        // Step 1: 检查Redis锁定状态
        checkAccountLockStatus(account);
        
        // Step 2: 查询用户（支持用户名/邮箱/手机号）
        User user = getUserByAccount(account);
        if (user == null) {
            log.warn("用户不存在：account={}", account);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // Step 3: 检查账号状态（数据库层面）
        checkAccountStatus(user, account);
        
        // Step 4: 验证密码
        boolean isPasswordCorrect = passwordEncoder.matches(password, user.getPassword());
        
        if (!isPasswordCorrect) {
            // 密码错误：累加失败次数
            handleLoginFailed(account, user.getId());
            log.warn("密码错误：account={}", account);
            throw new BusinessException(ErrorCode.USERNAME_PASSWORD_ERROR);
        }
        
        // Step 5: 登录成功：清除失败次数，生成Token
        clearLoginFailed(account);
        
        // Step 6: 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("生成JWT Token成功：userId={}, username={}", user.getId(), user.getUsername());
        
        // Step 7: 更新登录信息（异步）
        updateLoginInfo(user.getId(), loginIp);
        
        // Step 8: 缓存用户信息和会话
        cacheUserSession(user.getId(), token);
        
        // Step 9: 构建响应
        LoginResponse response = buildLoginResponse(user, token);
        log.info("用户登录成功：userId={}, username={}", user.getId(), user.getUsername());
        
        return response;
    }
    
    /**
     * 根据账号查询用户（支持用户名/邮箱/手机号）
     */
    @Override
    public User getUserByAccount(String account) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 判断账号类型
        if (account.contains("@")) {
            // 邮箱登录
            wrapper.eq(User::getEmail, account);
            log.debug("使用邮箱查询用户：email={}", account);
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            // 手机号登录（中国大陆手机号）
            wrapper.eq(User::getPhone, account);
            log.debug("使用手机号查询用户：phone={}", account);
        } else {
            // 用户名登录
            wrapper.eq(User::getUsername, account);
            log.debug("使用用户名查询用户：username={}", account);
        }
        
        return userMapper.selectOne(wrapper);
    }
    
    /**
     * 检查账号锁定状态（Redis）
     */
    private void checkAccountLockStatus(String account) {
        String lockKey = CacheConstants.ACCOUNT_LOCK_PREFIX + account;
        
        if (redisUtil.hasKey(lockKey)) {
            Long ttl = redisUtil.getExpire(lockKey);
            long remainingMinutes = ttl / 60;
            
            log.warn("账号已被锁定：account={}, 剩余时间={}分钟", account, remainingMinutes);
            throw new BusinessException(
                ErrorCode.ACCOUNT_LOCKED,
                "账号已锁定，请" + remainingMinutes + "分钟后重试"
            );
        }
    }
    
    /**
     * 检查账号状态（数据库）
     */
    private void checkAccountStatus(User user, String account) {
        if (user.getStatus() == 1) {
            log.warn("账号已锁定（数据库）：account={}", account);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (user.getStatus() == 2) {
            log.warn("账号已禁用：account={}", account);
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
    }
    
    /**
     * 处理登录失败
     */
    private void handleLoginFailed(String account, Long userId) {
        try {
            String failedKey = CacheConstants.LOGIN_FAILED_PREFIX + account;
            
            // 累加失败次数（安全的类型转换）
            Object failedObj = redisUtil.get(failedKey);
            Integer failedCount = 0;
            
            if (failedObj != null) {
                // 使用Hutool的Convert工具类进行安全转换
                failedCount = Convert.toInt(failedObj, 0);
            }
            failedCount++;
            
            // 存储失败次数（30分钟过期）
            redisUtil.set(failedKey, failedCount, CacheConstants.LOGIN_FAILED_TTL);
            log.info("登录失败次数累加：account={}, failedCount={}", account, failedCount);
            
            // 达到5次，锁定账号30分钟
            if (failedCount >= CacheConstants.MAX_LOGIN_FAILED_COUNT) {
                String lockKey = CacheConstants.ACCOUNT_LOCK_PREFIX + account;
                redisUtil.set(lockKey, true, CacheConstants.ACCOUNT_LOCK_TTL);
                
                log.warn("账号登录失败{}次，已锁定30分钟：account={}, userId={}", 
                    failedCount, account, userId);
            }
        } catch (Exception e) {
            log.error("处理登录失败异常，不影响主流程：account={}", account, e);
        }
    }
    
    /**
     * 清除登录失败记录
     */
    private void clearLoginFailed(String account) {
        try {
            String failedKey = CacheConstants.LOGIN_FAILED_PREFIX + account;
            redisUtil.delete(failedKey);
            log.debug("清除登录失败记录：account={}", account);
        } catch (Exception e) {
            log.error("清除登录失败记录异常，不影响主流程：account={}", account, e);
        }
    }
    
    /**
     * 更新登录信息（数据库）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Long userId, String loginIp) {
        try {
            User user = new User();
            user.setId(userId);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(loginIp);
            user.setLoginFailedCount(0);  // 重置失败次数
            
            userMapper.updateById(user);
            log.info("更新登录信息成功：userId={}, loginIp={}", userId, loginIp);
        } catch (Exception e) {
            log.error("更新登录信息失败：userId={}", userId, e);
            // 不抛出异常，不影响登录流程
        }
    }
    
    /**
     * 缓存用户会话
     */
    private void cacheUserSession(Long userId, String token) {
        try {
            // 1. 缓存用户信息（用于快速查询）
            User user = userMapper.selectById(userId);
            String userKey = CacheConstants.USER_INFO_PREFIX + userId;
            redisUtil.setWithRandomExpire(userKey, user, CacheConstants.USER_INFO_TTL,TimeUnit.SECONDS);
            log.debug("缓存用户信息：userId={}", userId);
            
            // 2. 缓存会话信息（用于在线状态管理）
            String sessionKey = CacheConstants.USER_SESSION_PREFIX + userId;
            Map<String, Object> session = new HashMap<>();
            session.put("userId", userId);
            session.put("token", token);
            session.put("loginTime", LocalDateTime.now());
            
            redisUtil.set(sessionKey, session, CacheConstants.USER_SESSION_TTL);
            log.debug("缓存用户会话：userId={}", userId);
        } catch (Exception e) {
            log.error("缓存用户会话失败：userId={}", userId, e);
            // 不抛出异常，不影响登录流程
        }
    }
    
    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user, String token) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setLevel(user.getLevel());
        response.setExperience(user.getExperience());
        response.setVipType(user.getVipType());
        response.setVipExpireTime(user.getVipExpireTime());
        response.setLoginTime(LocalDateTime.now());
        return response;
    }
    
    // ==================== 用户信息管理实现 ====================
    
    /**
     * 获取用户详细信息
     */
    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("获取用户信息，userId={}", userId);
        
        // Step 1: 先查Redis缓存
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // Step 2: 检查VIP是否过期
        if (isVipActive(user) && user.getVipExpireTime().isBefore(LocalDateTime.now())) {
            // VIP已过期，更新数据库状态
            updateVipExpired(userId);
            user.setVipType(BusinessConstants.VIP_TYPE_NORMAL);
            user.setVipExpireTime(null);
        }
        
        // Step 3: 转换为DTO并返回
        UserProfileResponse response = UserProfileResponse.fromUser(user);
        log.info("获取用户信息成功，userId={}, vipType={}", userId, response.getVipType());
        
        return response;
    }
    
    /**
     * 更新用户信息（延迟双删方案）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("更新用户信息，userId={}, request={}", userId, request);
        
        // Step 1: 校验参数
        request.validate();
        
        if (!request.hasFieldsToUpdate()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "没有需要更新的字段");
        }
        
        // Step 2: 检查用户是否存在
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
        
        // Step 3: 第一次删除缓存（防止并发读）
        try {
            redisUtil.delete(cacheKey);
            log.debug("第一次删除缓存，userId={}", userId);
        } catch (Exception e) {
            log.warn("删除缓存失败，userId={}", userId, e);
        }
        
        // Step 4: 更新数据库
        User updateUser = new User();
        updateUser.setId(userId);
        
        if (StrUtil.isNotBlank(request.getNickname())) {
            updateUser.setNickname(request.getNickname());
        }
        if (StrUtil.isNotBlank(request.getAvatar())) {
            updateUser.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            updateUser.setSignature(request.getSignature());
        }
        
        int result = userMapper.updateById(updateUser);
        if (result != 1) {
            throw new BusinessException(ErrorCode.UPDATE_ERROR, "更新用户信息失败");
        }
        
        log.info("用户信息更新成功，userId={}", userId);
        
        // Step 5: 延迟双删（异步，延迟500ms再删一次）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(BusinessConstants.DELAY_DELETE_CACHE_MS);
                redisUtil.delete(cacheKey);
                log.debug("延迟双删执行成功，userId={}", userId);
            } catch (Exception e) {
                log.error("延迟双删失败，userId={}", userId, e);
            }
        });
        
        // Step 6: 查询最新数据并返回
        User updatedUser = userMapper.selectById(userId);
        return UserProfileResponse.fromUser(updatedUser);
    }
    
    /**
     * 上传头像
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        log.info("上传头像，userId={}, fileName={}, size={}", 
            userId, file.getOriginalFilename(), file.getSize());
        
        // Step 1: 校验文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
        
        // Step 2: 校验文件大小（最大2MB）
        if (file.getSize() > BusinessConstants.MAX_AVATAR_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                "文件大小不能超过2MB，当前大小：" + (file.getSize() / 1024 / 1024) + "MB");
        }
        
        // Step 3: 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不合法");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(BusinessConstants.ALLOWED_IMAGE_TYPES).contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, 
                "不支持的文件类型，仅支持：" + Arrays.toString(BusinessConstants.ALLOWED_IMAGE_TYPES));
        }
        
        // Step 4: 生成唯一文件名（userId_timestamp_随机UUID.ext）
        String fileName = userId + "_" + System.currentTimeMillis() + "_" + IdUtil.simpleUUID() + "." + extension;
        
        // Step 5: 构建文件保存路径
        String uploadPath = System.getProperty("user.dir") + BusinessConstants.AVATAR_UPLOAD_PATH + userId + "/";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        File destFile = new File(uploadPath + fileName);
        
        // Step 6: 保存文件
        try {
            file.transferTo(destFile);
            log.info("头像保存成功，path={}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("头像保存失败，userId={}", userId, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败");
        }
        
        // Step 7: 构建访问URL
        String avatarUrl = BusinessConstants.AVATAR_URL_PREFIX + userId + "/" + fileName;
        
        // Step 8: 更新用户表avatar字段
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        
        // Step 9: 删除缓存（让下次查询重新加载）
        String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
        redisUtil.delete(cacheKey);
        
        log.info("头像上传成功，userId={}, avatarUrl={}", userId, avatarUrl);
        return avatarUrl;
    }
    
    /**
     * 根据ID查询用户（带缓存）
     */
    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
        
        // 1. 尝试查Redis缓存
        try {
            User cached = redisUtil.get(cacheKey, User.class);
            if (cached != null) {
                log.debug("命中缓存：用户信息，userId={}", userId);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis查询失败，降级到数据库查询，userId={}", userId, e);
        }
        
        // 2. 缓存未命中，查询数据库
        User user = userMapper.selectById(userId);
        
        // 3. 缓存结果
        if (user != null) {
            try {
                redisUtil.setWithRandomExpire(cacheKey, user, CacheConstants.USER_INFO_TTL, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Redis缓存失败，userId={}", userId, e);
            }
        }
        
        return user;
    }
    
    /**
     * 计算用户等级
     */
    @Override
    public Integer calculateLevel(Integer experience) {
        if (experience == null || experience < 0) {
            return BusinessConstants.MIN_LEVEL;
        }
        
        int level = experience / BusinessConstants.EXP_PER_LEVEL + 1;
        
        // 封顶99级
        return Math.min(level, BusinessConstants.MAX_LEVEL);
    }
    
    /**
     * 获取每日占卜次数限制
     */
    @Override
    public Integer getDailyDivinationLimit(Integer vipType, Boolean isVipActive) {
        if (!isVipActive || vipType == null || vipType == BusinessConstants.VIP_TYPE_NORMAL) {
            return BusinessConstants.BASE_DIVINATION_TIMES;
        }
        
        switch (vipType) {
            case BusinessConstants.VIP_TYPE_MONTH:
                return BusinessConstants.VIP_MONTH_TIMES;
            case BusinessConstants.VIP_TYPE_YEAR:
                return BusinessConstants.VIP_YEAR_TIMES;
            default:
                return BusinessConstants.BASE_DIVINATION_TIMES;
        }
    }
    
    /**
     * 检查VIP是否有效
     */
    @Override
    public Boolean isVipActive(User user) {
        if (user == null || user.getVipType() == null || user.getVipType() == BusinessConstants.VIP_TYPE_NORMAL) {
            return false;
        }
        if (user.getVipExpireTime() == null) {
            return false;
        }
        return user.getVipExpireTime().isAfter(LocalDateTime.now());
    }
    
    /**
     * 更新VIP过期状态（内部方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateVipExpired(Long userId) {
        try {
            User user = new User();
            user.setId(userId);
            user.setVipType(BusinessConstants.VIP_TYPE_NORMAL);
            user.setVipExpireTime(null);
            
            userMapper.updateById(user);
            log.info("VIP已过期，已更新状态，userId={}", userId);
            
            // 删除缓存
            String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
            redisUtil.delete(cacheKey);
        } catch (Exception e) {
            log.error("更新VIP过期状态失败，userId={}", userId, e);
        }
    }
}

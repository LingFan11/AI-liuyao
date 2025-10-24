package com.lingfan.liuyao.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
}

package com.lingfan.liuyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.request.RegisterRequest;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.model.vo.UserVO;
import com.lingfan.liuyao.service.UserRegisterService;
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

import java.util.concurrent.TimeUnit;

/**
 * 用户注册服务实现类
 * 
 * 核心功能：
 * 1. 用户注册（分布式锁防并发）
 * 2. 用户名/邮箱/手机号重复检查（带Redis缓存）
 * 3. 密码加密存储（BCrypt）
 * 4. 事务提交后缓存（避免脏数据）
 * 
 * 重构说明（2025-10-26）：
 * - 从UserServiceImpl拆分出来
 * - 简化注册流程，消除过多的if分支
 * - 保留分布式锁和缓存策略
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Service
@Slf4j
public class UserRegisterServiceImpl implements UserRegisterService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisUtil redisUtil;
    
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
    
    /**
     * 用户注册
     * 使用分布式锁防止并发注册同一用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterRequest request) {
        log.info("开始注册用户，username={}, email={}, phone={}", 
            request.getUsername(), request.getEmail(), request.getPhone());
        
        // 使用分布式锁防止并发注册
        String lockKey = CacheConstants.REGISTER_LOCK_PREFIX + request.getUsername();
        
        return redisUtil.executeWithLock(lockKey, CacheConstants.REGISTER_LOCK_TTL, TimeUnit.SECONDS, () -> {
            
            // 1. 验证码校验（后期启用）
            if (emailVerifyEnabled) {
                validateEmailCode(request.getEmail(), request.getEmailCode());
            }
            if (phoneVerifyEnabled) {
                validatePhoneCode(request.getPhone(), request.getPhoneCode());
            }
            
            // 2. 重复检查
            validateUnique(request);
            
            // 3. 构建用户对象
            User user = buildNewUser(request);
            
            // 4. 保存到数据库
            saveUserToDatabase(user, request);
            
            log.info("用户注册成功，userId={}, username={}", user.getId(), user.getUsername());
            
            // 5. 事务提交后缓存（避免脏数据）
            cacheUserAfterCommit(user);
            
            // 6. 删除验证码（后期启用）
            if (emailVerifyEnabled) {
                deleteEmailCode(request.getEmail());
            }
            if (phoneVerifyEnabled) {
                deletePhoneCode(request.getPhone());
            }
            
            // 7. 返回UserVO
            return UserVO.fromEntity(user);
        });
    }
    
    /**
     * 统一校验：用户名、邮箱、手机号是否重复
     */
    private void validateUnique(RegisterRequest request) {
        if (isUsernameExists(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, 
                "用户名已存在：" + request.getUsername());
        }
        if (isEmailExists(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, 
                "邮箱已存在：" + request.getEmail());
        }
        if (isPhoneExists(request.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS, 
                "手机号已存在：" + request.getPhone());
        }
    }
    
    /**
     * 构建新用户对象
     */
    private User buildNewUser(RegisterRequest request) {
        User user = new User();
        
        // 账号信息
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        user.setLastDivinationTime(null);
        
        // 初始化账号状态
        user.setStatus(0);
        user.setLoginFailedCount(0);
        user.setLastLoginTime(null);
        user.setLastLoginIp(null);
        
        return user;
    }
    
    /**
     * 保存用户到数据库（捕获唯一约束冲突）
     */
    private void saveUserToDatabase(User user, RegisterRequest request) {
        try {
            int result = userMapper.insert(user);
            if (result != 1) {
                throw new BusinessException(ErrorCode.INSERT_ERROR, "用户注册失败");
            }
        } catch (DuplicateKeyException e) {
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
                        log.error("缓存用户信息失败，userId={}", user.getId(), e);
                    }
                }
            }
        );
    }
    
    // ==================== 重复检查（带Redis缓存） ====================
    
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
            log.warn("Redis查询失败，降级到数据库查询，username={}, error={}", 
                username, e.getMessage());
        }
        
        // 2. 查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        boolean exists = (count > 0);
        
        // 3. 缓存结果
        try {
            long ttl = exists ? CacheConstants.USER_CHECK_TTL : 60;
            redisUtil.set(cacheKey, exists, ttl);
        } catch (Exception e) {
            log.warn("Redis缓存失败，username={}, error={}", username, e.getMessage());
        }
        
        log.debug("数据库查询：用户名检查，username={}, exists={}", username, exists);
        return exists;
    }
    
    @Override
    public boolean isEmailExists(String email) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        
        String cacheKey = CacheConstants.USER_CHECK_EMAIL_PREFIX + email;
        
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
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        Long count = userMapper.selectCount(wrapper);
        boolean exists = (count > 0);
        
        try {
            long ttl = exists ? CacheConstants.USER_CHECK_TTL : 60;
            redisUtil.set(cacheKey, exists, ttl);
        } catch (Exception e) {
            log.warn("Redis缓存失败，email={}, error={}", email, e.getMessage());
        }
        
        log.debug("数据库查询：邮箱检查，email={}, exists={}", email, exists);
        return exists;
    }
    
    @Override
    public boolean isPhoneExists(String phone) {
        if (StrUtil.isBlank(phone)) {
            return false;
        }
        
        String cacheKey = CacheConstants.USER_CHECK_PHONE_PREFIX + phone;
        
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
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        Long count = userMapper.selectCount(wrapper);
        boolean exists = (count > 0);
        
        try {
            long ttl = exists ? CacheConstants.USER_CHECK_TTL : 60;
            redisUtil.set(cacheKey, exists, ttl);
        } catch (Exception e) {
            log.warn("Redis缓存失败，phone={}, error={}", phone, e.getMessage());
        }
        
        log.debug("数据库查询：手机号检查，phone={}, exists={}", phone, exists);
        return exists;
    }
    
    // ==================== 验证码相关（后期实现） ====================
    
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
    
    private void deleteEmailCode(String email) {
        String cacheKey = CacheConstants.VERIFY_EMAIL_PREFIX + email;
        redisUtil.delete(cacheKey);
        log.debug("邮箱验证码已删除，email={}", email);
    }
    
    private void deletePhoneCode(String phone) {
        String cacheKey = CacheConstants.VERIFY_PHONE_PREFIX + phone;
        redisUtil.delete(cacheKey);
        log.debug("手机验证码已删除，phone={}", phone);
    }
}

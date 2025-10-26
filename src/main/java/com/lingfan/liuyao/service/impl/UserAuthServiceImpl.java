package com.lingfan.liuyao.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.request.LoginRequest;
import com.lingfan.liuyao.model.dto.response.LoginResponse;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.service.UserAuthService;
import com.lingfan.liuyao.utils.JwtUtil;
import com.lingfan.liuyao.utils.PasswordEncoder;
import com.lingfan.liuyao.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务实现类
 * 
 * 核心功能：
 * 1. 用户登录（支持用户名/邮箱/手机号）
 * 2. 登录失败次数限制（5次锁定30分钟）
 * 3. JWT Token生成和会话管理
 * 4. 更新登录信息（时间、IP）
 * 
 * 重构说明（2025-10-26）：
 * - 从UserServiceImpl拆分出来
 * - 专注于认证相关功能
 * - 简化登录流程，消除过多的if分支
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Service
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private JwtUtil jwtUtil;
    
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
        
        // Step 3: 检查账号状态
        checkAccountStatus(user, account);
        
        // Step 4: 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            handleLoginFailed(account, user.getId());
            log.warn("密码错误：account={}", account);
            throw new BusinessException(ErrorCode.USERNAME_PASSWORD_ERROR);
        }
        
        // Step 5: 登录成功 - 清除失败次数，生成Token
        clearLoginFailed(account);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("生成JWT Token成功：userId={}, username={}", user.getId(), user.getUsername());
        
        // Step 6: 更新登录信息（异步）
        updateLoginInfo(user.getId(), loginIp);
        
        // Step 7: 缓存用户信息和会话
        cacheUserSession(user.getId(), token);
        
        // Step 8: 构建响应
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
            wrapper.eq(User::getEmail, account);
            log.debug("使用邮箱查询用户：email={}", account);
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            wrapper.eq(User::getPhone, account);
            log.debug("使用手机号查询用户：phone={}", account);
        } else {
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
            Integer failedCount = Convert.toInt(failedObj, 0);
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
            user.setLoginFailedCount(0);
            
            userMapper.updateById(user);
            log.info("更新登录信息成功：userId={}, loginIp={}", userId, loginIp);
        } catch (Exception e) {
            log.error("更新登录信息失败：userId={}", userId, e);
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
            redisUtil.setWithRandomExpire(userKey, user, CacheConstants.USER_INFO_TTL, TimeUnit.SECONDS);
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

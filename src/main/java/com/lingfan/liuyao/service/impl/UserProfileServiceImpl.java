package com.lingfan.liuyao.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.constant.CacheConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.UserMapper;
import com.lingfan.liuyao.model.dto.request.UpdateProfileRequest;
import com.lingfan.liuyao.model.dto.response.UserProfileResponse;
import com.lingfan.liuyao.model.entity.User;
import com.lingfan.liuyao.service.UserProfileService;
import com.lingfan.liuyao.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息管理服务实现类
 * 
 * 核心功能：
 * 1. 获取用户详细信息（带缓存、数据脱敏）
 * 2. 更新用户信息（延迟双删策略）
 * 3. 头像上传（文件校验、存储）
 * 4. 用户等级计算
 * 5. VIP状态管理
 * 6. 占卜次数限制
 * 
 * 重构说明（2025-10-26）：
 * - 从UserServiceImpl拆分出来
 * - 专注于用户信息管理功能
 * - 保留延迟双删缓存策略
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
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

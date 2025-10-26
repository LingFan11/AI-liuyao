package com.lingfan.liuyao.service;

import com.lingfan.liuyao.model.dto.request.UpdateProfileRequest;
import com.lingfan.liuyao.model.dto.response.UserProfileResponse;
import com.lingfan.liuyao.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息管理服务接口
 * 
 * 职责：
 * - 获取用户详细信息
 * - 更新用户信息（昵称、头像、签名）
 * - 头像上传
 * - 用户等级计算
 * - VIP状态管理
 * - 占卜次数限制
 * 
 * 重构说明（2025-10-26）：
 * - 从UserService拆分出来，遵循单一职责原则
 * - 专注于用户信息管理功能
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
public interface UserProfileService {
    
    /**
     * 获取用户详细信息
     * 
     * @param userId 用户ID
     * @return UserProfileResponse（脱敏处理）
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param request 更新请求（昵称、头像、签名）
     * @return 更新后的用户信息
     */
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * 上传头像
     * 
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile file);
    
    /**
     * 根据ID查询用户（带缓存）
     * 
     * @param userId 用户ID
     * @return User实体
     */
    User getUserById(Long userId);
    
    /**
     * 计算用户等级
     * 
     * @param experience 经验值
     * @return 等级（1-99）
     */
    Integer calculateLevel(Integer experience);
    
    /**
     * 获取每日占卜次数限制
     * 
     * @param vipType VIP类型（0-普通，1-月度，2-年度）
     * @param isVipActive VIP是否有效
     * @return 每日占卜次数限制
     */
    Integer getDailyDivinationLimit(Integer vipType, Boolean isVipActive);
    
    /**
     * 检查VIP是否有效
     * 
     * @param user 用户实体
     * @return true=有效, false=无效或已过期
     */
    Boolean isVipActive(User user);
}

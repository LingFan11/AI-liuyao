package com.lingfan.liuyao.model.dto.response;

import cn.hutool.core.util.DesensitizedUtil;
import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.model.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 返回给前端的用户详细信息（脱敏处理）
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Data
public class UserProfileResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== 基本信息 ====================
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱（脱敏）
     * 示例：abc***@qq.com
     */
    private String email;
    
    /**
     * 手机号（脱敏）
     * 示例：138****5678
     */
    private String phone;
    
    // ==================== 个人资料 ====================
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 个性签名
     */
    private String signature;
    
    // ==================== 等级和权限 ====================
    
    /**
     * 用户等级
     */
    private Integer level;
    
    /**
     * 当前经验值
     */
    private Integer experience;
    
    /**
     * 升级所需经验值
     */
    private Integer nextLevelExp;
    
    /**
     * VIP类型
     * 0-普通用户, 1-月度VIP, 2-年度VIP
     */
    private Integer vipType;
    
    /**
     * VIP到期时间
     */
    private LocalDateTime vipExpireTime;
    
    /**
     * VIP是否有效（未过期）
     */
    private Boolean isVipActive;
    
    /**
     * 每日占卜次数限制
     */
    private Integer dailyDivinationLimit;
    
    // ==================== 占卜统计 ====================
    
    /**
     * 今日已占卜次数
     */
    private Integer dailyDivinationCount;
    
    /**
     * 今日剩余占卜次数
     */
    private Integer remainingDivinationCount;
    
    /**
     * 总占卜次数
     */
    private Integer totalDivinationCount;
    
    /**
     * 最后占卜时间（精确到秒，包含日期信息）
     */
    private LocalDateTime lastDivinationTime;
    
    // ==================== 账号信息 ====================
    
    /**
     * 账号状态
     * 0-正常, 1-锁定, 2-禁用
     */
    private Integer status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 账号创建时间
     */
    private LocalDateTime createdAt;
    
    // ==================== 工具方法 ====================
    
    /**
     * 从User实体转换为UserProfileResponse
     * 
     * @param user 用户实体
     * @return 用户信息响应DTO
     */
    public static UserProfileResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        UserProfileResponse response = new UserProfileResponse();
        
        // 基本信息
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(maskEmail(user.getEmail()));
        response.setPhone(maskPhone(user.getPhone()));
        
        // 个人资料
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setSignature(user.getSignature());
        
        // 等级和权限
        response.setLevel(user.getLevel());
        response.setExperience(user.getExperience());
        response.setNextLevelExp(calculateNextLevelExp(user.getLevel()));
        response.setVipType(user.getVipType());
        response.setVipExpireTime(user.getVipExpireTime());
        response.setIsVipActive(isVipActive(user));
        response.setDailyDivinationLimit(getDailyDivinationLimit(user.getVipType(), isVipActive(user)));
        
        // 占卜统计
        response.setDailyDivinationCount(user.getDailyDivinationCount());
        response.setRemainingDivinationCount(calculateRemainingCount(user));
        response.setTotalDivinationCount(user.getTotalDivinationCount());
        response.setLastDivinationTime(user.getLastDivinationTime());
        
        // 账号信息
        response.setStatus(user.getStatus());
        response.setLastLoginTime(user.getLastLoginTime());
        response.setCreatedAt(user.getCreatedAt());
        
        return response;
    }
    
    /**
     * 邮箱脱敏
     * 示例：abcdefg@qq.com → abc***@qq.com
     */
    private static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        return DesensitizedUtil.email(email);
    }
    
    /**
     * 手机号脱敏
     * 示例：13812345678 → 138****5678
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        return DesensitizedUtil.mobilePhone(phone);
    }
    
    /**
     * 计算升级所需经验值
     * 
     * @param currentLevel 当前等级
     * @return 升级所需经验值
     */
    private static Integer calculateNextLevelExp(Integer currentLevel) {
        if (currentLevel == null || currentLevel >= BusinessConstants.MAX_LEVEL) {
            return 0;  // 已满级
        }
        return (currentLevel + 1) * BusinessConstants.EXP_PER_LEVEL;
    }
    
    /**
     * 判断VIP是否有效
     * 
     * @param user 用户对象
     * @return true=有效, false=已过期或非VIP
     */
    private static Boolean isVipActive(User user) {
        if (user.getVipType() == null || user.getVipType() == BusinessConstants.VIP_TYPE_NORMAL) {
            return false;
        }
        if (user.getVipExpireTime() == null) {
            return false;
        }
        return user.getVipExpireTime().isAfter(LocalDateTime.now());
    }
    
    /**
     * 获取每日占卜次数限制
     * 
     * @param vipType VIP类型
     * @param isVipActive VIP是否有效
     * @return 每日次数限制
     */
    private static Integer getDailyDivinationLimit(Integer vipType, Boolean isVipActive) {
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
     * 计算今日剩余占卜次数
     * 
     * @param user 用户对象
     * @return 剩余次数
     */
    private static Integer calculateRemainingCount(User user) {
        Integer limit = getDailyDivinationLimit(user.getVipType(), isVipActive(user));
        Integer used = user.getDailyDivinationCount() != null ? user.getDailyDivinationCount() : 0;
        int remaining = limit - used;
        return Math.max(remaining, 0);
    }
}

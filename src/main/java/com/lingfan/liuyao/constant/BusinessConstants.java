package com.lingfan.liuyao.constant;

/**
 * 业务常量类
 * 统一管理业务相关的常量（等级、VIP、次数限制等）
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
public class BusinessConstants {
    
    // ==================== 用户等级相关 ====================
    
    /**
     * 最大用户等级
     */
    public static final int MAX_LEVEL = 99;
    
    /**
     * 每级所需经验值
     */
    public static final int EXP_PER_LEVEL = 100;
    
    /**
     * 最小等级
     */
    public static final int MIN_LEVEL = 1;
    
    // ==================== 占卜次数限制 ====================
    
    /**
     * 普通用户每日占卜次数
     */
    public static final int BASE_DIVINATION_TIMES = 3;
    
    /**
     * 月度VIP每日占卜次数
     */
    public static final int VIP_MONTH_TIMES = 15;
    
    /**
     * 年度VIP每日占卜次数
     */
    public static final int VIP_YEAR_TIMES = 30;
    
    // ==================== VIP类型 ====================
    
    /**
     * 普通用户
     */
    public static final int VIP_TYPE_NORMAL = 0;
    
    /**
     * 月度VIP
     */
    public static final int VIP_TYPE_MONTH = 1;
    
    /**
     * 年度VIP
     */
    public static final int VIP_TYPE_YEAR = 2;
    
    // ==================== 头像上传限制 ====================
    
    /**
     * 头像文件最大大小（字节）
     * 2MB = 2 * 1024 * 1024
     */
    public static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    
    /**
     * 允许的图片类型
     */
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};
    
    /**
     * 头像上传路径（相对路径）
     */
    public static final String AVATAR_UPLOAD_PATH = "/uploads/avatars/";
    
    /**
     * 头像访问URL前缀
     */
    public static final String AVATAR_URL_PREFIX = "/files/avatars/";
    
    // ==================== 昵称和签名限制 ====================
    
    /**
     * 昵称最小长度
     */
    public static final int MIN_NICKNAME_LENGTH = 2;
    
    /**
     * 昵称最大长度
     */
    public static final int MAX_NICKNAME_LENGTH = 20;
    
    /**
     * 个性签名最大长度
     */
    public static final int MAX_SIGNATURE_LENGTH = 200;
    
    // ==================== 账号状态 ====================
    
    /**
     * 账号状态：正常
     */
    public static final int ACCOUNT_STATUS_NORMAL = 0;
    
    /**
     * 账号状态：锁定
     */
    public static final int ACCOUNT_STATUS_LOCKED = 1;
    
    /**
     * 账号状态：禁用
     */
    public static final int ACCOUNT_STATUS_DISABLED = 2;
    
    // ==================== 缓存更新延迟 ====================
    
    /**
     * 延迟双删等待时间（毫秒）
     * 用于等待MySQL主从同步
     */
    public static final long DELAY_DELETE_CACHE_MS = 500;
}

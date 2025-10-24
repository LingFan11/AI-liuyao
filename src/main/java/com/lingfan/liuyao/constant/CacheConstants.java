package com.lingfan.liuyao.constant;

/**
 * Redis缓存常量类
 * 统一管理所有缓存Key前缀和过期时间
 * 
 * 命名规范：
 * - Key前缀：模块:功能:
 * - 示例：user:info:、user:check:username:
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
public class CacheConstants {
    
    // ==================== 用户模块缓存 ====================
    
    /**
     * 用户信息缓存前缀
     * 完整Key: user:info:{userId}
     */
    public static final String USER_INFO_PREFIX = "user:info:";
    
    /**
     * 用户名检查缓存前缀
     * 完整Key: user:check:username:{username}
     */
    public static final String USER_CHECK_USERNAME_PREFIX = "user:check:username:";
    
    /**
     * 邮箱检查缓存前缀
     * 完整Key: user:check:email:{email}
     */
    public static final String USER_CHECK_EMAIL_PREFIX = "user:check:email:";
    
    /**
     * 手机号检查缓存前缀
     * 完整Key: user:check:phone:{phone}
     */
    public static final String USER_CHECK_PHONE_PREFIX = "user:check:phone:";
    
    /**
     * 用户注册分布式锁前缀
     * 完整Key: lock:register:{username}
     */
    public static final String REGISTER_LOCK_PREFIX = "lock:register:";
    
    // ==================== 登录相关缓存 ====================
    
    /**
     * 登录失败次数缓存前缀
     * 完整Key: login:failed:{account}
     */
    public static final String LOGIN_FAILED_PREFIX = "login:failed:";
    
    /**
     * 账号锁定缓存前缀
     * 完整Key: login:lock:{account}
     */
    public static final String ACCOUNT_LOCK_PREFIX = "login:lock:";
    
    /**
     * 用户会话信息缓存前缀
     * 完整Key: user:session:{userId}
     */
    public static final String USER_SESSION_PREFIX = "user:session:";
    
    /**
     * 登录失败次数过期时间（秒）
     * 30分钟
     */
    public static final long LOGIN_FAILED_TTL = 1800;
    
    /**
     * 账号锁定过期时间（秒）
     * 30分钟
     */
    public static final long ACCOUNT_LOCK_TTL = 1800;
    
    /**
     * 用户会话过期时间（秒）
     * 30分钟
     */
    public static final long USER_SESSION_TTL = 1800;
    
    /**
     * 最大登录失败次数
     * 连续5次失败后锁定账号
     */
    public static final int MAX_LOGIN_FAILED_COUNT = 5;
    
    // ==================== 缓存过期时间 ====================
    
    /**
     * 用户信息缓存过期时间（秒）
     * 30分钟
     */
    public static final long USER_INFO_TTL = 1800;
    
    /**
     * 用户检查缓存过期时间（秒）
     * 5分钟
     */
    public static final long USER_CHECK_TTL = 300;
    
    /**
     * 注册锁过期时间（秒）
     * 30秒
     */
    public static final long REGISTER_LOCK_TTL = 30;
    
    // ==================== 验证码缓存（预留） ====================
    
    /**
     * 邮箱验证码缓存前缀
     * 完整Key: verify:email:{email}
     */
    public static final String VERIFY_EMAIL_PREFIX = "verify:email:";
    
    /**
     * 手机验证码缓存前缀
     * 完整Key: verify:phone:{phone}
     */
    public static final String VERIFY_PHONE_PREFIX = "verify:phone:";
    
    /**
     * 验证码过期时间（秒）
     * 5分钟
     */
    public static final long VERIFY_CODE_TTL = 300;
    
    /**
     * 验证码发送频率限制前缀
     * 完整Key: verify:limit:{type}:{target}
     */
    public static final String VERIFY_LIMIT_PREFIX = "verify:limit:";
    
    /**
     * 验证码发送频率限制时间（秒）
     * 1分钟内只能发送1次
     */
    public static final long VERIFY_LIMIT_TTL = 60;
    
    // ==================== 会话缓存 ====================
    
    /**
     * 用户在线状态缓存前缀
     * 完整Key: user:online:{userId}
     */
    public static final String USER_ONLINE_PREFIX = "user:online:";
    
    /**
     * 用户在线状态过期时间（秒）
     * 30分钟
     */
    public static final long USER_ONLINE_TTL = 1800;
    
    /**
     * JWT黑名单前缀
     * 完整Key: jwt:blacklist:{token}
     */
    public static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";
    
    // ==================== 占卜模块缓存（预留） ====================
    
    /**
     * 卦象信息缓存前缀
     * 完整Key: hexagram:{hexagramId}
     */
    public static final String HEXAGRAM_PREFIX = "hexagram:";
    
    /**
     * 卦象缓存过期时间（秒）
     * 24小时
     */
    public static final long HEXAGRAM_TTL = 86400;
    
    /**
     * 占卜次数限制缓存前缀
     * 完整Key: divination:limit:{userId}:{date}
     */
    public static final String DIVINATION_LIMIT_PREFIX = "divination:limit:";
    
    /**
     * 占卜会话缓存前缀
     * 完整Key: divination:session:{sessionId}
     */
    public static final String DIVINATION_SESSION_PREFIX = "divination:session:";
    
    /**
     * 占卜会话过期时间（秒）
     * 30分钟
     */
    public static final long DIVINATION_SESSION_TTL = 1800;
}

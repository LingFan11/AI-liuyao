package com.lingfan.liuyao.enums;

/**
 * 错误码枚举
 * 统一管理系统中的所有错误码和错误消息
 * 
 * 错误码规范：
 * - 200: 成功
 * - 1000-1999: 通用错误
 * - 2000-2999: 用户相关错误
 * - 3000-3999: 占卜相关错误
 * - 4000-4999: 数据库相关错误
 * - 5000-5999: 第三方服务错误
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
public enum ErrorCode {
    
    // ==================== 通用错误码 (1000-1999) ====================
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_NULL(1002, "参数不能为空"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    SYSTEM_ERROR(1004, "系统错误"),
    UNKNOWN_ERROR(1005, "未知错误"),
    REQUEST_TIMEOUT(1006, "请求超时"),
    SERVICE_UNAVAILABLE(1007, "服务不可用"),
    
    // ==================== 用户相关错误码 (2000-2999) ====================
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    USERNAME_PASSWORD_ERROR(2003, "用户名或密码错误"),
    ACCOUNT_LOCKED(2004, "账号已被锁定"),
    ACCOUNT_DISABLED(2005, "账号已被禁用"),
    TOKEN_INVALID(2006, "Token无效"),
    TOKEN_EXPIRED(2007, "Token已过期"),
    TOKEN_MISSING(2008, "缺少Token"),
    UNAUTHORIZED(2009, "未授权访问"),
    PERMISSION_DENIED(2010, "权限不足"),
    LOGIN_FAILED(2011, "登录失败"),
    LOGOUT_FAILED(2012, "登出失败"),
    PASSWORD_ERROR(2013, "密码错误"),
    OLD_PASSWORD_ERROR(2014, "原密码错误"),
    EMAIL_ALREADY_EXISTS(2015, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(2016, "手机号已存在"),
    VERIFICATION_CODE_ERROR(2017, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(2018, "验证码已过期"),
    
    // ==================== 占卜相关错误码 (3000-3999) ====================
    DIVINATION_TIMES_LIMIT(3001, "占卜次数已用完"),
    DIVINATION_NOT_FOUND(3002, "占卜记录不存在"),
    DIVINATION_TYPE_ERROR(3003, "起卦方式错误"),
    DIVINATION_PARAM_ERROR(3004, "起卦参数错误"),
    HEXAGRAM_NOT_FOUND(3005, "卦象不存在"),
    INTERPRETATION_NOT_FOUND(3006, "解卦信息不存在"),
    DIVINATION_SESSION_EXPIRED(3007, "起卦会话已过期"),
    DIVINATION_SESSION_NOT_FOUND(3008, "起卦会话不存在"),
    DIVINATION_ALREADY_COMPLETED(3009, "起卦已完成"),
    YAO_COUNT_ERROR(3010, "爻数错误"),
    
    // ==================== 数据库相关错误码 (4000-4999) ====================
    DATABASE_ERROR(4001, "数据库操作失败"),
    INSERT_ERROR(4002, "插入数据失败"),
    UPDATE_ERROR(4003, "更新数据失败"),
    DELETE_ERROR(4004, "删除数据失败"),
    QUERY_ERROR(4005, "查询数据失败"),
    DATA_NOT_FOUND(4006, "数据不存在"),
    DATA_ALREADY_EXISTS(4007, "数据已存在"),
    
    // ==================== 第三方服务错误码 (5000-5999) ====================
    AI_SERVICE_ERROR(5001, "AI服务调用失败"),
    AI_RESPONSE_ERROR(5002, "AI响应解析失败"),
    REDIS_ERROR(5003, "Redis操作失败"),
    MONGODB_ERROR(5004, "MongoDB操作失败"),
    FILE_UPLOAD_ERROR(5005, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(5006, "文件下载失败"),
    SMS_SEND_ERROR(5007, "短信发送失败"),
    EMAIL_SEND_ERROR(5008, "邮件发送失败");
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    /**
     * 构造方法
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }
}

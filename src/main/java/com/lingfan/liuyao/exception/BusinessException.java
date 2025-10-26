package com.lingfan.liuyao.exception;

import com.lingfan.liuyao.enums.ErrorCode;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常情况
 * 
 * 使用场景：
 * - 参数校验失败
 * - 业务规则校验失败
 * - 数据不存在
 * - 权限校验失败
 * 
 * 示例：
 * <pre>
 * if (user == null) {
 *     throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * }
 * </pre>
 * 
 * 重构说明（2025-10-26）：
 * - 删除了重复的message字段，直接使用父类RuntimeException的message
 * - 避免了字段重复导致的数据不一致问题
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 构造方法 - 使用默认错误消息
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
    }
    
    /**
     * 构造方法 - 自定义错误码和消息
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造方法 - 使用错误码枚举
     * 
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    /**
     * 构造方法 - 使用错误码枚举和自定义消息
     * 
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }
    
    /**
     * 构造方法 - 包装其他异常
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
    }
    
    /**
     * 构造方法 - 使用错误码枚举并包装其他异常
     * 
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }
    
    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
}

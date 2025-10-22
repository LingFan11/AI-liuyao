package com.lingfan.liuyao.utils;

import com.lingfan.liuyao.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应类
 * 用于封装所有Controller返回的数据
 * 
 * 响应格式：
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": {...},
 *   "timestamp": 1698745632000
 * }
 * </pre>
 * 
 * 使用示例：
 * <pre>
 * // 成功响应，无数据
 * return ApiResponse.success();
 * 
 * // 成功响应，有数据
 * return ApiResponse.success(user);
 * 
 * // 失败响应
 * return ApiResponse.error("用户不存在");
 * return ApiResponse.error(ErrorCode.USER_NOT_FOUND);
 * </pre>
 * 
 * @param <T> 响应数据类型
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Data
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL) // null字段不序列化
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    @Schema(description = "状态码", example = "200")
    private Integer code;
    
    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;
    
    /**
     * 时间戳
     */
    @Schema(description = "响应时间戳", example = "1698745632000")
    private Long timestamp;
    
    /**
     * 私有构造方法，防止外部直接实例化
     */
    private ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 私有构造方法
     * 
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // ==================== 成功响应 ====================
    
    /**
     * 成功响应 - 无数据
     * 
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(
            ErrorCode.SUCCESS.getCode(),
            ErrorCode.SUCCESS.getMessage(),
            null
        );
    }
    
    /**
     * 成功响应 - 有数据
     * 
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            ErrorCode.SUCCESS.getCode(),
            ErrorCode.SUCCESS.getMessage(),
            data
        );
    }
    
    /**
     * 成功响应 - 自定义消息和数据
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
            ErrorCode.SUCCESS.getCode(),
            message,
            data
        );
    }
    
    /**
     * 成功响应 - 只有自定义消息
     * 
     * @param message 响应消息
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(
            ErrorCode.SUCCESS.getCode(),
            message,
            null
        );
    }
    
    // ==================== 失败响应 ====================
    
    /**
     * 失败响应 - 自定义消息
     * 
     * @param message 错误消息
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
            ErrorCode.SYSTEM_ERROR.getCode(),
            message,
            null
        );
    }
    
    /**
     * 失败响应 - 自定义状态码和消息
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 失败响应 - 使用错误码枚举
     * 
     * @param errorCode 错误码枚举
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
            errorCode.getCode(),
            errorCode.getMessage(),
            null
        );
    }
    
    /**
     * 失败响应 - 使用错误码枚举和自定义消息
     * 
     * @param errorCode 错误码枚举
     * @param customMessage 自定义消息
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(
            errorCode.getCode(),
            customMessage,
            null
        );
    }
    
    /**
     * 失败响应 - 包含错误数据
     * 
     * @param errorCode 错误码枚举
     * @param data 错误数据
     * @param <T> 响应数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(
            errorCode.getCode(),
            errorCode.getMessage(),
            data
        );
    }
    
    // ==================== 判断方法 ====================
    
    /**
     * 判断是否成功
     * 
     * @return true=成功, false=失败
     */
    public boolean isSuccess() {
        return ErrorCode.SUCCESS.getCode().equals(this.code);
    }
    
    /**
     * 判断是否失败
     * 
     * @return true=失败, false=成功
     */
    public boolean isError() {
        return !isSuccess();
    }
}

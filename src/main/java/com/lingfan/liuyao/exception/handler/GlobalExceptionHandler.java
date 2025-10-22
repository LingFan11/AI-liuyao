package com.lingfan.liuyao.exception.handler;

import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的所有异常，返回标准的ApiResponse格式
 * 
 * 处理的异常类型：
 * 1. BusinessException - 业务异常
 * 2. MethodArgumentNotValidException - @Valid参数校验异常
 * 3. BindException - 参数绑定异常
 * 4. ConstraintViolationException - @Validated参数校验异常
 * 5. MissingServletRequestParameterException - 缺少请求参数异常
 * 6. MethodArgumentTypeMismatchException - 参数类型不匹配异常
 * 7. Exception - 其他未知异常
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * 
     * @param e 业务异常
     * @return 响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理@Valid参数校验异常（用于@RequestBody）
     * 
     * @param e 参数校验异常
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
        // 获取所有字段错误信息
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数校验异常：{}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR, errorMessage);
    }
    
    /**
     * 处理参数绑定异常（用于form-data和@ModelAttribute）
     * 
     * @param e 参数绑定异常
     * @return 响应结果
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数绑定异常：{}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR, errorMessage);
    }
    
    /**
     * 处理@Validated参数校验异常（用于@RequestParam和@PathVariable）
     * 
     * @param e 参数校验异常
     * @return 响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数约束异常：{}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR, errorMessage);
    }
    
    /**
     * 处理缺少请求参数异常
     * 
     * @param e 缺少请求参数异常
     * @return 响应结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<?> handleMissingParameterException(MissingServletRequestParameterException e) {
        String errorMessage = String.format("缺少必需的参数：%s", e.getParameterName());
        log.warn("缺少请求参数：{}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_NULL, errorMessage);
    }
    
    /**
     * 处理参数类型不匹配异常
     * 
     * @param e 参数类型不匹配异常
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("参数类型错误：%s 应为 %s", 
                e.getName(), 
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型");
        log.warn("参数类型不匹配：{}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_TYPE_ERROR, errorMessage);
    }
    
    /**
     * 处理空指针异常
     * 
     * @param e 空指针异常
     * @return 响应结果
     */
    @ExceptionHandler(NullPointerException.class)
    public ApiResponse<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常：", e);
        return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "系统内部错误，请联系管理员");
    }
    
    /**
     * 处理非法参数异常
     * 
     * @param e 非法参数异常
     * @return 响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常：{}", e.getMessage());
        return ApiResponse.error(ErrorCode.PARAM_ERROR, e.getMessage());
    }
    
    /**
     * 处理其他未知异常
     * 捕获所有未被上面方法处理的异常
     * 
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常：", e);
        // 生产环境不要返回具体的异常信息，避免泄露系统信息
        return ApiResponse.error(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
    }
}

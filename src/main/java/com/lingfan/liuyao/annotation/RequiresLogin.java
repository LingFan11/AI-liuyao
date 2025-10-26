package com.lingfan.liuyao.annotation;

import java.lang.annotation.*;

/**
 * 需要登录注解
 * 
 * 标注在Controller方法或类上，表示该接口需要用户登录才能访问
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @GetMapping("/profile")
 * @RequiresLogin
 * public ApiResponse<UserVO> getProfile() {
 *     Long userId = UserContextHolder.getCurrentUserId();
 *     // 业务逻辑
 * }
 * }
 * </pre>
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresLogin {
    // 无参数，只要登录即可访问
}

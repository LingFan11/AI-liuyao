package com.lingfan.liuyao.annotation;

import java.lang.annotation.*;

/**
 * 需要权限注解
 * 
 * 标注在Controller方法或类上，表示该接口需要特定权限才能访问
 * 
 * 使用示例：
 * <pre>
 * {@code
 * // AND逻辑：必须同时拥有user:create和user:update权限
 * @PostMapping("/user")
 * @RequiresPermissions({"user:create", "user:update"})
 * public ApiResponse<String> createUser() { }
 * 
 * // OR逻辑：拥有user:view或user:list权限之一即可
 * @GetMapping("/users")
 * @RequiresPermissions(value = {"user:view", "user:list"}, logical = RequiresPermissions.Logical.OR)
 * public ApiResponse<List<UserVO>> listUsers() { }
 * }
 * </pre>
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {
    
    /**
     * 权限编码列表（对应permissions表的permission_code字段）
     * 
     * @return 权限编码数组
     */
    String[] value();
    
    /**
     * 逻辑关系
     * 
     * @return AND或OR逻辑
     */
    Logical logical() default Logical.AND;
    
    /**
     * 逻辑枚举
     */
    enum Logical {
        /**
         * AND逻辑：用户必须拥有所有指定权限（默认）
         */
        AND,
        
        /**
         * OR逻辑：用户拥有任意一个权限即可
         */
        OR
    }
}

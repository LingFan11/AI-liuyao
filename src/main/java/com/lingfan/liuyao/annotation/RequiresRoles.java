package com.lingfan.liuyao.annotation;

import java.lang.annotation.*;

/**
 * 需要角色注解
 * 
 * 标注在Controller方法或类上，表示该接口需要特定角色才能访问
 * 
 * 使用示例：
 * <pre>
 * {@code
 * // OR逻辑：拥有admin或vip_month角色之一即可
 * @GetMapping("/vip-feature")
 * @RequiresRoles({"admin", "vip_month"})
 * public ApiResponse<String> vipFeature() { }
 * 
 * // AND逻辑：必须同时拥有admin和manager角色
 * @GetMapping("/admin-feature")
 * @RequiresRoles(value = {"admin", "manager"}, logical = RequiresRoles.Logical.AND)
 * public ApiResponse<String> adminFeature() { }
 * }
 * </pre>
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {
    
    /**
     * 角色编码列表（对应roles表的role_code字段）
     * 
     * @return 角色编码数组
     */
    String[] value();
    
    /**
     * 逻辑关系
     * 
     * @return AND或OR逻辑
     */
    Logical logical() default Logical.OR;
    
    /**
     * 逻辑枚举
     */
    enum Logical {
        /**
         * AND逻辑：用户必须拥有所有指定角色
         */
        AND,
        
        /**
         * OR逻辑：用户拥有任意一个角色即可（默认）
         */
        OR
    }
}

package com.lingfan.liuyao.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密
 * 
 * BCrypt特点：
 * 1. 单向加密，无法解密
 * 2. 自动加盐，每次加密结果不同
 * 3. 慢速算法，防止暴力破解
 * 4. 安全性高，适合密码加密
 * 
 * 使用场景：
 * - 用户注册时加密密码
 * - 用户登录时验证密码
 * - 修改密码时加密新密码
 * 
 * 示例：
 * <pre>
 * // 加密
 * String encodedPassword = passwordEncoder.encode("123456");
 * 
 * // 验证
 * boolean matches = passwordEncoder.matches("123456", encodedPassword);
 * </pre>
 * 
 * @author Liuyao Team
 * @since 2025-10-22
 */
@Slf4j
@Component
public class PasswordEncoder {
    
    /**
     * BCrypt加密器
     * strength默认为10，可以调整加密强度（4-31）
     * 强度越高，加密越慢，安全性越高
     */
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    
    /**
     * 构造方法
     * 使用默认强度10
     */
    public PasswordEncoder() {
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * 构造方法（自定义强度）
     * 
     * @param strength 加密强度（4-31）
     */
    public PasswordEncoder(int strength) {
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder(strength);
    }
    
    /**
     * 加密密码
     * 使用BCrypt算法，自动加盐
     * 每次加密结果都不同，但都能验证通过
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码（60个字符）
     */
    public String encode(String rawPassword) {
        if (StrUtil.isBlank(rawPassword)) {
            throw new IllegalArgumentException("原始密码不能为空");
        }
        
        long startTime = System.currentTimeMillis();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        long endTime = System.currentTimeMillis();
        
        log.debug("密码加密完成，耗时：{}ms", endTime - startTime);
        return encodedPassword;
    }
    
    /**
     * 验证密码
     * 将原始密码与加密后的密码进行比对
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return true=密码正确, false=密码错误
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (StrUtil.isBlank(rawPassword)) {
            log.warn("验证密码失败：原始密码为空");
            return false;
        }
        
        if (StrUtil.isBlank(encodedPassword)) {
            log.warn("验证密码失败：加密密码为空");
            return false;
        }
        
        long startTime = System.currentTimeMillis();
        boolean matches = bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
        long endTime = System.currentTimeMillis();
        
        log.debug("密码验证完成，结果：{}，耗时：{}ms", matches, endTime - startTime);
        return matches;
    }
    
    /**
     * 判断密码是否需要重新加密
     * BCrypt的加密结果以$2a$开头
     * 
     * @param encodedPassword 加密后的密码
     * @return true=需要重新加密, false=不需要
     */
    public boolean upgradeEncoding(String encodedPassword) {
        if (StrUtil.isBlank(encodedPassword)) {
            return true;
        }
        
        // BCrypt密码格式：$2a$10$...（60个字符）
        if (!StrUtil.startWith(encodedPassword, "$2a$") && !StrUtil.startWith(encodedPassword, "$2b$")) {
            return true;
        }
        
        return bCryptPasswordEncoder.upgradeEncoding(encodedPassword);
    }
}

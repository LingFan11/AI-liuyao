package com.lingfan.liuyao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfan.liuyao.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 * 使用MyBatis-Plus，提供基础CRUD操作
 * 
 * 自定义查询使用LambdaQueryWrapper，无需XML
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户的角色列表
     * 联表查询：user_roles + roles
     * 
     * @param userId 用户ID
     * @return 角色编码列表（role_code）
     */
    @Select("SELECT r.role_code FROM user_roles ur " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    List<String> selectUserRoles(@Param("userId") Long userId);

    /**
     * 查询用户的权限列表（去重）
     * 联表查询：user_roles + role_permissions + permissions
     * 
     * @param userId 用户ID
     * @return 权限编码列表（permission_code）
     */
    @Select("SELECT DISTINCT p.permission_code FROM user_roles ur " +
            "JOIN role_permissions rp ON ur.role_id = rp.role_id " +
            "JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = 0")
    List<String> selectUserPermissions(@Param("userId") Long userId);
}

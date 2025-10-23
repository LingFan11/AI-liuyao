package com.lingfan.liuyao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfan.liuyao.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

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

}

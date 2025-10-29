package com.lingfan.liuyao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfan.liuyao.model.entity.Hexagram;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 起卦记录Mapper
 * 
 * <p>
 * 负责hexagrams表的数据库访问
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Mapper
public interface HexagramMapper extends BaseMapper<Hexagram> {
    
    /**
     * 根据用户ID查询起卦记录（按时间倒序）
     * 
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 起卦记录列表
     */
    @Select("SELECT * FROM hexagrams WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY divination_time DESC LIMIT #{limit}")
    List<Hexagram> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 根据签名查询起卦记录（用于防重复）
     * 
     * @param userId 用户ID
     * @param signature 同一性签名
     * @return 起卦记录
     */
    @Select("SELECT * FROM hexagrams WHERE user_id = #{userId} AND signature = #{signature} AND deleted = 0")
    Hexagram selectBySignature(@Param("userId") Long userId, @Param("signature") String signature);
    
    /**
     * 统计用户起卦总次数
     * 
     * @param userId 用户ID
     * @return 总次数
     */
    @Select("SELECT COUNT(*) FROM hexagrams WHERE user_id = #{userId} AND deleted = 0")
    Integer countByUserId(@Param("userId") Long userId);
}

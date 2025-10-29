package com.lingfan.liuyao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfan.liuyao.model.entity.DivinationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 占卜历史记录Mapper
 * 
 * <p>
 * 负责divination_histories表的数据库访问
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Mapper
public interface DivinationHistoryMapper extends BaseMapper<DivinationHistory> {
    
    /**
     * 根据用户ID和卦象ID查询历史记录
     * 
     * @param userId 用户ID
     * @param hexagramId 卦象ID
     * @return 历史记录
     */
    @Select("SELECT * FROM divination_histories " +
            "WHERE user_id = #{userId} AND hexagram_id = #{hexagramId} AND deleted = 0")
    DivinationHistory selectByUserAndHexagram(@Param("userId") Long userId, 
                                               @Param("hexagramId") Long hexagramId);
    
    /**
     * 查询用户的收藏列表
     * 
     * @param userId 用户ID
     * @return 收藏列表
     */
    @Select("SELECT * FROM divination_histories " +
            "WHERE user_id = #{userId} AND is_favorite = 1 AND deleted = 0 " +
            "ORDER BY favorite_time DESC")
    List<DivinationHistory> selectFavorites(@Param("userId") Long userId);
    
    /**
     * 增加查看次数
     * 
     * @param id 历史记录ID
     */
    @Update("UPDATE divination_histories " +
            "SET view_count = view_count + 1, last_viewed_at = NOW() " +
            "WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
}

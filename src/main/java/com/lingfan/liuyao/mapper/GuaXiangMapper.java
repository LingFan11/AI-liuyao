package com.lingfan.liuyao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfan.liuyao.model.entity.GuaXiang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 卦象Mapper
 * 
 * <p>
 * 负责卦象数据的数据库访问
 * 对应数据库表: gua_xiang_base
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Mapper
public interface GuaXiangMapper extends BaseMapper<GuaXiang> {
    
    /**
     * 根据上下卦组合查询卦象
     * 
     * <p>
     * 使用场景：根据6个爻的阴阳识别卦象
     * </p>
     * 
     * @param shangGua 上卦名称（如"乾"）
     * @param xiaGua 下卦名称（如"乾"）
     * @return 卦象对象
     */
    @Select("SELECT id, gua_name, suo_shu_gong, gong_wu_xing as gongWuXingName, " +
            "shi_yao_wei, ying_yao_wei, shang_gua, xia_gua, gua_lei_xing " +
            "FROM gua_xiang_base WHERE shang_gua = #{shangGua} AND xia_gua = #{xiaGua}")
    GuaXiang selectByGuaComposition(
            @Param("shangGua") String shangGua, 
            @Param("xiaGua") String xiaGua
    );
    
    /**
     * 根据卦名查询卦象
     * 
     * @param guaName 卦名（如"乾为天"）
     * @return 卦象对象
     */
    @Select("SELECT id, gua_name, suo_shu_gong, gong_wu_xing as gongWuXingName, " +
            "shi_yao_wei, ying_yao_wei, shang_gua, xia_gua, gua_lei_xing " +
            "FROM gua_xiang_base WHERE gua_name = #{guaName}")
    GuaXiang selectByGuaName(@Param("guaName") String guaName);
    
    /**
     * 根据所属宫查询所有卦象
     * 
     * @param suoShuGong 所属宫（如"乾宫"）
     * @return 卦象列表
     */
    @Select("SELECT id, gua_name, suo_shu_gong, gong_wu_xing as gongWuXingName, " +
            "shi_yao_wei, ying_yao_wei, shang_gua, xia_gua, gua_lei_xing " +
            "FROM gua_xiang_base WHERE suo_shu_gong = #{suoShuGong} ORDER BY id")
    java.util.List<GuaXiang> selectByGong(@Param("suoShuGong") String suoShuGong);
}

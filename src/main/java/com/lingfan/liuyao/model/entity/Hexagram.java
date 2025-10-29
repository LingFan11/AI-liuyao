package com.lingfan.liuyao.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 起卦记录实体
 * 
 * <p>
 * 对应数据库表：hexagrams
 * 用途：保存用户每次起卦的完整记录（包含时空信息、用神、卦象等）
 * </p>
 * 
 * <p>
 * 与gua_xiang_base的关系：
 * - original_hex_number 关联 gua_xiang_base.id（本卦）
 * - changed_hex_number 关联 gua_xiang_base.id（变卦）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Data
@TableName("hexagrams")
public class Hexagram {
    
    /**
     * 起卦记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 占卜问题
     */
    @TableField("question")
    private String question;
    
    /**
     * 占卜类别
     * career-事业, love-感情, wealth-财运, health-健康, study-学业, other-其他
     */
    @TableField("category")
    private String category;
    
    /**
     * 六爻编码（如：111000，1表示阳爻，0表示阴爻）
     */
    @TableField("hexagram_code")
    private String hexagramCode;
    
    /**
     * 本卦名称（如：乾为天）
     */
    @TableField("original_hex")
    private String originalHex;
    
    /**
     * 本卦序号（1-64，关联gua_xiang_base.id）
     */
    @TableField("original_hex_number")
    private Integer originalHexNumber;
    
    /**
     * 变卦名称（如有变爻）
     */
    @TableField("changed_hex")
    private String changedHex;
    
    /**
     * 变卦序号（1-64，关联gua_xiang_base.id）
     */
    @TableField("changed_hex_number")
    private Integer changedHexNumber;
    
    /**
     * 变爻位置（如：1,3,5表示初爻、三爻、五爻为变爻）
     */
    @TableField("changing_lines")
    private String changingLines;
    
    /**
     * 六爻详细信息（JSON数组，包含每爻的阴阳、五行、六亲等）
     */
    @TableField("yao_details")
    private String yaoDetails;
    
    /**
     * 卦宫（乾、坎、艮、震、巽、离、坤、兑）
     */
    @TableField("palace")
    private String palace;
    
    /**
     * 世爻位置（1-6）
     */
    @TableField("shi_line")
    private Integer shiLine;
    
    /**
     * 应爻位置（1-6）
     */
    @TableField("ying_line")
    private Integer yingLine;
    
    /**
     * 互卦序号（1-64）
     */
    @TableField("mutual_hex_number")
    private Integer mutualHexNumber;
    
    /**
     * 综卦序号（1-64）
     */
    @TableField("opposite_hex_number")
    private Integer oppositeHexNumber;
    
    /**
     * 纳甲排盘（JSON：六爻地支、六亲、六神等）
     */
    @TableField("na_jia")
    private String naJia;
    
    /**
     * 用神六亲（父母、兄弟、子孙、妻财、官鬼）
     */
    @TableField("yong_shen")
    private String yongShen;
    
    /**
     * 用神五行（金、木、水、火、土）
     */
    @TableField("yong_shen_element")
    private String yongShenElement;
    
    /**
     * 用神地支（子丑寅卯辰巳午未申酉戌亥）
     */
    @TableField("yong_shen_branch")
    private String yongShenBranch;
    
    /**
     * 用神所在爻位（1-6）
     */
    @TableField("yong_shen_line")
    private Integer yongShenLine;
    
    /**
     * 用神旺衰状态（旺、相、休、囚、死）
     */
    @TableField("yong_shen_state")
    private String yongShenState;
    
    /**
     * 元神爻位（生用神的爻）
     */
    @TableField("yuan_shen_line")
    private Integer yuanShenLine;
    
    /**
     * 元神五行
     */
    @TableField("yuan_shen_element")
    private String yuanShenElement;
    
    /**
     * 忌神爻位（克用神的爻）
     */
    @TableField("ji_shen_line")
    private Integer jiShenLine;
    
    /**
     * 忌神五行
     */
    @TableField("ji_shen_element")
    private String jiShenElement;
    
    /**
     * 仇神爻位（生忌神、克元神的爻）
     */
    @TableField("chou_shen_line")
    private Integer chouShenLine;
    
    /**
     * 仇神五行
     */
    @TableField("chou_shen_element")
    private String chouShenElement;
    
    /**
     * 空亡地支（如：子丑）
     */
    @TableField("kong_wang")
    private String kongWang;
    
    /**
     * 空亡爻位（如：1,3表示初爻、三爻空亡）
     */
    @TableField("kong_wang_lines")
    private String kongWangLines;
    
    /**
     * 月建（地支）
     */
    @TableField("yue_jian")
    private String yueJian;
    
    /**
     * 日支（地支）
     */
    @TableField("ri_zhi")
    private String riZhi;
    
    /**
     * 同一性签名（SHA-256）
     * 用于防止同一用户重复保存完全相同的卦象
     */
    @TableField("signature")
    private String signature;
    
    /**
     * 起卦方式
     * 1-手动投币, 2-时间起卦, 3-数字起卦, 4-随机起卦
     */
    @TableField("method")
    private Integer method;
    
    /**
     * 起卦详情（JSON格式，记录起卦的具体参数）
     */
    @TableField("method_detail")
    private String methodDetail;
    
    /**
     * 占卜时间
     */
    @TableField("divination_time")
    private LocalDateTime divinationTime;
    
    /**
     * 占卜地点
     */
    @TableField("location")
    private String location;
    
    /**
     * 天气情况
     */
    @TableField("weather")
    private String weather;
    
    /**
     * 农历日期
     */
    @TableField("lunar_date")
    private String lunarDate;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标志：0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

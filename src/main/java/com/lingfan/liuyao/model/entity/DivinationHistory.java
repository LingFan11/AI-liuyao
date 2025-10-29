package com.lingfan.liuyao.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 占卜历史记录实体
 * 
 * <p>
 * 对应数据库表：divination_histories
 * 用途：记录用户对卦象的访问、收藏、验证等信息
 * </p>
 * 
 * <p>
 * 与hexagrams的关系：
 * - hexagram_id 关联 hexagrams.id
 * - 一个用户对同一个卦象只能有一条历史记录（唯一约束）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Data
@TableName("divination_histories")
public class DivinationHistory {
    
    /**
     * 历史记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 卦象ID（关联hexagrams表）
     */
    @TableField("hexagram_id")
    private Long hexagramId;
    
    /**
     * 解释ID（关联interpretations表，可为空）
     */
    @TableField("interpretation_id")
    private Long interpretationId;
    
    /**
     * 是否收藏
     */
    @TableField("is_favorite")
    private Boolean isFavorite;
    
    /**
     * 收藏时间
     */
    @TableField("favorite_time")
    private LocalDateTime favoriteTime;
    
    /**
     * 用户备注
     */
    @TableField("notes")
    private String notes;
    
    /**
     * 标签（逗号分隔）
     */
    @TableField("tags")
    private String tags;
    
    /**
     * 是否已验证结果
     */
    @TableField("is_verified")
    private Boolean isVerified;
    
    /**
     * 验证结果
     * accurate-准确, inaccurate-不准确, partially-部分准确
     */
    @TableField("verification_result")
    private String verificationResult;
    
    /**
     * 验证备注
     */
    @TableField("verification_notes")
    private String verificationNotes;
    
    /**
     * 验证时间
     */
    @TableField("verification_time")
    private LocalDateTime verificationTime;
    
    /**
     * 查看次数
     */
    @TableField("view_count")
    private Integer viewCount;
    
    /**
     * 最后查看时间
     */
    @TableField("last_viewed_at")
    private LocalDateTime lastViewedAt;
    
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

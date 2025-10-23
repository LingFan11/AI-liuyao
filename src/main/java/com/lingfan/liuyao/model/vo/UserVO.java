package com.lingfan.liuyao.model.vo;

import com.lingfan.liuyao.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户VO（视图对象）
 * 返回给前端，不包含敏感信息
 * 
 * 敏感信息不返回：
 * - password（密码）
 * - email（邮箱）
 * - phone（手机号）
 * - loginFailedCount（登录失败次数）
 * - lastLoginIp（最后登录IP）
 * - status（账号状态）
 * - deleted（删除标志）
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Data
@Schema(description = "用户信息视图对象")
public class UserVO {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名", example = "zhangsan")
    private String username;
    
    @Schema(description = "昵称", example = "张三")
    private String nickname;
    
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @Schema(description = "个性签名", example = "人生如梦，转瞬即逝")
    private String signature;
    
    @Schema(description = "用户等级", example = "1")
    private Integer level;
    
    @Schema(description = "经验值", example = "0")
    private Integer experience;
    
    @Schema(description = "VIP类型（0-普通，1-月度，2-年度）", example = "0")
    private Integer vipType;
    
    @Schema(description = "VIP到期时间")
    private LocalDateTime vipExpireTime;
    
    @Schema(description = "今日占卜次数", example = "0")
    private Integer dailyDivinationCount;
    
    @Schema(description = "总占卜次数", example = "0")
    private Integer totalDivinationCount;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    /**
     * 从User实体转换为UserVO
     * 只复制非敏感字段
     * 
     * @param user 用户实体
     * @return UserVO对象
     */
    public static UserVO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setSignature(user.getSignature());
        vo.setLevel(user.getLevel());
        vo.setExperience(user.getExperience());
        vo.setVipType(user.getVipType());
        vo.setVipExpireTime(user.getVipExpireTime());
        vo.setDailyDivinationCount(user.getDailyDivinationCount());
        vo.setTotalDivinationCount(user.getTotalDivinationCount());
        vo.setCreatedAt(user.getCreatedAt());
        
        return vo;
    }
}

package com.lingfan.liuyao.model.dto.request;

import cn.hutool.core.util.StrUtil;
import com.lingfan.liuyao.constant.BusinessConstants;
import com.lingfan.liuyao.enums.ErrorCode;
import com.lingfan.liuyao.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 更新用户信息请求DTO
 * 
 * @author Liuyao Team
 * @since 2025-10-26
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateProfileRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 昵称（2-20字符）
     */
    @Schema(description = "昵称（2-20字符）", example = "易学爱好者")
    private String nickname;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "/files/avatars/123/avatar_1698765432.jpg")
    private String avatar;
    
    /**
     * 个性签名（0-200字符）
     */
    @Schema(description = "个性签名（0-200字符）", example = "万事皆可问卦")
    private String signature;
    
    /**
     * 校验请求参数
     * 
     * @throws BusinessException 参数不合法时抛出异常
     */
    public void validate() {
        // 校验昵称
        if (nickname != null && !nickname.isEmpty()) {
            int length = nickname.length();
            if (length < BusinessConstants.MIN_NICKNAME_LENGTH) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "昵称长度不能少于" + BusinessConstants.MIN_NICKNAME_LENGTH + "个字符");
            }
            if (length > BusinessConstants.MAX_NICKNAME_LENGTH) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "昵称长度不能超过" + BusinessConstants.MAX_NICKNAME_LENGTH + "个字符");
            }
            
            // 校验昵称格式（不允许特殊字符）
            if (!nickname.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$")) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "昵称只能包含中文、字母、数字和下划线");
            }
        }
        
        // 校验头像URL格式
        if (avatar != null && !avatar.isEmpty()) {
            if (!avatar.startsWith("http://") && !avatar.startsWith("https://") && !avatar.startsWith("/")) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "头像URL格式不正确");
            }
        }
        
        // 校验签名长度
        if (signature != null) {
            int length = signature.length();
            if (length > BusinessConstants.MAX_SIGNATURE_LENGTH) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                    "个性签名长度不能超过" + BusinessConstants.MAX_SIGNATURE_LENGTH + "个字符");
            }
        }
    }
    
    /**
     * 判断是否有字段需要更新
     * 
     * @return true=有字段需要更新, false=所有字段都为空
     */
    public boolean hasFieldsToUpdate() {
        return StrUtil.isNotBlank(nickname) 
            || StrUtil.isNotBlank(avatar) 
            || signature != null;  // 允许清空签名
    }
}

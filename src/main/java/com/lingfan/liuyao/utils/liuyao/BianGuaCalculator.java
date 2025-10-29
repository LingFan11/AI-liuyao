package com.lingfan.liuyao.utils.liuyao;

import com.lingfan.liuyao.model.entity.GuaXiang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 变卦计算器
 * 
 * <p>
 * 根据本卦和动爻位置计算变卦
 * 核心逻辑：动爻变化（阳爻→阴爻，阴爻→阳爻），生成新的二进制编码，识别变卦
 * </p>
 * 
 * <p>
 * 业务流程：
 * 1. 获取本卦的二进制编码
 * 2. 根据动爻位置反转对应位的值（1→0，0→1）
 * 3. 生成新的二进制编码
 * 4. 调用GuaXiangIdentifier识别变卦
 * 5. 返回变卦GuaXiang
 * </p>
 * 
 * <p>
 * 特殊规则：
 * - 无动爻：变卦=null
 * - 1-5个动爻：正常计算变卦
 * - 6个全动：正常计算变卦（用变卦的世爻断）
 * </p>
 * 
 * <p>
 * 示例：
 * - 本卦: 乾为天(111111)，初爻动 → 变卦: 天风姤(111110)
 * - 本卦: 坤为地(000000)，初爻动 → 变卦: 地雷复(000001)
 * </p>
 * 
 * <p>
 * 知识库: knowledge-liuyao02.md (256-343行)
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Component
public class BianGuaCalculator {
    
    private static final Logger log = LoggerFactory.getLogger(BianGuaCalculator.class);
    
    @Autowired
    private GuaXiangIdentifier identifier;
    
    /**
     * 计算变卦
     * 
     * @param benGua 本卦
     * @param dongYaoPositions 动爻位置列表（1-6），如[1, 3]表示初爻和三爻动
     * @return 变卦GuaXiang，如果无动爻返回null
     * @throws IllegalArgumentException 参数错误
     */
    public GuaXiang calculate(GuaXiang benGua, List<Integer> dongYaoPositions) {
        // 1. 参数校验
        if (benGua == null) {
            throw new IllegalArgumentException("本卦不能为空");
        }
        
        // 2. 无动爻，返回null
        if (dongYaoPositions == null || dongYaoPositions.isEmpty()) {
            log.debug("无动爻，不计算变卦");
            return null;
        }
        
        // 3. 验证动爻位置
        for (Integer position : dongYaoPositions) {
            if (position < 1 || position > 6) {
                throw new IllegalArgumentException(
                        String.format("动爻位置必须在1-6之间，当前值: %d", position)
                );
            }
        }
        
        log.debug("开始计算变卦 - 本卦: {}, 动爻位置: {}", benGua.getGuaName(), dongYaoPositions);
        
        // 4. 获取本卦二进制编码
        String benGuaCode = benGua.getBinaryCode();
        if (benGuaCode == null || benGuaCode.length() != 6) {
            throw new IllegalStateException(
                    String.format("本卦二进制编码无效: %s", benGuaCode)
            );
        }
        
        // 5. 反转动爻位置的值
        char[] codeArray = benGuaCode.toCharArray();
        for (int position : dongYaoPositions) {
            int index = position - 1;  // 爻位从1开始，数组从0开始
            char originalValue = codeArray[index];
            codeArray[index] = (originalValue == '1') ? '0' : '1';
            log.debug("第{}爻变化: {} → {}", position, originalValue, codeArray[index]);
        }
        
        // 6. 生成新的二进制编码
        String bianGuaCode = new String(codeArray);
        log.debug("变卦二进制编码: {} → {}", benGuaCode, bianGuaCode);
        
        // 7. 识别变卦
        GuaXiang bianGua = identifier.identify(bianGuaCode);
        
        log.info("变卦计算成功 - 本卦: {}, 动爻: {}, 变卦: {}", 
                benGua.getGuaName(), 
                dongYaoPositions, 
                bianGua.getGuaName());
        
        return bianGua;
    }
    
    /**
     * 判断是否全动（六爻皆动）
     * 
     * @param dongYaoPositions 动爻位置列表
     * @return 是否全动
     */
    public boolean isQuanDong(List<Integer> dongYaoPositions) {
        return dongYaoPositions != null && dongYaoPositions.size() == 6;
    }
    
    /**
     * 判断是否静卦（无动爻）
     * 
     * @param dongYaoPositions 动爻位置列表
     * @return 是否静卦
     */
    public boolean isJingGua(List<Integer> dongYaoPositions) {
        return dongYaoPositions == null || dongYaoPositions.isEmpty();
    }
}

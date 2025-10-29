package com.lingfan.liuyao.utils.liuyao;

import com.lingfan.liuyao.enums.BaGua;
import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.LiuQin;
import com.lingfan.liuyao.enums.WuXing;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.mapper.GuaXiangMapper;
import com.lingfan.liuyao.model.entity.GuaXiang;
import com.lingfan.liuyao.model.entity.Yao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 卦象识别器
 * 
 * <p>
 * 根据6个爻的阴阳组合识别对应的64卦之一
 * 核心逻辑：将6个爻转换为二进制，拆分上下卦，查询数据库
 * </p>
 * 
 * <p>
 * 业务流程：
 * 1. 接收6位二进制字符串（阳=1，阴=0，从初爻到上爻）
 * 2. 拆分为下卦（初爻、二爻、三爻）和上卦（四爻、五爻、上爻）
 * 3. 转换为八卦枚举
 * 4. 查询数据库匹配卦象
 * 5. 返回完整的GuaXiang对象
 * </p>
 * 
 * <p>
 * 示例：
 * - "111111" → 下卦=乾(111)，上卦=乾(111) → 乾为天
 * - "000000" → 下卦=坤(000)，上卦=坤(000) → 坤为地
 * - "100100" → 下卦=震(100)，上卦=震(100) → 震为雷
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Component
public class GuaXiangIdentifier {
    
    private static final Logger log = LoggerFactory.getLogger(GuaXiangIdentifier.class);
    
    @Autowired
    private GuaXiangMapper guaXiangMapper;
    
    /**
     * 根据二进制编码识别卦象
     * 
     * @param binaryCode 6位二进制字符串（如"111111"），从初爻到上爻
     * @return GuaXiang对象（包含完整的卦象信息）
     * @throws IllegalArgumentException 二进制编码格式错误
     * @throws BusinessException 数据库中不存在该卦象
     */
    public GuaXiang identify(String binaryCode) {
        // 1. 参数校验
        validateBinaryCode(binaryCode);
        
        // 2. 拆分上下卦
        String xiaGuaCode = binaryCode.substring(0, 3);  // 初爻、二爻、三爻
        String shangGuaCode = binaryCode.substring(3, 6); // 四爻、五爻、上爻
        
        log.debug("识别卦象 - 二进制: {}, 下卦: {}, 上卦: {}", binaryCode, xiaGuaCode, shangGuaCode);
        
        // 3. 转换为八卦
        BaGua xiaGua = BaGua.getByBinaryCode(xiaGuaCode);
        BaGua shangGua = BaGua.getByBinaryCode(shangGuaCode);
        
        if (xiaGua == null || shangGua == null) {
            String errorMsg = String.format("无法识别八卦 - 下卦编码: %s, 上卦编码: %s", xiaGuaCode, shangGuaCode);
            log.error(errorMsg);
            throw new BusinessException(errorMsg);
        }
        
        log.debug("八卦识别成功 - 下卦: {}({}), 上卦: {}({})", 
                xiaGua.getName(), xiaGuaCode, 
                shangGua.getName(), shangGuaCode);
        
        // 4. 查询数据库
        GuaXiang guaXiang = guaXiangMapper.selectByGuaComposition(
                shangGua.getName(), 
                xiaGua.getName()
        );
        
        if (guaXiang == null) {
            String errorMsg = String.format("数据库中不存在该卦象 - 上卦: %s, 下卦: %s", 
                    shangGua.getName(), xiaGua.getName());
            log.error(errorMsg);
            throw new BusinessException(errorMsg);
        }
        
        log.info("卦象识别成功 - {}", guaXiang.getGuaName());
        log.debug("卦象详细信息 - ID:{}, 宫:{}, 宫五行名称:{}, 世爻位:{}", 
                guaXiang.getId(), 
                guaXiang.getSuoShuGong(), 
                guaXiang.getGongWuXingName(),
                guaXiang.getShiYaoWei());
        
        // 5. 生成爻列表（使用纳甲配置和六亲生成）
        java.util.List<Yao> yaoList = generateYaoList(guaXiang);
        
        // 6. 使用Builder重新构建包含爻列表的GuaXiang
        GuaXiang completeGuaXiang = new GuaXiang.Builder()
                .id(guaXiang.getId())
                .guaName(guaXiang.getGuaName())
                .suoShuGong(guaXiang.getSuoShuGong())
                .gongWuXingName(guaXiang.getGongWuXingName())
                .shiYaoWei(guaXiang.getShiYaoWei())
                .yingYaoWei(guaXiang.getYingYaoWei())
                .shangGuaName(guaXiang.getShangGuaName())
                .xiaGuaName(guaXiang.getXiaGuaName())
                .guaLeiXing(guaXiang.getGuaLeiXing())
                .yaoList(yaoList)
                .build();
        
        log.debug("爻列表生成成功，共{}个爻", yaoList.size());
        
        // 7. 返回完整的卦象
        return completeGuaXiang;
    }
    
    /**
     * 根据卦名识别卦象
     * 
     * @param guaName 卦名（如"乾为天"）
     * @return GuaXiang对象
     * @throws BusinessException 卦名不存在
     */
    public GuaXiang identifyByName(String guaName) {
        if (guaName == null || guaName.trim().isEmpty()) {
            throw new IllegalArgumentException("卦名不能为空");
        }
        
        GuaXiang guaXiang = guaXiangMapper.selectByGuaName(guaName);
        
        if (guaXiang == null) {
            String errorMsg = String.format("数据库中不存在卦象: %s", guaName);
            log.error(errorMsg);
            throw new BusinessException(errorMsg);
        }
        
        log.info("根据卦名识别成功 - {}", guaName);
        
        // 生成爻列表并重新构建完整卦象
        java.util.List<Yao> yaoList = generateYaoList(guaXiang);
        GuaXiang completeGuaXiang = new GuaXiang.Builder()
                .id(guaXiang.getId())
                .guaName(guaXiang.getGuaName())
                .suoShuGong(guaXiang.getSuoShuGong())
                .gongWuXingName(guaXiang.getGongWuXingName())
                .shiYaoWei(guaXiang.getShiYaoWei())
                .yingYaoWei(guaXiang.getYingYaoWei())
                .shangGuaName(guaXiang.getShangGuaName())
                .xiaGuaName(guaXiang.getXiaGuaName())
                .guaLeiXing(guaXiang.getGuaLeiXing())
                .yaoList(yaoList)
                .build();
        
        return completeGuaXiang;
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 生成六爻列表
     * 
     * @param guaXiang 卦象（从数据库查询）
     * @return 6个爻的列表
     */
    private java.util.List<Yao> generateYaoList(GuaXiang guaXiang) {
        // 1. 获取宫五行
        WuXing gongWuXing = WuXing.getByName(guaXiang.getGongWuXingName());
        if (gongWuXing == null) {
            throw new BusinessException("宫五行无效: " + guaXiang.getGongWuXingName());
        }
        
        // 2. 获取纳甲序列
        DiZhi[] naJiaSeq = NaJiaConfigurator.getNaJiaSequence(guaXiang.getSuoShuGong());
        
        // 3. 生成6个爻
        java.util.List<Yao> yaoList = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int weiZhi = i + 1;
            DiZhi diZhi = naJiaSeq[i];
            
            // 生成六亲
            LiuQin liuQin = LiuQinGenerator.generate(gongWuXing, diZhi);
            
            // 创建静爻（初始都是静爻，动静信息由起卦方法决定）
            Yao yao = Yao.createJingYao(weiZhi, diZhi, liuQin);
            yaoList.add(yao);
        }
        
        return yaoList;
    }
    
    /**
     * 验证二进制编码格式
     * 
     * @param binaryCode 二进制编码
     * @throws IllegalArgumentException 格式错误
     */
    private void validateBinaryCode(String binaryCode) {
        if (binaryCode == null) {
            throw new IllegalArgumentException("二进制编码不能为空");
        }
        
        if (binaryCode.length() != 6) {
            throw new IllegalArgumentException(
                    String.format("二进制编码必须是6位，当前长度: %d", binaryCode.length())
            );
        }
        
        // 验证是否只包含0和1
        if (!binaryCode.matches("[01]{6}")) {
            throw new IllegalArgumentException(
                    String.format("二进制编码只能包含0和1，当前值: %s", binaryCode)
            );
        }
    }
}

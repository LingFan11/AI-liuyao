package com.lingfan.liuyao.service.divination.method;

import com.lingfan.liuyao.constant.DivinationConstants;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.DivinationResult;
import com.lingfan.liuyao.model.dto.request.DivinationRequest;
import com.lingfan.liuyao.model.dto.request.ManualDivinationRequest;
import com.lingfan.liuyao.model.entity.GuaXiang;
import com.lingfan.liuyao.model.entity.Yao;
import com.lingfan.liuyao.service.divination.DivinationMethod;
import com.lingfan.liuyao.utils.liuyao.BianGuaCalculator;
import com.lingfan.liuyao.utils.liuyao.GuaXiangIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 手动输入卦象法
 * 
 * <p>
 * 使用场景：
 * - 用户线下已起卦（摇硬币、蓍草等），仅需系统解卦
 * - 用户从书籍或其他来源获得卦象
 * - 用户想测试特定卦象
 * </p>
 * 
 * <p>
 * 业务流程：
 * 1. 验证ManualDivinationRequest参数（时空信息、6个爻）
 * 2. 将6个爻的阴阳转换为二进制编码
 * 3. 调用GuaXiangIdentifier识别本卦
 * 4. 提取动爻位置列表
 * 5. 调用BianGuaCalculator计算变卦
 * 6. 构建完整的爻列表（结合本卦纳甲配置和动静标识）
 * 7. 封装DivinationResult返回
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Component
public class ManualInputDivinationMethod implements DivinationMethod {
    
    private static final Logger log = LoggerFactory.getLogger(ManualInputDivinationMethod.class);
    
    @Autowired
    private GuaXiangIdentifier identifier;
    
    @Autowired
    private BianGuaCalculator calculator;
    
    @Override
    public DivinationResult cast(DivinationRequest request) {
        log.info("开始手动输入法起卦");
        
        // 1. 类型转换和验证
        ManualDivinationRequest manualRequest = validateAndCast(request);
        
        // 2. 验证时空信息
        if (!manualRequest.hasValidTimeInfo()) {
            throw new BusinessException(DivinationConstants.ERROR_MISSING_TIME_INFO);
        }
        
        // 3. 验证爻列表
        if (!manualRequest.hasValidYaoList()) {
            throw new BusinessException(DivinationConstants.ERROR_INVALID_YAO_COUNT);
        }
        
        log.debug("请求验证通过 - 时空信息: 日干={}, 日辰={}, 月建={}", 
                manualRequest.getRiGan(), 
                manualRequest.getRiChen(), 
                manualRequest.getYueJian());
        
        // 4. 转换为二进制编码
        String binaryCode = buildBinaryCode(manualRequest.getYaoInputList());
        log.debug("二进制编码: {}", binaryCode);
        
        // 5. 识别本卦
        GuaXiang benGua = identifier.identify(binaryCode);
        log.info("本卦识别成功: {}", benGua.getGuaName());
        
        // 6. 提取动爻位置
        List<Integer> dongYaoPositions = extractDongYaoPositions(manualRequest.getYaoInputList());
        log.debug("动爻位置: {}", dongYaoPositions);
        
        // 7. 计算变卦
        GuaXiang bianGua = calculator.calculate(benGua, dongYaoPositions);
        if (bianGua != null) {
            log.info("变卦计算成功: {}", bianGua.getGuaName());
        } else {
            log.info("无动爻，不计算变卦");
        }
        
        // 8. 构建爻列表（结合本卦纳甲配置和动静标识）
        List<Yao> yaoList = buildYaoList(benGua, manualRequest.getYaoInputList(), bianGua);
        
        // 9. 封装返回结果
        DivinationResult result = new DivinationResult.Builder()
                .benGua(benGua)
                .bianGua(bianGua)
                .yaoList(yaoList)
                .dongYaoCount(dongYaoPositions.size())
                .methodType(getMethodType())
                .methodName(getMethodName())
                .createTime(manualRequest.getOrDefaultDivinationTime())
                .build();
        
        log.info("手动输入法起卦完成 - 本卦: {}, 变卦: {}, 动爻数: {}", 
                benGua.getGuaName(), 
                bianGua != null ? bianGua.getGuaName() : "无", 
                dongYaoPositions.size());
        
        return result;
    }
    
    @Override
    public String getMethodName() {
        return "手动输入法";
    }
    
    @Override
    public String getMethodType() {
        return DivinationConstants.METHOD_MANUAL;
    }
    
    @Override
    public String getMethodDescription() {
        return "用户线下已起卦，输入6个爻的阴阳和动静信息，系统识别卦象并解卦";
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 验证并转换请求类型
     * 
     * @param request 起卦请求
     * @return ManualDivinationRequest
     * @throws IllegalArgumentException 类型错误
     */
    private ManualDivinationRequest validateAndCast(DivinationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("起卦请求不能为空");
        }
        
        if (!(request instanceof ManualDivinationRequest)) {
            throw new IllegalArgumentException(
                    String.format("请求类型错误，期望: ManualDivinationRequest, 实际: %s", 
                            request.getClass().getSimpleName())
            );
        }
        
        return (ManualDivinationRequest) request;
    }
    
    /**
     * 将爻输入列表转换为二进制编码
     * 
     * @param yaoInputList 爻输入列表
     * @return 6位二进制字符串（阳=1，阴=0）
     */
    private String buildBinaryCode(List<ManualDivinationRequest.YaoInput> yaoInputList) {
        StringBuilder code = new StringBuilder();
        for (ManualDivinationRequest.YaoInput yaoInput : yaoInputList) {
            // 阳爻=1，阴爻=0
            code.append(yaoInput.isYang() ? "1" : "0");
        }
        return code.toString();
    }
    
    /**
     * 提取动爻位置列表
     * 
     * @param yaoInputList 爻输入列表
     * @return 动爻位置列表（1-6）
     */
    private List<Integer> extractDongYaoPositions(List<ManualDivinationRequest.YaoInput> yaoInputList) {
        List<Integer> positions = new ArrayList<>();
        for (ManualDivinationRequest.YaoInput yaoInput : yaoInputList) {
            if (yaoInput.isDong()) {
                positions.add(yaoInput.getWeiZhi());
            }
        }
        return positions;
    }
    
    /**
     * 构建完整的爻列表
     * 
     * <p>
     * 核心逻辑：
     * 1. 从本卦的GuaXiang获取纳甲配置的爻列表（包含地支、六亲）
     * 2. 结合用户输入的动静标识
     * 3. 如果是动爻，关联变爻（从变卦的对应位置获取）
     * 4. 返回完整的6个Yao对象
     * </p>
     * 
     * @param benGua 本卦
     * @param yaoInputList 用户输入的爻列表（动静信息）
     * @param bianGua 变卦（可能为null）
     * @return 完整的爻列表
     */
    private List<Yao> buildYaoList(GuaXiang benGua, 
                                    List<ManualDivinationRequest.YaoInput> yaoInputList, 
                                    GuaXiang bianGua) {
        // 从本卦获取纳甲配置的爻列表
        List<Yao> benGuaYaoList = benGua.getYaoList();
        
        if (benGuaYaoList == null || benGuaYaoList.size() != 6) {
            throw new BusinessException(
                    String.format("本卦爻列表无效，卦名: %s, 爻数量: %d", 
                            benGua.getGuaName(), 
                            benGuaYaoList != null ? benGuaYaoList.size() : 0)
            );
        }
        
        // 结合动静标识构建最终的爻列表
        List<Yao> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Yao benYao = benGuaYaoList.get(i);
            ManualDivinationRequest.YaoInput input = yaoInputList.get(i);
            
            if (input.isDong()) {
                // 动爻：需要构建变爻
                Yao bianYao = null;
                if (bianGua != null && bianGua.getYaoList() != null && bianGua.getYaoList().size() > i) {
                    // 从变卦的对应位置获取变爻（地支和六亲）
                    Yao bianGuaYao = bianGua.getYaoList().get(i);
                    bianYao = Yao.createJingYao(
                            bianGuaYao.getWeiZhi(),
                            bianGuaYao.getDiZhi(),
                            bianGuaYao.getLiuQin()
                    );
                }
                
                // 创建动爻
                Yao yao = Yao.createDongYao(
                        benYao.getWeiZhi(),
                        benYao.getDiZhi(),
                        benYao.getLiuQin(),
                        bianYao
                );
                result.add(yao);
            } else {
                // 静爻：直接使用本卦的爻
                result.add(benYao);
            }
        }
        
        return result;
    }
}

package com.lingfan.liuyao.service.divination.method;

import com.lingfan.liuyao.constant.DivinationConstants;
import com.lingfan.liuyao.exception.BusinessException;
import com.lingfan.liuyao.model.dto.DivinationResult;
import com.lingfan.liuyao.model.dto.request.CoinDivinationRequest;
import com.lingfan.liuyao.model.dto.request.DivinationRequest;
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
import java.util.Random;

/**
 * 钱币起卦法（自动模拟）
 * 
 * <p>
 * 使用场景：
 * - 系统自动模拟摇硬币过程
 * - 用户只需提供时空信息，系统随机生成卦象
 * </p>
 * 
 * <p>
 * 业务流程：
 * 1. 验证CoinDivinationRequest参数（时空信息必填）
 * 2. 模拟6次投币（从初爻到上爻）
 * 3. 每次投3枚硬币，根据正反面数量判断爻的类型
 * 4. 将6次结果转换为二进制编码
 * 5. 调用GuaXiangIdentifier识别本卦
 * 6. 提取动爻位置列表
 * 7. 调用BianGuaCalculator计算变卦
 * 8. 构建完整的爻列表
 * 9. 封装DivinationResult返回
 * </p>
 * 
 * <p>
 * 钱币规则（知识库: knowledge-liuyao01.md 107-218行）：
 * - 3个正面（三阳）→ 老阴（6）→ 动爻 → 阴爻（画作 - - ×）
 * - 3个反面（三阴）→ 老阳（9）→ 动爻 → 阳爻（画作 ━━━ ○）
 * - 2个正面1反面 → 少阳（7）→ 静爻 → 阳爻（画作 ━━━）
 * - 1个正面2反面 → 少阴（8）→ 静爻 → 阴爻（画作 - -）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Component
public class CoinDivinationMethod implements DivinationMethod {
    
    private static final Logger log = LoggerFactory.getLogger(CoinDivinationMethod.class);
    
    /**
     * 每次投币的硬币数量
     */
    private static final int COINS_PER_THROW = 3;
    
    /**
     * 投币次数（对应6个爻）
     */
    private static final int THROW_TIMES = 6;
    
    @Autowired
    private GuaXiangIdentifier identifier;
    
    @Autowired
    private BianGuaCalculator calculator;
    
    @Override
    public DivinationResult cast(DivinationRequest request) {
        log.info("开始钱币起卦法");
        
        // 1. 类型转换和验证
        CoinDivinationRequest coinRequest = validateAndCast(request);
        
        // 2. 验证时空信息
        if (!coinRequest.hasValidTimeInfo()) {
            throw new BusinessException(DivinationConstants.ERROR_MISSING_TIME_INFO);
        }
        
        log.debug("请求验证通过 - 时空信息: 日干={}, 日辰={}, 月建={}", 
                coinRequest.getRiGan(), 
                coinRequest.getRiChen(), 
                coinRequest.getYueJian());
        
        // 3. 创建随机数生成器
        Random random = createRandom(coinRequest.getRandomSeed());
        
        // 4. 模拟6次投币，生成6个爻
        List<YaoThrowResult> throwResults = simulateThrows(random);
        
        // 5. 记录投币结果
        logThrowResults(throwResults);
        
        // 6. 转换为二进制编码
        String binaryCode = buildBinaryCode(throwResults);
        log.debug("二进制编码: {}", binaryCode);
        
        // 7. 识别本卦
        GuaXiang benGua = identifier.identify(binaryCode);
        log.info("本卦识别成功: {}", benGua.getGuaName());
        
        // 8. 提取动爻位置
        List<Integer> dongYaoPositions = extractDongYaoPositions(throwResults);
        log.debug("动爻位置: {}", dongYaoPositions);
        
        // 9. 计算变卦
        GuaXiang bianGua = calculator.calculate(benGua, dongYaoPositions);
        if (bianGua != null) {
            log.info("变卦计算成功: {}", bianGua.getGuaName());
        } else {
            log.info("无动爻，不计算变卦");
        }
        
        // 10. 构建爻列表
        List<Yao> yaoList = buildYaoList(benGua, throwResults, bianGua);
        
        // 11. 封装返回结果
        DivinationResult result = new DivinationResult.Builder()
                .benGua(benGua)
                .bianGua(bianGua)
                .yaoList(yaoList)
                .dongYaoCount(dongYaoPositions.size())
                .methodType(getMethodType())
                .methodName(getMethodName())
                .createTime(coinRequest.getOrDefaultDivinationTime())
                .build();
        
        log.info("钱币起卦法完成 - 本卦: {}, 变卦: {}, 动爻数: {}", 
                benGua.getGuaName(), 
                bianGua != null ? bianGua.getGuaName() : "无", 
                dongYaoPositions.size());
        
        return result;
    }
    
    @Override
    public String getMethodName() {
        return "钱币起卦法";
    }
    
    @Override
    public String getMethodType() {
        return DivinationConstants.METHOD_COIN;
    }
    
    @Override
    public String getMethodDescription() {
        return "系统自动模拟摇硬币过程（6次，每次3枚硬币），根据正反面数量自动生成卦象";
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 验证并转换请求类型
     */
    private CoinDivinationRequest validateAndCast(DivinationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("起卦请求不能为空");
        }
        
        if (!(request instanceof CoinDivinationRequest)) {
            throw new IllegalArgumentException(
                    String.format("请求类型错误，期望: CoinDivinationRequest, 实际: %s", 
                            request.getClass().getSimpleName())
            );
        }
        
        return (CoinDivinationRequest) request;
    }
    
    /**
     * 创建随机数生成器
     */
    private Random createRandom(Long seed) {
        if (seed != null) {
            log.debug("使用固定种子: {}", seed);
            return new Random(seed);
        } else {
            log.debug("使用系统时间作为随机种子");
            return new Random();
        }
    }
    
    /**
     * 模拟6次投币
     */
    private List<YaoThrowResult> simulateThrows(Random random) {
        List<YaoThrowResult> results = new ArrayList<>();
        
        for (int i = 1; i <= THROW_TIMES; i++) {
            int headCount = throwCoins(random);
            YaoThrowResult result = analyzeThrow(i, headCount);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 投掷3枚硬币，返回正面数量
     * 
     * @param random 随机数生成器
     * @return 正面数量（0-3）
     */
    private int throwCoins(Random random) {
        int headCount = 0;
        for (int i = 0; i < COINS_PER_THROW; i++) {
            // 0=反面，1=正面
            if (random.nextBoolean()) {
                headCount++;
            }
        }
        return headCount;
    }
    
    /**
     * 分析投币结果
     * 
     * @param weiZhi 爻位（1-6）
     * @param headCount 正面数量（0-3）
     * @return 投币结果
     */
    private YaoThrowResult analyzeThrow(int weiZhi, int headCount) {
        YaoThrowResult result = new YaoThrowResult();
        result.weiZhi = weiZhi;
        result.headCount = headCount;
        result.tailCount = COINS_PER_THROW - headCount;
        
        switch (headCount) {
            case 3:
                // 3个正面 → 老阴（6）→ 动爻 → 阴爻
                result.value = DivinationConstants.LAO_YIN;
                result.yinYang = DivinationConstants.YIN;
                result.isDong = true;
                result.name = "老阴（交）";
                break;
            case 2:
                // 2个正面1反面 → 少阳（7）→ 静爻 → 阳爻
                result.value = DivinationConstants.SHAO_YANG;
                result.yinYang = DivinationConstants.YANG;
                result.isDong = false;
                result.name = "少阳（单）";
                break;
            case 1:
                // 1个正面2反面 → 少阴（8）→ 静爻 → 阴爻
                result.value = DivinationConstants.SHAO_YIN;
                result.yinYang = DivinationConstants.YIN;
                result.isDong = false;
                result.name = "少阴（拆）";
                break;
            case 0:
                // 3个反面 → 老阳（9）→ 动爻 → 阳爻
                result.value = DivinationConstants.LAO_YANG;
                result.yinYang = DivinationConstants.YANG;
                result.isDong = true;
                result.name = "老阳（重）";
                break;
            default:
                throw new IllegalStateException("正面数量无效: " + headCount);
        }
        
        return result;
    }
    
    /**
     * 记录投币结果
     */
    private void logThrowResults(List<YaoThrowResult> results) {
        StringBuilder sb = new StringBuilder("\n投币结果:\n");
        for (YaoThrowResult result : results) {
            sb.append(String.format("  第%d爻: %d正%d反 → %s (%d) → %s%s\n",
                    result.weiZhi,
                    result.headCount,
                    result.tailCount,
                    result.name,
                    result.value,
                    result.yinYang,
                    result.isDong ? " 动" : " 静"
            ));
        }
        log.info(sb.toString());
    }
    
    /**
     * 转换为二进制编码
     */
    private String buildBinaryCode(List<YaoThrowResult> results) {
        StringBuilder code = new StringBuilder();
        for (YaoThrowResult result : results) {
            code.append(DivinationConstants.YANG.equals(result.yinYang) ? "1" : "0");
        }
        return code.toString();
    }
    
    /**
     * 提取动爻位置
     */
    private List<Integer> extractDongYaoPositions(List<YaoThrowResult> results) {
        List<Integer> positions = new ArrayList<>();
        for (YaoThrowResult result : results) {
            if (result.isDong) {
                positions.add(result.weiZhi);
            }
        }
        return positions;
    }
    
    /**
     * 构建完整的爻列表
     */
    private List<Yao> buildYaoList(GuaXiang benGua, 
                                    List<YaoThrowResult> throwResults, 
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
        
        // 结合投币结果构建最终的爻列表
        List<Yao> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Yao benYao = benGuaYaoList.get(i);
            YaoThrowResult throwResult = throwResults.get(i);
            
            if (throwResult.isDong) {
                // 动爻：需要构建变爻
                Yao bianYao = null;
                if (bianGua != null && bianGua.getYaoList() != null && bianGua.getYaoList().size() > i) {
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
    
    // ========== 内部类：投币结果 ==========
    
    /**
     * 单次投币结果
     */
    private static class YaoThrowResult {
        int weiZhi;          // 爻位（1-6）
        int headCount;       // 正面数量（0-3）
        int tailCount;       // 反面数量（0-3）
        int value;           // 数值（6/7/8/9）
        String yinYang;      // 阴阳标识（YANG/YIN）
        boolean isDong;      // 是否动爻
        String name;         // 名称（老阳、少阴等）
    }
}

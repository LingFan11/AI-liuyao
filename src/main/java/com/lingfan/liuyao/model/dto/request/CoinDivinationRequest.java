package com.lingfan.liuyao.model.dto.request;

import com.lingfan.liuyao.constant.DivinationConstants;

/**
 * 钱币起卦法请求
 * 
 * <p>
 * 使用场景：
 * - 系统自动模拟摇硬币过程（6次，每次3枚硬币）
 * - 用户只需提供时空信息和占卜问题
 * - 系统随机生成卦象
 * </p>
 * 
 * <p>
 * 钱币起卦规则（知识库: knowledge-liuyao01.md 107-218行）：
 * - 3个正面（三阳）→ 老阴（6）→ 动爻 → 阴爻
 * - 3个反面（三阴）→ 老阳（9）→ 动爻 → 阳爻
 * - 2个正面1反面 → 少阳（7）→ 静爻 → 阳爻
 * - 1个正面2反面 → 少阴（8）→ 静爻 → 阴爻
 * </p>
 * 
 * <p>
 * 与手动输入法的区别：
 * - 手动输入法：用户提供6个爻的阴阳和动静
 * - 钱币起卦法：系统自动模拟生成6个爻
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class CoinDivinationRequest extends DivinationRequest {
    
    /**
     * 随机种子（可选）
     * 如果提供，则使用固定种子生成随机数，便于测试
     * 如果不提供，使用系统时间作为种子，真随机
     */
    private Long randomSeed;
    
    // ========== 抽象方法实现 ==========
    
    @Override
    public String getMethodType() {
        return DivinationConstants.METHOD_COIN;
    }
    
    // ========== Getter和Setter ==========
    
    public Long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        return "CoinDivinationRequest{" +
                "randomSeed=" + randomSeed +
                ", " + super.toString() +
                '}';
    }
}

package com.lingfan.liuyao.utils.liuyao;

/**
 * 世应定位器
 * <p>
 * 知识库: knowledge-liuyao02.md (229-253行)
 * </p>
 * 
 * <p>
 * 世应规律：
 * - 本宫卦：世在六爻，应在三爻
 * - 一世卦：世在初爻，应在四爻
 * - 二世卦：世在二爻，应在五爻
 * - 三世卦：世在三爻，应在六爻
 * - 四世卦：世在四爻，应在初爻
 * - 五世卦：世在五爻，应在二爻
 * - 游魂卦：世在四爻，应在初爻
 * - 归魂卦：世在三爻，应在六爻
 * </p>
 * 
 * <p>
 * 应爻恒定规律：应爻与世爻相隔3位（循环计算）
 * 公式：应 = (世 + 2) % 6 + 1
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class ShiYingLocator {
    
    /**
     * 获取世爻位（根据卦序）
     * 
     * @param guaIndex 卦序号（0=本宫, 1=一世, 2=二世, 3=三世, 4=四世, 5=五世, 6=游魂, 7=归魂）
     * @return 世爻位（1-6）
     */
    public static int getShiYaoWei(int guaIndex) {
        if (guaIndex < 0 || guaIndex > 7) {
            throw new IllegalArgumentException("卦序号必须在0-7之间");
        }
        
        switch (guaIndex) {
            case 0: return 6;  // 本宫
            case 1: return 1;  // 一世
            case 2: return 2;  // 二世
            case 3: return 3;  // 三世
            case 4: return 4;  // 四世
            case 5: return 5;  // 五世
            case 6: return 4;  // 游魂
            case 7: return 3;  // 归魂
            default:
                throw new IllegalArgumentException("无效的卦序号: " + guaIndex);
        }
    }
    
    /**
     * 获取应爻位
     * <p>
     * 规律：应爻与世爻相隔3位（循环）
     * 公式：应 = (世 + 2) % 6 + 1
     * </p>
     * 
     * @param shiYaoWei 世爻位（1-6）
     * @return 应爻位（1-6）
     */
    public static int getYingYaoWei(int shiYaoWei) {
        if (shiYaoWei < 1 || shiYaoWei > 6) {
            throw new IllegalArgumentException("世爻位必须在1-6之间");
        }
        
        // 公式：应 = (世 + 2) % 6 + 1
        // 例如：世=6，应=(6+2)%6+1=3
        // 例如：世=1，应=(1+2)%6+1=4
        return (shiYaoWei + 2) % 6 + 1;
    }
    
    /**
     * 获取卦类型名称
     * 
     * @param guaIndex 卦序号（0-7）
     * @return 卦类型名称
     */
    public static String getGuaLeiXing(int guaIndex) {
        if (guaIndex < 0 || guaIndex > 7) {
            throw new IllegalArgumentException("卦序号必须在0-7之间");
        }
        
        String[] names = {"本宫", "一世", "二世", "三世", "四世", "五世", "游魂", "归魂"};
        return names[guaIndex];
    }
}

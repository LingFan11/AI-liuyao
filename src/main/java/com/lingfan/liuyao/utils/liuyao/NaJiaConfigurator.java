package com.lingfan.liuyao.utils.liuyao;

import com.lingfan.liuyao.enums.DiZhi;

/**
 * 纳甲配置器
 * <p>
 * 知识库: knowledge-liuyao02.md (10-178行)
 * </p>
 * 
 * <p>
 * 纳甲规则：
 * - 阳四宫（乾、坎、艮、震）：使用阳支（子、寅、辰、午、申、戌）
 * - 阴四宫（巽、离、坤、兑）：使用阴支（丑、卯、巳、未、酉、亥）
 * - 每个宫的起始位置不同（循环或逆序）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class NaJiaConfigurator {
    
    /**
     * 获取指定宫的纳甲序列
     * 
     * @param gongName 宫名（如"乾宫"）
     * @return 6个地支数组（从初爻到上爻）
     */
    public static DiZhi[] getNaJiaSequence(String gongName) {
        switch (gongName) {
            case "乾宫":
                // 乾宫：子寅辰午申戌（阳支顺序）
                return new DiZhi[]{
                    DiZhi.ZI, DiZhi.YIN, DiZhi.CHEN,
                    DiZhi.WU, DiZhi.SHEN, DiZhi.XU
                };
                
            case "坎宫":
                // 坎宫：寅辰午申戌子（起始：寅）
                return new DiZhi[]{
                    DiZhi.YIN, DiZhi.CHEN, DiZhi.WU,
                    DiZhi.SHEN, DiZhi.XU, DiZhi.ZI
                };
                
            case "艮宫":
                // 艮宫：辰午申戌子寅（起始：辰）
                return new DiZhi[]{
                    DiZhi.CHEN, DiZhi.WU, DiZhi.SHEN,
                    DiZhi.XU, DiZhi.ZI, DiZhi.YIN
                };
                
            case "震宫":
                // 震宫：子寅辰午申戌（与乾宫相同）
                return new DiZhi[]{
                    DiZhi.ZI, DiZhi.YIN, DiZhi.CHEN,
                    DiZhi.WU, DiZhi.SHEN, DiZhi.XU
                };
                
            case "巽宫":
                // 巽宫：丑卯巳未酉亥（阴支顺序）
                return new DiZhi[]{
                    DiZhi.CHOU, DiZhi.MAO, DiZhi.SI,
                    DiZhi.WEI, DiZhi.YOU, DiZhi.HAI
                };
                
            case "离宫":
                // 离宫：卯巳未酉亥丑（起始：卯）
                return new DiZhi[]{
                    DiZhi.MAO, DiZhi.SI, DiZhi.WEI,
                    DiZhi.YOU, DiZhi.HAI, DiZhi.CHOU
                };
                
            case "坤宫":
                // 坤宫：未巳卯丑亥酉（特殊：逆序）
                return new DiZhi[]{
                    DiZhi.WEI, DiZhi.SI, DiZhi.MAO,
                    DiZhi.CHOU, DiZhi.HAI, DiZhi.YOU
                };
                
            case "兑宫":
                // 兑宫：巳卯丑亥酉未（特殊：逆序）
                return new DiZhi[]{
                    DiZhi.SI, DiZhi.MAO, DiZhi.CHOU,
                    DiZhi.HAI, DiZhi.YOU, DiZhi.WEI
                };
                
            default:
                throw new IllegalArgumentException("未知的宫名: " + gongName);
        }
    }
    
    /**
     * 获取指定宫、指定爻位的纳甲地支
     * 
     * @param gongName 宫名
     * @param yaoWei 爻位（1-6）
     * @return 地支
     */
    public static DiZhi getNaJia(String gongName, int yaoWei) {
        if (yaoWei < 1 || yaoWei > 6) {
            throw new IllegalArgumentException("爻位必须在1-6之间");
        }
        DiZhi[] seq = getNaJiaSequence(gongName);
        return seq[yaoWei - 1];
    }
}

package com.lingfan.liuyao.model.dto;

import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.WuXing;

/**
 * 宫位配置类
 * <p>
 * 存储8个宫的基础信息（宫名、五行、纳甲序列）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class GongConfig {
    
    /**
     * 宫名（如"乾宫"）
     */
    private final String gongName;
    
    /**
     * 宫五行
     */
    private final WuXing gongWuXing;
    
    /**
     * 纳甲序列（6个地支，从初爻到上爻）
     */
    private final DiZhi[] naJiaSeq;
    
    /**
     * 本宫卦的阴阳序列（用于生成变卦）
     */
    private final boolean[] benGongYinYang;
    
    /**
     * 构造函数
     * 
     * @param gongName 宫名
     * @param gongWuXing 宫五行
     * @param naJiaSeq 纳甲序列（6个地支）
     * @param benGongYinYang 本宫卦阴阳序列（6个布尔值，true=阳，false=阴）
     */
    public GongConfig(String gongName, WuXing gongWuXing, DiZhi[] naJiaSeq, boolean[] benGongYinYang) {
        if (gongName == null || gongName.isEmpty()) {
            throw new IllegalArgumentException("宫名不能为空");
        }
        if (gongWuXing == null) {
            throw new IllegalArgumentException("宫五行不能为空");
        }
        if (naJiaSeq == null || naJiaSeq.length != 6) {
            throw new IllegalArgumentException("纳甲序列必须包含6个地支");
        }
        if (benGongYinYang == null || benGongYinYang.length != 6) {
            throw new IllegalArgumentException("本宫卦阴阳序列必须包含6个值");
        }
        
        this.gongName = gongName;
        this.gongWuXing = gongWuXing;
        this.naJiaSeq = naJiaSeq.clone();
        this.benGongYinYang = benGongYinYang.clone();
    }
    
    /**
     * 获取宫名
     */
    public String getGongName() {
        return gongName;
    }
    
    /**
     * 获取宫五行
     */
    public WuXing getGongWuXing() {
        return gongWuXing;
    }
    
    /**
     * 获取纳甲序列
     */
    public DiZhi[] getNaJiaSeq() {
        return naJiaSeq.clone();
    }
    
    /**
     * 获取指定爻位的纳甲地支
     * 
     * @param yaoWei 爻位（1-6）
     * @return 地支
     */
    public DiZhi getNaJia(int yaoWei) {
        if (yaoWei < 1 || yaoWei > 6) {
            throw new IllegalArgumentException("爻位必须在1-6之间");
        }
        return naJiaSeq[yaoWei - 1];
    }
    
    /**
     * 获取本宫卦阴阳序列
     */
    public boolean[] getBenGongYinYang() {
        return benGongYinYang.clone();
    }
    
    @Override
    public String toString() {
        return String.format("GongConfig{宫名='%s', 五行=%s}", gongName, gongWuXing.getName());
    }
}

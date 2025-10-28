package com.lingfan.liuyao.enums;

/**
 * 五行枚举
 * <p>
 * 包含五行的基本属性和关系：
 * - 五行名称
 * - 十二长生信息（长生、帝旺、墓库、绝地）
 * - 五行生克关系
 * </p>
 * 
 * 知识库: knowledge-liuyao08.md (49-223行)
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum WuXing {
    
    /**
     * 金：长生申、帝旺酉、墓库戌、绝地寅
     */
    JIN("金", "申", "酉", "戌", "寅"),
    
    /**
     * 木：长生亥、帝旺卯、墓库未、绝地申
     */
    MU("木", "亥", "卯", "未", "申"),
    
    /**
     * 水：长生申、帝旺子、墓库辰、绝地巳
     */
    SHUI("水", "申", "子", "辰", "巳"),
    
    /**
     * 火：长生寅、帝旺午、墓库戌、绝地亥
     */
    HUO("火", "寅", "午", "戌", "亥"),
    
    /**
     * 土：长生寅、帝旺午、墓库戌、绝地申
     */
    TU("土", "寅", "午", "戌", "申");

    /**
     * 五行名称
     */
    private final String name;
    
    /**
     * 长生地支名称
     */
    private final String changShengName;
    
    /**
     * 帝旺地支名称
     */
    private final String diWangName;
    
    /**
     * 墓库地支名称
     */
    private final String muKuName;
    
    /**
     * 绝地地支名称
     */
    private final String jueDiName;

    /**
     * 构造函数
     */
    WuXing(String name, String changShengName, String diWangName, String muKuName, String jueDiName) {
        this.name = name;
        this.changShengName = changShengName;
        this.diWangName = diWangName;
        this.muKuName = muKuName;
        this.jueDiName = jueDiName;
    }

    /**
     * 获取五行名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取长生地支名称
     */
    public String getChangShengName() {
        return changShengName;
    }

    /**
     * 获取帝旺地支名称
     */
    public String getDiWangName() {
        return diWangName;
    }

    /**
     * 获取墓库地支名称
     */
    public String getMuKuName() {
        return muKuName;
    }

    /**
     * 获取绝地地支名称
     */
    public String getJueDiName() {
        return jueDiName;
    }

    /**
     * 五行相生
     * <p>
     * 金生水、水生木、木生火、火生土、土生金
     * </p>
     * 
     * @return 我生的五行
     */
    public WuXing sheng() {
        switch (this) {
            case JIN:
                return SHUI;  // 金生水
            case SHUI:
                return MU;    // 水生木
            case MU:
                return HUO;   // 木生火
            case HUO:
                return TU;    // 火生土
            case TU:
                return JIN;   // 土生金
            default:
                throw new IllegalStateException("未知的五行类型: " + this);
        }
    }

    /**
     * 五行相克
     * <p>
     * 金克木、木克土、土克水、水克火、火克金
     * </p>
     * 
     * @return 我克的五行
     */
    public WuXing ke() {
        switch (this) {
            case JIN:
                return MU;    // 金克木
            case MU:
                return TU;    // 木克土
            case TU:
                return SHUI;  // 土克水
            case SHUI:
                return HUO;   // 水克火
            case HUO:
                return JIN;   // 火克金
            default:
                throw new IllegalStateException("未知的五行类型: " + this);
        }
    }

    /**
     * 判断是否相生
     * 
     * @param other 另一个五行
     * @return true-我生他
     */
    public boolean isSheng(WuXing other) {
        return this.sheng() == other;
    }

    /**
     * 判断是否相克
     * 
     * @param other 另一个五行
     * @return true-我克他
     */
    public boolean isKe(WuXing other) {
        return this.ke() == other;
    }

    /**
     * 判断是否被生
     * 
     * @param other 另一个五行
     * @return true-他生我
     */
    public boolean isShengBy(WuXing other) {
        return other.sheng() == this;
    }

    /**
     * 判断是否被克
     * 
     * @param other 另一个五行
     * @return true-他克我
     */
    public boolean isKeBy(WuXing other) {
        return other.ke() == this;
    }

    /**
     * 判断是否比和（同类五行）
     * 
     * @param other 另一个五行
     * @return true-比和
     */
    public boolean isBiHe(WuXing other) {
        return this == other;
    }

    /**
     * 根据名称获取五行
     * 
     * @param name 五行名称
     * @return 五行枚举
     */
    public static WuXing getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (WuXing wuXing : values()) {
            if (wuXing.name.equals(name)) {
                return wuXing;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}

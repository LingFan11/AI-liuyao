package com.lingfan.liuyao.enums;

/**
 * 地支枚举
 * <p>
 * 子丑寅卯辰巳午未申酉戌亥（12个）
 * 每个地支对应一个五行属性，并包含六合、六冲关系
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum DiZhi {
    
    /**
     * 子水
     */
    ZI("子", 1, WuXing.SHUI, "丑", "午"),
    
    /**
     * 丑土
     */
    CHOU("丑", 2, WuXing.TU, "子", "未"),
    
    /**
     * 寅木
     */
    YIN("寅", 3, WuXing.MU, "亥", "申"),
    
    /**
     * 卯木
     */
    MAO("卯", 4, WuXing.MU, "戌", "酉"),
    
    /**
     * 辰土
     */
    CHEN("辰", 5, WuXing.TU, "酉", "戌"),
    
    /**
     * 巳火
     */
    SI("巳", 6, WuXing.HUO, "申", "亥"),
    
    /**
     * 午火
     */
    WU("午", 7, WuXing.HUO, "未", "子"),
    
    /**
     * 未土
     */
    WEI("未", 8, WuXing.TU, "午", "丑"),
    
    /**
     * 申金
     */
    SHEN("申", 9, WuXing.JIN, "巳", "寅"),
    
    /**
     * 酉金
     */
    YOU("酉", 10, WuXing.JIN, "辰", "卯"),
    
    /**
     * 戌土
     */
    XU("戌", 11, WuXing.TU, "卯", "辰"),
    
    /**
     * 亥水
     */
    HAI("亥", 12, WuXing.SHUI, "寅", "巳");

    /**
     * 地支名称
     */
    private final String name;
    
    /**
     * 序号（1-12）
     */
    private final int order;
    
    /**
     * 对应五行
     */
    private final WuXing wuXing;
    
    /**
     * 六合地支名称
     */
    private final String liuHeName;
    
    /**
     * 六冲地支名称
     */
    private final String liuChongName;

    /**
     * 构造函数
     */
    DiZhi(String name, int order, WuXing wuXing, String liuHeName, String liuChongName) {
        this.name = name;
        this.order = order;
        this.wuXing = wuXing;
        this.liuHeName = liuHeName;
        this.liuChongName = liuChongName;
    }

    /**
     * 获取地支名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取序号
     */
    public int getOrder() {
        return order;
    }

    /**
     * 获取对应五行
     */
    public WuXing getWuXing() {
        return wuXing;
    }

    /**
     * 获取六合地支
     * <p>
     * 子丑合、寅亥合、卯戌合、辰酉合、巳申合、午未合
     * </p>
     * 
     * @return 六合地支
     */
    public DiZhi getLiuHe() {
        return getByName(liuHeName);
    }

    /**
     * 获取六冲地支
     * <p>
     * 子午冲、丑未冲、寅申冲、卯酉冲、辰戌冲、巳亥冲
     * </p>
     * 
     * @return 六冲地支
     */
    public DiZhi getLiuChong() {
        return getByName(liuChongName);
    }

    /**
     * 判断是否六合
     * 
     * @param other 另一个地支
     * @return true-六合
     */
    public boolean isLiuHe(DiZhi other) {
        if (other == null) {
            return false;
        }
        return this.getLiuHe() == other;
    }

    /**
     * 判断是否六冲
     * 
     * @param other 另一个地支
     * @return true-六冲
     */
    public boolean isLiuChong(DiZhi other) {
        if (other == null) {
            return false;
        }
        return this.getLiuChong() == other;
    }

    /**
     * 根据名称获取地支
     * 
     * @param name 地支名称
     * @return 地支枚举
     */
    public static DiZhi getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (DiZhi diZhi : values()) {
            if (diZhi.name.equals(name)) {
                return diZhi;
            }
        }
        return null;
    }

    /**
     * 根据序号获取地支
     * 
     * @param order 序号（1-12）
     * @return 地支枚举
     */
    public static DiZhi getByOrder(int order) {
        if (order < 1 || order > 12) {
            return null;
        }
        for (DiZhi diZhi : values()) {
            if (diZhi.order == order) {
                return diZhi;
            }
        }
        return null;
    }

    /**
     * 获取下一个地支（循环）
     * 
     * @return 下一个地支
     */
    public DiZhi next() {
        int nextOrder = this.order % 12 + 1;
        return getByOrder(nextOrder);
    }

    /**
     * 获取上一个地支（循环）
     * 
     * @return 上一个地支
     */
    public DiZhi previous() {
        int prevOrder = this.order == 1 ? 12 : this.order - 1;
        return getByOrder(prevOrder);
    }

    @Override
    public String toString() {
        return name;
    }
}

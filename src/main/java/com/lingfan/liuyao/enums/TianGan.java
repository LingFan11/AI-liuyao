package com.lingfan.liuyao.enums;

/**
 * 天干枚举
 * <p>
 * 甲乙丙丁戊己庚辛壬癸（10个）
 * 每个天干对应一个五行属性
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum TianGan {
    
    /**
     * 甲木（阳木）
     */
    JIA("甲", 1, WuXing.MU),
    
    /**
     * 乙木（阴木）
     */
    YI("乙", 2, WuXing.MU),
    
    /**
     * 丙火（阳火）
     */
    BING("丙", 3, WuXing.HUO),
    
    /**
     * 丁火（阴火）
     */
    DING("丁", 4, WuXing.HUO),
    
    /**
     * 戊土（阳土）
     */
    WU("戊", 5, WuXing.TU),
    
    /**
     * 己土（阴土）
     */
    JI("己", 6, WuXing.TU),
    
    /**
     * 庚金（阳金）
     */
    GENG("庚", 7, WuXing.JIN),
    
    /**
     * 辛金（阴金）
     */
    XIN("辛", 8, WuXing.JIN),
    
    /**
     * 壬水（阳水）
     */
    REN("壬", 9, WuXing.SHUI),
    
    /**
     * 癸水（阴水）
     */
    GUI("癸", 10, WuXing.SHUI);

    /**
     * 天干名称
     */
    private final String name;
    
    /**
     * 序号（1-10）
     */
    private final int order;
    
    /**
     * 对应五行
     */
    private final WuXing wuXing;

    /**
     * 构造函数
     */
    TianGan(String name, int order, WuXing wuXing) {
        this.name = name;
        this.order = order;
        this.wuXing = wuXing;
    }

    /**
     * 获取天干名称
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
     * 根据名称获取天干
     * 
     * @param name 天干名称
     * @return 天干枚举
     */
    public static TianGan getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (TianGan tianGan : values()) {
            if (tianGan.name.equals(name)) {
                return tianGan;
            }
        }
        return null;
    }

    /**
     * 根据序号获取天干
     * 
     * @param order 序号（1-10）
     * @return 天干枚举
     */
    public static TianGan getByOrder(int order) {
        if (order < 1 || order > 10) {
            return null;
        }
        for (TianGan tianGan : values()) {
            if (tianGan.order == order) {
                return tianGan;
            }
        }
        return null;
    }

    /**
     * 获取下一个天干（循环）
     * 
     * @return 下一个天干
     */
    public TianGan next() {
        int nextOrder = this.order % 10 + 1;
        return getByOrder(nextOrder);
    }

    /**
     * 获取上一个天干（循环）
     * 
     * @return 上一个天干
     */
    public TianGan previous() {
        int prevOrder = this.order == 1 ? 10 : this.order - 1;
        return getByOrder(prevOrder);
    }

    @Override
    public String toString() {
        return name;
    }
}

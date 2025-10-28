package com.lingfan.liuyao.enums;

/**
 * 六亲枚举
 * <p>
 * 父母、兄弟、子孙、妻财、官鬼（5个）
 * 六亲关系基于五行生克：
 * - 我克者为妻财
 * - 克我者为官鬼
 * - 生我者为父母
 * - 我生者为子孙
 * - 比和者为兄弟
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum LiuQin {
    
    /**
     * 父母爻 - 文书、证件、长辈
     */
    FU_MU("父母", "文书、证件、长辈"),
    
    /**
     * 兄弟爻 - 同辈、竞争、劫财
     */
    XIONG_DI("兄弟", "同辈、竞争、劫财"),
    
    /**
     * 子孙爻 - 晚辈、才华、制鬼
     */
    ZI_SUN("子孙", "晚辈、才华、制鬼"),
    
    /**
     * 妻财爻 - 钱财、妻子、资源
     */
    QI_CAI("妻财", "钱财、妻子、资源"),
    
    /**
     * 官鬼爻 - 官职、丈夫、病灾
     */
    GUAN_GUI("官鬼", "官职、丈夫、病灾");

    /**
     * 六亲名称
     */
    private final String name;
    
    /**
     * 含义说明
     */
    private final String meaning;

    /**
     * 构造函数
     */
    LiuQin(String name, String meaning) {
        this.name = name;
        this.meaning = meaning;
    }

    /**
     * 获取六亲名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取含义说明
     */
    public String getMeaning() {
        return meaning;
    }

    /**
     * 根据名称获取六亲
     * 
     * @param name 六亲名称
     * @return 六亲枚举
     */
    public static LiuQin getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (LiuQin liuQin : values()) {
            if (liuQin.name.equals(name)) {
                return liuQin;
            }
        }
        return null;
    }

    /**
     * 根据五行生克关系计算六亲
     * <p>
     * 计算规则：
     * - 我克者为妻财
     * - 克我者为官鬼
     * - 生我者为父母
     * - 我生者为子孙
     * - 比和者为兄弟
     * </p>
     * 
     * @param gongWuXing 宫五行（我）
     * @param yaoWuXing 爻五行（他）
     * @return 六亲枚举
     */
    public static LiuQin calculate(WuXing gongWuXing, WuXing yaoWuXing) {
        if (gongWuXing == null || yaoWuXing == null) {
            return null;
        }
        
        // 我克者为妻财
        if (gongWuXing.isKe(yaoWuXing)) {
            return QI_CAI;
        }
        
        // 克我者为官鬼
        if (gongWuXing.isKeBy(yaoWuXing)) {
            return GUAN_GUI;
        }
        
        // 生我者为父母
        if (gongWuXing.isShengBy(yaoWuXing)) {
            return FU_MU;
        }
        
        // 我生者为子孙
        if (gongWuXing.isSheng(yaoWuXing)) {
            return ZI_SUN;
        }
        
        // 比和者为兄弟
        if (gongWuXing.isBiHe(yaoWuXing)) {
            return XIONG_DI;
        }
        
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}

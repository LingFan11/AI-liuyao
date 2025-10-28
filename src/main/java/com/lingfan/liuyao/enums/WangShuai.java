package com.lingfan.liuyao.enums;

/**
 * 旺衰枚举
 * <p>
 * 旺、相、休、囚、死（5个，含力量权重）
 * 旺衰根据月建和五行关系判断，影响爻的力量强弱
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum WangShuai {
    
    /**
     * 旺 - 最强，力量权重5
     */
    WANG("旺", 5, "旺相有力"),
    
    /**
     * 相 - 次旺，力量权重4
     */
    XIANG("相", 4, "次旺"),
    
    /**
     * 休 - 休息，力量权重3
     */
    XIU("休", 3, "休息"),
    
    /**
     * 囚 - 被囚，力量权重2
     */
    QIU("囚", 2, "被囚"),
    
    /**
     * 死 - 最弱，力量权重1
     */
    SI("死", 1, "最弱");

    /**
     * 旺衰名称
     */
    private final String name;
    
    /**
     * 力量权重
     */
    private final int power;
    
    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     */
    WangShuai(String name, int power, String description) {
        this.name = name;
        this.power = power;
        this.description = description;
    }

    /**
     * 获取旺衰名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取力量权重
     */
    public int getPower() {
        return power;
    }

    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 判断是否旺相（旺或相）
     * 
     * @return true-旺相
     */
    public boolean isWangXiang() {
        return this == WANG || this == XIANG;
    }

    /**
     * 判断是否休囚死
     * 
     * @return true-休囚死
     */
    public boolean isXiuQiuSi() {
        return this == XIU || this == QIU || this == SI;
    }

    /**
     * 根据名称获取旺衰
     * 
     * @param name 旺衰名称
     * @return 旺衰枚举
     */
    public static WangShuai getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (WangShuai wangShuai : values()) {
            if (wangShuai.name.equals(name)) {
                return wangShuai;
            }
        }
        return null;
    }

    /**
     * 判断旺衰
     * <p>
     * 四时旺相规则：
     * - 春季（寅卯辰月）：木旺、火相、土死、金囚、水休
     * - 夏季（巳午未月）：火旺、土相、金死、水囚、木休
     * - 秋季（申酉戌月）：金旺、水相、木死、火囚、土休
     * - 冬季（亥子丑月）：水旺、木相、火死、土囚、金休
     * </p>
     * 
     * @param yueJian 月建（地支）
     * @param wuXing 五行
     * @return 旺衰枚举
     */
    public static WangShuai judge(DiZhi yueJian, WuXing wuXing) {
        if (yueJian == null || wuXing == null) {
            return null;
        }
        
        // 获取月建对应的五行
        WuXing yueWuXing = yueJian.getWuXing();
        
        // 当令者旺
        if (wuXing == yueWuXing) {
            return WANG;
        }
        
        // 我生者相
        if (wuXing.isSheng(yueWuXing)) {
            return XIANG;
        }
        
        // 生我者休
        if (wuXing.isShengBy(yueWuXing)) {
            return XIU;
        }
        
        // 克我者囚
        if (wuXing.isKeBy(yueWuXing)) {
            return QIU;
        }
        
        // 我克者死
        if (wuXing.isKe(yueWuXing)) {
            return SI;
        }
        
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}

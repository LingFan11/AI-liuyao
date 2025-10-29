package com.lingfan.liuyao.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 八卦枚举
 * <p>
 * 乾、兑、离、震、巽、坎、艮、坤（8个）
 * 每个卦包含名称、符号、自然属性、二进制编码
 * 二进制编码：阳爻=1，阴爻=0，从下到上（初爻、中爻、上爻）
 * </p>
 * 
 * 知识库: knowledge-liuyao01.md (10-104行)
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum BaGua {
    
    /**
     * 乾三连（☰）- 天
     */
    QIAN("乾", "☰", "天", "111"),
    
    /**
     * 兑上缺（☱）- 泽
     */
    DUI("兑", "☱", "泽", "110"),
    
    /**
     * 离中虚（☲）- 火
     */
    LI("离", "☲", "火", "101"),
    
    /**
     * 震仰盂（☳）- 雷
     */
    ZHEN("震", "☳", "雷", "100"),
    
    /**
     * 巽下断（☴）- 风
     */
    XUN("巽", "☴", "风", "011"),
    
    /**
     * 坎中满（☵）- 水
     */
    KAN("坎", "☵", "水", "010"),
    
    /**
     * 艮覆碗（☶）- 山
     */
    GEN("艮", "☶", "山", "001"),
    
    /**
     * 坤六断（☷）- 地
     */
    KUN("坤", "☷", "地", "000");

    /**
     * 八卦名称（数据库映射字段）
     */
    @EnumValue
    private final String name;
    
    /**
     * 八卦符号
     */
    private final String symbol;
    
    /**
     * 自然属性
     */
    private final String nature;
    
    /**
     * 二进制编码（阳=1，阴=0，从下到上）
     */
    private final String binaryCode;

    /**
     * 构造函数
     */
    BaGua(String name, String symbol, String nature, String binaryCode) {
        this.name = name;
        this.symbol = symbol;
        this.nature = nature;
        this.binaryCode = binaryCode;
    }

    /**
     * 获取八卦名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取八卦符号
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * 获取自然属性
     */
    public String getNature() {
        return nature;
    }

    /**
     * 获取二进制编码
     */
    public String getBinaryCode() {
        return binaryCode;
    }

    /**
     * 根据名称获取八卦
     * 
     * @param name 八卦名称
     * @return 八卦枚举
     */
    public static BaGua getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (BaGua baGua : values()) {
            if (baGua.name.equals(name)) {
                return baGua;
            }
        }
        return null;
    }

    /**
     * 根据二进制编码获取八卦
     * 
     * @param code 二进制编码（3位，如"111"）
     * @return 八卦枚举
     */
    public static BaGua getByBinaryCode(String code) {
        if (code == null || code.length() != 3) {
            return null;
        }
        for (BaGua baGua : values()) {
            if (baGua.binaryCode.equals(code)) {
                return baGua;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + symbol;
    }
}

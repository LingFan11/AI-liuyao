package com.lingfan.liuyao.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 六神枚举
 * <p>
 * 青龙、朱雀、勾陈、螣蛇、白虎、玄武（6个）
 * 六神配置根据日干确定，用于六爻占卜的辅助判断
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum LiuShen {
    
    /**
     * 青龙 - 吉庆、喜事
     */
    QING_LONG("青龙", "吉庆、喜事"),
    
    /**
     * 朱雀 - 文书、口舌
     */
    ZHU_QUE("朱雀", "文书、口舌"),
    
    /**
     * 勾陈 - 田土、牢狱
     */
    GOU_CHEN("勾陈", "田土、牢狱"),
    
    /**
     * 螣蛇 - 虚惊、怪异
     */
    TENG_SHE("螣蛇", "虚惊、怪异"),
    
    /**
     * 白虎 - 凶丧、血光
     */
    BAI_HU("白虎", "凶丧、血光"),
    
    /**
     * 玄武 - 盗贼、暗昧
     */
    XUAN_WU("玄武", "盗贼、暗昧");

    /**
     * 六神名称
     */
    private final String name;
    
    /**
     * 含义说明
     */
    private final String meaning;

    /**
     * 构造函数
     */
    LiuShen(String name, String meaning) {
        this.name = name;
        this.meaning = meaning;
    }

    /**
     * 获取六神名称
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
     * 根据名称获取六神
     * 
     * @param name 六神名称
     * @return 六神枚举
     */
    public static LiuShen getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (LiuShen liuShen : values()) {
            if (liuShen.name.equals(name)) {
                return liuShen;
            }
        }
        return null;
    }

    /**
     * 根据日干获取六神起始位置
     * <p>
     * 配置规则：
     * - 甲乙日起青龙
     * - 丙丁日起朱雀
     * - 戊日起勾陈
     * - 己日起螣蛇
     * - 庚辛日起白虎
     * - 壬癸日起玄武
     * </p>
     * 
     * @param riGan 日干
     * @return 初爻对应的六神
     */
    public static LiuShen getByRiGan(TianGan riGan) {
        if (riGan == null) {
            return null;
        }
        
        switch (riGan) {
            case JIA:
            case YI:
                return QING_LONG;  // 甲乙日起青龙
            case BING:
            case DING:
                return ZHU_QUE;    // 丙丁日起朱雀
            case WU:
                return GOU_CHEN;   // 戊日起勾陈
            case JI:
                return TENG_SHE;   // 己日起螣蛇
            case GENG:
            case XIN:
                return BAI_HU;     // 庚辛日起白虎
            case REN:
            case GUI:
                return XUAN_WU;    // 壬癸日起玄武
            default:
                return null;
        }
    }

    /**
     * 获取六神序列（从初爻到上爻）
     * <p>
     * 六神按固定顺序循环：青龙→朱雀→勾陈→螣蛇→白虎→玄武
     * 根据日干确定初爻的六神，然后依次向上配置
     * </p>
     * 
     * @param riGan 日干
     * @return 六神序列（6个元素，对应初爻到上爻）
     */
    public static List<LiuShen> getLiuShenSequence(TianGan riGan) {
        List<LiuShen> sequence = new ArrayList<>(6);
        
        // 获取初爻对应的六神
        LiuShen startLiuShen = getByRiGan(riGan);
        if (startLiuShen == null) {
            return sequence;
        }
        
        // 获取所有六神（按固定顺序）
        LiuShen[] allLiuShen = values();
        
        // 找到起始六神的索引
        int startIndex = 0;
        for (int i = 0; i < allLiuShen.length; i++) {
            if (allLiuShen[i] == startLiuShen) {
                startIndex = i;
                break;
            }
        }
        
        // 依次添加6个六神（循环）
        for (int i = 0; i < 6; i++) {
            int index = (startIndex + i) % allLiuShen.length;
            sequence.add(allLiuShen[index]);
        }
        
        return sequence;
    }

    /**
     * 获取下一个六神（循环）
     * 
     * @return 下一个六神
     */
    public LiuShen next() {
        LiuShen[] allLiuShen = values();
        int currentIndex = this.ordinal();
        int nextIndex = (currentIndex + 1) % allLiuShen.length;
        return allLiuShen[nextIndex];
    }

    /**
     * 获取上一个六神（循环）
     * 
     * @return 上一个六神
     */
    public LiuShen previous() {
        LiuShen[] allLiuShen = values();
        int currentIndex = this.ordinal();
        int prevIndex = currentIndex == 0 ? allLiuShen.length - 1 : currentIndex - 1;
        return allLiuShen[prevIndex];
    }

    @Override
    public String toString() {
        return name;
    }
}

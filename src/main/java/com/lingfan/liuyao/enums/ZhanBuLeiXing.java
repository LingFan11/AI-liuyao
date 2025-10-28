package com.lingfan.liuyao.enums;

/**
 * 占卜类型枚举
 * <p>
 * 定义15+种占卜类型，每种类型对应默认的用神六亲
 * 特殊情况（如婚姻占）需根据性别动态确定用神
 * </p>
 * 
 * 知识库: knowledge-liuyao02.md (348-397行)
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public enum ZhanBuLeiXing {
    
    /**
     * 功名 - 求官、考试、升职（用神：官鬼爻）
     */
    GONG_MING("功名", LiuQin.GUAN_GUI, "求官、考试、升职"),
    
    /**
     * 财运 - 求财、生意、投资（用神：妻财爻）
     */
    CAI_YUN("财运", LiuQin.QI_CAI, "求财、生意、投资"),
    
    /**
     * 婚姻 - 感情、婚配（用神：男占财，女占官）
     */
    HUN_YIN("婚姻", null, "感情、婚配，男占财女占官"),
    
    /**
     * 疾病 - 健康、病症（用神：官鬼爻作为病神）
     */
    JI_BING("疾病", LiuQin.GUAN_GUI, "健康、病症"),
    
    /**
     * 出行 - 旅行、远行（用神：根据具体情况确定）
     */
    CHU_XING("出行", null, "旅行、远行"),
    
    /**
     * 求事 - 谋事、办事（用神：根据具体事项确定）
     */
    QIU_SHI("求事", null, "谋事、办事"),
    
    /**
     * 购买 - 置业、买物（用神：妻财爻）
     */
    GOU_MAI("购买", LiuQin.QI_CAI, "置业、买物"),
    
    /**
     * 官司 - 诉讼、纠纷（用神：官鬼爻）
     */
    GUAN_SI("官司", LiuQin.GUAN_GUI, "诉讼、纠纷"),
    
    /**
     * 失物 - 寻找失物（用神：妻财爻）
     */
    SHI_WU("失物", LiuQin.QI_CAI, "寻找失物"),
    
    /**
     * 探人 - 寻人、访友（用神：根据关系确定）
     */
    TAN_REN("探人", null, "寻人、访友"),
    
    /**
     * 天气 - 预测天气（用神：根据具体情况确定）
     */
    TIAN_QI("天气", null, "预测天气"),
    
    /**
     * 年运 - 流年运势（用神：根据具体问题确定）
     */
    NIAN_YUN("年运", null, "流年运势"),
    
    /**
     * 家宅 - 居住、搬迁（用神：根据具体情况确定）
     */
    JIA_ZHAI("家宅", null, "居住、搬迁"),
    
    /**
     * 胎产 - 怀孕、生育（用神：子孙爻）
     */
    TAI_CHAN("胎产", LiuQin.ZI_SUN, "怀孕、生育"),
    
    /**
     * 交易 - 买卖、合作（用神：妻财爻）
     */
    JIAO_YI("交易", LiuQin.QI_CAI, "买卖、合作");

    /**
     * 占卜类型名称
     */
    private final String name;
    
    /**
     * 默认用神（可能为null）
     */
    private final LiuQin defaultYongShen;
    
    /**
     * 描述
     */
    private final String description;

    /**
     * 构造函数
     */
    ZhanBuLeiXing(String name, LiuQin defaultYongShen, String description) {
        this.name = name;
        this.defaultYongShen = defaultYongShen;
        this.description = description;
    }

    /**
     * 获取占卜类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取默认用神
     */
    public LiuQin getDefaultYongShen() {
        return defaultYongShen;
    }

    /**
     * 获取描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据名称获取占卜类型
     * 
     * @param name 占卜类型名称
     * @return 占卜类型枚举
     */
    public static ZhanBuLeiXing getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (ZhanBuLeiXing type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取用神
     * <p>
     * 特殊处理：
     * - 婚姻占：男占财（妻财爻），女占官（官鬼爻）
     * - 其他类型：返回defaultYongShen
     * </p>
     * 
     * @param gender 性别（"男"/"女"，可为null）
     * @return 用神六亲
     */
    public LiuQin getYongShen(String gender) {
        // 婚姻占特殊处理
        if (this == HUN_YIN) {
            if ("男".equals(gender)) {
                return LiuQin.QI_CAI;   // 男占财
            } else if ("女".equals(gender)) {
                return LiuQin.GUAN_GUI; // 女占官
            } else {
                return null;  // 性别未知，无法确定用神
            }
        }
        
        // 其他类型返回默认用神
        return defaultYongShen;
    }

    /**
     * 判断是否需要性别信息
     * 
     * @return true-需要性别信息才能确定用神
     */
    public boolean needsGender() {
        return this == HUN_YIN;
    }

    @Override
    public String toString() {
        return name;
    }
}

package com.lingfan.liuyao.constant;

/**
 * 起卦相关常量
 * 
 * <p>
 * 包含起卦方法类型、钱币起卦规则、爻位范围等常量定义
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class DivinationConstants {
    
    // ========== 起卦方法类型 ==========
    
    /**
     * 手动输入法
     * 使用场景：用户线下已起卦（摇硬币、蓍草等），仅需系统解卦
     */
    public static final String METHOD_MANUAL = "MANUAL";
    
    /**
     * 钱币起卦法（自动模拟）
     * 使用场景：系统自动模拟摇硬币过程
     */
    public static final String METHOD_COIN = "COIN";
    
    /**
     * 时间起卦法（梅花易数）
     * 使用场景：根据年月日时起卦
     */
    public static final String METHOD_TIME = "TIME";
    
    /**
     * 数字起卦法
     * 使用场景：用户输入数字起卦
     */
    public static final String METHOD_NUMBER = "NUMBER";
    
    // ========== 钱币起卦相关常量 ==========
    
    /**
     * 老阳（三正，数值9，变爻）
     * 三个硬币全部为正面，阳爻变阴爻
     */
    public static final int LAO_YANG = 9;
    
    /**
     * 少阴（两正一反，数值8，静爻）
     * 两正一反，阴爻不变
     */
    public static final int SHAO_YIN = 8;
    
    /**
     * 少阳（两反一正，数值7，静爻）
     * 两反一正，阳爻不变
     */
    public static final int SHAO_YANG = 7;
    
    /**
     * 老阴（三反，数值6，变爻）
     * 三个硬币全部为反面，阴爻变阳爻
     */
    public static final int LAO_YIN = 6;
    
    // ========== 爻位常量 ==========
    
    /**
     * 爻位最小值（初爻）
     */
    public static final int YAO_WEI_MIN = 1;
    
    /**
     * 爻位最大值（上爻）
     */
    public static final int YAO_WEI_MAX = 6;
    
    /**
     * 六爻数量
     */
    public static final int YAO_COUNT = 6;
    
    // ========== 阴阳常量 ==========
    
    /**
     * 阳爻标识
     */
    public static final String YANG = "YANG";
    
    /**
     * 阴爻标识
     */
    public static final String YIN = "YIN";
    
    // ========== 起卦结果来源 ==========
    
    /**
     * Web端起卦
     */
    public static final String SOURCE_WEB = "WEB";
    
    /**
     * 移动端起卦
     */
    public static final String SOURCE_MOBILE = "MOBILE";
    
    /**
     * API调用起卦
     */
    public static final String SOURCE_API = "API";
    
    // ========== 验证消息 ==========
    
    /**
     * 爻位无效错误消息
     */
    public static final String ERROR_INVALID_YAO_WEI = "爻位必须在1-6之间";
    
    /**
     * 爻列表数量错误消息
     */
    public static final String ERROR_INVALID_YAO_COUNT = "爻列表必须包含6个爻";
    
    /**
     * 阴阳标识无效错误消息
     */
    public static final String ERROR_INVALID_YIN_YANG = "阴阳标识必须为YANG或YIN";
    
    /**
     * 时空信息缺失错误消息
     */
    public static final String ERROR_MISSING_TIME_INFO = "时空信息（日干、日辰、月建）不能为空";
    
    /**
     * 起卦方法不支持错误消息
     */
    public static final String ERROR_UNSUPPORTED_METHOD = "不支持的起卦方法";
    
    // ========== 私有构造函数（禁止实例化）==========
    
    private DivinationConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}

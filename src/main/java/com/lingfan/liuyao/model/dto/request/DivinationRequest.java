package com.lingfan.liuyao.model.dto.request;

import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.TianGan;
import com.lingfan.liuyao.enums.ZhanBuLeiXing;

import java.time.LocalDateTime;

/**
 * 起卦请求基类
 * 
 * <p>
 * 所有起卦方法的请求DTO都继承此类
 * 包含所有起卦方法都需要的公共参数（时空信息、占卜类型等）
 * </p>
 * 
 * <p>
 * 设计理念：
 * - 抽象基类：定义公共字段和方法
 * - 子类扩展：不同起卦方法有各自的特殊参数
 * - 类型安全：通过继承体系保证类型安全
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public abstract class DivinationRequest {
    
    // ========== 时空信息（必填）==========
    
    /**
     * 日干（用于配置六神）
     */
    private TianGan riGan;
    
    /**
     * 日辰（用于判断日合、日冲、日生克、旬空等）
     */
    private DiZhi riChen;
    
    /**
     * 月建（用于判断旺衰、月破、月合等）
     */
    private DiZhi yueJian;
    
    /**
     * 起卦时间
     */
    private LocalDateTime divinationTime;
    
    // ========== 占卜信息（选填）==========
    
    /**
     * 占卜类型（功名、财运、婚姻等）
     */
    private ZhanBuLeiXing zhanBuLeiXing;
    
    /**
     * 问事内容
     */
    private String wenShi;
    
    /**
     * 性别（"男"/"女"，用于婚姻占确定用神）
     */
    private String gender;
    
    // ========== 抽象方法（子类实现）==========
    
    /**
     * 获取起卦方法类型
     * 子类必须返回对应的方法类型常量
     * 
     * @return 方法类型（如"MANUAL"、"COIN"、"TIME"等）
     */
    public abstract String getMethodType();
    
    // ========== Getter和Setter ==========
    
    public TianGan getRiGan() {
        return riGan;
    }

    public void setRiGan(TianGan riGan) {
        this.riGan = riGan;
    }

    public DiZhi getRiChen() {
        return riChen;
    }

    public void setRiChen(DiZhi riChen) {
        this.riChen = riChen;
    }

    public DiZhi getYueJian() {
        return yueJian;
    }

    public void setYueJian(DiZhi yueJian) {
        this.yueJian = yueJian;
    }

    public LocalDateTime getDivinationTime() {
        return divinationTime;
    }

    public void setDivinationTime(LocalDateTime divinationTime) {
        this.divinationTime = divinationTime;
    }

    public ZhanBuLeiXing getZhanBuLeiXing() {
        return zhanBuLeiXing;
    }

    public void setZhanBuLeiXing(ZhanBuLeiXing zhanBuLeiXing) {
        this.zhanBuLeiXing = zhanBuLeiXing;
    }

    public String getWenShi() {
        return wenShi;
    }

    public void setWenShi(String wenShi) {
        this.wenShi = wenShi;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 验证时空信息是否完整
     * 
     * @return 是否有效
     */
    public boolean hasValidTimeInfo() {
        return riGan != null && riChen != null && yueJian != null;
    }
    
    /**
     * 获取起卦时间（如果为空则返回当前时间）
     * 
     * @return 起卦时间
     */
    public LocalDateTime getOrDefaultDivinationTime() {
        return divinationTime != null ? divinationTime : LocalDateTime.now();
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        return "DivinationRequest{" +
                "methodType=" + getMethodType() +
                ", riGan=" + (riGan != null ? riGan.getName() : "null") +
                ", riChen=" + (riChen != null ? riChen.getName() : "null") +
                ", yueJian=" + (yueJian != null ? yueJian.getName() : "null") +
                ", zhanBuLeiXing=" + (zhanBuLeiXing != null ? zhanBuLeiXing.getName() : "null") +
                ", wenShi='" + wenShi + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}

package com.lingfan.liuyao.model.dto;

import com.lingfan.liuyao.model.entity.GuaXiang;
import com.lingfan.liuyao.model.entity.Yao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 起卦结果
 * 
 * <p>
 * 存储起卦方法执行后的结果，包括本卦、变卦、爻列表等
 * 不可变对象，一旦创建就不能修改
 * </p>
 * 
 * <p>
 * 设计理念：
 * - 不可变对象：起卦结果一旦生成就不能修改
 * - 完整信息：包含本卦、变卦、爻列表、动爻数量等所有起卦信息
 * - 便于传递：可序列化，方便在层与层之间传递
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class DivinationResult {
    
    // ========== 核心数据 ==========
    
    /**
     * 本卦（起卦得到的原始卦象）
     */
    private final GuaXiang benGua;
    
    /**
     * 变卦（如果有动爻，根据动爻变化后的卦象；无动爻时为null）
     */
    private final GuaXiang bianGua;
    
    /**
     * 六爻列表（包含动静信息）
     */
    private final List<Yao> yaoList;
    
    /**
     * 动爻数量
     */
    private final int dongYaoCount;
    
    // ========== 元数据 ==========
    
    /**
     * 起卦方法类型
     */
    private final String methodType;
    
    /**
     * 起卦方法名称
     */
    private final String methodName;
    
    /**
     * 起卦时间
     */
    private final LocalDateTime createTime;
    
    // ========== 构造函数（私有，使用Builder）==========
    
    /**
     * 私有构造函数
     */
    private DivinationResult(Builder builder) {
        this.benGua = builder.benGua;
        this.bianGua = builder.bianGua;
        this.yaoList = builder.yaoList != null ? 
                Collections.unmodifiableList(new ArrayList<>(builder.yaoList)) : 
                Collections.emptyList();
        this.dongYaoCount = builder.dongYaoCount;
        this.methodType = builder.methodType;
        this.methodName = builder.methodName;
        this.createTime = builder.createTime != null ? builder.createTime : LocalDateTime.now();
    }
    
    // ========== Getter方法 ==========
    
    public GuaXiang getBenGua() {
        return benGua;
    }
    
    public GuaXiang getBianGua() {
        return bianGua;
    }
    
    public List<Yao> getYaoList() {
        return yaoList;
    }
    
    public int getDongYaoCount() {
        return dongYaoCount;
    }
    
    public String getMethodType() {
        return methodType;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 判断是否有动爻
     */
    public boolean hasDongYao() {
        return dongYaoCount > 0;
    }
    
    /**
     * 判断是否有变卦
     */
    public boolean hasBianGua() {
        return bianGua != null;
    }
    
    /**
     * 获取本卦二进制编码
     * 
     * @return 6位二进制字符串，如"111111"代表乾卦
     */
    public String getBenGuaBinaryCode() {
        return benGua != null ? benGua.getBinaryCode() : null;
    }
    
    /**
     * 获取变卦二进制编码
     * 
     * @return 6位二进制字符串，如果无变卦返回null
     */
    public String getBianGuaBinaryCode() {
        return bianGua != null ? bianGua.getBinaryCode() : null;
    }
    
    /**
     * 获取指定位置的爻
     * 
     * @param weiZhi 爻位（1-6）
     * @return 爻对象，如果位置无效返回null
     */
    public Yao getYaoByWeiZhi(int weiZhi) {
        if (weiZhi < 1 || weiZhi > 6 || yaoList.isEmpty()) {
            return null;
        }
        return yaoList.stream()
                .filter(yao -> yao.getWeiZhi() == weiZhi)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取所有动爻
     */
    public List<Yao> getDongYaoList() {
        if (yaoList == null) {
            return Collections.emptyList();
        }
        return yaoList.stream()
                .filter(Yao::isDong)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有静爻
     */
    public List<Yao> getJingYaoList() {
        if (yaoList == null) {
            return Collections.emptyList();
        }
        return yaoList.stream()
                .filter(Yao::isJingYao)
                .collect(Collectors.toList());
    }
    
    /**
     * 判断是否静卦（无动爻）
     */
    public boolean isJingGua() {
        return dongYaoCount == 0;
    }
    
    /**
     * 判断是否全动（六爻皆动）
     */
    public boolean isQuanDong() {
        return dongYaoCount == 6;
    }
    
    // ========== Builder模式 ==========
    
    /**
     * Builder模式构建器
     */
    public static class Builder {
        private GuaXiang benGua;
        private GuaXiang bianGua;
        private List<Yao> yaoList;
        private int dongYaoCount;
        private String methodType;
        private String methodName;
        private LocalDateTime createTime;
        
        public Builder benGua(GuaXiang benGua) {
            this.benGua = benGua;
            return this;
        }
        
        public Builder bianGua(GuaXiang bianGua) {
            this.bianGua = bianGua;
            return this;
        }
        
        public Builder yaoList(List<Yao> yaoList) {
            this.yaoList = yaoList;
            return this;
        }
        
        public Builder dongYaoCount(int dongYaoCount) {
            this.dongYaoCount = dongYaoCount;
            return this;
        }
        
        public Builder methodType(String methodType) {
            this.methodType = methodType;
            return this;
        }
        
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        /**
         * 构建DivinationResult
         * 
         * 业务逻辑：
         * 1. 验证必填字段（本卦、爻列表）
         * 2. 自动计算动爻数量（如果未设置）
         * 3. 返回不可变对象
         */
        public DivinationResult build() {
            // 参数校验
            if (benGua == null) {
                throw new IllegalArgumentException("本卦不能为空");
            }
            if (yaoList == null || yaoList.size() != 6) {
                throw new IllegalArgumentException("爻列表必须包含6个爻");
            }
            
            // 自动计算动爻数量（如果未设置）
            if (dongYaoCount == 0) {
                dongYaoCount = (int) yaoList.stream().filter(Yao::isDong).count();
            }
            
            // 如果有动爻但没有变卦，打印警告（实际应用中使用log.warn）
            if (dongYaoCount > 0 && bianGua == null) {
                // log.warn("有{}个动爻但未设置变卦", dongYaoCount);
            }
            
            return new DivinationResult(this);
        }
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("起卦结果 [\n");
        sb.append("  起卦方法: ").append(methodName).append(" (").append(methodType).append(")\n");
        sb.append("  起卦时间: ").append(createTime).append("\n");
        sb.append("  本卦: ").append(benGua != null ? benGua.getGuaName() : "未知");
        if (benGua != null) {
            sb.append(" (").append(getBenGuaBinaryCode()).append(")");
        }
        sb.append("\n");
        
        if (bianGua != null) {
            sb.append("  变卦: ").append(bianGua.getGuaName());
            sb.append(" (").append(getBianGuaBinaryCode()).append(")\n");
        }
        
        sb.append("  动爻数量: ").append(dongYaoCount).append("\n");
        sb.append("  爻列表: ");
        if (yaoList != null && !yaoList.isEmpty()) {
            sb.append("\n");
            for (Yao yao : yaoList) {
                sb.append("    ").append(yao.toString()).append("\n");
            }
        } else {
            sb.append("无\n");
        }
        sb.append("]");
        return sb.toString();
    }
}

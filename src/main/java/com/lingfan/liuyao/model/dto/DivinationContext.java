package com.lingfan.liuyao.model.dto;

import com.lingfan.liuyao.enums.*;
import com.lingfan.liuyao.model.entity.GuaXiang;
import com.lingfan.liuyao.model.entity.Yao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 起卦上下文（完整版）
 * <p>
 * 存储起卦的完整信息，包括时空条件、卦象、占卜类型等
 * 提供爻状态缓存机制，避免重复计算
 * </p>
 * 
 * <p>
 * 设计理念：
 * - 集中管理起卦的所有上下文信息
 * - 缓存YaoState计算结果（使用ConcurrentHashMap保证线程安全）
 * - Builder模式自动计算六神和用神
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public class DivinationContext {
    
    // ========== 时空条件（起卦时的时间信息）==========
    
    /**
     * 日干（用于配置六神）
     */
    private final TianGan riGan;
    
    /**
     * 日辰（用于判断日合、日冲、日生克、旬空等）
     */
    private final DiZhi riChen;
    
    /**
     * 月建（用于判断旺衰、月破、月合等）
     */
    private final DiZhi yueJian;
    
    /**
     * 起卦时间
     */
    private final LocalDateTime divinationTime;
    
    // ========== 卦象信息 ==========
    
    /**
     * 本卦
     */
    private final GuaXiang benGua;
    
    /**
     * 变卦（如果有动爻）
     */
    private final GuaXiang bianGua;
    
    // ========== 占卜信息 ==========
    
    /**
     * 占卜类型
     */
    private final ZhanBuLeiXing zhanBuLeiXing;
    
    /**
     * 问事内容
     */
    private final String wenShi;
    
    /**
     * 性别（"男"/"女"，用于婚姻占确定用神）
     */
    private final String gender;
    
    // ========== 六神配置 ==========
    
    /**
     * 六神序列（从初爻到上爻）
     */
    private final List<LiuShen> liuShenList;
    
    // ========== 用神信息 ==========
    
    /**
     * 用神六亲
     */
    private final LiuQin yongShen;
    
    // ========== 爻状态缓存（计算属性缓存）==========
    
    /**
     * 爻状态缓存
     * Key: 爻位（1-6）
     * Value: YaoState
     * 使用ConcurrentHashMap保证线程安全
     */
    private final Map<Integer, YaoState> yaoStateCache;

    // ========== 构造函数（私有，使用Builder）==========
    
    /**
     * 私有构造函数
     */
    private DivinationContext(Builder builder) {
        this.riGan = builder.riGan;
        this.riChen = builder.riChen;
        this.yueJian = builder.yueJian;
        this.divinationTime = builder.divinationTime;
        this.benGua = builder.benGua;
        this.bianGua = builder.bianGua;
        this.zhanBuLeiXing = builder.zhanBuLeiXing;
        this.wenShi = builder.wenShi;
        this.gender = builder.gender;
        this.liuShenList = builder.liuShenList;
        this.yongShen = builder.yongShen;
        this.yaoStateCache = new ConcurrentHashMap<>();
    }

    // ========== Getter方法 ==========
    
    public TianGan getRiGan() {
        return riGan;
    }

    public DiZhi getRiChen() {
        return riChen;
    }

    public DiZhi getYueJian() {
        return yueJian;
    }

    public LocalDateTime getDivinationTime() {
        return divinationTime;
    }

    public GuaXiang getBenGua() {
        return benGua;
    }

    public GuaXiang getBianGua() {
        return bianGua;
    }

    public ZhanBuLeiXing getZhanBuLeiXing() {
        return zhanBuLeiXing;
    }

    public String getWenShi() {
        return wenShi;
    }

    public String getGender() {
        return gender;
    }

    public List<LiuShen> getLiuShenList() {
        return liuShenList;
    }

    public LiuQin getYongShen() {
        return yongShen;
    }

    // ========== 核心方法 ==========
    
    /**
     * 获取爻状态（带缓存）
     * <p>
     * 注意：此方法只返回缓存中的YaoState，不负责计算
     * 计算逻辑由YaoStateCalculator负责，并通过此方法缓存
     * </p>
     * 
     * @param yaoWei 爻位（1-6）
     * @return 爻状态，如果缓存中没有返回null
     */
    public YaoState getYaoState(int yaoWei) {
        return yaoStateCache.get(yaoWei);
    }

    /**
     * 设置爻状态（用于缓存）
     * 
     * @param yaoWei 爻位
     * @param yaoState 爻状态
     */
    public void setYaoState(int yaoWei, YaoState yaoState) {
        if (yaoWei >= 1 && yaoWei <= 6 && yaoState != null) {
            yaoStateCache.put(yaoWei, yaoState);
        }
    }

    /**
     * 获取所有爻的状态（带缓存）
     * 
     * @return 爻状态列表（按爻位顺序）
     */
    public List<YaoState> getAllYaoStates() {
        List<YaoState> states = new ArrayList<>(6);
        for (int i = 1; i <= 6; i++) {
            YaoState state = yaoStateCache.get(i);
            if (state != null) {
                states.add(state);
            }
        }
        return states;
    }

    /**
     * 根据爻位获取六神
     * 
     * @param yaoWei 爻位（1-6）
     * @return 六神，如果爻位无效返回null
     */
    public LiuShen getLiuShenByWei(int yaoWei) {
        if (yaoWei < 1 || yaoWei > 6 || liuShenList == null || liuShenList.size() != 6) {
            return null;
        }
        return liuShenList.get(yaoWei - 1);  // 爻位从1开始，List索引从0开始
    }

    /**
     * 判断是否有动爻
     */
    public boolean hasDongYao() {
        return benGua != null && benGua.hasDongYao();
    }

    /**
     * 获取动爻数量
     */
    public int getDongYaoCount() {
        return benGua != null ? benGua.getDongYaoCount() : 0;
    }

    /**
     * 查找用神爻（可能有多个）
     * 
     * @return 用神爻列表
     */
    public List<Yao> findYongShenYaoList() {
        if (benGua == null || yongShen == null) {
            return new ArrayList<>();
        }
        
        return benGua.getYaoList().stream()
                .filter(yao -> yao.getLiuQin() == yongShen)
                .collect(Collectors.toList());
    }

    /**
     * 判断用神是否上卦（是否存在）
     */
    public boolean isYongShenShangGua() {
        return !findYongShenYaoList().isEmpty();
    }

    /**
     * 获取用神爻的状态列表
     */
    public List<YaoState> getYongShenStateList() {
        List<Yao> yongShenYaoList = findYongShenYaoList();
        List<YaoState> states = new ArrayList<>();
        
        for (Yao yao : yongShenYaoList) {
            YaoState state = yaoStateCache.get(yao.getWeiZhi());
            if (state != null) {
                states.add(state);
            }
        }
        
        return states;
    }

    /**
     * 清除缓存（用于重新计算）
     */
    public void clearCache() {
        yaoStateCache.clear();
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return yaoStateCache.size();
    }

    /**
     * 判断缓存是否完整（6个爻都已缓存）
     */
    public boolean isCacheComplete() {
        return yaoStateCache.size() == 6;
    }

    // ========== Builder模式 ==========
    
    /**
     * Builder模式构建器
     */
    public static class Builder {
        private TianGan riGan;
        private DiZhi riChen;
        private DiZhi yueJian;
        private LocalDateTime divinationTime;
        private GuaXiang benGua;
        private GuaXiang bianGua;
        private ZhanBuLeiXing zhanBuLeiXing;
        private String wenShi;
        private String gender;
        private List<LiuShen> liuShenList;
        private LiuQin yongShen;

        public Builder riGan(TianGan riGan) {
            this.riGan = riGan;
            return this;
        }

        public Builder riChen(DiZhi riChen) {
            this.riChen = riChen;
            return this;
        }

        public Builder yueJian(DiZhi yueJian) {
            this.yueJian = yueJian;
            return this;
        }

        public Builder divinationTime(LocalDateTime divinationTime) {
            this.divinationTime = divinationTime;
            return this;
        }

        public Builder benGua(GuaXiang benGua) {
            this.benGua = benGua;
            return this;
        }

        public Builder bianGua(GuaXiang bianGua) {
            this.bianGua = bianGua;
            return this;
        }

        public Builder zhanBuLeiXing(ZhanBuLeiXing zhanBuLeiXing) {
            this.zhanBuLeiXing = zhanBuLeiXing;
            return this;
        }

        public Builder wenShi(String wenShi) {
            this.wenShi = wenShi;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder liuShenList(List<LiuShen> liuShenList) {
            this.liuShenList = liuShenList;
            return this;
        }

        public Builder yongShen(LiuQin yongShen) {
            this.yongShen = yongShen;
            return this;
        }

        /**
         * 构建DivinationContext对象
         * 自动计算六神序列和用神
         */
        public DivinationContext build() {
            // 参数校验
            if (benGua == null) {
                throw new IllegalArgumentException("本卦不能为空");
            }
            if (riGan == null) {
                throw new IllegalArgumentException("日干不能为空");
            }
            if (riChen == null) {
                throw new IllegalArgumentException("日辰不能为空");
            }
            if (yueJian == null) {
                throw new IllegalArgumentException("月建不能为空");
            }
            
            // 自动计算六神序列（如果未指定）
            if (liuShenList == null) {
                liuShenList = LiuShen.getLiuShenSequence(riGan);
            }
            
            // 自动计算用神（如果未指定）
            if (yongShen == null && zhanBuLeiXing != null) {
                yongShen = zhanBuLeiXing.getYongShen(gender);
            }
            
            return new DivinationContext(this);
        }
    }

    // ========== 静态工厂方法 ==========
    
    /**
     * 从起卦结果创建上下文（便捷方法）
     * 
     * @param benGua 本卦
     * @param bianGua 变卦
     * @param riGan 日干
     * @param riChen 日辰
     * @param yueJian 月建
     * @param time 起卦时间
     * @param type 占卜类型
     * @param wenShi 问事
     * @param gender 性别
     * @return 起卦上下文
     */
    public static DivinationContext create(
            GuaXiang benGua,
            GuaXiang bianGua,
            TianGan riGan,
            DiZhi riChen,
            DiZhi yueJian,
            LocalDateTime time,
            ZhanBuLeiXing type,
            String wenShi,
            String gender) {
        
        return new Builder()
                .benGua(benGua)
                .bianGua(bianGua)
                .riGan(riGan)
                .riChen(riChen)
                .yueJian(yueJian)
                .divinationTime(time)
                .zhanBuLeiXing(type)
                .wenShi(wenShi)
                .gender(gender)
                .build();
    }

    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("起卦上下文 [\n");
        sb.append("  时间: ").append(divinationTime).append("\n");
        sb.append("  日干: ").append(riGan != null ? riGan.getName() : "未知").append("\n");
        sb.append("  日辰: ").append(riChen != null ? riChen.getName() : "未知").append("\n");
        sb.append("  月建: ").append(yueJian != null ? yueJian.getName() : "未知").append("\n");
        sb.append("  本卦: ").append(benGua != null ? benGua.getGuaName() : "未知").append("\n");
        if (bianGua != null) {
            sb.append("  变卦: ").append(bianGua.getGuaName()).append("\n");
        }
        sb.append("  占卜类型: ").append(zhanBuLeiXing != null ? zhanBuLeiXing.getName() : "未知").append("\n");
        sb.append("  用神: ").append(yongShen != null ? yongShen.getName() : "未定").append("\n");
        sb.append("  问事: ").append(wenShi != null ? wenShi : "无").append("\n");
        sb.append("  缓存状态: ").append(yaoStateCache.size()).append("/6\n");
        sb.append("]");
        return sb.toString();
    }
}

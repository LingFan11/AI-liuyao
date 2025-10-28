package com.lingfan.liuyao.model.dto;

import com.lingfan.liuyao.enums.WangShuai;
import com.lingfan.liuyao.model.entity.Yao;

/**
 * 爻状态（计算属性）
 * <p>
 * 存储爻的动态计算属性，由YaoStateCalculator统一计算
 * 区别于Yao（固有属性），YaoState包含所有运算后的状态
 * </p>
 * 
 * <p>
 * 设计理念：固有属性与计算属性彻底分离
 * - Yao：固有属性（爻位、地支、六亲、动静、变爻）
 * - YaoState：计算属性（旺衰、旬空、月破、日月生克等）
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public class YaoState {
    
    // ========== 关联对象 ==========
    
    /**
     * 关联的爻（固有属性）
     */
    private final Yao yao;
    
    // ========== 旺衰状态 ==========
    
    /**
     * 旺衰状态（根据月建判断）
     */
    private final WangShuai wangShuai;
    
    // ========== 特殊状态 ==========
    
    /**
     * 是否旬空
     */
    private final boolean xunKong;
    
    /**
     * 是否月破（地支六冲）
     */
    private final boolean yuePo;
    
    /**
     * 是否暗动（日冲旺相之静爻）
     */
    private final boolean anDong;
    
    /**
     * 是否动散（日冲动爻）
     */
    private final boolean dongSan;
    
    // ========== 日月关系 ==========
    
    /**
     * 是否日合
     */
    private final boolean riHe;
    
    /**
     * 是否月合
     */
    private final boolean yueHe;
    
    /**
     * 是否日冲
     */
    private final boolean riChong;
    
    /**
     * 是否月冲
     */
    private final boolean yueChong;
    
    // ========== 生克关系 ==========
    
    /**
     * 是否日生
     */
    private final boolean riSheng;
    
    /**
     * 是否月生
     */
    private final boolean yueSheng;
    
    /**
     * 是否日克
     */
    private final boolean riKe;
    
    /**
     * 是否月克
     */
    private final boolean yueKe;
    
    // ========== 十二长生状态 ==========
    
    /**
     * 是否入墓（日墓、月墓、动墓、化墓）
     */
    private final boolean ruMu;
    
    /**
     * 是否临绝
     */
    private final boolean linJue;
    
    /**
     * 是否临长生
     */
    private final boolean linChangSheng;
    
    /**
     * 是否临帝旺
     */
    private final boolean linDiWang;
    
    // ========== 进退神 ==========
    
    /**
     * 进退神类型（"进神"/"退神"/null）
     */
    private final String jinTuiType;
    
    // ========== 综合力量 ==========
    
    /**
     * 综合力量等级（1-10）
     * 计算规则：基础旺衰权重 + 日月生克加减 + 特殊状态影响
     */
    private final int powerLevel;

    // ========== 构造函数（私有，使用Builder）==========
    
    /**
     * 私有构造函数
     */
    private YaoState(Builder builder) {
        this.yao = builder.yao;
        this.wangShuai = builder.wangShuai;
        this.xunKong = builder.xunKong;
        this.yuePo = builder.yuePo;
        this.anDong = builder.anDong;
        this.dongSan = builder.dongSan;
        this.riHe = builder.riHe;
        this.yueHe = builder.yueHe;
        this.riChong = builder.riChong;
        this.yueChong = builder.yueChong;
        this.riSheng = builder.riSheng;
        this.yueSheng = builder.yueSheng;
        this.riKe = builder.riKe;
        this.yueKe = builder.yueKe;
        this.ruMu = builder.ruMu;
        this.linJue = builder.linJue;
        this.linChangSheng = builder.linChangSheng;
        this.linDiWang = builder.linDiWang;
        this.jinTuiType = builder.jinTuiType;
        this.powerLevel = builder.powerLevel;
    }

    // ========== Getter方法 ==========
    
    public Yao getYao() {
        return yao;
    }

    public WangShuai getWangShuai() {
        return wangShuai;
    }

    public boolean isXunKong() {
        return xunKong;
    }

    public boolean isYuePo() {
        return yuePo;
    }

    public boolean isAnDong() {
        return anDong;
    }

    public boolean isDongSan() {
        return dongSan;
    }

    public boolean isRiHe() {
        return riHe;
    }

    public boolean isYueHe() {
        return yueHe;
    }

    public boolean isRiChong() {
        return riChong;
    }

    public boolean isYueChong() {
        return yueChong;
    }

    public boolean isRiSheng() {
        return riSheng;
    }

    public boolean isYueSheng() {
        return yueSheng;
    }

    public boolean isRiKe() {
        return riKe;
    }

    public boolean isYueKe() {
        return yueKe;
    }

    public boolean isRuMu() {
        return ruMu;
    }

    public boolean isLinJue() {
        return linJue;
    }

    public boolean isLinChangSheng() {
        return linChangSheng;
    }

    public boolean isLinDiWang() {
        return linDiWang;
    }

    public String getJinTuiType() {
        return jinTuiType;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    // ========== 综合判断方法 ==========
    
    /**
     * 是否旺相有力
     * 判断标准：旺衰为旺或相
     */
    public boolean isWangXiangYouLi() {
        return wangShuai != null && wangShuai.isWangXiang();
    }

    /**
     * 是否休囚无力
     * 判断标准：旺衰为休、囚、死
     */
    public boolean isXiuQiuWuLi() {
        return wangShuai != null && wangShuai.isXiuQiuSi();
    }

    /**
     * 是否真空（旬空且无救）
     * <p>
     * 判断标准：
     * - 旬空 = true
     * - 且以下条件都不满足（无救）：
     *   1. 动爻
     *   2. 旺相
     *   3. 日冲
     *   4. 日月生扶
     * </p>
     */
    public boolean isZhenKong() {
        if (!xunKong) {
            return false;
        }
        
        // 以下情况不是真空（有救）
        if (yao.isDong()) return false;           // 动爻空不为真空
        if (isWangXiangYouLi()) return false;     // 旺相空不为真空
        if (riChong) return false;                // 日冲空不为真空
        if (isDeRiYueShengFu()) return false;     // 日月生扶空不为真空
        
        return true;  // 以上都不满足，才是真空
    }

    /**
     * 是否真破（月破且无救）
     * <p>
     * 判断标准：
     * - 月破 = true
     * - 且以下条件都不满足（无救）：
     *   1. 动爻（野鹤新论：月破爻发动仍有用）
     *   2. 旺相
     *   3. 日月生扶
     * </p>
     */
    public boolean isZhenPo() {
        if (!yuePo) {
            return false;
        }
        
        // 以下情况不是真破（有救）
        if (yao.isDong()) return false;           // 动爻破不为真破
        if (isWangXiangYouLi()) return false;     // 旺相破不为真破
        if (isDeRiYueShengFu()) return false;     // 日月生扶破不为真破
        
        return true;  // 以上都不满足，才是真破
    }

    /**
     * 是否无根（用神无根规则）
     * <p>
     * 判断标准（AND关系）：
     * 1. 月破 = true
     * 2. 日克 = true
     * 3. 休囚 = true
     * </p>
     * 
     * 知识库: knowledge-liuyao02.md (609-691行)
     */
    public boolean isWuGen() {
        return yuePo && riKe && isXiuQiuWuLi();
    }

    /**
     * 是否得日月生扶
     */
    public boolean isDeRiYueShengFu() {
        return riSheng || yueSheng;
    }

    /**
     * 是否被日月克制
     */
    public boolean isBeiRiYueKeZhi() {
        return riKe || yueKe;
    }

    /**
     * 是否得日月合
     */
    public boolean isDeRiYueHe() {
        return riHe || yueHe;
    }

    /**
     * 是否被日月冲
     */
    public boolean isBeiRiYueChong() {
        return riChong || yueChong;
    }

    /**
     * 获取状态描述（用于调试和展示）
     */
    public String getStateDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(yao.toString()).append(" [");
        
        // 旺衰
        if (wangShuai != null) {
            sb.append(wangShuai.getName()).append(" ");
        }
        
        // 特殊状态
        if (xunKong) sb.append("旬空 ");
        if (yuePo) sb.append("月破 ");
        if (anDong) sb.append("暗动 ");
        if (dongSan) sb.append("动散 ");
        
        // 日月关系
        if (riHe) sb.append("日合 ");
        if (yueHe) sb.append("月合 ");
        if (riChong) sb.append("日冲 ");
        if (yueChong) sb.append("月冲 ");
        
        // 生克
        if (riSheng) sb.append("日生 ");
        if (yueSheng) sb.append("月生 ");
        if (riKe) sb.append("日克 ");
        if (yueKe) sb.append("月克 ");
        
        // 十二长生
        if (linChangSheng) sb.append("临长生 ");
        if (linDiWang) sb.append("临帝旺 ");
        if (ruMu) sb.append("入墓 ");
        if (linJue) sb.append("临绝 ");
        
        // 进退神
        if (jinTuiType != null) {
            sb.append(jinTuiType).append(" ");
        }
        
        // 综合力量
        sb.append("力量:").append(powerLevel);
        
        sb.append("]");
        return sb.toString();
    }

    // ========== Builder模式 ==========
    
    /**
     * Builder模式构建器
     */
    public static class Builder {
        private Yao yao;
        private WangShuai wangShuai;
        private boolean xunKong = false;
        private boolean yuePo = false;
        private boolean anDong = false;
        private boolean dongSan = false;
        private boolean riHe = false;
        private boolean yueHe = false;
        private boolean riChong = false;
        private boolean yueChong = false;
        private boolean riSheng = false;
        private boolean yueSheng = false;
        private boolean riKe = false;
        private boolean yueKe = false;
        private boolean ruMu = false;
        private boolean linJue = false;
        private boolean linChangSheng = false;
        private boolean linDiWang = false;
        private String jinTuiType;
        private int powerLevel = 5;  // 默认中等力量

        public Builder yao(Yao yao) {
            this.yao = yao;
            return this;
        }

        public Builder wangShuai(WangShuai wangShuai) {
            this.wangShuai = wangShuai;
            return this;
        }

        public Builder xunKong(boolean xunKong) {
            this.xunKong = xunKong;
            return this;
        }

        public Builder yuePo(boolean yuePo) {
            this.yuePo = yuePo;
            return this;
        }

        public Builder anDong(boolean anDong) {
            this.anDong = anDong;
            return this;
        }

        public Builder dongSan(boolean dongSan) {
            this.dongSan = dongSan;
            return this;
        }

        public Builder riHe(boolean riHe) {
            this.riHe = riHe;
            return this;
        }

        public Builder yueHe(boolean yueHe) {
            this.yueHe = yueHe;
            return this;
        }

        public Builder riChong(boolean riChong) {
            this.riChong = riChong;
            return this;
        }

        public Builder yueChong(boolean yueChong) {
            this.yueChong = yueChong;
            return this;
        }

        public Builder riSheng(boolean riSheng) {
            this.riSheng = riSheng;
            return this;
        }

        public Builder yueSheng(boolean yueSheng) {
            this.yueSheng = yueSheng;
            return this;
        }

        public Builder riKe(boolean riKe) {
            this.riKe = riKe;
            return this;
        }

        public Builder yueKe(boolean yueKe) {
            this.yueKe = yueKe;
            return this;
        }

        public Builder ruMu(boolean ruMu) {
            this.ruMu = ruMu;
            return this;
        }

        public Builder linJue(boolean linJue) {
            this.linJue = linJue;
            return this;
        }

        public Builder linChangSheng(boolean linChangSheng) {
            this.linChangSheng = linChangSheng;
            return this;
        }

        public Builder linDiWang(boolean linDiWang) {
            this.linDiWang = linDiWang;
            return this;
        }

        public Builder jinTuiType(String jinTuiType) {
            this.jinTuiType = jinTuiType;
            return this;
        }

        public Builder powerLevel(int powerLevel) {
            this.powerLevel = powerLevel;
            return this;
        }

        /**
         * 构建YaoState对象
         */
        public YaoState build() {
            if (yao == null) {
                throw new IllegalArgumentException("爻对象不能为空");
            }
            return new YaoState(this);
        }
    }

    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        return getStateDescription();
    }
}

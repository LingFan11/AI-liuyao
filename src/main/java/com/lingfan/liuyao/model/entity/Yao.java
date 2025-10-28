package com.lingfan.liuyao.model.entity;

import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.LiuQin;
import com.lingfan.liuyao.enums.WuXing;

/**
 * 爻实体（不可变对象）
 * <p>
 * 存储爻的固有属性，一旦创建不可修改
 * 区别于YaoState（计算属性），Yao只包含本质属性
 * </p>
 * 
 * <p>
 * 设计理念：固有属性与计算属性彻底分离
 * - 固有属性（Yao）：爻位、地支、六亲、动静、变爻
 * - 计算属性（YaoState）：旺衰、旬空、月破、暗动等
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
public final class Yao {
    
    // ========== 固有属性（final，不可变）==========
    
    /**
     * 爻位（1-6，从下到上）
     * 1-初爻，2-二爻，3-三爻，4-四爻，5-五爻，6-上爻
     */
    private final int weiZhi;
    
    /**
     * 地支
     */
    private final DiZhi diZhi;
    
    /**
     * 六亲
     */
    private final LiuQin liuQin;
    
    /**
     * 是否动爻
     */
    private final boolean isDong;
    
    /**
     * 变爻（如果是动爻，指向变化后的爻）
     * 注意：变爻只是变化后的地支和六亲，不会再次变化
     */
    private final Yao bianYao;

    // ========== 构造函数（私有，使用Builder模式）==========
    
    /**
     * 私有构造函数
     */
    private Yao(int weiZhi, DiZhi diZhi, LiuQin liuQin, boolean isDong, Yao bianYao) {
        this.weiZhi = weiZhi;
        this.diZhi = diZhi;
        this.liuQin = liuQin;
        this.isDong = isDong;
        this.bianYao = bianYao;
    }

    // ========== Getter方法（只读）==========
    
    /**
     * 获取爻位
     */
    public int getWeiZhi() {
        return weiZhi;
    }

    /**
     * 获取地支
     */
    public DiZhi getDiZhi() {
        return diZhi;
    }

    /**
     * 获取六亲
     */
    public LiuQin getLiuQin() {
        return liuQin;
    }

    /**
     * 是否动爻
     */
    public boolean isDong() {
        return isDong;
    }

    /**
     * 获取变爻
     */
    public Yao getBianYao() {
        return bianYao;
    }

    // ========== 辅助方法 ==========
    
    /**
     * 获取爻的五行
     */
    public WuXing getWuXing() {
        return diZhi.getWuXing();
    }

    /**
     * 获取爻的阴阳
     * 规则：阳爻（―），阴爻（- -）
     * 根据地支序号判断：奇数为阳，偶数为阴
     */
    public String getYinYang() {
        return diZhi.getOrder() % 2 == 1 ? "阳" : "阴";
    }

    /**
     * 判断是否阳爻
     */
    public boolean isYang() {
        return diZhi.getOrder() % 2 == 1;
    }

    /**
     * 判断是否阴爻
     */
    public boolean isYin() {
        return diZhi.getOrder() % 2 == 0;
    }

    /**
     * 判断是否变爻
     */
    public boolean isBianYao() {
        return bianYao != null;
    }

    /**
     * 判断是否静爻
     */
    public boolean isJingYao() {
        return !isDong;
    }

    /**
     * 获取爻位名称
     */
    public String getWeiZhiName() {
        switch (weiZhi) {
            case 1: return "初爻";
            case 2: return "二爻";
            case 3: return "三爻";
            case 4: return "四爻";
            case 5: return "五爻";
            case 6: return "上爻";
            default: return "未知";
        }
    }

    // ========== Builder模式 ==========
    
    /**
     * Builder模式构建器
     */
    public static class Builder {
        private int weiZhi;
        private DiZhi diZhi;
        private LiuQin liuQin;
        private boolean isDong = false;
        private Yao bianYao = null;

        public Builder weiZhi(int weiZhi) {
            this.weiZhi = weiZhi;
            return this;
        }

        public Builder diZhi(DiZhi diZhi) {
            this.diZhi = diZhi;
            return this;
        }

        public Builder liuQin(LiuQin liuQin) {
            this.liuQin = liuQin;
            return this;
        }

        public Builder isDong(boolean isDong) {
            this.isDong = isDong;
            return this;
        }

        public Builder bianYao(Yao bianYao) {
            this.bianYao = bianYao;
            return this;
        }

        /**
         * 构建Yao对象
         * 包含参数校验
         */
        public Yao build() {
            // 参数校验
            if (weiZhi < 1 || weiZhi > 6) {
                throw new IllegalArgumentException("爻位必须在1-6之间");
            }
            if (diZhi == null) {
                throw new IllegalArgumentException("地支不能为空");
            }
            if (liuQin == null) {
                throw new IllegalArgumentException("六亲不能为空");
            }
            if (isDong && bianYao == null) {
                throw new IllegalArgumentException("动爻必须指定变爻");
            }
            
            return new Yao(weiZhi, diZhi, liuQin, isDong, bianYao);
        }
    }

    // ========== 静态工厂方法（便捷创建）==========
    
    /**
     * 创建静爻
     * 
     * @param weiZhi 爻位（1-6）
     * @param diZhi 地支
     * @param liuQin 六亲
     * @return 静爻对象
     */
    public static Yao createJingYao(int weiZhi, DiZhi diZhi, LiuQin liuQin) {
        return new Builder()
                .weiZhi(weiZhi)
                .diZhi(diZhi)
                .liuQin(liuQin)
                .isDong(false)
                .build();
    }

    /**
     * 创建动爻
     * 
     * @param weiZhi 爻位（1-6）
     * @param diZhi 本爻地支
     * @param liuQin 本爻六亲
     * @param bianYao 变爻
     * @return 动爻对象
     */
    public static Yao createDongYao(int weiZhi, DiZhi diZhi, LiuQin liuQin, Yao bianYao) {
        return new Builder()
                .weiZhi(weiZhi)
                .diZhi(diZhi)
                .liuQin(liuQin)
                .isDong(true)
                .bianYao(bianYao)
                .build();
    }

    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getWeiZhiName())
          .append("-")
          .append(diZhi.getName())
          .append("-")
          .append(liuQin.getName());
        
        if (isDong) {
            sb.append("-动");
            if (bianYao != null) {
                sb.append("→").append(bianYao.getDiZhi().getName());
            }
        } else {
            sb.append("-静");
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Yao yao = (Yao) o;
        
        if (weiZhi != yao.weiZhi) return false;
        if (isDong != yao.isDong) return false;
        if (!diZhi.equals(yao.diZhi)) return false;
        if (!liuQin.equals(yao.liuQin)) return false;
        return bianYao != null ? bianYao.equals(yao.bianYao) : yao.bianYao == null;
    }

    @Override
    public int hashCode() {
        int result = weiZhi;
        result = 31 * result + diZhi.hashCode();
        result = 31 * result + liuQin.hashCode();
        result = 31 * result + (isDong ? 1 : 0);
        result = 31 * result + (bianYao != null ? bianYao.hashCode() : 0);
        return result;
    }
}

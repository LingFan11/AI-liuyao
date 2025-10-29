package com.lingfan.liuyao.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lingfan.liuyao.enums.BaGua;
import com.lingfan.liuyao.enums.WuXing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 卦象实体（不可变对象）
 * <p>
 * 存储卦象的固有属性，对应数据库gua_xiang_base表
 * 64卦数据由程序生成，而不是手写SQL
 * </p>
 * 
 * <p>
 * 设计理念：
 * - 不可变对象，线程安全
 * - 包含6个爻的列表
 * - 自动关联宫位和世应
 * - 数据库字段与业务字段分离
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-28
 */
@TableName("gua_xiang_base")
public class GuaXiang {
    
    // ========== 数据库字段 ==========
    
    /**
     * 卦象ID（1-64）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 卦名（如"乾为天"）
     */
    @TableField("gua_name")
    private String guaName;
    
    /**
     * 所属宫（如"乾宫"）
     */
    @TableField("suo_shu_gong")
    private String suoShuGong;
    
    /**
     * 宫五行名称
     */
    @TableField("gong_wu_xing")
    private String gongWuXingName;
    
    /**
     * 世爻位（1-6）
     */
    @TableField("shi_yao_wei")
    private Integer shiYaoWei;
    
    /**
     * 应爻位（1-6）
     */
    @TableField("ying_yao_wei")
    private Integer yingYaoWei;
    
    /**
     * 上卦名称
     */
    @TableField("shang_gua")
    private String shangGuaName;
    
    /**
     * 下卦名称
     */
    @TableField("xia_gua")
    private String xiaGuaName;
    
    /**
     * 卦类型（本宫、一世、二世...游魂、归魂）
     */
    @TableField("gua_lei_xing")
    private String guaLeiXing;
    
    // ========== 业务字段（非数据库字段）==========
    
    /**
     * 宫五行（业务枚举）
     */
    @TableField(exist = false)
    private WuXing gongWuXing;
    
    /**
     * 上卦（业务枚举）
     */
    @TableField(exist = false)
    private BaGua shangGua;
    
    /**
     * 下卦（业务枚举）
     */
    @TableField(exist = false)
    private BaGua xiaGua;
    
    /**
     * 六爻列表（不可变List）
     */
    @TableField(exist = false)
    private List<Yao> yaoList;

    // ========== 构造函数（私有，使用Builder模式）==========
    
    /**
     * 私有构造函数（完整参数）
     */
    private GuaXiang(Long id, String guaName, String suoShuGong, String gongWuXingName,
                     Integer shiYaoWei, Integer yingYaoWei, String shangGuaName, 
                     String xiaGuaName, String guaLeiXing, WuXing gongWuXing,
                     BaGua shangGua, BaGua xiaGua, List<Yao> yaoList) {
        this.id = id;
        this.guaName = guaName;
        this.suoShuGong = suoShuGong;
        this.gongWuXingName = gongWuXingName;
        this.shiYaoWei = shiYaoWei;
        this.yingYaoWei = yingYaoWei;
        this.shangGuaName = shangGuaName;
        this.xiaGuaName = xiaGuaName;
        this.guaLeiXing = guaLeiXing;
        this.gongWuXing = gongWuXing;
        this.shangGua = shangGua;
        this.xiaGua = xiaGua;
        // 深拷贝并包装为不可变List
        this.yaoList = yaoList == null ? 
                Collections.emptyList() : 
                Collections.unmodifiableList(new ArrayList<>(yaoList));
    }
    
    /**
     * MyBatis-Plus需要的无参构造函数
     */
    public GuaXiang() {
        // 空构造函数，字段通过setter设置
    }

    // ========== Getter方法（只读）==========
    
    public Long getId() {
        return id;
    }

    public String getGuaName() {
        return guaName;
    }

    public String getSuoShuGong() {
        return suoShuGong;
    }

    public String getGongWuXingName() {
        return gongWuXingName;
    }

    public Integer getShiYaoWei() {
        return shiYaoWei;
    }

    public Integer getYingYaoWei() {
        return yingYaoWei;
    }

    public String getShangGuaName() {
        return shangGuaName;
    }

    public String getXiaGuaName() {
        return xiaGuaName;
    }

    public String getGuaLeiXing() {
        return guaLeiXing;
    }

    public WuXing getGongWuXing() {
        return gongWuXing;
    }

    public BaGua getShangGua() {
        return shangGua;
    }

    public BaGua getXiaGua() {
        return xiaGua;
    }

    /**
     * 获取六爻列表（不可变List）
     */
    public List<Yao> getYaoList() {
        return yaoList;
    }

    // ========== Setter方法（MyBatis-Plus需要）==========
    
    public void setId(Long id) {
        this.id = id;
    }

    public void setGuaName(String guaName) {
        this.guaName = guaName;
    }

    public void setSuoShuGong(String suoShuGong) {
        this.suoShuGong = suoShuGong;
    }

    public void setGongWuXingName(String gongWuXingName) {
        this.gongWuXingName = gongWuXingName;
    }

    public void setShiYaoWei(Integer shiYaoWei) {
        this.shiYaoWei = shiYaoWei;
    }

    public void setYingYaoWei(Integer yingYaoWei) {
        this.yingYaoWei = yingYaoWei;
    }

    public void setShangGuaName(String shangGuaName) {
        this.shangGuaName = shangGuaName;
    }

    public void setXiaGuaName(String xiaGuaName) {
        this.xiaGuaName = xiaGuaName;
    }

    public void setGuaLeiXing(String guaLeiXing) {
        this.guaLeiXing = guaLeiXing;
    }
    
    public void setGongWuXing(WuXing gongWuXing) {
        this.gongWuXing = gongWuXing;
    }
    
    public void setShangGua(BaGua shangGua) {
        this.shangGua = shangGua;
    }
    
    public void setXiaGua(BaGua xiaGua) {
        this.xiaGua = xiaGua;
    }
    
    public void setYaoList(List<Yao> yaoList) {
        // 包装为不可变List
        this.yaoList = yaoList == null ? 
                Collections.emptyList() : 
                Collections.unmodifiableList(new ArrayList<>(yaoList));
    }

    // ========== 辅助方法 ==========
    
    /**
     * 根据爻位获取爻
     * 
     * @param weiZhi 爻位（1-6）
     * @return 爻对象，如果不存在返回null
     */
    public Yao getYaoByWei(int weiZhi) {
        if (weiZhi < 1 || weiZhi > 6 || yaoList == null) {
            return null;
        }
        for (Yao yao : yaoList) {
            if (yao.getWeiZhi() == weiZhi) {
                return yao;
            }
        }
        return null;
    }

    /**
     * 获取世爻
     */
    public Yao getShiYao() {
        return shiYaoWei == null ? null : getYaoByWei(shiYaoWei);
    }

    /**
     * 获取应爻
     */
    public Yao getYingYao() {
        return yingYaoWei == null ? null : getYaoByWei(yingYaoWei);
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
     * 获取动爻数量
     */
    public int getDongYaoCount() {
        return getDongYaoList().size();
    }

    /**
     * 判断是否有动爻
     */
    public boolean hasDongYao() {
        return getDongYaoCount() > 0;
    }

    /**
     * 获取卦的二进制编码（阳=1，阴=0，从初爻到上爻）
     * 
     * @return 6位二进制字符串，如"111111"代表乾卦
     */
    public String getBinaryCode() {
        if (yaoList == null || yaoList.size() != 6) {
            return null;
        }
        StringBuilder code = new StringBuilder();
        for (Yao yao : yaoList) {
            code.append(yao.isYang() ? "1" : "0");
        }
        return code.toString();
    }

    /**
     * 获取完整卦象描述
     */
    public String getFullDescription() {
        return String.format("%s[%s-%s]，世%d应%d", 
                guaName, suoShuGong, guaLeiXing, shiYaoWei, yingYaoWei);
    }

    // ========== Builder模式 ==========
    
    /**
     * Builder模式构建器
     */
    public static class Builder {
        private Long id;
        private String guaName;
        private String suoShuGong;
        private String gongWuXingName;
        private Integer shiYaoWei;
        private Integer yingYaoWei;
        private String shangGuaName;
        private String xiaGuaName;
        private String guaLeiXing;
        private WuXing gongWuXing;
        private BaGua shangGua;
        private BaGua xiaGua;
        private List<Yao> yaoList;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder guaName(String guaName) {
            this.guaName = guaName;
            return this;
        }

        public Builder suoShuGong(String suoShuGong) {
            this.suoShuGong = suoShuGong;
            return this;
        }

        public Builder gongWuXingName(String gongWuXingName) {
            this.gongWuXingName = gongWuXingName;
            return this;
        }

        public Builder shiYaoWei(Integer shiYaoWei) {
            this.shiYaoWei = shiYaoWei;
            return this;
        }

        public Builder yingYaoWei(Integer yingYaoWei) {
            this.yingYaoWei = yingYaoWei;
            return this;
        }

        public Builder shangGuaName(String shangGuaName) {
            this.shangGuaName = shangGuaName;
            return this;
        }

        public Builder xiaGuaName(String xiaGuaName) {
            this.xiaGuaName = xiaGuaName;
            return this;
        }

        public Builder guaLeiXing(String guaLeiXing) {
            this.guaLeiXing = guaLeiXing;
            return this;
        }

        public Builder gongWuXing(WuXing gongWuXing) {
            this.gongWuXing = gongWuXing;
            // 同步更新名称
            if (gongWuXing != null) {
                this.gongWuXingName = gongWuXing.getName();
            }
            return this;
        }

        public Builder shangGua(BaGua shangGua) {
            this.shangGua = shangGua;
            // 同步更新名称
            if (shangGua != null) {
                this.shangGuaName = shangGua.getName();
            }
            return this;
        }

        public Builder xiaGua(BaGua xiaGua) {
            this.xiaGua = xiaGua;
            // 同步更新名称
            if (xiaGua != null) {
                this.xiaGuaName = xiaGua.getName();
            }
            return this;
        }

        public Builder yaoList(List<Yao> yaoList) {
            this.yaoList = yaoList;
            return this;
        }

        /**
         * 构建GuaXiang对象
         * 包含参数校验和自动转换
         */
        public GuaXiang build() {
            // 自动转换枚举（如果只有名称）
            if (gongWuXing == null && gongWuXingName != null) {
                gongWuXing = WuXing.getByName(gongWuXingName);
            }
            if (shangGua == null && shangGuaName != null) {
                shangGua = BaGua.getByName(shangGuaName);
            }
            if (xiaGua == null && xiaGuaName != null) {
                xiaGua = BaGua.getByName(xiaGuaName);
            }
            
            // 参数校验
            if (guaName == null || guaName.isEmpty()) {
                throw new IllegalArgumentException("卦名不能为空");
            }
            if (shiYaoWei != null && (shiYaoWei < 1 || shiYaoWei > 6)) {
                throw new IllegalArgumentException("世爻位必须在1-6之间");
            }
            if (yingYaoWei != null && (yingYaoWei < 1 || yingYaoWei > 6)) {
                throw new IllegalArgumentException("应爻位必须在1-6之间");
            }
            if (yaoList != null && yaoList.size() != 6) {
                throw new IllegalArgumentException("六爻列表必须包含6个爻");
            }
            
            return new GuaXiang(id, guaName, suoShuGong, gongWuXingName, 
                    shiYaoWei, yingYaoWei, shangGuaName, xiaGuaName, guaLeiXing,
                    gongWuXing, shangGua, xiaGua, yaoList);
        }
    }

    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        return getFullDescription();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        GuaXiang guaXiang = (GuaXiang) o;
        
        if (id != null && guaXiang.id != null) {
            return id.equals(guaXiang.id);
        }
        
        return guaName.equals(guaXiang.guaName);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : guaName.hashCode();
    }
}

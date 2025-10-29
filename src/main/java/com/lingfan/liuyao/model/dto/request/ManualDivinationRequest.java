package com.lingfan.liuyao.model.dto.request;

import com.lingfan.liuyao.constant.DivinationConstants;

import java.util.List;

/**
 * 手动输入法起卦请求
 * 
 * <p>
 * 使用场景：
 * - 用户线下已起卦（摇硬币、蓍草等），仅需系统解卦
 * - 用户从书籍或其他来源获得卦象
 * - 用户想测试特定卦象
 * </p>
 * 
 * <p>
 * 输入格式：6个爻的阴阳和动静信息
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class ManualDivinationRequest extends DivinationRequest {
    
    /**
     * 六爻输入列表（从初爻到上爻，必须6个）
     */
    private List<YaoInput> yaoInputList;
    
    // ========== 抽象方法实现 ==========
    
    @Override
    public String getMethodType() {
        return DivinationConstants.METHOD_MANUAL;
    }
    
    // ========== Getter和Setter ==========
    
    public List<YaoInput> getYaoInputList() {
        return yaoInputList;
    }

    public void setYaoInputList(List<YaoInput> yaoInputList) {
        this.yaoInputList = yaoInputList;
    }
    
    // ========== 验证方法 ==========
    
    /**
     * 验证爻列表是否有效
     * 
     * @return 是否有效
     */
    public boolean hasValidYaoList() {
        if (yaoInputList == null || yaoInputList.size() != DivinationConstants.YAO_COUNT) {
            return false;
        }
        
        // 验证每个爻的数据
        for (int i = 0; i < yaoInputList.size(); i++) {
            YaoInput yaoInput = yaoInputList.get(i);
            if (yaoInput == null) {
                return false;
            }
            
            // 验证爻位连续性（1-6）
            if (yaoInput.getWeiZhi() != i + 1) {
                return false;
            }
            
            // 验证阴阳标识
            if (!DivinationConstants.YANG.equals(yaoInput.getYinYang()) 
                    && !DivinationConstants.YIN.equals(yaoInput.getYinYang())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取动爻数量
     * 
     * @return 动爻数量
     */
    public int getDongYaoCount() {
        if (yaoInputList == null) {
            return 0;
        }
        return (int) yaoInputList.stream().filter(YaoInput::isDong).count();
    }
    
    // ========== 内部类：爻输入 ==========
    
    /**
     * 单个爻的输入信息
     */
    public static class YaoInput {
        
        /**
         * 爻位（1-6）
         */
        private int weiZhi;
        
        /**
         * 阴阳标识（"YANG"或"YIN"）
         */
        private String yinYang;
        
        /**
         * 是否动爻
         */
        private boolean isDong;
        
        // ========== 构造函数 ==========
        
        public YaoInput() {
        }
        
        public YaoInput(int weiZhi, String yinYang, boolean isDong) {
            this.weiZhi = weiZhi;
            this.yinYang = yinYang;
            this.isDong = isDong;
        }
        
        // ========== Getter和Setter ==========
        
        public int getWeiZhi() {
            return weiZhi;
        }

        public void setWeiZhi(int weiZhi) {
            this.weiZhi = weiZhi;
        }

        public String getYinYang() {
            return yinYang;
        }

        public void setYinYang(String yinYang) {
            this.yinYang = yinYang;
        }

        public boolean isDong() {
            return isDong;
        }

        public void setDong(boolean dong) {
            isDong = dong;
        }
        
        // ========== 辅助方法 ==========
        
        /**
         * 判断是否阳爻
         */
        public boolean isYang() {
            return DivinationConstants.YANG.equals(yinYang);
        }
        
        /**
         * 判断是否阴爻
         */
        public boolean isYin() {
            return DivinationConstants.YIN.equals(yinYang);
        }
        
        /**
         * 判断是否静爻
         */
        public boolean isJing() {
            return !isDong;
        }
        
        // ========== Object方法重写 ==========
        
        @Override
        public String toString() {
            return "YaoInput{" +
                    "weiZhi=" + weiZhi +
                    ", yinYang='" + yinYang + '\'' +
                    ", isDong=" + isDong +
                    '}';
        }
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public String toString() {
        return "ManualDivinationRequest{" +
                "yaoInputList=" + yaoInputList +
                ", " + super.toString() +
                '}';
    }
}

package com.lingfan.liuyao.utils.liuyao;

import com.lingfan.liuyao.enums.*;
import com.lingfan.liuyao.model.dto.GongConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 64卦数据生成器（Linus式：程序生成，非手写SQL）
 * <p>
 * 知识库: knowledge-liuyao01.md (321-986行)
 * </p>
 * 
 * <p>
 * 核心算法：
 * 1. 初始化本宫卦（根据八卦符号的阴阳序列）
 * 2. 按变爻规律生成一世~五世卦
 * 3. 生成游魂卦（四爻变回）
 * 4. 生成归魂卦（下卦三爻变回本宫）
 * 5. 为每卦配置纳甲地支和六亲
 * 6. 生成INSERT SQL语句
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class GuaXiangDataGenerator {
    
    // ========== 64卦名称映射表（硬编码，从知识库提取）==========
    
    private static final String[][] GUA_NAMES = {
        // 乾宫8卦
        {"乾为天", "天风姤", "天山遁", "天地否", "风地观", "山地剥", "火地晋", "火天大有"},
        
        // 坎宫8卦
        {"坎为水", "水泽节", "水雷屯", "水火既济", "泽火革", "雷火丰", "地火明夷", "地水师"},
        
        // 艮宫8卦
        {"艮为山", "山火贲", "山天大畜", "山泽损", "火泽睽", "天泽履", "风泽中孚", "风山渐"},
        
        // 震宫8卦
        {"震为雷", "雷地豫", "雷水解", "雷风恒", "地风升", "水风井", "泽风大过", "泽雷随"},
        
        // 巽宫8卦
        {"巽为风", "风天小畜", "风火家人", "风雷益", "天雷无妄", "火雷噬嗑", "山雷颐", "山风蛊"},
        
        // 离宫8卦
        {"离为火", "火山旅", "火风鼎", "火水未济", "山水蒙", "风水涣", "天水讼", "天火同人"},
        
        // 坤宫8卦
        {"坤为地", "地雷复", "地泽临", "地天泰", "雷天大壮", "泽天夬", "水天需", "水地比"},
        
        // 兑宫8卦
        {"兑为泽", "泽水困", "泽地萃", "泽山咸", "水山蹇", "地山谦", "雷山小过", "雷泽归妹"}
    };
    
    // ========== main方法：执行生成器 ==========
    
    /**
     * main方法：执行生成器
     */
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("   64卦数据生成器（程序生成，非手写SQL）");
            System.out.println("========================================\n");
            
            // 1. 初始化8宫配置
            System.out.println("Step 1: 初始化8宫配置...");
            List<GongConfig> gongList = initGongConfigs();
            System.out.println("✅ 完成：共" + gongList.size() + "个宫\n");
            
            // 2. 生成64卦数据
            System.out.println("Step 2: 生成64卦数据...");
            List<GuaData> allGua = new ArrayList<>();
            int guaId = 1;
            
            for (int gongIndex = 0; gongIndex < gongList.size(); gongIndex++) {
                GongConfig gong = gongList.get(gongIndex);
                System.out.println("  生成" + gong.getGongName() + "8卦...");
                List<GuaData> gongGua = generateGongGua(gong, gongIndex, guaId);
                allGua.addAll(gongGua);
                guaId += 8;
            }
            System.out.println("✅ 完成：共" + allGua.size() + "卦\n");
            
            // 3. 验证数据
            System.out.println("Step 3: 验证数据完整性...");
            validateData(allGua);
            System.out.println("✅ 验证通过\n");
            
            // 4. 生成SQL文件
            System.out.println("Step 4: 生成INSERT SQL文件...");
            String filePath = "src/main/resources/sql/08_insert_64_hexagrams_generated.sql";
            String absolutePath = generateSQL(allGua, filePath);
            System.out.println("✅ SQL文件已生成");
            System.out.println("   相对路径: " + filePath);
            System.out.println("   绝对路径: " + absolutePath + "\n");
            
            System.out.println("========================================");
            System.out.println("   ✅ 64卦数据生成完成！");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== 初始化8宫配置 ==========
    
    /**
     * 初始化8宫配置
     */
    private static List<GongConfig> initGongConfigs() {
        List<GongConfig> list = new ArrayList<>();
        
        // 乾宫（金）：乾☰ = 111111（6阳）
        list.add(new GongConfig(
            "乾宫",
            WuXing.JIN,
            NaJiaConfigurator.getNaJiaSequence("乾宫"),
            new boolean[]{true, true, true, true, true, true}
        ));
        
        // 坎宫（水）：坎☵ = 010010（阴阳阴，重复）
        list.add(new GongConfig(
            "坎宫",
            WuXing.SHUI,
            NaJiaConfigurator.getNaJiaSequence("坎宫"),
            new boolean[]{false, true, false, false, true, false}
        ));
        
        // 艮宫（土）：艮☶ = 001001（阴阴阳，重复）
        list.add(new GongConfig(
            "艮宫",
            WuXing.TU,
            NaJiaConfigurator.getNaJiaSequence("艮宫"),
            new boolean[]{false, false, true, false, false, true}
        ));
        
        // 震宫（木）：震☳ = 100100（阳阴阴，重复）
        list.add(new GongConfig(
            "震宫",
            WuXing.MU,
            NaJiaConfigurator.getNaJiaSequence("震宫"),
            new boolean[]{true, false, false, true, false, false}
        ));
        
        // 巽宫（木）：巽☴ = 011011（阴阳阳，重复）
        list.add(new GongConfig(
            "巽宫",
            WuXing.MU,
            NaJiaConfigurator.getNaJiaSequence("巽宫"),
            new boolean[]{false, true, true, false, true, true}
        ));
        
        // 离宫（火）：离☲ = 101101（阳阴阳，重复）
        list.add(new GongConfig(
            "离宫",
            WuXing.HUO,
            NaJiaConfigurator.getNaJiaSequence("离宫"),
            new boolean[]{true, false, true, true, false, true}
        ));
        
        // 坤宫（土）：坤☷ = 000000（6阴）
        list.add(new GongConfig(
            "坤宫",
            WuXing.TU,
            NaJiaConfigurator.getNaJiaSequence("坤宫"),
            new boolean[]{false, false, false, false, false, false}
        ));
        
        // 兑宫（金）：兑☱ = 110110（阳阳阴，重复）
        list.add(new GongConfig(
            "兑宫",
            WuXing.JIN,
            NaJiaConfigurator.getNaJiaSequence("兑宫"),
            new boolean[]{true, true, false, true, true, false}
        ));
        
        return list;
    }
    
    // ========== 生成一个宫的8卦 ==========
    
    /**
     * 生成一个宫的8卦
     * 
     * @param gong 宫配置
     * @param gongIndex 宫序号（0-7）
     * @param startId 起始ID
     * @return 8卦数据
     */
    private static List<GuaData> generateGongGua(GongConfig gong, int gongIndex, int startId) {
        List<GuaData> guaList = new ArrayList<>();
        
        // 本宫卦的初始阴阳序列（从初爻到上爻）
        boolean[] yinYangSeq = gong.getBenGongYinYang();
        boolean[] benGongSeq = gong.getBenGongYinYang();  // 保存本宫序列，用于归魂卦
        
        // 生成8卦
        for (int guaIndex = 0; guaIndex < 8; guaIndex++) {
            // 1. 应用变爻规律
            if (guaIndex > 0) {
                yinYangSeq = applyBianYao(yinYangSeq, guaIndex, benGongSeq);
            }
            
            // 2. 获取世应位
            int shiWei = ShiYingLocator.getShiYaoWei(guaIndex);
            int yingWei = ShiYingLocator.getYingYaoWei(shiWei);
            
            // 3. 获取卦名
            String guaName = GUA_NAMES[gongIndex][guaIndex];
            
            // 4. 生成六爻数据
            DiZhi[] naJiaSeq = gong.getNaJiaSeq();
            List<YaoData> yaoList = new ArrayList<>();
            
            for (int weiZhi = 1; weiZhi <= 6; weiZhi++) {
                DiZhi diZhi = naJiaSeq[weiZhi - 1];
                LiuQin liuQin = LiuQinGenerator.generate(gong.getGongWuXing(), diZhi);
                boolean isYang = yinYangSeq[weiZhi - 1];
                
                yaoList.add(new YaoData(weiZhi, diZhi, liuQin, isYang));
            }
            
            // 5. 计算上卦、下卦
            String shangGua = getShangGua(yinYangSeq);
            String xiaGua = getXiaGua(yinYangSeq);
            
            // 6. 构建GuaData
            GuaData gua = new GuaData();
            gua.id = startId + guaIndex;
            gua.guaName = guaName;
            gua.suoShuGong = gong.getGongName();
            gua.gongWuXing = gong.getGongWuXing().getName();
            gua.shiYaoWei = shiWei;
            gua.yingYaoWei = yingWei;
            gua.guaLeiXing = ShiYingLocator.getGuaLeiXing(guaIndex);
            gua.shangGua = shangGua;
            gua.xiaGua = xiaGua;
            gua.yaoList = yaoList;
            
            guaList.add(gua);
        }
        
        return guaList;
    }
    
    // ========== 应用变爻规律 ==========
    
    /**
     * 应用变爻规律
     * <p>
     * 规律：
     * - 本宫→一世：初爻变（1爻变）
     * - 一世→二世：二爻变（2爻变）
     * - 二世→三世：三爻变（3爻变）
     * - 三世→四世：四爻变（4爻变）
     * - 四世→五世：五爻变（5爻变）
     * - 五世→游魂：四爻变回（4爻反转）
     * - 游魂→归魂：下卦三爻（1、2、3爻）全部变回本宫卦
     * </p>
     * 
     * @param currentSeq 当前阴阳序列
     * @param guaIndex 卦序号（1-7）
     * @param benGongSeq 本宫卦阴阳序列（用于归魂卦）
     * @return 变化后的阴阳序列
     */
    private static boolean[] applyBianYao(boolean[] currentSeq, int guaIndex, boolean[] benGongSeq) {
        boolean[] newSeq = currentSeq.clone();
        
        switch (guaIndex) {
            case 1:  // 本宫→一世：初爻变
                newSeq[0] = !newSeq[0];
                break;
            case 2:  // 一世→二世：二爻变
                newSeq[1] = !newSeq[1];
                break;
            case 3:  // 二世→三世：三爻变
                newSeq[2] = !newSeq[2];
                break;
            case 4:  // 三世→四世：四爻变
                newSeq[3] = !newSeq[3];
                break;
            case 5:  // 四世→五世：五爻变
                newSeq[4] = !newSeq[4];
                break;
            case 6:  // 五世→游魂：四爻变回
                newSeq[3] = !newSeq[3];
                break;
            case 7:  // 游魂→归魂：下卦三爻变回本宫
                newSeq[0] = benGongSeq[0];
                newSeq[1] = benGongSeq[1];
                newSeq[2] = benGongSeq[2];
                break;
        }
        
        return newSeq;
    }
    
    // ========== 获取上卦、下卦 ==========
    
    /**
     * 获取上卦（4、5、6爻）
     */
    private static String getShangGua(boolean[] yinYangSeq) {
        boolean yao4 = yinYangSeq[3];
        boolean yao5 = yinYangSeq[4];
        boolean yao6 = yinYangSeq[5];
        
        return getBaGuaName(yao4, yao5, yao6);
    }
    
    /**
     * 获取下卦（1、2、3爻）
     */
    private static String getXiaGua(boolean[] yinYangSeq) {
        boolean yao1 = yinYangSeq[0];
        boolean yao2 = yinYangSeq[1];
        boolean yao3 = yinYangSeq[2];
        
        return getBaGuaName(yao1, yao2, yao3);
    }
    
    /**
     * 根据三爻阴阳获取八卦名称
     * 
     * @param yao1 初爻（下）
     * @param yao2 二爻（中）
     * @param yao3 三爻（上）
     * @return 八卦名称
     */
    private static String getBaGuaName(boolean yao1, boolean yao2, boolean yao3) {
        if (yao1 && yao2 && yao3) return "乾";  // 111 (乾三连)
        if (!yao1 && !yao2 && !yao3) return "坤";  // 000 (坤六断)
        if (yao1 && !yao2 && !yao3) return "震";  // 100 (震仰盂)
        if (!yao1 && !yao2 && yao3) return "艮";  // 001 (艮覆碗)
        if (!yao1 && yao2 && !yao3) return "坎";  // 010 (坎中满)
        if (yao1 && !yao2 && yao3) return "离";  // 101 (离中虚)
        if (!yao1 && yao2 && yao3) return "巽";  // 011 (巽下断)
        if (yao1 && yao2 && !yao3) return "兑";  // 110 (兑上缺)
        
        return "未知";
    }
    
    // ========== 验证数据完整性 ==========
    
    /**
     * 验证数据完整性
     */
    private static void validateData(List<GuaData> guaList) {
        // 验证总数
        if (guaList.size() != 64) {
            throw new IllegalStateException("卦数量不正确，应为64卦，实际" + guaList.size() + "卦");
        }
        
        // 验证每卦
        for (GuaData gua : guaList) {
            // 验证六爻数量
            if (gua.yaoList.size() != 6) {
                throw new IllegalStateException(
                    String.format("卦%s的爻数量不正确，应为6爻，实际%d爻", 
                        gua.guaName, gua.yaoList.size())
                );
            }
            
            // 验证世应位
            if (gua.shiYaoWei < 1 || gua.shiYaoWei > 6) {
                throw new IllegalStateException(
                    String.format("卦%s的世爻位无效：%d", gua.guaName, gua.shiYaoWei)
                );
            }
            if (gua.yingYaoWei < 1 || gua.yingYaoWei > 6) {
                throw new IllegalStateException(
                    String.format("卦%s的应爻位无效：%d", gua.guaName, gua.yingYaoWei)
                );
            }
        }
        
        System.out.println("  ✓ 卦数量: 64卦");
        System.out.println("  ✓ 每卦爻数: 6爻");
        System.out.println("  ✓ 世应位有效");
    }
    
    // ========== 生成INSERT SQL ==========
    
    /**
     * 生成INSERT SQL文件
     * 
     * @return 生成文件的绝对路径
     */
    private static String generateSQL(List<GuaData> guaList, String filePath) throws IOException {
        // 获取绝对路径
        java.nio.file.Path path = Paths.get(filePath).toAbsolutePath();
        
        // 确保目录存在
        Files.createDirectories(path.getParent());
        
        try (FileWriter writer = new FileWriter(path.toFile())) {
            // 写入文件头
            writer.write("-- ========================================\n");
            writer.write("-- 64卦数据插入脚本（程序自动生成）\n");
            writer.write("-- 生成时间: " + java.time.LocalDateTime.now() + "\n");
            writer.write("-- 生成工具: GuaXiangDataGenerator.java\n");
            writer.write("-- ========================================\n\n");
            
            writer.write("-- 删除旧数据\n");
            writer.write("DELETE FROM gua_xiang_base WHERE id BETWEEN 1 AND 64;\n\n");
            
            writer.write("-- 插入64卦数据\n");
            
            for (GuaData gua : guaList) {
                writer.write(String.format(
                    "INSERT INTO gua_xiang_base (id, gua_name, suo_shu_gong, gong_wu_xing, " +
                    "shi_yao_wei, ying_yao_wei, shang_gua, xia_gua, gua_lei_xing) VALUES\n" +
                    "(%d, '%s', '%s', '%s', %d, %d, '%s', '%s', '%s');\n",
                    gua.id, gua.guaName, gua.suoShuGong, gua.gongWuXing,
                    gua.shiYaoWei, gua.yingYaoWei, gua.shangGua, gua.xiaGua, gua.guaLeiXing
                ));
            }
            
            writer.write("\n-- ✅ 64卦数据插入完成\n");
        }
        
        return path.toString();
    }
    
    // ========== 内部数据类 ==========
    
    /**
     * 卦数据临时存储类
     */
    private static class GuaData {
        int id;
        String guaName;
        String suoShuGong;
        String gongWuXing;
        int shiYaoWei;
        int yingYaoWei;
        String shangGua;
        String xiaGua;
        String guaLeiXing;
        List<YaoData> yaoList;
    }
    
    /**
     * 爻数据临时存储类
     */
    private static class YaoData {
        int weiZhi;
        DiZhi diZhi;
        LiuQin liuQin;
        boolean isYang;
        
        YaoData(int weiZhi, DiZhi diZhi, LiuQin liuQin, boolean isYang) {
            this.weiZhi = weiZhi;
            this.diZhi = diZhi;
            this.liuQin = liuQin;
            this.isYang = isYang;
        }
    }
}

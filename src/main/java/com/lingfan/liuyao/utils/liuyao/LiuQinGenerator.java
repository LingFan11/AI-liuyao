package com.lingfan.liuyao.utils.liuyao;

import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.LiuQin;
import com.lingfan.liuyao.enums.WuXing;

/**
 * 六亲生成器
 * <p>
 * 知识库: knowledge-liuyao02.md (10-178行)
 * </p>
 * 
 * <p>
 * 六亲规则：
 * - 生我者 → 父母
 * - 我生者 → 子孙
 * - 克我者 → 官鬼
 * - 我克者 → 妻财
 * - 同我者 → 兄弟
 * </p>
 * 
 * <p>
 * 五行生克速查：
 * - 乾兑金克兄弟父，木鬼火鬼水子孙 - 乾兑两宫，八卦俱属金
 * - 坎宫木子克属水，金父火财土为鬼 - 坎宫属水
 * - 坤艮土克兄子父，水鬼木财金子孙 - 坤艮两宫，八卦俱属土
 * - 离宫木父交土子，水鬼金财火克兄 - 离宫属火
 * - 震巽木克兄父母，金鬼火子财是土 - 震巽两宫，八卦俱属木
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public class LiuQinGenerator {
    
    /**
     * 生成六亲
     * 
     * @param gongWuXing 宫五行（"我"）
     * @param yaoZhi 爻地支（用于获取爻五行）
     * @return 六亲类型
     */
    public static LiuQin generate(WuXing gongWuXing, DiZhi yaoZhi) {
        if (gongWuXing == null) {
            throw new IllegalArgumentException("宫五行不能为空");
        }
        if (yaoZhi == null) {
            throw new IllegalArgumentException("爻地支不能为空");
        }
        
        // 1. 获取爻五行
        WuXing yaoWuXing = yaoZhi.getWuXing();
        
        // 2. 判断五行关系
        // 生我者 → 父母（爻五行生宫五行）
        if (yaoWuXing.isSheng(gongWuXing)) {
            return LiuQin.FU_MU;
        }
        
        // 我生者 → 子孙（宫五行生爻五行）
        if (gongWuXing.isSheng(yaoWuXing)) {
            return LiuQin.ZI_SUN;
        }
        
        // 克我者 → 官鬼（爻五行克宫五行）
        if (yaoWuXing.isKe(gongWuXing)) {
            return LiuQin.GUAN_GUI;
        }
        
        // 我克者 → 妻财（宫五行克爻五行）
        if (gongWuXing.isKe(yaoWuXing)) {
            return LiuQin.QI_CAI;
        }
        
        // 同我者 → 兄弟
        if (yaoWuXing == gongWuXing) {
            return LiuQin.XIONG_DI;
        }
        
        // 理论上不会到这里
        throw new IllegalStateException(
            String.format("无法确定六亲关系：宫五行=%s, 爻五行=%s", 
                gongWuXing.getName(), yaoWuXing.getName())
        );
    }
}

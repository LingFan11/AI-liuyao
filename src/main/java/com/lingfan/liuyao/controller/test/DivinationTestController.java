package com.lingfan.liuyao.controller.test;

import com.lingfan.liuyao.constant.DivinationConstants;
import com.lingfan.liuyao.enums.DiZhi;
import com.lingfan.liuyao.enums.TianGan;
import com.lingfan.liuyao.enums.ZhanBuLeiXing;
import com.lingfan.liuyao.model.dto.DivinationResult;
import com.lingfan.liuyao.model.dto.request.CoinDivinationRequest;
import com.lingfan.liuyao.model.dto.request.ManualDivinationRequest;
import com.lingfan.liuyao.model.entity.Yao;
import com.lingfan.liuyao.service.divination.DivinationFactory;
import com.lingfan.liuyao.service.divination.DivinationMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 起卦测试控制器
 * 
 * <p>
 * 提供起卦功能的测试接口，包括：
 * - 查看工厂注册信息
 * - 测试手动输入法
 * - 快速测试预设卦象
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@RestController
@RequestMapping("/test/divination")
public class DivinationTestController {
    
    @Autowired
    private DivinationFactory factory;
    
    /**
     * 查看工厂注册信息
     * 
     * @return 工厂信息
     */
    @GetMapping("/factory/info")
    public Map<String, Object> getFactoryInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("methodCount", factory.getMethodCount());
        result.put("supportedMethods", factory.getSupportedMethods());
        result.put("methodInfo", factory.getAllMethodInfo());
        return result;
    }
    
    /**
     * 测试手动输入法起卦
     * 
     * <p>
     * 请求体示例：
     * <pre>
     * {
     *   "riGan": "JIA",
     *   "riChen": "ZI",
     *   "yueJian": "YIN",
     *   "zhanBuLeiXing": "GONG_MING",
     *   "wenShi": "测试起卦",
     *   "gender": "男",
     *   "yaoInputList": [
     *     {"weiZhi": 1, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 2, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 3, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 4, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 5, "yinYang": "YANG", "isDong": false},
     *     {"weiZhi": 6, "yinYang": "YANG", "isDong": false}
     *   ]
     * }
     * </pre>
     * </p>
     * 
     * @param request 手动输入法请求
     * @return 起卦结果
     */
    @PostMapping("/manual")
    public Map<String, Object> testManualDivination(@RequestBody ManualDivinationRequest request) {
        // 获取手动输入法
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        
        // 执行起卦
        DivinationResult result = method.cast(request);
        
        // 封装返回结果
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：乾为天（六爻皆静）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/qian")
    public Map<String, Object> testQianGua() {
        ManualDivinationRequest request = createTestRequest(
                "111111",  // 乾卦
                new boolean[]{false, false, false, false, false, false}  // 全静
        );
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：坤为地（六爻皆静）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/kun")
    public Map<String, Object> testKunGua() {
        ManualDivinationRequest request = createTestRequest(
                "000000",  // 坤卦
                new boolean[]{false, false, false, false, false, false}  // 全静
        );
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：乾为天变天风姤（初爻动）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/qian-bian-gou")
    public Map<String, Object> testQianBianGou() {
        ManualDivinationRequest request = createTestRequest(
                "111111",  // 乾卦
                new boolean[]{true, false, false, false, false, false}  // 初爻动
        );
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：乾为天变同人卦（五爻动）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/qian-bian-tongren")
    public Map<String, Object> testQianBianTongRen() {
        ManualDivinationRequest request = createTestRequest(
                "111111",  // 乾卦
                new boolean[]{false, false, false, false, true, false}  // 五爻动
        );
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：坤为地变地雷复（初爻动）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/kun-bian-fu")
    public Map<String, Object> testKunBianFu() {
        ManualDivinationRequest request = createTestRequest(
                "000000",  // 坤卦
                new boolean[]{true, false, false, false, false, false}  // 初爻动
        );
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：多个动爻（初爻、三爻、五爻动）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/multiple-dong")
    public Map<String, Object> testMultipleDong() {
        ManualDivinationRequest request = createTestRequest(
                "111111",  // 乾卦
                new boolean[]{true, false, true, false, true, false}  // 初、三、五爻动
        );
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_MANUAL);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    // ========== 钱币起卦法测试接口 ==========
    
    /**
     * 测试钱币起卦法（随机）
     * 
     * <p>
     * 请求体示例：
     * <pre>
     * {
     *   "riGan": "JIA",
     *   "riChen": "ZI",
     *   "yueJian": "YIN",
     *   "zhanBuLeiXing": "GONG_MING",
     *   "wenShi": "测试钱币起卦",
     *   "gender": "男"
     * }
     * </pre>
     * </p>
     * 
     * @param request 钱币起卦请求
     * @return 起卦结果
     */
    @PostMapping("/coin")
    public Map<String, Object> testCoinDivination(@RequestBody CoinDivinationRequest request) {
        // 获取钱币起卦法
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_COIN);
        
        // 执行起卦
        DivinationResult result = method.cast(request);
        
        // 封装返回结果
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：钱币起卦（真随机）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/coin-random")
    public Map<String, Object> testCoinRandom() {
        CoinDivinationRequest request = createCoinTestRequest(null);
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_COIN);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：钱币起卦（固定种子1）
     * 
     * <p>
     * 使用固定种子，每次结果相同，便于测试
     * </p>
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/coin-seed1")
    public Map<String, Object> testCoinSeed1() {
        CoinDivinationRequest request = createCoinTestRequest(12345L);
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_COIN);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 快速测试：钱币起卦（固定种子2）
     * 
     * @return 起卦结果
     */
    @GetMapping("/quick/coin-seed2")
    public Map<String, Object> testCoinSeed2() {
        CoinDivinationRequest request = createCoinTestRequest(54321L);
        
        DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_COIN);
        DivinationResult result = method.cast(request);
        
        return buildResultMap(result);
    }
    
    /**
     * 批量测试：钱币起卦10次
     * 
     * <p>
     * 测试随机性分布
     * </p>
     * 
     * @return 10次起卦结果的汇总
     */
    @GetMapping("/quick/coin-batch")
    public Map<String, Object> testCoinBatch() {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            CoinDivinationRequest request = createCoinTestRequest(null);
            DivinationMethod method = factory.getMethod(DivinationConstants.METHOD_COIN);
            DivinationResult result = method.cast(request);
            
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("round", i + 1);
            summary.put("benGua", result.getBenGua().getGuaName());
            summary.put("bianGua", result.hasBianGua() ? result.getBianGua().getGuaName() : "无");
            summary.put("dongYaoCount", result.getDongYaoCount());
            summary.put("binaryCode", result.getBenGuaBinaryCode());
            
            results.add(summary);
        }
        
        Map<String, Object> batchResult = new LinkedHashMap<>();
        batchResult.put("totalRounds", 10);
        batchResult.put("results", results);
        
        return batchResult;
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 创建测试请求
     * 
     * @param binaryCode 6位二进制（如"111111"）
     * @param dongFlags 动爻标识数组（6个元素）
     * @return ManualDivinationRequest
     */
    private ManualDivinationRequest createTestRequest(String binaryCode, boolean[] dongFlags) {
        ManualDivinationRequest request = new ManualDivinationRequest();
        
        // 时空信息
        request.setRiGan(TianGan.JIA);
        request.setRiChen(DiZhi.ZI);
        request.setYueJian(DiZhi.YIN);
        request.setDivinationTime(LocalDateTime.now());
        
        // 占卜信息
        request.setZhanBuLeiXing(ZhanBuLeiXing.GONG_MING);
        request.setWenShi("测试起卦");
        request.setGender("男");
        
        // 爻列表
        List<ManualDivinationRequest.YaoInput> yaoList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ManualDivinationRequest.YaoInput yao = new ManualDivinationRequest.YaoInput();
            yao.setWeiZhi(i + 1);
            yao.setYinYang(binaryCode.charAt(i) == '1' ? DivinationConstants.YANG : DivinationConstants.YIN);
            yao.setDong(dongFlags[i]);
            yaoList.add(yao);
        }
        request.setYaoInputList(yaoList);
        
        return request;
    }
    
    /**
     * 创建钱币起卦测试请求
     * 
     * @param seed 随机种子（null表示真随机）
     * @return CoinDivinationRequest
     */
    private CoinDivinationRequest createCoinTestRequest(Long seed) {
        CoinDivinationRequest request = new CoinDivinationRequest();
        
        // 时空信息
        request.setRiGan(TianGan.JIA);
        request.setRiChen(DiZhi.ZI);
        request.setYueJian(DiZhi.YIN);
        request.setDivinationTime(LocalDateTime.now());
        
        // 占卜信息
        request.setZhanBuLeiXing(ZhanBuLeiXing.GONG_MING);
        request.setWenShi("测试钱币起卦");
        request.setGender("男");
        
        // 随机种子
        request.setRandomSeed(seed);
        
        return request;
    }
    
    /**
     * 构建返回结果Map
     * 
     * @param result 起卦结果
     * @return 结果Map
     */
    private Map<String, Object> buildResultMap(DivinationResult result) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        
        // 基本信息
        resultMap.put("methodType", result.getMethodType());
        resultMap.put("methodName", result.getMethodName());
        resultMap.put("createTime", result.getCreateTime());
        
        // 本卦信息
        Map<String, Object> benGuaMap = new LinkedHashMap<>();
        benGuaMap.put("id", result.getBenGua().getId());
        benGuaMap.put("guaName", result.getBenGua().getGuaName());
        benGuaMap.put("suoShuGong", result.getBenGua().getSuoShuGong());
        benGuaMap.put("guaLeiXing", result.getBenGua().getGuaLeiXing());
        benGuaMap.put("binaryCode", result.getBenGuaBinaryCode());
        benGuaMap.put("shiYaoWei", result.getBenGua().getShiYaoWei());
        benGuaMap.put("yingYaoWei", result.getBenGua().getYingYaoWei());
        resultMap.put("benGua", benGuaMap);
        
        // 变卦信息
        if (result.hasBianGua()) {
            Map<String, Object> bianGuaMap = new LinkedHashMap<>();
            bianGuaMap.put("id", result.getBianGua().getId());
            bianGuaMap.put("guaName", result.getBianGua().getGuaName());
            bianGuaMap.put("suoShuGong", result.getBianGua().getSuoShuGong());
            bianGuaMap.put("guaLeiXing", result.getBianGua().getGuaLeiXing());
            bianGuaMap.put("binaryCode", result.getBianGuaBinaryCode());
            bianGuaMap.put("shiYaoWei", result.getBianGua().getShiYaoWei());
            bianGuaMap.put("yingYaoWei", result.getBianGua().getYingYaoWei());
            resultMap.put("bianGua", bianGuaMap);
        } else {
            resultMap.put("bianGua", null);
        }
        
        // 动爻信息
        resultMap.put("dongYaoCount", result.getDongYaoCount());
        resultMap.put("hasDongYao", result.hasDongYao());
        resultMap.put("isJingGua", result.isJingGua());
        resultMap.put("isQuanDong", result.isQuanDong());
        
        // 爻列表
        List<Map<String, Object>> yaoListMap = new ArrayList<>();
        for (Yao yao : result.getYaoList()) {
            Map<String, Object> yaoMap = new LinkedHashMap<>();
            yaoMap.put("weiZhi", yao.getWeiZhi());
            yaoMap.put("weiZhiName", yao.getWeiZhiName());
            yaoMap.put("diZhi", yao.getDiZhi().getName());
            yaoMap.put("liuQin", yao.getLiuQin().getName());
            yaoMap.put("wuXing", yao.getWuXing().getName());
            yaoMap.put("yinYang", yao.getYinYang());
            yaoMap.put("isDong", yao.isDong());
            
            if (yao.isDong() && yao.getBianYao() != null) {
                Map<String, Object> bianYaoMap = new LinkedHashMap<>();
                bianYaoMap.put("diZhi", yao.getBianYao().getDiZhi().getName());
                bianYaoMap.put("liuQin", yao.getBianYao().getLiuQin().getName());
                bianYaoMap.put("wuXing", yao.getBianYao().getWuXing().getName());
                yaoMap.put("bianYao", bianYaoMap);
            }
            
            yaoListMap.add(yaoMap);
        }
        resultMap.put("yaoList", yaoListMap);
        
        return resultMap;
    }
}

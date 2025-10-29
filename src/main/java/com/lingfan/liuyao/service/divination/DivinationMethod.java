package com.lingfan.liuyao.service.divination;

import com.lingfan.liuyao.model.dto.DivinationResult;
import com.lingfan.liuyao.model.dto.request.DivinationRequest;

/**
 * 起卦方法接口
 * 
 * <p>
 * 所有起卦方法（手动输入、钱币、时间等）都必须实现此接口
 * 使用策略模式，不同起卦方法实现同一接口
 * </p>
 * 
 * <p>
 * 设计理念：
 * - 策略模式：不同起卦方法实现同一接口，上层调用无需关心具体实现
 * - 依赖倒置：上层业务依赖接口，不依赖具体实现
 * - 开闭原则：新增起卦方法时只需实现此接口，无需修改现有代码
 * </p>
 * 
 * <p>
 * 实现类建议：
 * - 使用@Component注解，让Spring自动注册
 * - 实现类会被DivinationFactory自动发现和注册
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
public interface DivinationMethod {
    
    /**
     * 执行起卦
     * 
     * <p>
     * 业务流程：
     * 1. 验证输入参数（request中必须包含的信息）
     * 2. 执行具体的起卦逻辑（根据不同方法实现）
     * 3. 生成本卦（GuaXiang）
     * 4. 计算变卦（如果有动爻）
     * 5. 封装返回DivinationResult
     * </p>
     * 
     * <p>
     * 异常处理：
     * - 参数无效时抛出IllegalArgumentException
     * - 业务异常时抛出BusinessException
     * </p>
     * 
     * @param request 起卦请求（包含时空信息、占卜类型等，不同方法有不同的Request子类）
     * @return 起卦结果（包含本卦、变卦、爻列表、动爻数量）
     * @throws IllegalArgumentException 参数无效
     * @throws com.lingfan.liuyao.exception.BusinessException 起卦失败
     */
    DivinationResult cast(DivinationRequest request);
    
    /**
     * 获取起卦方法名称
     * 
     * @return 方法名称（如"手动输入法"、"钱币起卦法"、"时间起卦法"等）
     */
    String getMethodName();
    
    /**
     * 获取起卦方法类型代码
     * 
     * @return 类型代码（如"MANUAL"、"COIN"、"TIME"等，建议使用DivinationConstants中的常量）
     */
    String getMethodType();
    
    /**
     * 获取方法描述
     * 
     * @return 方法的详细描述（可选实现，默认返回方法名称）
     */
    default String getMethodDescription() {
        return getMethodName();
    }
    
    /**
     * 判断是否支持指定的请求类型
     * 
     * @param request 起卦请求
     * @return 是否支持
     */
    default boolean supportsRequest(DivinationRequest request) {
        return request != null && getMethodType().equals(request.getMethodType());
    }
}

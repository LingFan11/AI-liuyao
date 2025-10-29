package com.lingfan.liuyao.service.divination;

import com.lingfan.liuyao.constant.DivinationConstants;
import com.lingfan.liuyao.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 起卦方法工厂
 * 
 * <p>
 * 负责管理所有起卦方法的注册和获取
 * 使用工厂模式，根据类型代码返回对应的起卦方法实现
 * </p>
 * 
 * <p>
 * 设计理念：
 * - 工厂模式：根据类型代码返回对应的起卦方法实现
 * - Spring自动注册：启动时自动注册所有DivinationMethod实现类
 * - 线程安全：使用ConcurrentHashMap保证并发安全
 * - 扩展性强：新增起卦方法无需修改工厂代码，只需实现接口并添加@Component注解
 * </p>
 * 
 * <p>
 * 使用方式：
 * <pre>
 * // 注入工厂
 * &#64;Autowired
 * private DivinationFactory factory;
 * 
 * // 获取起卦方法
 * DivinationMethod method = factory.getMethod("MANUAL");
 * 
 * // 执行起卦
 * DivinationResult result = method.cast(request);
 * </pre>
 * </p>
 * 
 * @author LingFan
 * @since 2025-10-29
 */
@Component
public class DivinationFactory implements InitializingBean {
    
    private static final Logger log = LoggerFactory.getLogger(DivinationFactory.class);
    
    /**
     * 起卦方法注册表
     * Key: 方法类型代码（如"MANUAL"、"COIN"、"TIME"）
     * Value: 起卦方法实现
     */
    private final Map<String, DivinationMethod> methodRegistry = new ConcurrentHashMap<>();
    
    /**
     * 所有起卦方法实现（Spring自动注入）
     */
    private final List<DivinationMethod> allMethods;
    
    /**
     * 构造函数（Spring自动注入所有DivinationMethod实现类）
     * 
     * @param allMethods Spring容器中所有DivinationMethod的实现类
     */
    @Autowired
    public DivinationFactory(List<DivinationMethod> allMethods) {
        this.allMethods = allMethods;
    }
    
    /**
     * Spring容器启动后自动执行
     * 注册所有起卦方法
     */
    @Override
    public void afterPropertiesSet() {
        if (allMethods == null || allMethods.isEmpty()) {
            log.warn("未发现任何起卦方法实现类，请检查是否添加了@Component注解");
            return;
        }
        
        log.info("开始注册起卦方法，共发现{}个实现类", allMethods.size());
        
        for (DivinationMethod method : allMethods) {
            registerMethod(method);
        }
        
        log.info("起卦方法注册完成，共注册{}个方法：{}", 
                methodRegistry.size(), 
                methodRegistry.keySet());
    }
    
    /**
     * 注册起卦方法
     * 
     * @param method 起卦方法实现
     */
    private void registerMethod(DivinationMethod method) {
        if (method == null) {
            log.warn("尝试注册空的起卦方法，已忽略");
            return;
        }
        
        String methodType = method.getMethodType();
        if (methodType == null || methodType.trim().isEmpty()) {
            log.warn("起卦方法{}的类型代码为空，已忽略", method.getClass().getSimpleName());
            return;
        }
        
        // 转换为大写，统一格式
        String typeUpperCase = methodType.toUpperCase();
        
        // 检查是否重复注册
        if (methodRegistry.containsKey(typeUpperCase)) {
            log.warn("起卦方法类型{}已存在，新的实现类{}将覆盖旧的", 
                    typeUpperCase, 
                    method.getClass().getSimpleName());
        }
        
        // 注册到工厂
        methodRegistry.put(typeUpperCase, method);
        
        log.info("注册起卦方法: {} ({}) - {}", 
                method.getMethodName(), 
                typeUpperCase,
                method.getClass().getSimpleName());
    }
    
    /**
     * 获取起卦方法
     * 
     * <p>
     * 业务流程：
     * 1. 验证方法类型代码
     * 2. 从注册表中查找对应的方法实现
     * 3. 如果找不到，抛出异常
     * </p>
     * 
     * @param methodType 方法类型代码（如"MANUAL"、"COIN"、"TIME"，不区分大小写）
     * @return 起卦方法实现
     * @throws BusinessException 如果找不到对应的方法
     */
    public DivinationMethod getMethod(String methodType) {
        // 参数校验
        if (methodType == null || methodType.trim().isEmpty()) {
            throw new BusinessException("起卦方法类型不能为空");
        }
        
        // 从注册表中查找（转换为大写）
        String typeUpperCase = methodType.toUpperCase();
        DivinationMethod method = methodRegistry.get(typeUpperCase);
        
        // 找不到则抛出异常
        if (method == null) {
            String errorMsg = String.format("%s: %s，支持的方法：%s", 
                    DivinationConstants.ERROR_UNSUPPORTED_METHOD,
                    methodType,
                    String.join(", ", methodRegistry.keySet()));
            throw new BusinessException(errorMsg);
        }
        
        return method;
    }
    
    /**
     * 判断是否支持指定的起卦方法
     * 
     * @param methodType 方法类型代码
     * @return 是否支持
     */
    public boolean supportsMethod(String methodType) {
        if (methodType == null || methodType.trim().isEmpty()) {
            return false;
        }
        return methodRegistry.containsKey(methodType.toUpperCase());
    }
    
    /**
     * 获取所有已注册的起卦方法类型
     * 
     * @return 方法类型集合（不可变）
     */
    public Set<String> getSupportedMethods() {
        return new HashSet<>(methodRegistry.keySet());
    }
    
    /**
     * 获取已注册的起卦方法数量
     * 
     * @return 数量
     */
    public int getMethodCount() {
        return methodRegistry.size();
    }
    
    /**
     * 获取指定方法的详细信息
     * 
     * @param methodType 方法类型代码
     * @return 方法信息描述
     */
    public String getMethodInfo(String methodType) {
        if (!supportsMethod(methodType)) {
            return "不支持的起卦方法: " + methodType;
        }
        
        DivinationMethod method = methodRegistry.get(methodType.toUpperCase());
        return String.format("方法名称: %s, 类型: %s, 描述: %s", 
                method.getMethodName(), 
                method.getMethodType(),
                method.getMethodDescription());
    }
    
    /**
     * 获取所有起卦方法的信息列表
     * 
     * @return 方法信息列表
     */
    public List<String> getAllMethodInfo() {
        return methodRegistry.values().stream()
                .map(method -> String.format("%s (%s)", method.getMethodName(), method.getMethodType()))
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
}

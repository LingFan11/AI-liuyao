package com.lingfan.liuyao.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * MongoDB配置类
 * 
 * 功能：
 * 1. 配置MongoDB连接
 * 2. 配置自定义转换器（LocalDateTime <-> Date）
 * 3. 启用审计功能
 * 
 * @author Liuyao Team
 * @since 2025-10-23
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    
    /**
     * 配置MongoDB自定义转换器
     * 
     * @return MongoCustomConversions
     */
    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = CollUtil.newArrayList(
            new LocalDateTimeToDateConverter(),
            new DateToLocalDateTimeConverter()
        );
        return new MongoCustomConversions(converters);
    }
    
    /**
     * LocalDateTime写入转换器
     * MongoDB不支持LocalDateTime，需要转换为Date
     */
    private static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(LocalDateTime source) {
            return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
        }
    }
    
    /**
     * LocalDateTime读取转换器
     * 从MongoDB读取Date，转换为LocalDateTime
     */
    private static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date source) {
            return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }
}

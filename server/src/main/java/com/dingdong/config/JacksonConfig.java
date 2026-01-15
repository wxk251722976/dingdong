package com.dingdong.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Jackson配置类
 * 1. 解决Long类型精度丢失问题（序列化为String）
 * 2. 将LocalDateTime/LocalDate序列化为时间戳（毫秒）
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder
                // Long类型序列化为String，防止精度丢失
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance)
                // LocalDateTime序列化为时间戳（毫秒）
                .serializerByType(LocalDateTime.class, new LocalDateTimeToTimestampSerializer())
                // LocalDate序列化为时间戳（毫秒，取当天0点）
                .serializerByType(LocalDate.class, new LocalDateToTimestampSerializer());
    }

    /**
     * LocalDateTime -> 时间戳（毫秒）
     */
    public static class LocalDateTimeToTimestampSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value != null) {
                long timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                gen.writeNumber(timestamp);
            } else {
                gen.writeNull();
            }
        }
    }

    /**
     * LocalDate -> 时间戳（毫秒，取当天0点）
     */
    public static class LocalDateToTimestampSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                long timestamp = value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                gen.writeNumber(timestamp);
            } else {
                gen.writeNull();
            }
        }
    }
}

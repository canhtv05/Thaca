package com.thaca.framework.core.utils;

import com.thaca.framework.core.utils.json.InstantToStringSerializer;
import com.thaca.framework.core.utils.json.StringToInstantDeserializer;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.TypeFactory;

@Slf4j
public class JsonF {

    private static final ObjectMapper objectMapper;

    static {
        SimpleModule timeModule = new SimpleModule();
        timeModule.addSerializer(Instant.class, new InstantToStringSerializer());
        timeModule.addDeserializer(Instant.class, new StringToInstantDeserializer());

        objectMapper = JsonMapper.builder().addModule(timeModule).findAndAddModules().build();
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("[JsonF] toJson()]:: ", e);
            return null;
        }
    }

    public static <T> T jsonToObject(String str, Class<T> clazz) {
        if (str == null) return null;
        try {
            return objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            log.error("[JsonF] jsonToObject()]:: ", e);
            return null;
        }
    }

    public static <T> T jsonToObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            log.error("[JsonF] jsonToObject(byte[]):: ", e);
            return null;
        }
    }

    public static <T> T jsonToObject(String str, TypeReference<T> type) {
        if (str == null) return null;
        try {
            return objectMapper.readValue(str, type);
        } catch (Exception e) {
            log.error("[JsonF] jsonToObject()]:: ", e);
            return null;
        }
    }

    public static <T> T jsonToObject(String str, ParameterizedTypeReference<T> typeRef) {
        if (str == null) return null;
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            JavaType javaType = typeFactory.constructType(typeRef.getType());
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.error("[JsonF] jsonToObject(ParameterizedTypeReference):: ", e);
            return null;
        }
    }

    public static JsonNode readTree(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return objectMapper.readTree(bytes);
        } catch (Exception e) {
            log.error("[JsonF] readTree()]:: ", e);
            return null;
        }
    }
}

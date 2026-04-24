package com.thaca.framework.core.configs;

import com.thaca.framework.core.annotations.ServletOnly;
import com.thaca.framework.core.context.FwContextBody;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiPayload;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ServletOnly
public class ApiPayloadArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterIndex() == 0 && !isFrameworkOrJdkType(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        @NonNull MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null || "GET".equalsIgnoreCase(servletRequest.getMethod())) {
            return null;
        }
        byte[] bodyBytes = StreamUtils.copyToByteArray(servletRequest.getInputStream());
        if (bodyBytes.length == 0) {
            return null;
        }
        String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
        JavaType targetType = objectMapper
            .getTypeFactory()
            .constructParametricType(
                ApiPayload.class,
                objectMapper.getTypeFactory().constructType(parameter.getGenericParameterType())
            );
        ApiPayload<?> payload = objectMapper.readValue(bodyString, targetType);
        if (payload == null) {
            return null;
        }
        if (payload.getHeader() != null) {
            FwContextHeader.set(payload.getHeader());
        }
        if (payload.getBody() != null) {
            FwContextBody.set(payload.getBody());
        }
        if (payload.getBody() != null) {
            return payload.getBody().getData();
        }
        return null;
    }

    private boolean isFrameworkOrJdkType(Class<?> type) {
        String name = type.getName();
        return (
            type.isPrimitive() ||
            name.startsWith("java.") ||
            name.startsWith("jakarta.") ||
            name.startsWith("org.springframework.") ||
            name.startsWith("com.fasterxml.") ||
            ApiPayload.class.isAssignableFrom(type)
        );
    }
}

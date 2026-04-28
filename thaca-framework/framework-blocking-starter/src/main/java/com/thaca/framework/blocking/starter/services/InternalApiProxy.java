package com.thaca.framework.blocking.starter.services;

import com.thaca.framework.core.annotations.FwInternalApi;
import com.thaca.framework.core.dtos.ApiPayload;
import java.lang.reflect.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;

@Slf4j
@RequiredArgsConstructor
public class InternalApiProxy implements InvocationHandler {

    private final InternalApiClient client;
    private final String baseUrl;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> "InternalApiProxy[" + baseUrl + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException();
            };
        }

        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }

        FwInternalApi api = method.getAnnotation(FwInternalApi.class);
        if (api == null) {
            throw new UnsupportedOperationException(
                "Method " + method.getName() + " is missing @FwInternalApi annotation"
            );
        }

        String url = baseUrl + api.path();
        Object requestData = (args != null && args.length > 0) ? args[0] : null;
        Type returnType = method.getGenericReturnType();

        if (returnType == byte[].class) {
            return client.postRaw(url, requestData);
        }

        if (returnType == void.class || returnType == Void.class) {
            client.post(url, requestData, buildTypeReference(returnType));
            return null;
        }

        return client.post(url, requestData, buildTypeReference(returnType));
    }

    @SuppressWarnings("rawtypes")
    private ParameterizedTypeReference buildTypeReference(Type returnType) {
        Type apiPayloadType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { returnType };
            }

            @Override
            public Type getRawType() {
                return ApiPayload.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return ParameterizedTypeReference.forType(apiPayloadType);
    }
}

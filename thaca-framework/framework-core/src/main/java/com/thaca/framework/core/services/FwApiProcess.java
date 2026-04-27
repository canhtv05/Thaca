package com.thaca.framework.core.services;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.context.FwServiceContext;
import com.thaca.framework.core.exceptions.FwException;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FwApiProcess {

    private final FwModeRegistry registry;

    public <T> T process(Void request) {
        return process(request);
    }

    @SuppressWarnings("unchecked")
    public <T> T process(Object request) {
        String serviceName = FwServiceContext.get();
        log.info("[FwApiProcess] Processing service: '{}'", serviceName);

        if (serviceName == null || serviceName.isEmpty()) {
            log.error("[FwApiProcess] No service name found in FwServiceContext!");
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }

        Function<Object, Object> handler = registry.getHandleMethods().get(serviceName);
        if (handler == null) {
            Set<String> availableServices = registry.getHandleMethods().keySet();
            log.error(
                "[FwApiProcess] No handler found for service: '{}'. Available services in registry: {}",
                serviceName,
                availableServices
            );
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }

        try {
            log.debug("[FwApiProcess] Executing handler for service: '{}'", serviceName);
            return (T) handler.apply(request);
        } catch (Exception e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;
            log.error("[FwApiProcess] Error executing service: '{}'", serviceName, cause);
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        }
    }
}

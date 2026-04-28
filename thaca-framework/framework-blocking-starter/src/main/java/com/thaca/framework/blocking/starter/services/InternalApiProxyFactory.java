package com.thaca.framework.blocking.starter.services;

import com.thaca.framework.core.annotations.FwInternalApi;
import com.thaca.framework.core.annotations.FwInternalClient;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.services.FwModeRegistry;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalApiProxyFactory {

    private final InternalApiClient internalApiClient;
    private final FrameworkProperties frameworkProperties;
    private final FwModeRegistry fwModeRegistry;

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientInterface) {
        FwInternalClient annotation = clientInterface.getAnnotation(FwInternalClient.class);
        if (annotation == null) {
            throw new IllegalArgumentException(clientInterface.getName() + " is missing @FwInternalClient annotation");
        }

        String baseUrl = resolveBaseUrl(annotation.service());
        InternalApiProxy handler = new InternalApiProxy(internalApiClient, baseUrl);

        T proxy = (T) Proxy.newProxyInstance(
            clientInterface.getClassLoader(),
            new Class<?>[] { clientInterface },
            handler
        );

        registerHandlers(clientInterface, proxy);

        log.info("[InternalApiProxyFactory] Created proxy for {} → {}", clientInterface.getSimpleName(), baseUrl);

        return proxy;
    }

    private void registerHandlers(Class<?> clientInterface, Object proxy) {
        for (Method method : clientInterface.getMethods()) {
            FwInternalApi api = method.getAnnotation(FwInternalApi.class);
            if (api == null) continue;

            String name = api.name();

            if (fwModeRegistry.getHandleMethods().containsKey(name)) {
                log.warn(
                    "[InternalApiProxyFactory] Handler '{}' already registered, skipping {}.{}()",
                    name,
                    clientInterface.getSimpleName(),
                    method.getName()
                );
                continue;
            }

            Function<Object, Object> handlerFn = req -> {
                try {
                    if (method.getParameterCount() == 0) {
                        return method.invoke(proxy);
                    }
                    return method.invoke(proxy, req);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof RuntimeException re) throw re;
                    throw new RuntimeException(cause);
                }
            };

            fwModeRegistry.getHandleMethods().put(name, handlerFn);
            log.info(
                "[InternalApiProxyFactory] Registered: '{}' → {}.{}()",
                name,
                clientInterface.getSimpleName(),
                method.getName()
            );
        }
    }

    private String resolveBaseUrl(String service) {
        return switch (service.toLowerCase()) {
            case "auth" -> frameworkProperties.getRoutes().authServiceInternalRoute();
            default -> throw new IllegalArgumentException(
                "[InternalApiProxyFactory] Unknown service route: '" + service + "'"
            );
        };
    }
}

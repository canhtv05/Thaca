package com.thaca.framework.core.services;

import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Getter
@Slf4j
@Component
public class FwModeRegistry implements BeanPostProcessor {

    private final Map<String, Function<Object, Object>> handleMethods = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        for (Method method : targetClass.getMethods()) {
            FwMode fwMode = AnnotationUtils.findAnnotation(method, FwMode.class);
            if (fwMode != null && ModeType.HANDLE.equals(fwMode.type())) {
                String name = fwMode.name();
                if (handleMethods.containsKey(name)) {
                    throw new IllegalStateException(
                        "Duplicate HANDLE FwMode name detected: '" +
                            name +
                            "' in " +
                            targetClass.getName() +
                            "." +
                            method.getName()
                    );
                }
                Function<Object, Object> handler = req -> {
                    try {
                        method.setAccessible(true);
                        if (method.getParameterCount() == 0) {
                            return method.invoke(bean);
                        }
                        return method.invoke(bean, req);
                    } catch (InvocationTargetException ex) {
                        Throwable target = ex.getTargetException();
                        if (target instanceof RuntimeException runtimeException) {
                            throw runtimeException;
                        }
                        throw new RuntimeException(target);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                handleMethods.put(name, handler);
                log.info(
                    "[FwModeRegistry] Registered handler: '{}' -> {}.{}()",
                    name,
                    targetClass.getSimpleName(),
                    method.getName()
                );
            }
        }
        return bean;
    }
}

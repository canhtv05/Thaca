package com.thaca.framework.core.aspects;

import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.annotations.ServletOnly;
import com.thaca.framework.core.enums.ModeType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@ServletOnly
@Order(1)
public class FwModeAspect {

    private final Map<String, List<Method>> validatorCache = new ConcurrentHashMap<>();

    @Around("@annotation(fwMode)")
    public Object validateApi(ProceedingJoinPoint joinPoint, FwMode fwMode) throws Throwable {
        if (ModeType.HANDLE.equals(fwMode.type())) {
            runValidators(joinPoint, fwMode.name());
        }
        return joinPoint.proceed();
    }

    private void runValidators(ProceedingJoinPoint joinPoint, String modeName) throws Throwable {
        Object target = joinPoint.getTarget();
        Method handleMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?>[] paramTypes = handleMethod.getParameterTypes();
        String cacheKey = buildCacheKey(target.getClass(), modeName, paramTypes);

        List<Method> validators = validatorCache.computeIfAbsent(cacheKey, key ->
            findValidatorMethods(target.getClass(), modeName, paramTypes)
        );
        if (validators.isEmpty()) {
            log.debug("[ValidationAspect] No validator found for name: {}", modeName);
            return;
        }
        Object[] args = joinPoint.getArgs();
        for (Method validator : validators) {
            try {
                validator.invoke(target, args);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                log.error("[ValidationAspect] Error in validator: {}", modeName, cause);
                throw cause;
            }
        }
    }

    private List<Method> findValidatorMethods(Class<?> clazz, String modeName, Class<?>[] paramTypes) {
        return Arrays.stream(clazz.getMethods())
            .filter(method -> {
                FwMode mode = method.getAnnotation(FwMode.class);
                if (mode == null) return false;
                if (!mode.name().equals(modeName)) return false;
                if (!ModeType.VALIDATE.equals(mode.type())) return false;
                return isParameterCompatible(method.getParameterTypes(), paramTypes);
            })
            .toList();
    }

    private String buildCacheKey(Class<?> clazz, String modeName, Class<?>[] paramTypes) {
        String params = Arrays.stream(paramTypes)
            .map(Class::getName)
            .reduce((a, b) -> a + "," + b)
            .orElse("");
        return clazz.getName() + "_" + modeName + "_" + params;
    }

    private boolean isParameterCompatible(Class<?>[] methodParams, Class<?>[] actualParams) {
        if (methodParams.length != actualParams.length) return false;
        for (int i = 0; i < methodParams.length; i++) {
            if (!methodParams[i].isAssignableFrom(actualParams[i])) {
                return false;
            }
        }
        return true;
    }
}

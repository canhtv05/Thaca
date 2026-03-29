package com.thaca.framework.core.aspects;

import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class FwValidationAspect {

    private final Map<String, Method> validatorCache = new ConcurrentHashMap<>();

    @Around("@annotation(com.thaca.framework.core.annotations.FwMode)")
    public Object validateApi(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FwMode fwMode = method.getAnnotation(FwMode.class);

        if (Objects.nonNull(fwMode) && ModeType.HANDLE.equals(fwMode.type())) {
            runValidator(joinPoint, fwMode.name());
        }

        return joinPoint.proceed();
    }

    private void runValidator(ProceedingJoinPoint joinPoint, String modeName) throws Throwable {
        Object target = joinPoint.getTarget();
        Class<?>[] argTypes = Arrays.stream(joinPoint.getArgs()).map(Object::getClass).toArray(Class<?>[]::new);

        String cacheKey = modeName + "_" + Arrays.stream(argTypes).map(Class::getName).collect(Collectors.joining(","));

        Method validationMethod = validatorCache.computeIfAbsent(cacheKey, key -> {
            List<Method> matched = Arrays.stream(target.getClass().getMethods())
                .filter(m -> {
                    FwMode mode = m.getAnnotation(FwMode.class);
                    if (mode == null || !mode.name().equals(modeName) || !ModeType.VALIDATE.equals(mode.type())) {
                        return false;
                    }
                    return isParameterCompatible(m.getParameterTypes(), argTypes);
                })
                .toList();

            if (matched.size() > 1) {
                throw new IllegalStateException(
                    "[ValidationAspect] Duplicate validator found for name: " +
                        modeName +
                        ". Please check your @FwMode annotations."
                );
            }

            return matched.isEmpty() ? null : matched.getFirst();
        });

        if (Objects.isNull(validationMethod)) {
            log.debug("[ValidationAspect] No validator found for name: {}", modeName);
            return;
        }

        try {
            validationMethod.invoke(target, joinPoint.getArgs());
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("[ValidationAspect] Error running validator: {}", modeName, cause);
            throw cause;
        }
    }

    private boolean isParameterCompatible(Class<?>[] methodParams, Class<?>[] argTypes) {
        if (methodParams.length != argTypes.length) return false;
        for (int i = 0; i < methodParams.length; i++) {
            if (!methodParams[i].isAssignableFrom(argTypes[i])) return false;
        }
        return true;
    }
}

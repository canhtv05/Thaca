package com.thaca.framework.core.aspects;

import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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

        // tìm hết loại validate
        Method validationMethod = validatorCache.computeIfAbsent(modeName, key ->
            Arrays.stream(target.getClass().getMethods())
                .filter(m -> {
                    FwMode mode = m.getAnnotation(FwMode.class);
                    return Objects.nonNull(mode) && mode.name().equals(key) && ModeType.VALIDATE.equals(mode.type());
                })
                .findFirst()
                .orElse(null)
        );

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
}

package com.thaca.framework.core.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class ValidationAspect {

    @Pointcut("@annotation(com.thaca.framework.core.annotations.PublicApi)")
    public void isPublic() {}

    @Pointcut("@annotation(com.thaca.framework.core.annotations.ProtectedApi)")
    public void isProtected() {}

    @Pointcut("@annotation(com.thaca.framework.core.annotations.InternalApi)")
    public void isInternal() {}

    @Around("isPublic() || isProtected() || isInternal()")
    public Object validateApi(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        Method handlerMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        this.runValidator(target, handlerMethod, joinPoint.getArgs());
        return joinPoint.proceed();
    }

    private void runValidator(Object target, Method handlerMethod, Object[] args) throws Exception {
        String handlerName = handlerMethod.getName();
        String validatorName = "validate" + capitalize(handlerName);
        Class<?>[] paramTypes = handlerMethod.getParameterTypes();

        try {
            Method validator = target.getClass().getDeclaredMethod(validatorName, paramTypes);
            validator.setAccessible(true);
            validator.invoke(target, args);
        } catch (NoSuchMethodException ex) {
            log.error("[ValidationAspect]:: no such method: ", ex);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1);
    }
}
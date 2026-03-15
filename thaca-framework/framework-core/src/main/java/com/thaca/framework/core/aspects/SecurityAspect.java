package com.thaca.framework.core.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {

    @Around("@annotation(com.thaca.framework.core.annotations.ProtectedApi)")
    public Object protectedApi(ProceedingJoinPoint joinPoint) throws Throwable {
        // todo handle protected
        return joinPoint.proceed();
    }
}

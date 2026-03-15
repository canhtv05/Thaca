package com.thaca.framework.core.aspects;

import com.thaca.framework.core.annotations.Validation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

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

        this.runValidators(target, handlerMethod, joinPoint.getArgs());
        return joinPoint.proceed();
    }

    private void runValidators(Object target, Method handlerMethod, Object[] args) throws Exception {
        Method[] methods = target.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Validation.class)) {
                Validation validator = method.getAnnotation(Validation.class);
                if (validator.methodName().equals(handlerMethod.getName())) {
                    method.setAccessible(true);
                    method.invoke(target, args);
                }
            }
        }
    }
}

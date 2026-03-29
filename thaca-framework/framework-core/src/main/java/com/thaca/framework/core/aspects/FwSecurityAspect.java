package com.thaca.framework.core.aspects;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import java.lang.reflect.Method;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FwSecurityAspect {

    @Around("@annotation(com.thaca.framework.core.annotations.FwRequestMode)")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FwRequestMode requestMode = method.getAnnotation(FwRequestMode.class);
        if (Objects.isNull(requestMode)) {
            requestMode = joinPoint.getTarget().getClass().getAnnotation(FwRequestMode.class);
        }

        if (Objects.nonNull(requestMode) && RequestType.PUBLIC.equals(requestMode.type())) {
            return joinPoint.proceed();
        }

        if (Objects.nonNull(requestMode) && RequestType.PROTECTED.equals(requestMode.type())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                throw new FwException(CommonErrorMessage.UNAUTHORIZED);
            }
            return joinPoint.proceed();
        }

        if (Objects.nonNull(requestMode) && RequestType.INTERNAL.equals(requestMode.type())) {
            // todo handle internal security logic
            return joinPoint.proceed();
        }

        return joinPoint.proceed();
    }
}

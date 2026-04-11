package com.thaca.framework.core.aspects;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.CheckPermission;
import com.thaca.framework.core.exceptions.FwException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(com.thaca.framework.core.annotations.CheckPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CheckPermission annotation = method.getAnnotation(CheckPermission.class);

        if (Objects.isNull(annotation)) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(CheckPermission.class);
        }

        if (Objects.nonNull(annotation)) {
            String[] requiredPermissions = annotation.value();
            if (requiredPermissions.length > 0) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth == null || !auth.isAuthenticated()) {
                    throw new FwException(CommonErrorMessage.UNAUTHORIZED);
                }

                Set<String> userAuthorities = auth
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

                boolean isAllowed;
                if (annotation.allMatched()) {
                    isAllowed = Arrays.stream(requiredPermissions).allMatch(userAuthorities::contains);
                } else {
                    isAllowed = Arrays.stream(requiredPermissions).anyMatch(userAuthorities::contains);
                }

                if (!isAllowed) {
                    throw new FwException(CommonErrorMessage.FORBIDDEN);
                }
            }
        }
        return joinPoint.proceed();
    }
}

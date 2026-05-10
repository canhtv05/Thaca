package com.thaca.framework.core.aspects;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.CheckPermission;
import com.thaca.framework.core.annotations.ServletOnly;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.PermissionProvider;
import com.thaca.framework.core.security.SecurityUtils;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ServletOnly
@ConditionalOnBean(PermissionProvider.class)
@RequiredArgsConstructor
public class CheckPermissionAspect {

    private final PermissionProvider permissionProvider;

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
                if (SecurityUtils.isSuperAdmin()) {
                    return joinPoint.proceed();
                }
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    throw new FwException(CommonErrorMessage.UNAUTHORIZED);
                }
                Set<String> authorities = auth
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
                Set<String> deniedPermissions = authorities
                    .stream()
                    .filter(authority -> authority.startsWith("DENY_"))
                    .map(authority -> authority.substring(5))
                    .collect(Collectors.toSet());
                Set<String> userPermissions = new HashSet<>();
                for (String authority : authorities) {
                    if (authority.startsWith("DENY_")) {
                        continue;
                    }
                    userPermissions.addAll(permissionProvider.getPermissions(authority));
                    if (authority.startsWith("ROLE_")) {
                        userPermissions.addAll(permissionProvider.getPermissions(authority.substring(5)));
                    }
                }
                userPermissions.removeAll(deniedPermissions);
                boolean isAllowed;
                if (annotation.allMatched()) {
                    isAllowed = Arrays.stream(requiredPermissions).allMatch(userPermissions::contains);
                } else {
                    isAllowed = Arrays.stream(requiredPermissions).anyMatch(userPermissions::contains);
                }
                if (!isAllowed) {
                    throw new FwException(CommonErrorMessage.FORBIDDEN);
                }
            }
        }
        return joinPoint.proceed();
    }
}

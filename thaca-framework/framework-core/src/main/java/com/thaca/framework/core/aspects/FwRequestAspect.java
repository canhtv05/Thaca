package com.thaca.framework.core.aspects;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.annotations.ServletOnly;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.context.FwServiceContext;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@ServletOnly
@Order
@Slf4j
public class FwRequestAspect {

    private final FrameworkProperties frameworkProperties;

    @Around("@annotation(com.thaca.framework.core.annotations.FwRequest)")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FwRequest requestMode = method.getAnnotation(FwRequest.class);
        if (Objects.isNull(requestMode)) {
            requestMode = joinPoint.getTarget().getClass().getAnnotation(FwRequest.class);
        }
        if (Objects.nonNull(requestMode)) {
            FwServiceContext.set(requestMode.name());
        }
        try {
            boolean isSuperAdmin = SecurityUtils.isSuperAdmin();

            if (isSuperAdmin) {
                return joinPoint.proceed();
            }

            if (Objects.nonNull(requestMode)) {
                if (RequestType.PUBLIC.equals(requestMode.type())) {
                    if (requestMode.isSuperAdmin()) {
                        throw new FwException(CommonErrorMessage.FORBIDDEN);
                    }
                    return joinPoint.proceed();
                }

                if (RequestType.PROTECTED.equals(requestMode.type())) {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                        throw new FwException(CommonErrorMessage.UNAUTHORIZED);
                    }
                    if (requestMode.isSuperAdmin()) {
                        throw new FwException(CommonErrorMessage.FORBIDDEN);
                    }
                    return joinPoint.proceed();
                }

                if (requestMode.isSuperAdmin()) {
                    throw new FwException(CommonErrorMessage.FORBIDDEN);
                }

                if (RequestType.INTERNAL.equals(requestMode.type())) {
                    String expectedApiKey = frameworkProperties.getHttpClient().getApiKey();

                    ApiHeader contextHeader = FwContextHeader.get();
                    if (contextHeader != null && StringUtils.hasText(contextHeader.getApiKey())) {
                        if (Objects.equals(contextHeader.getApiKey(), expectedApiKey)) {
                            return joinPoint.proceed();
                        }
                    }
                    ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Basic ")) {
                            String apiKey = authHeader.substring(6);
                            if (Objects.equals(apiKey, expectedApiKey)) {
                                return joinPoint.proceed();
                            }
                        }
                    }
                    throw new FwException(CommonErrorMessage.UNAUTHORIZED);
                }
            }
            return joinPoint.proceed();
        } catch (Throwable t) {
            log.error("[FwRequestAspect] error in {}: {}", method.getName(), t.getMessage(), t);
            throw new FwException(CommonErrorMessage.INTERNAL_SERVER_ERROR);
        } finally {
            FwServiceContext.clear();
        }
    }
}

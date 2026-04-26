package com.thaca.framework.core.aspects;

import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.annotations.FwSecurity;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class FwSecurityAspect {

    @Before("@annotation(fwSecurity)")
    public void checkSecurityMethod(JoinPoint joinPoint, FwSecurity fwSecurity) {
        verify(joinPoint, fwSecurity);
    }

    @Before("@within(fwSecurity)")
    public void checkSecurityClass(JoinPoint joinPoint, FwSecurity fwSecurity) {
        verify(joinPoint, fwSecurity);
    }

    private void verify(JoinPoint joinPoint, FwSecurity fwSecurity) {
        if (fwSecurity.isSuperAdmin()) {
            boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
            if (!isSuperAdmin) {
                log.warn("Access denied: User is not SuperAdmin. Method: {}", joinPoint.getSignature().toShortString());
                throw new FwException(CommonErrorMessage.FORBIDDEN);
            }
        }
    }
}

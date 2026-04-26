package com.thaca.framework.blocking.starter.aspects;

import com.thaca.framework.core.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.thaca..services..*(..)) || execution(* com.thaca..repositories..*(..))")
    public void enableTenantFilter() {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        } else {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("tenantFilter");
        }
    }
}

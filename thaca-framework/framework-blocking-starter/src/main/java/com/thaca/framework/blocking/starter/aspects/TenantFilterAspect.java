package com.thaca.framework.blocking.starter.aspects;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Lazy
public class TenantFilterAspect {

    private final ObjectProvider<EntityManager> entityManagerProvider;

    public TenantFilterAspect(ObjectProvider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Before(
        "(execution(* com.thaca..services..*(..)) || execution(* com.thaca..repositories..*(..))) " +
            "&& !within(com.thaca.framework..*)"
    )
    public void enableTenantFilter() {
        Long tenantId = com.thaca.framework.core.security.SecurityUtils.getCurrentTenantId();
        EntityManager entityManager = entityManagerProvider.getIfAvailable();
        if (entityManager == null) {
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        try {
            if (session.getSessionFactory().getDefinedFilterNames().contains("tenantFilter")) {
                if (tenantId != null) {
                    session.enableFilter("tenantFilter").setParameterList("tenantIds", java.util.List.of(tenantId));
                } else {
                    session.disableFilter("tenantFilter");
                }
            }
        } catch (Exception ignored) {}
    }
}

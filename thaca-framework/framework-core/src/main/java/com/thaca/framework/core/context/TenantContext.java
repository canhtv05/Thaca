package com.thaca.framework.core.context;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static void set(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long get() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}

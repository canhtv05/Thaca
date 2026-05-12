package com.thaca.framework.core.context;

import java.util.List;

public class TenantContext {

    private static final ThreadLocal<List<Long>> currentTenantIds = new ThreadLocal<>();

    public static void set(List<Long> tenantIds) {
        currentTenantIds.set(tenantIds);
    }

    public static List<Long> get() {
        return currentTenantIds.get();
    }

    public static void clear() {
        currentTenantIds.remove();
    }
}

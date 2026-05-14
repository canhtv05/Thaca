package com.thaca.framework.core.security;

import java.util.List;

public interface UserPrincipal {
    String getUsername();

    String getRole();

    String getChannel();

    boolean isSuperAdmin();

    boolean isAdminUser();

    List<Long> getTenantIds();

    Long getTenantId();
}

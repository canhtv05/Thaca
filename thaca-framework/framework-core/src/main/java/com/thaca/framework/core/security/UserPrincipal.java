package com.thaca.framework.core.security;

public interface UserPrincipal {
    String getUsername();

    String getRole();

    String getChannel();

    boolean isSuperAdmin();

    boolean isCmsUser();
}

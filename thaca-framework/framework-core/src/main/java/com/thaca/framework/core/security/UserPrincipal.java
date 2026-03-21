package com.thaca.framework.core.security;

public interface UserPrincipal {
    String getUsername();

    boolean isGlobal();

    String getRole();

    String getChannel();
}

package com.thaca.framework.core.security;

public interface UserPrincipal {

    String getUsername();

    boolean isAdmin();

    String getRole();

    String getChannel();
}

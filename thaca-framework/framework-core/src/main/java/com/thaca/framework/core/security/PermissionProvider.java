package com.thaca.framework.core.security;

import java.util.Set;

@FunctionalInterface
public interface PermissionProvider {
    Set<String> getPermissions(String roleCode);
}

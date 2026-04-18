package com.thaca.framework.core.security;

import java.util.Set;

public interface PermissionProvider {
    Set<String> getPermissions(String roleCode);
}

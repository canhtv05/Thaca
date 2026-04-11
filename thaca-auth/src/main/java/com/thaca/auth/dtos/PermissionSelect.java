package com.thaca.auth.dtos;

import com.thaca.auth.domains.Permission;

public record PermissionSelect(String code, String description) {
    public static PermissionSelect fromEntity(Permission permission) {
        return new PermissionSelect(permission.getCode(), permission.getDescription());
    }
}

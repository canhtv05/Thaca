package com.thaca.auth.mappers;

import com.thaca.auth.domains.Permission;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.enums.PermissionEffect;

public class PermissionMapper {

    private PermissionMapper() {}

    public static PermissionDTO fromEntity(
        Permission permission,
        String roleCode,
        String roleDescription,
        PermissionEffect effect
    ) {
        return PermissionDTO.builder()
            .code(permission.getCode())
            .description(permission.getDescription())
            .roleCode(roleCode)
            .roleDescription(roleDescription)
            .effect(effect)
            .build();
    }

    public static PermissionDTO fromEntity(Permission permission, String roleCode, String roleDescription) {
        return fromEntity(permission, roleCode, roleDescription, PermissionEffect.GRANT);
    }

    public static PermissionDTO fromEntity(Permission permission) {
        return fromEntity(permission, null, null, PermissionEffect.GRANT);
    }
}

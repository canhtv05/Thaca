package com.thaca.auth.mappers;

import com.thaca.auth.domains.Permission;
import com.thaca.common.dtos.internal.PermissionDTO;

public class PermissionMapper {

    public static PermissionDTO fromEntity(Permission permission, String roleDescription) {
        return PermissionDTO.builder()
            .code(permission.getCode())
            .description(permission.getDescription())
            .roleDescription(roleDescription)
            .build();
    }
}

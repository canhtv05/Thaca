package com.thaca.auth.mappers;

import com.thaca.auth.domains.Permission;
import com.thaca.common.dtos.internal.PermissionDTO;

public class PermissionMapper {

    public static PermissionDTO fromEntity(Permission entity) {
        if (entity == null) return null;
        return PermissionDTO.builder().code(entity.getCode()).description(entity.getDescription()).build();
    }
}

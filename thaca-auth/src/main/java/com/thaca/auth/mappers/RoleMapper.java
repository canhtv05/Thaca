package com.thaca.auth.mappers;

import com.thaca.auth.domains.Role;
import com.thaca.common.dtos.internal.RoleDTO;

public class RoleMapper {

    private RoleMapper() {}

    public static RoleDTO fromEntity(Role entity) {
        if (entity == null) return null;
        return RoleDTO.builder().code(entity.getCode()).description(entity.getDescription()).build();
    }
}

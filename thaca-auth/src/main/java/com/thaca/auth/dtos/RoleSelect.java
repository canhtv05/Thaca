package com.thaca.auth.dtos;

import com.thaca.auth.domains.Role;

public record RoleSelect(String code, String name, String description) {
    public static RoleSelect fromEntity(Role entity) {
        return new RoleSelect(entity.getCode(), entity.getName(), entity.getDescription());
    }
}

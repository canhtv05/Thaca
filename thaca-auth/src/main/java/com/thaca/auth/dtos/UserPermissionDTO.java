package com.thaca.auth.dtos;

import com.thaca.auth.domains.UserPermission;
import com.thaca.auth.enums.PermissionAction;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String permissionCode;
    private PermissionAction action;

    public UserPermission fromEntity(Long userId) {
        return UserPermission.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .permissionCode(this.permissionCode)
            .action(this.action)
            .build();
    }
}

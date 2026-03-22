package com.thaca.auth.dtos;

import com.thaca.auth.domains.UserPermission;
import com.thaca.auth.enums.PermissionAction;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDTO {

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

package com.thaca.auth.mappers;

import com.thaca.auth.domains.User;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.framework.core.utils.DateUtils;
import java.util.ArrayList;

public class UserMapper {

    private UserMapper() {}

    public static UserDTO fromEntity(User user) {
        return fromEntityWithadmin(user, false);
    }

    public static UserDTO fromEntityWithadmin(User user, boolean isAdmin) {
        if (user == null) return null;
        UserDTO dto = UserDTO.builder()
            .id(user.getId())
            .tenantIds(user.getTenantIds() != null ? new ArrayList<>(user.getTenantIds()) : null)
            .username(user.getUsername())
            .email(user.getEmail())
            .isActivated(user.getIsActivated())
            .isLocked(user.getIsLocked())
            .build();

        if (isAdmin) {
            dto.setCreatedAt(DateUtils.dateToString(user.getCreatedAt()));
            dto.setUpdatedAt(DateUtils.dateToString(user.getUpdatedAt()));
            dto.setCreatedBy(user.getCreatedBy());
            dto.setUpdatedBy(user.getUpdatedBy());
        }
        return dto;
    }
}

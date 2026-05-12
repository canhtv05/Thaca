package com.thaca.auth.mappers;

import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.User;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.framework.core.utils.DateUtils;

public class UserMapper {

    private UserMapper() {}

    public static UserDTO fromEntity(User user) {
        return fromEntityWithCms(user, false);
    }

    public static UserDTO fromEntityWithCms(User user, boolean isCms) {
        if (user == null) return null;
        UserDTO dto = UserDTO.builder()
            .id(user.getId())
            .tenantIds(user.getTenants() != null ? user.getTenants().stream().map(Tenant::getId).toList() : null)
            .username(user.getUsername())
            .email(user.getEmail())
            .isActivated(user.getIsActivated())
            .isLocked(user.getIsLocked())
            .build();

        if (isCms) {
            dto.setCreatedAt(DateUtils.dateToString(user.getCreatedAt()));
            dto.setUpdatedAt(DateUtils.dateToString(user.getUpdatedAt()));
            dto.setCreatedBy(user.getCreatedBy());
            dto.setUpdatedBy(user.getUpdatedBy());
        }
        return dto;
    }
}

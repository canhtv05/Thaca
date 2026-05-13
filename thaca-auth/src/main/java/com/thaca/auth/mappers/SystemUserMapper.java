package com.thaca.auth.mappers;

import com.thaca.auth.domains.*;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.enums.PermissionEffect;
import com.thaca.framework.core.utils.DateUtils;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class SystemUserMapper {

    private SystemUserMapper() {}

    public static SystemUserDTO toSearchDTO(SystemCredential sc, SystemUser su) {
        return SystemUserDTO.builder()
            .id(su.getId())
            .tenantIds(new ArrayList<>(su.getTenantIds()))
            .username(sc.getUsername())
            .email(su.getEmail())
            .fullname(su.getFullname())
            .isActivated(su.getIsActivated())
            .isLocked(su.getIsLocked())
            .isSuperAdmin(su.getIsSuperAdmin())
            .avatarUrl(su.getAvatarUrl())
            .createdAt(DateUtils.dateToString(sc.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(sc.getUpdatedAt()))
            .createdBy(sc.getCreatedBy())
            .updatedBy(sc.getUpdatedBy())
            .build();
    }

    public static SystemUserDTO toFullDTO(SystemCredential sc, SystemUser su) {
        Map<String, PermissionEffect> overrides = sc
            .getCredentialPermissions()
            .stream()
            .collect(
                Collectors.toMap(
                    scp -> scp.getId().getRoleCode() + ":" + scp.getPermission().getCode(),
                    SystemCredentialPermission::getEffect,
                    (e1, e2) -> e1
                )
            );

        return SystemUserDTO.builder()
            .id(su.getId())
            .tenantIds(new ArrayList<>(su.getTenantIds()))
            .username(sc.getUsername())
            .email(su.getEmail())
            .fullname(su.getFullname())
            .isActivated(su.getIsActivated())
            .isLocked(su.getIsLocked())
            .isSuperAdmin(su.getIsSuperAdmin())
            .avatarUrl(su.getAvatarUrl())
            .roles(
                sc
                    .getRoles()
                    .stream()
                    .collect(
                        Collectors.toMap(Role::getCode, r ->
                            r
                                .getPermissions()
                                .stream()
                                .collect(
                                    Collectors.toMap(Permission::getCode, p ->
                                        overrides.getOrDefault(r.getCode() + ":" + p.getCode(), PermissionEffect.GRANT)
                                    )
                                )
                        )
                    )
            )
            .createdAt(DateUtils.dateToString(sc.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(sc.getUpdatedAt()))
            .createdBy(sc.getCreatedBy())
            .updatedBy(sc.getUpdatedBy())
            .build();
    }
}

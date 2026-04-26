package com.thaca.auth.services;

import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.SystemUser;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemUserService {

    private final SystemCredentialRepository systemCredentialRepository;

    @Transactional(readOnly = true)
    public AuthUserDTO getSystemProfile() {
        String username = SecurityUtils.getCurrentUsername();
        return systemCredentialRepository
            .findByUsername(username)
            .map(sc -> {
                SystemUser su = sc.getSystemUser();
                return AuthUserDTO.builder()
                    .id(su.getId())
                    .tenantId(sc.getTenantId())
                    .username(sc.getUsername())
                    .email(su.getEmail())
                    .fullname(su.getFullname())
                    .isActivated(su.getIsActivated())
                    .isLocked(su.getIsLocked())
                    .isSuperAdmin(su.getIsSuperAdmin())
                    .avatarUrl(su.getAvatarUrl())
                    .roles(sc.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()))
                    .build();
            })
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }
}

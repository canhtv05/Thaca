package com.thaca.auth.services;

import com.thaca.auth.dtos.PermissionSelect;
import com.thaca.auth.dtos.RoleSelect;
import com.thaca.auth.repositories.PermissionRepository;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.common.enums.CacheKey;
import com.thaca.framework.blocking.starter.configs.cache.InMemoryCacheService;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.security.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicApiService {

    private final InMemoryCacheService<String, List<PermissionSelect>> permissionCache;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public List<PermissionSelect> getPermissionSelect() {
        if (permissionCache.containsKey(CacheKey.LIST_PERMISSION.name())) {
            return permissionCache.get(CacheKey.LIST_PERMISSION.name()).get();
        }
        List<PermissionSelect> permissions = permissionRepository
            .findAll()
            .stream()
            .map(PermissionSelect::fromEntity)
            .toList();
        if (!permissions.isEmpty()) {
            permissionCache.put(CacheKey.LIST_PERMISSION.name(), permissions);
        }
        return permissions;
    }

    public List<RoleSelect> getRoleSelect() {
        List<RoleSelect> roles = roleRepository.findAll().stream().map(RoleSelect::fromEntity).toList();
        if (!SecurityUtils.isGlobalAdmin()) {
            return roles
                .stream()
                .filter(t -> !AuthoritiesConstants.ADMIN.equalsIgnoreCase(t.code()))
                .toList();
        }
        return roles;
    }
}

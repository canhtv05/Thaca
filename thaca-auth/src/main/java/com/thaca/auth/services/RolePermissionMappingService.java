package com.thaca.auth.services;

import com.thaca.auth.domains.Permission;
import com.thaca.auth.domains.Role;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.core.constants.CommonConstants;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionMappingService {

    private final RoleRepository roleRepository;
    private final RedisCacheService redisService;

    @Transactional(readOnly = true)
    public void syncAllToRedis() {
        List<Role> roles = roleRepository.findAll();
        for (Role role : roles) {
            syncRoleToRedis(role);
        }
    }

    public void syncRoleToRedis(Role role) {
        List<String> permissions = role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toList());

        String key = CommonConstants.REDIS_ROLE_PERM_PREFIX + role.getCode();
        redisService.set(key, permissions);
    }
}

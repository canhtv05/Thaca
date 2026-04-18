package com.thaca.framework.blocking.starter.services;

import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.security.PermissionProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPermissionProvider implements PermissionProvider {

    private final RedisCacheService redisService;

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getPermissions(String roleCode) {
        try {
            List<String> permissions = redisService.get(CommonConstants.REDIS_ROLE_PERM_PREFIX + roleCode, List.class);
            return permissions != null ? new HashSet<>(permissions) : new HashSet<>();
        } catch (Exception e) {
            log.error("[RedisPermissionProvider] Failed to fetch permissions from Redis for role: {}", roleCode, e);
            return new HashSet<>();
        }
    }
}

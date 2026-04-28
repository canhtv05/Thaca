package com.thaca.auth.services;

import com.thaca.auth.domains.Permission;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.SystemCredential;
import com.thaca.auth.domains.SystemCredentialPermission;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.PermissionMapper;
import com.thaca.auth.mappers.RoleMapper;
import com.thaca.auth.repositories.PermissionRepository;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.PermissionEffect;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.core.exceptions.FwException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SystemCredentialRepository systemCredentialRepository;
    private final RedisCacheService redisService;
    public static final String REDIS_ROLE_PERM_PREFIX = "auth:role-permissions:";

    @Transactional(readOnly = true)
    public SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> request) {
        Specification<Role> spec = createRoleSpecification(request);
        Page<Role> roles = roleRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt"));
        return new SearchResponse<>(
            roles.getContent().stream().map(RoleMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(roles)
        );
    }

    @Transactional(readOnly = true)
    public SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> request) {
        String roleCode = request.getFilter() != null ? request.getFilter().getRoleCode() : null;
        String roleDescription;
        if (StringUtils.isNotBlank(roleCode)) {
            roleDescription = roleRepository.findByCode(roleCode).map(Role::getDescription).orElse(null);
        } else {
            roleDescription = null;
        }
        Specification<Permission> spec = createPermissionSpecification(request);
        Page<Permission> permissions = permissionRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "updatedAt")
        );
        List<PermissionDTO> content = permissions
            .getContent()
            .stream()
            .map(p -> PermissionMapper.fromEntity(p, roleDescription))
            .toList();
        return new SearchResponse<>(content, PaginationResponse.of(permissions));
    }

    private Specification<Role> createRoleSpecification(SearchRequest<RoleDTO> req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                RoleDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getCode())) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Permission> createPermissionSpecification(SearchRequest<PermissionDTO> req) {
        return (root, query, cb) -> {
            Join<Permission, Role> roleJoin;
            if (Permission.class.equals(query.getResultType())) {
                root.fetch("roles", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                PermissionDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getCode())) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
                }
                if (StringUtils.isNotBlank(filter.getRoleCode())) {
                    roleJoin = root.join("roles", JoinType.INNER);
                    predicates.add(cb.equal(roleJoin.get("code"), filter.getRoleCode()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public void syncAllToRedis() {
        List<Role> roles = roleRepository.findAll();
        for (Role role : roles) {
            syncRoleToRedis(role);
        }
    }

    private void syncRoleToRedis(Role role) {
        List<String> permissions = role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toList());
        String key = REDIS_ROLE_PERM_PREFIX + role.getCode();
        redisService.put(key, permissions);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserPermissions(String username, List<String> deniedPermissionCodes) {
        SystemCredential sc = systemCredentialRepository
            .findByUsername(username)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));

        sc.getCredentialPermissions().clear();

        if (deniedPermissionCodes != null && !deniedPermissionCodes.isEmpty()) {
            for (String code : deniedPermissionCodes) {
                Permission perm = permissionRepository.findById(code).orElse(null);
                if (perm != null) {
                    SystemCredentialPermission scp = new SystemCredentialPermission();
                    scp.setId(new SystemCredentialPermission.SystemCredentialPermissionId(username, code));
                    scp.setCredential(sc);
                    scp.setPermission(perm);
                    scp.setEffect(PermissionEffect.DENY);
                    sc.getCredentialPermissions().add(scp);
                }
            }
        }
        systemCredentialRepository.save(sc);
    }
}

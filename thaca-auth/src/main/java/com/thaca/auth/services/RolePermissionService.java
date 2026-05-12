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
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.internal.req.RoleCodesReq;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.PermissionEffect;
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.*;
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
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_ROLES, type = ModeType.HANDLE)
    public SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> request) {
        Specification<Role> spec = createRoleSpecification(request);
        Page<Role> roles = roleRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt"));
        return new SearchResponse<>(
            roles.getContent().stream().map(RoleMapper::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(roles)
        );
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_ALL_ROLES, type = ModeType.HANDLE)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream().map(RoleMapper::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_PERMISSIONS, type = ModeType.HANDLE)
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
            .map(p -> PermissionMapper.fromEntity(p, roleCode, roleDescription))
            .toList();
        return new SearchResponse<>(content, PaginationResponse.of(permissions));
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_ALL_PERMISSIONS, type = ModeType.HANDLE)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository
            .findAll()
            .stream()
            .map(p -> PermissionMapper.fromEntity(p, null, null))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PERMISSIONS_BY_ROLES, type = ModeType.HANDLE)
    public List<PermissionDTO> getPermissionsByRoles(RoleCodesReq request) {
        if (request == null || request.getRoleCodes() == null || request.getRoleCodes().isEmpty()) {
            return new ArrayList<>();
        }
        return roleRepository
            .findAllById(request.getRoleCodes())
            .stream()
            .flatMap(r ->
                r
                    .getPermissions()
                    .stream()
                    .map(p -> PermissionMapper.fromEntity(p, r.getCode(), r.getDescription()))
            )
            .toList();
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
            Set<String> deniedSet = new HashSet<>(deniedPermissionCodes);
            for (Role role : sc.getRoles()) {
                for (Permission perm : role.getPermissions()) {
                    if (deniedSet.contains(perm.getCode())) {
                        SystemCredentialPermission scp = new SystemCredentialPermission();
                        scp.setId(
                            new SystemCredentialPermission.SystemCredentialPermissionId(
                                sc.getId(),
                                role.getCode(),
                                perm.getCode()
                            )
                        );
                        scp.setCredential(sc);
                        scp.setRole(role);
                        scp.setPermission(perm);
                        scp.setEffect(PermissionEffect.DENY);
                        sc.getCredentialPermissions().add(scp);
                    }
                }
            }
        }
        systemCredentialRepository.save(sc);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Export Excel
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_ROLES, type = ModeType.HANDLE)
    public byte[] exportRoles(SearchRequest<RoleDTO> request) throws IOException {
        boolean isVietnamese = "vi".equalsIgnoreCase(FwContextHeader.get().getLanguage());
        Specification<Role> spec = createRoleSpecification(request);

        Sort sort =
            request.getPage() != null && StringUtils.isNotBlank(request.getPage().getSortField())
                ? request.getPage().toPageable(Sort.Direction.DESC, "updatedAt").getSort()
                : Sort.by(Sort.Direction.DESC, "updatedAt");

        List<Role> roles = roleRepository.findAll(spec, sort);
        List<RoleDTO> data = roles.stream().map(RoleMapper::fromEntity).toList();

        List<Map<String, Object>> rows = new ArrayList<>();
        for (RoleDTO role : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", role.getCode());
            row.put("description", role.getDescription() != null ? role.getDescription() : "");
            rows.add(row);
        }
        return ExcelEngine.exportData(buildRoleSchema(isVietnamese), rows);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_PERMISSIONS, type = ModeType.HANDLE)
    public byte[] exportPermissions(SearchRequest<PermissionDTO> request) throws IOException {
        boolean isVietnamese = "vi".equalsIgnoreCase(FwContextHeader.get().getLanguage());

        Specification<Permission> spec = createPermissionSpecification(request);
        Sort sort =
            request.getPage() != null && StringUtils.isNotBlank(request.getPage().getSortField())
                ? request.getPage().toPageable(Sort.Direction.DESC, "updatedAt").getSort()
                : Sort.by(Sort.Direction.DESC, "updatedAt");

        List<Permission> permissions = permissionRepository.findAll(spec, sort);
        List<PermissionDTO> data = permissions
            .stream()
            .map(p -> PermissionMapper.fromEntity(p, null, null))
            .toList();

        List<Map<String, Object>> rows = new ArrayList<>();
        for (PermissionDTO perm : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", perm.getCode());
            row.put("description", perm.getDescription() != null ? perm.getDescription() : "");
            rows.add(row);
        }
        return ExcelEngine.exportData(buildPermissionSchema(isVietnamese), rows);
    }

    private ExcelSchema buildRoleSchema(boolean isVietnamese) {
        return ExcelSchema.builder()
            .sheetName(isVietnamese ? "Danh sách vai trò" : "Roles")
            .addColumn(
                ExcelColumn.builder("code", isVietnamese ? "Mã vai trò" : "Role Code")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("description", isVietnamese ? "Mô tả" : "Description")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .build();
    }

    private ExcelSchema buildPermissionSchema(boolean isVietnamese) {
        return ExcelSchema.builder()
            .sheetName(isVietnamese ? "Danh sách quyền" : "Permissions")
            .addColumn(
                ExcelColumn.builder("code", isVietnamese ? "Mã quyền" : "Permission Code")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("description", isVietnamese ? "Mô tả" : "Description")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .build();
    }
}

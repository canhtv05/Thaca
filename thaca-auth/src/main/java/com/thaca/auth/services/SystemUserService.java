package com.thaca.auth.services;

import com.thaca.auth.clients.AdminClient;
import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.*;
import com.thaca.auth.dtos.SystemUserDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.SystemUserMapper;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.auth.repositories.SystemUserRepository;
import com.thaca.auth.repositories.UserLockHistoryRepository;
import com.thaca.auth.repositories.projection.DuplicateCheckPrj;
import com.thaca.auth.utils.TenantEnrichmentHelper;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.AccountStatus;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.PermissionEffect;
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.validations.Validator;
import com.thaca.framework.core.validations.rules.*;
import jakarta.persistence.criteria.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class SystemUserService {

    private final SystemCredentialRepository systemCredentialRepository;
    private final SystemUserRepository systemUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLockHistoryRepository userLockHistoryRepository;
    private final AdminClient adminClient;
    private final TenantEnrichmentHelper tenantHelper;

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.ADMIN_GET_PROFILE, type = ModeType.HANDLE)
    public SystemUserDTO getSystemProfile() {
        String username = SecurityUtils.getCurrentUsername();
        return systemCredentialRepository
            .findByUsername(username)
            .map(sc -> {
                SystemUserDTO dto = SystemUserMapper.toFullDTO(sc, sc.getSystemUser());
                return tenantHelper.enrichTenantFull(dto);
            })
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.ADMIN_SEARCH_SYSTEM_USERS, type = ModeType.HANDLE)
    public SearchResponse<SystemUserDTO> searchSystemUsers(SearchRequest<SystemUserDTO> request) {
        Specification<SystemCredential> spec = createSpecification(request);

        if (
            request.getPage() != null &&
            org.apache.commons.lang3.StringUtils.isNotBlank(request.getPage().getSortField())
        ) {
            String sf = request.getPage().getSortField();
            if (Set.of("email", "fullname", "isActivated", "isLocked").contains(sf)) {
                request.getPage().setSortField("systemUser." + sf);
            }
        }

        assert request.getPage() != null;
        Page<SystemCredential> result = systemCredentialRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "createdAt")
        );
        List<SystemUserDTO> data = result
            .getContent()
            .stream()
            .map(sc -> SystemUserMapper.toFullDTO(sc, sc.getSystemUser()))
            .toList();

        Map<Long, TenantInfoPrj> tenantMap = tenantHelper.fetchTenantMap(tenantHelper.collectTenantIds(data));
        if (!tenantMap.isEmpty()) {
            data.forEach(d -> tenantHelper.enrichTenantInfo(d, tenantMap));
        }

        return new SearchResponse<>(data, PaginationResponse.of(result));
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.ADMIN_GET_SYSTEM_USER, type = ModeType.HANDLE)
    public SystemUserDTO getSystemUserById(SystemUserDTO request) {
        if (request.getId() == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        SystemUser su = systemUserRepository
            .findById(request.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        SystemCredential sc = systemCredentialRepository
            .findBySystemUser(su)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        SystemUserDTO dto = SystemUserMapper.toFullDTO(sc, su);
        return tenantHelper.enrichTenantFull(dto);
    }

    @FwMode(name = ServiceMethod.ADMIN_CREATE_SYSTEM_USER, type = ModeType.VALIDATE)
    public void validateCreate(SystemUserDTO request) {
        validateRequest(request, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_CREATE_SYSTEM_USER, type = ModeType.HANDLE)
    public SystemUserDTO create(SystemUserDTO request) {
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        SystemUser su = SystemUser.builder()
            .tenantIds(request.getTenantIds() != null ? new HashSet<>(request.getTenantIds()) : new HashSet<>())
            .fullname(request.getFullname())
            .email(request.getEmail())
            .isActivated(!isSuperAdmin || Boolean.TRUE.equals(request.getIsActivated()))
            .isLocked(isSuperAdmin && Boolean.TRUE.equals(request.getIsLocked()))
            .isSuperAdmin(isSuperAdmin && Boolean.TRUE.equals(request.getIsSuperAdmin()))
            .avatarUrl(request.getAvatarUrl())
            .build();
        su = systemUserRepository.save(su);
        SystemCredential sc = SystemCredential.builder()
            .username(request.getUsername())
            .systemUser(su)
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            sc.setRoles(new HashSet<>(roleRepository.findAllById(request.getRoles().keySet())));
            grantPermission(request, sc);
        }
        systemCredentialRepository.save(sc);

        if (Boolean.TRUE.equals(su.getIsLocked())) {
            userLockHistoryRepository.save(
                UserLockHistory.builder()
                    .targetUserId(su.getId())
                    .action(AccountStatus.LOCK)
                    .reason(StringUtils.defaultIfBlank(request.getLockReason(), "Initial lock"))
                    .build()
            );
        }
        SystemUserDTO dto = SystemUserMapper.toFullDTO(sc, su);
        return tenantHelper.enrichTenantFull(dto);
    }

    @FwMode(name = ServiceMethod.ADMIN_UPDATE_SYSTEM_USER, type = ModeType.VALIDATE)
    public void validateUpdate(SystemUserDTO request) {
        validateRequest(request, false);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_UPDATE_SYSTEM_USER, type = ModeType.HANDLE)
    public SystemUserDTO update(SystemUserDTO request) {
        SystemUser su = systemUserRepository
            .findById(request.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        SystemCredential sc = systemCredentialRepository
            .findBySystemUser(su)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        su.setFullname(request.getFullname());
        su.setEmail(request.getEmail());
        su.setAvatarUrl(request.getAvatarUrl());
        if (isSuperAdmin && request.getTenantIds() != null) {
            su.setTenantIds(new HashSet<>(request.getTenantIds()));
        }
        if (isSuperAdmin) {
            if (request.getIsActivated() != null) su.setIsActivated(request.getIsActivated());
            if (request.getIsLocked() != null) su.setIsLocked(request.getIsLocked());
            if (request.getIsSuperAdmin() != null) su.setIsSuperAdmin(request.getIsSuperAdmin());
        }
        if (StringUtils.isNotBlank(request.getPassword())) {
            sc.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (isSuperAdmin && request.getRoles() != null) {
            sc.setRoles(new HashSet<>(roleRepository.findAllById(request.getRoles().keySet())));
            sc.getCredentialPermissions().clear();
            grantPermission(request, sc);
        }

        boolean oldLocked = Boolean.TRUE.equals(su.getIsLocked());
        boolean newLocked = Boolean.TRUE.equals(request.getIsLocked());

        systemUserRepository.save(su);
        systemCredentialRepository.save(sc);

        if (isSuperAdmin && !oldLocked && newLocked) {
            userLockHistoryRepository.save(
                UserLockHistory.builder()
                    .targetUserId(su.getId())
                    .action(AccountStatus.LOCK)
                    .reason(request.getLockReason())
                    .build()
            );
        } else if (isSuperAdmin && oldLocked && !newLocked) {
            userLockHistoryRepository.save(
                UserLockHistory.builder()
                    .targetUserId(su.getId())
                    .action(AccountStatus.UNLOCK)
                    .reason(request.getLockReason())
                    .build()
            );
        }
        SystemUserDTO dto = SystemUserMapper.toFullDTO(sc, su);
        return tenantHelper.enrichTenantFull(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_LOCK_UNLOCK_SYSTEM_USER, type = ModeType.HANDLE)
    public void lockUnlock(SystemUserDTO request) {
        if (request.getId() == null || StringUtils.isBlank(request.getLockReason())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        SystemUser su = systemUserRepository
            .findById(request.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        boolean newStatus = !Boolean.TRUE.equals(su.getIsLocked());
        su.setIsLocked(newStatus);
        systemUserRepository.save(su);
        userLockHistoryRepository.save(
            UserLockHistory.builder()
                .targetUserId(request.getId())
                .action(newStatus ? AccountStatus.LOCK : AccountStatus.UNLOCK)
                .reason(request.getLockReason())
                .build()
        );
    }

    private Specification<SystemCredential> createSpecification(SearchRequest<SystemUserDTO> request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<SystemCredential, SystemUser> userJoin = root.join("systemUser");
            boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
            Long currentTenantId = SecurityUtils.getCurrentTenantId();

            if (request.getFilter() != null) {
                SystemUserDTO f = request.getFilter();
                if (isSuperAdmin) {
                    if (!CollectionUtils.isEmpty(f.getTenantIds())) {
                        predicates.add(userJoin.join("tenantIds").in(f.getTenantIds()));
                    }
                } else {
                    if (currentTenantId != null) {
                        predicates.add(cb.equal(userJoin.join("tenantIds"), currentTenantId));
                    } else {
                        predicates.add(cb.disjunction());
                    }
                }
                if (StringUtils.isNotBlank(f.getEmail())) predicates.add(
                    cb.like(cb.lower(userJoin.get("email")), "%" + f.getEmail().toLowerCase() + "%")
                );
                if (StringUtils.isNotBlank(f.getUsername())) predicates.add(
                    cb.like(cb.lower(root.get("username")), "%" + f.getUsername().toLowerCase() + "%")
                );
                if (f.getIsActivated() != null) predicates.add(
                    cb.equal(userJoin.get("isActivated"), f.getIsActivated())
                );
                if (f.getIsLocked() != null) predicates.add(cb.equal(userJoin.get("isLocked"), f.getIsLocked()));
                if (f.getRoles() != null && !f.getRoles().isEmpty()) {
                    Join<SystemCredential, Role> roleJoin = root.join("roles");
                    predicates.add(roleJoin.get("code").in(f.getRoles().keySet()));
                }
            } else if (!isSuperAdmin) {
                if (currentTenantId != null) {
                    predicates.add(cb.equal(userJoin.join("tenantIds"), currentTenantId));
                } else {
                    predicates.add(cb.disjunction());
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @FwMode(name = ServiceMethod.ADMIN_EXPORT_SYSTEM_USER, type = ModeType.HANDLE)
    public byte[] exportSystemUsers(SearchRequest<SystemUserDTO> request) throws IOException {
        boolean isVietnamese = "vi".equalsIgnoreCase(FwContextHeader.get().getLanguage());
        Specification<SystemCredential> spec = createSpecification(request);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (request.getPage() != null) {
            if (StringUtils.isNotBlank(request.getPage().getSortField())) {
                String sf = request.getPage().getSortField();
                if (Set.of("email", "fullname", "isActivated", "isLocked").contains(sf)) {
                    request.getPage().setSortField("systemUser." + sf);
                }
            }
            sort = request.getPage().toPageable(Sort.Direction.DESC, "createdAt").getSort();
        }

        List<SystemCredential> result = systemCredentialRepository.findAll(spec, sort);
        List<SystemUserDTO> data = result
            .stream()
            .map(sc -> SystemUserMapper.toSearchDTO(sc, sc.getSystemUser()))
            .toList();

        Map<Long, TenantInfoPrj> tenantMap = tenantHelper.fetchTenantMap(tenantHelper.collectTenantIds(data));
        if (!tenantMap.isEmpty()) {
            data.forEach(d -> tenantHelper.enrichTenantFull(d, tenantMap));
        }
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SystemUserDTO user : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", user.getUsername());
            row.put("fullname", user.getFullname());
            row.put("email", user.getEmail());
            String tenantNames =
                user.getTenants() != null
                    ? user
                          .getTenants()
                          .stream()
                          .map(t -> t.getCode() + " - " + t.getName())
                          .collect(Collectors.joining(", "))
                    : "";
            row.put("tenantName", tenantNames);
            row.put(
                "isActivated",
                Boolean.TRUE.equals(user.getIsActivated())
                    ? (isVietnamese ? "Hoạt động" : "Active")
                    : (isVietnamese ? "Chưa kích hoạt" : "Inactive")
            );
            row.put(
                "isLocked",
                Boolean.TRUE.equals(user.getIsLocked())
                    ? (isVietnamese ? "Đã khóa" : "Locked")
                    : (isVietnamese ? "Bình thường" : "Normal")
            );
            if (isSuperAdmin) {
                row.put(
                    "isSuperAdmin",
                    Boolean.TRUE.equals(user.getIsSuperAdmin())
                        ? (isVietnamese ? "Có" : "Yes")
                        : (isVietnamese ? "Không" : "No")
                );
            }
            row.put("createdAt", user.getCreatedAt());
            row.put("createdBy", user.getCreatedBy());
            row.put("updatedAt", user.getUpdatedAt());
            row.put("updatedBy", user.getUpdatedBy());
            rows.add(row);
        }
        return ExcelEngine.exportData(buildSchema(isVietnamese, isSuperAdmin), rows);
    }

    private ExcelSchema buildSchema(boolean isVietnamese, boolean isSuperAdmin) {
        return ExcelSchema.builder()
            .sheetName(isVietnamese ? "Danh sách quản trị viên" : "System Users")
            .addColumn(
                ExcelColumn.builder("username", isVietnamese ? "Tên đăng nhập" : "Username")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("fullname", isVietnamese ? "Họ và tên" : "Fullname")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(ExcelColumn.builder("email", "Email").required().dataType(ExcelDataType.STRING).build())
            .addColumn(
                ExcelColumn.builder("tenantName", isVietnamese ? "Tổ chức" : "Tenant")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("isActivated", isVietnamese ? "Đã kích hoạt" : "Activated")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("isLocked", isVietnamese ? "Bị khóa" : "Locked")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumnCondition(
                isSuperAdmin,
                6,
                ExcelColumn.builder("isSuperAdmin", isVietnamese ? "Quản trị tối cao" : "Super Admin")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("createdAt", isVietnamese ? "Ngày tạo" : "Created At")
                    .dataType(ExcelDataType.DATE)
                    .comment(isVietnamese ? "Ngày tạo" : "Created At")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("createdBy", isVietnamese ? "Người tạo" : "Created By")
                    .dataType(ExcelDataType.STRING)
                    .comment(isVietnamese ? "Người tạo" : "Created By")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("updatedAt", isVietnamese ? "Ngày cập nhật" : "Updated At")
                    .dataType(ExcelDataType.DATE)
                    .comment(isVietnamese ? "Ngày cập nhật" : "Updated At")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("updatedBy", isVietnamese ? "Người cập nhật" : "Updated By")
                    .dataType(ExcelDataType.STRING)
                    .comment(isVietnamese ? "Người cập nhật" : "Updated By")
                    .build()
            )
            .build();
    }

    @SuppressWarnings("null")
    private void validateRequest(SystemUserDTO request, boolean isCreate) {
        if (
            request != null &&
            (request.getTenantIds() == null || request.getTenantIds().isEmpty()) &&
            request.getTenantId() != null
        ) {
            request.setTenantIds(Collections.singletonList(request.getTenantId()));
        }

        boolean invalid =
            request == null ||
            StringUtils.isBlank(request.getUsername()) ||
            StringUtils.isBlank(request.getEmail()) ||
            StringUtils.isBlank(request.getFullname()) ||
            request.getTenantIds() == null ||
            request.getTenantIds().isEmpty() ||
            CollectionUtils.isEmpty(request.getRoles()) ||
            (isCreate && StringUtils.isBlank(request.getPassword()));

        if (invalid) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (isCreate) {
            throwIfConflicts(
                systemCredentialRepository.findConflictingTenantsByUsername(
                    request.getUsername(),
                    request.getTenantIds()
                ),
                ErrorMessage.USERNAME_ALREADY_EXISTS
            );
            throwIfConflicts(
                systemUserRepository.findConflictingTenantsByEmail(request.getEmail(), request.getTenantIds()),
                ErrorMessage.EMAIL_ALREADY_EXITS
            );
        } else {
            throwIfConflicts(
                systemUserRepository.findConflictingTenantsByEmailAndIdNot(
                    request.getEmail(),
                    request.getTenantIds(),
                    request.getId()
                ),
                ErrorMessage.EMAIL_ALREADY_EXITS
            );
        }
        Validator<UserDTO> validator = new Validator<>(
            new ArrayList<>(List.of(new UsernameRule<>(), new EmailRule<>(), new FullnameRule<>()))
        );
        if (isCreate || StringUtils.isNotBlank(request.getPassword())) {
            validator.add(new PasswordRule<>());
        }
        validator.validate(
            UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .fullname(request.getFullname())
                .build()
        );
    }

    private void grantPermission(SystemUserDTO request, SystemCredential sc) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            return;
        }
        for (Role role : sc.getRoles()) {
            Map<String, PermissionEffect> rolePerms = request
                .getRoles()
                .getOrDefault(role.getCode(), Collections.emptyMap());
            Set<String> grantedSet = rolePerms
                .entrySet()
                .stream()
                .filter(e -> PermissionEffect.GRANT.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

            for (Permission p : role.getPermissions()) {
                SystemCredentialPermission scp = new SystemCredentialPermission();
                scp.setId(
                    new SystemCredentialPermission.SystemCredentialPermissionId(sc.getId(), role.getCode(), p.getCode())
                );
                scp.setCredential(sc);
                scp.setRole(role);
                scp.setPermission(p);
                scp.setEffect(grantedSet.contains(p.getCode()) ? PermissionEffect.GRANT : PermissionEffect.DENY);
                sc.getCredentialPermissions().add(scp);
            }
        }
    }

    private record ConflictTenantResult(String label, List<Map<String, Object>> details) {}

    private void throwIfConflicts(List<DuplicateCheckPrj> conflicts, ErrorMessage errorMessage) {
        if (conflicts.isEmpty()) return;
        List<Long> conflictIds = conflicts.stream().map(DuplicateCheckPrj::getTenantId).distinct().toList();
        ConflictTenantResult conflict = buildConflictingTenantInfo(conflictIds);
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("conflictingTenants", conflict.label());
        errorData.put("conflictingTenantsData", conflict.details());
        throw new FwException(errorMessage, errorData);
    }

    private ConflictTenantResult buildConflictingTenantInfo(List<Long> tenantIds) {
        if (tenantIds.isEmpty()) return new ConflictTenantResult("", Collections.emptyList());
        try {
            List<TenantInfoPrj> tenants = adminClient.getTenantsByIds(
                TenantDTO.builder().tenantIds(new ArrayList<>(tenantIds)).build()
            );
            List<Map<String, Object>> details = tenants
                .stream()
                .map(t -> {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("id", t.getId());
                    info.put("code", t.getCode());
                    info.put("name", t.getName());
                    return info;
                })
                .toList();
            String label = tenants
                .stream()
                .map(t -> t.getCode() + " - " + t.getName())
                .collect(Collectors.joining(", "));
            return new ConflictTenantResult(label, details);
        } catch (Exception e) {
            String label = tenantIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
            List<Map<String, Object>> details = tenantIds
                .stream()
                .map(id -> {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("id", id);
                    return info;
                })
                .toList();
            return new ConflictTenantResult(label, details);
        }
    }
}

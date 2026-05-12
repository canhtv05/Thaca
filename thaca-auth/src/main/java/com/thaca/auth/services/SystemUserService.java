package com.thaca.auth.services;

import com.thaca.auth.domains.*;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.SystemUserMapper;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.auth.repositories.SystemUserRepository;
import com.thaca.auth.repositories.TenantRepository;
import com.thaca.auth.repositories.UserLockHistoryRepository;
import com.thaca.auth.validators.core.Validator;
import com.thaca.auth.validators.rules.*;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
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
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PROFILE, type = ModeType.HANDLE)
    public SystemUserDTO getSystemProfile() {
        String username = SecurityUtils.getCurrentUsername();
        return systemCredentialRepository
            .findByUsername(username)
            .map(sc -> {
                List<Tenant> userTenants = new ArrayList<>(sc.getSystemUser().getTenants());
                return SystemUserMapper.toFullDTO(sc, sc.getSystemUser(), userTenants);
            })
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_SYSTEM_USERS, type = ModeType.HANDLE)
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
            .map(sc -> {
                List<Tenant> userTenants = new ArrayList<>(sc.getSystemUser().getTenants());
                return SystemUserMapper.toSearchDTO(sc, sc.getSystemUser(), userTenants);
            })
            .toList();
        return new SearchResponse<>(data, PaginationResponse.of(result));
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_SYSTEM_USER, type = ModeType.HANDLE)
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
        List<Tenant> userTenants = new ArrayList<>(su.getTenants());
        return SystemUserMapper.toFullDTO(sc, su, userTenants);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_SYSTEM_USER, type = ModeType.VALIDATE)
    public void validateCreate(SystemUserDTO request) {
        validateRequest(request, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_SYSTEM_USER, type = ModeType.HANDLE)
    public SystemUserDTO create(SystemUserDTO request) {
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        SystemUser su = SystemUser.builder()
            .tenants(
                request.getTenantIds() != null
                    ? new HashSet<>(tenantRepository.findAllById(request.getTenantIds()))
                    : new HashSet<>()
            )
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

        List<Tenant> userTenants = new ArrayList<>(su.getTenants());
        return SystemUserMapper.toFullDTO(sc, su, userTenants);
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_UPDATE_SYSTEM_USER, type = ModeType.VALIDATE)
    public void validateUpdate(SystemUserDTO request) {
        validateRequest(request, false);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_UPDATE_SYSTEM_USER, type = ModeType.HANDLE)
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
            su.setTenants(new HashSet<>(tenantRepository.findAllById(request.getTenantIds())));
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

        List<Tenant> userTenants = new ArrayList<>(su.getTenants());
        return SystemUserMapper.toFullDTO(sc, su, userTenants);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_SYSTEM_USER, type = ModeType.HANDLE)
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
            List<Long> currentTenantIds = SecurityUtils.getCurrentTenantIds();
            if (request.getFilter() != null) {
                SystemUserDTO f = request.getFilter();
                if (!CollectionUtils.isEmpty(f.getTenantIds()) && isSuperAdmin) {
                    Join<SystemUser, Tenant> tenantJoin = userJoin.join("tenants");
                    predicates.add(tenantJoin.get("id").in(f.getTenantIds()));
                } else if (!isSuperAdmin) {
                    Join<SystemUser, Tenant> tenantJoin = userJoin.join("tenants");
                    predicates.add(tenantJoin.get("id").in(currentTenantIds));
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
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_SYSTEM_USER, type = ModeType.HANDLE)
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
            .map(sc -> {
                List<Tenant> userTenants = new ArrayList<>(sc.getSystemUser().getTenants());
                return SystemUserMapper.toFullDTO(sc, sc.getSystemUser(), userTenants);
            })
            .toList();

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
                          .map(com.thaca.common.dtos.internal.TenantDTO::getName)
                          .collect(Collectors.joining(", "))
                    : "";
            row.put("tenantName", tenantNames);
            row.put("roles", user.getRoles() != null ? String.join(", ", user.getRoles().keySet()) : "");
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
            rows.add(row);
        }
        return ExcelEngine.exportData(buildSchema(isVietnamese), rows);
    }

    private ExcelSchema buildSchema(boolean isVietnamese) {
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
                ExcelColumn.builder("roles", isVietnamese ? "Vai trò" : "Roles").dataType(ExcelDataType.STRING).build()
            )
            .addColumn(
                ExcelColumn.builder("isActivated", isVietnamese ? "Trạng thái kích hoạt" : "Activation Status")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("isLocked", isVietnamese ? "Trạng thái khóa" : "Lock Status")
                    .dataType(ExcelDataType.STRING)
                    .build()
            )
            .build();
    }

    @SuppressWarnings("null")
    private void validateRequest(SystemUserDTO request, boolean isCreate) {
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
            if (
                systemCredentialRepository.existsByUsernameAndTenantIds(request.getUsername(), request.getTenantIds())
            ) {
                throw new FwException(ErrorMessage.USERNAME_ALREADY_EXISTS);
            }
            if (systemUserRepository.existsByEmailAndTenantIds(request.getEmail(), request.getTenantIds())) {
                throw new FwException(ErrorMessage.EMAIL_ALREADY_EXITS);
            }
        } else {
            if (
                systemUserRepository.existsByEmailAndTenantIdsAndIdNot(
                    request.getEmail(),
                    request.getTenantIds(),
                    request.getId()
                )
            ) {
                throw new FwException(ErrorMessage.EMAIL_ALREADY_EXITS);
            }
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
}

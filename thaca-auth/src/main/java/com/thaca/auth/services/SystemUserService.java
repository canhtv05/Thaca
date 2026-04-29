package com.thaca.auth.services;

import com.thaca.auth.domains.*;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.auth.repositories.SystemUserRepository;
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
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
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

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PROFILE, type = ModeType.HANDLE)
    public SystemUserDTO getSystemProfile() {
        String username = SecurityUtils.getCurrentUsername();
        return systemCredentialRepository
            .findByUsername(username)
            .map(sc -> getSystemUserDTO(sc, sc.getSystemUser(), sc.getTenantId()))
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_SYSTEM_USERS, type = ModeType.HANDLE)
    public SearchResponse<SystemUserDTO> searchSystemUsers(SearchRequest<SystemUserDTO> request) {
        Specification<SystemCredential> spec = createSpecification(request);
        Page<SystemCredential> result = systemCredentialRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "createdAt")
        );
        List<SystemUserDTO> data = result
            .getContent()
            .stream()
            .map(sc -> getSystemUserDTO(sc, sc.getSystemUser(), sc.getTenantId()))
            .collect(Collectors.toList());
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
        return getSystemUserDTO(sc, su, sc.getTenantId());
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_SYSTEM_USER, type = ModeType.VALIDATE)
    public void validateCreate(SystemUserDTO request) {
        validateRequest(request, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_CREATE_SYSTEM_USER, type = ModeType.HANDLE)
    public SystemUserDTO create(SystemUserDTO request) {
        SystemUser su = SystemUser.builder()
            .tenantId(request.getTenantId())
            .fullname(request.getFullname())
            .email(request.getEmail())
            .isActivated(true)
            .isLocked(false)
            .isSuperAdmin(false)
            .avatarUrl(request.getAvatarUrl())
            .build();
        su = systemUserRepository.save(su);
        SystemCredential sc = SystemCredential.builder()
            .username(request.getUsername())
            .systemUser(su)
            .tenantId(request.getTenantId())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        sc.setRoles(new HashSet<>(roleRepository.findAllById(request.getRoleCodes())));
        systemCredentialRepository.save(sc);
        return getSystemUserDTO(sc, su, su.getTenantId());
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
        if (isSuperAdmin && request.getTenantId() != null) {
            su.setTenantId(request.getTenantId());
            sc.setTenantId(request.getTenantId());
        }
        if (isSuperAdmin) {
            if (request.getIsActivated() != null) su.setIsActivated(request.getIsActivated());
            if (request.getIsLocked() != null) su.setIsLocked(request.getIsLocked());
        }
        if (StringUtils.isNotBlank(request.getPassword())) {
            sc.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (isSuperAdmin && !CollectionUtils.isEmpty(request.getRoleCodes())) {
            sc.setRoles(new HashSet<>(roleRepository.findAllById(request.getRoleCodes())));
        }
        systemUserRepository.save(su);
        systemCredentialRepository.save(sc);
        return getSystemUserDTO(sc, su, sc.getTenantId());
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
            if (request.getFilter() != null) {
                SystemUserDTO f = request.getFilter();
                if (!CollectionUtils.isEmpty(f.getTenantIds())) predicates.add(
                    root.get("tenantId").in(f.getTenantIds())
                );
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
                if (!CollectionUtils.isEmpty(f.getRoleCodes())) {
                    Join<SystemCredential, Role> roleJoin = root.join("roles");
                    predicates.add(roleJoin.get("code").in(f.getRoleCodes()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SuppressWarnings("null")
    private void validateRequest(SystemUserDTO request, boolean isCreate) {
        boolean invalid =
            request == null ||
            StringUtils.isBlank(request.getUsername()) ||
            StringUtils.isBlank(request.getEmail()) ||
            StringUtils.isBlank(request.getFullname()) ||
            request.getTenantId() == null ||
            CollectionUtils.isEmpty(request.getRoleCodes()) ||
            (isCreate && StringUtils.isBlank(request.getPassword()));

        if (invalid) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (isCreate) {
            if (
                systemCredentialRepository.existsById(request.getUsername()) ||
                systemUserRepository.existsByEmail(request.getEmail())
            ) {
                throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
            }
        }
        Validator<UserDTO> validator = new Validator<>(
            List.of(new PasswordRule<>(), new UsernameRule<>(), new EmailRule<>(), new FullnameRule<>())
        );
        validator.validate(
            UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .fullname(request.getFullname())
                .build()
        );
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_SYSTEM_USER, type = ModeType.HANDLE)
    public byte[] exportSystemUsers(SearchRequest<SystemUserDTO> request) throws IOException {
        Specification<SystemCredential> spec = createSpecification(request);
        List<SystemCredential> result = systemCredentialRepository.findAll(
            spec,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        List<SystemUserDTO> data = result
            .stream()
            .map(sc -> getSystemUserDTO(sc, sc.getSystemUser(), sc.getTenantId()))
            .collect(Collectors.toList());

        List<Map<String, Object>> rows = new ArrayList<>();
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());

        for (SystemUserDTO user : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", user.getUsername());
            row.put("fullname", user.getFullname());
            row.put("email", user.getEmail());
            row.put("tenantId", user.getTenantId());
            row.put("roles", user.getRoles() != null ? String.join(", ", user.getRoles()) : "");
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
            .addColumn(ExcelColumn.builder("tenantId", "Tenant ID").dataType(ExcelDataType.NUMBER).build())
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

    private SystemUserDTO getSystemUserDTO(SystemCredential sc, SystemUser su, Long tenantId) {
        return SystemUserDTO.builder()
            .id(su.getId())
            .tenantId(tenantId)
            .username(sc.getUsername())
            .email(su.getEmail())
            .fullname(su.getFullname())
            .isActivated(su.getIsActivated())
            .isLocked(su.getIsLocked())
            .isSuperAdmin(su.getIsSuperAdmin())
            .avatarUrl(su.getAvatarUrl())
            .roles(sc.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()))
            .build();
    }
}

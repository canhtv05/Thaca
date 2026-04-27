package com.thaca.auth.internal.services;

import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.services.PlanService;
import com.thaca.auth.services.RolePermissionService;
import com.thaca.auth.services.SystemUserService;
import com.thaca.auth.services.TenantService;
import com.thaca.auth.services.UserService;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.PlanDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.VerifyEmailTokenDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalService {

    private final UserService userService;
    private final RolePermissionService rolePermissionService;
    private final TenantService tenantService;
    private final SystemUserService systemUserService;
    private final PlanService planService;

    @FwMode(name = InternalMethod.INTERNAL_CMS_ACTIVE_USER, type = ModeType.VALIDATE)
    public void validateActiveUserByUserName(VerifyEmailTokenDTO request) {
        if (request.email().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_ACTIVE_USER, type = ModeType.HANDLE)
    public VerifyEmailTokenDTO activeUserByUserName(VerifyEmailTokenDTO request) {
        return userService.activeUserByUserName(request);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_PROFILE, type = ModeType.HANDLE)
    public AuthUserDTO getSystemProfile() {
        return systemUserService.getSystemProfile();
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_USERS, type = ModeType.HANDLE)
    public SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> request) {
        return userService.searchUsers(request);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_ROLES, type = ModeType.HANDLE)
    public SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> request) {
        return rolePermissionService.searchRoles(request);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_PERMISSIONS, type = ModeType.HANDLE)
    public SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> request) {
        return rolePermissionService.searchPermissions(request);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_USER_BY_ID, type = ModeType.HANDLE)
    public UserDTO findById(Long id) {
        return userService.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_USER, type = ModeType.HANDLE)
    public void lockUser(Long id) {
        userService.lockUser(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = InternalMethod.INTERNAL_CMS_UNLOCK_USER, type = ModeType.HANDLE)
    public void unlockUser(Long id) {
        userService.unlockUser(id);
    }

    @Transactional(readOnly = true)
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_TENANTS, type = ModeType.HANDLE)
    public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> request) {
        return tenantService.searchTenants(request);
    }
}

package com.thaca.cms.clients;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.*;
import com.thaca.common.dtos.internal.projection.PlanInfoPrj;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.req.RoleCodesReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwInternalApi;
import java.util.List;

public interface AuthClient {
    // ==========================================
    // AUTHENTICATION & PROFILE
    // ==========================================
    @FwInternalApi(path = "/cms/sign-in", name = ServiceMethod.CMS_AUTHENTICATE)
    AuthenticateRes signIn(LoginReq loginReq);

    @FwInternalApi(path = "/cms/profile", name = ServiceMethod.CMS_GET_PROFILE)
    SystemUserDTO getProfile();

    // ==========================================
    // END USER MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/users/search", name = ServiceMethod.CMS_SEARCH_USERS)
    SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> search);

    @FwInternalApi(path = "/cms/users/lock", name = ServiceMethod.CMS_LOCK_USER)
    Void lockUser(Long id);

    @FwInternalApi(path = "/cms/users/unlock", name = ServiceMethod.CMS_UNLOCK_USER)
    Void unlockUser(Long id);

    // ==========================================
    // SYSTEM USER MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/system-users/search", name = ServiceMethod.CMS_SEARCH_SYSTEM_USERS)
    SearchResponse<SystemUserDTO> searchSystemUsers(SearchRequest<SystemUserDTO> search);

    @FwInternalApi(path = "/cms/system-users/search-lock-histories", name = ServiceMethod.CMS_SEARCH_USER_LOCK_HISTORY)
    SearchResponse<UserLockHistoryDTO> searchUserLockHistories(SearchRequest<UserLockHistoryDTO> search);

    @FwInternalApi(path = "/cms/system-users/get", name = ServiceMethod.CMS_GET_SYSTEM_USER)
    SystemUserDTO getSystemUser(SystemUserDTO user);

    @FwInternalApi(path = "/cms/system-users/create", name = ServiceMethod.CMS_CREATE_SYSTEM_USER)
    SystemUserDTO createSystemUser(SystemUserDTO user);

    @FwInternalApi(path = "/cms/system-users/update", name = ServiceMethod.CMS_UPDATE_SYSTEM_USER)
    SystemUserDTO updateSystemUser(SystemUserDTO user);

    @FwInternalApi(path = "/cms/system-users/lock-unlock", name = ServiceMethod.CMS_LOCK_UNLOCK_SYSTEM_USER)
    Void lockUnlockSystemUser(SystemUserDTO user);

    @FwInternalApi(path = "/cms/system-users/export", name = ServiceMethod.CMS_EXPORT_SYSTEM_USER)
    byte[] exportSystemUsers(SearchRequest<SystemUserDTO> request);

    // ==========================================
    // ROLE MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/roles/search", name = ServiceMethod.CMS_SEARCH_ROLES)
    SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> search);

    @FwInternalApi(path = "/cms/roles/all", name = ServiceMethod.CMS_GET_ALL_ROLES)
    List<RoleDTO> getAllRoles();

    // ==========================================
    // PERMISSION MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/permissions/search", name = ServiceMethod.CMS_SEARCH_PERMISSIONS)
    SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> search);

    @FwInternalApi(path = "/cms/permissions/all", name = ServiceMethod.CMS_GET_ALL_PERMISSIONS)
    List<PermissionDTO> getAllPermissions();

    @FwInternalApi(path = "/cms/permissions/by-roles", name = ServiceMethod.CMS_GET_PERMISSIONS_BY_ROLES)
    List<PermissionDTO> getPermissionsByRoles(RoleCodesReq request);

    // ==========================================
    // TENANT MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/tenants/search", name = ServiceMethod.CMS_SEARCH_TENANTS)
    SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> search);

    @FwInternalApi(path = "/cms/tenants/get", name = ServiceMethod.CMS_GET_TENANT)
    TenantDTO getTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/all", name = ServiceMethod.CMS_GET_ALL_TENANTS)
    List<TenantInfoPrj> getAllTenants();

    @FwInternalApi(path = "/cms/tenants/create", name = ServiceMethod.CMS_CREATE_TENANT)
    TenantDTO createTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/update", name = ServiceMethod.CMS_UPDATE_TENANT)
    TenantDTO updateTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/lock-unlock", name = ServiceMethod.CMS_LOCK_UNLOCK_TENANT)
    Void lockUnlockTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/export", name = ServiceMethod.CMS_EXPORT_TENANT)
    byte[] exportTenants(SearchRequest<TenantDTO> request);

    // ==========================================
    // PLAN MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/plans/search", name = ServiceMethod.CMS_SEARCH_PLANS)
    SearchResponse<PlanDTO> searchPlans(SearchRequest<PlanDTO> search);

    @FwInternalApi(path = "/cms/plans/get", name = ServiceMethod.CMS_GET_PLAN)
    PlanDTO getPlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/all", name = ServiceMethod.CMS_GET_ALL_PLANS)
    List<PlanInfoPrj> getAllPlans();

    @FwInternalApi(path = "/cms/plans/create", name = ServiceMethod.CMS_CREATE_PLAN)
    PlanDTO createPlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/update", name = ServiceMethod.CMS_UPDATE_PLAN)
    PlanDTO updatePlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/lock-unlock", name = ServiceMethod.CMS_LOCK_UNLOCK_PLAN)
    Void lockUnlockPlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/export", name = ServiceMethod.CMS_EXPORT_PLAN)
    byte[] exportPlan(SearchRequest<PlanDTO> request);
}

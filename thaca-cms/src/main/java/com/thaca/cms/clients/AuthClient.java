package com.thaca.cms.clients;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.*;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwInternalApi;
import com.thaca.framework.core.annotations.FwInternalClient;
import java.util.List;

@FwInternalClient(service = "auth")
public interface AuthClient {
    @FwInternalApi(path = "/cms/sign-in", name = ServiceMethod.CMS_AUTHENTICATE)
    AuthenticateRes signIn(LoginReq loginReq);

    @FwInternalApi(path = "/cms/profile", name = ServiceMethod.CMS_GET_PROFILE)
    AuthUserDTO getProfile();

    @FwInternalApi(path = "/cms/users/search", name = ServiceMethod.CMS_SEARCH_USERS)
    SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> search);

    @FwInternalApi(path = "/cms/users/lock", name = ServiceMethod.CMS_LOCK_USER)
    Void lockUser(Long id);

    @FwInternalApi(path = "/cms/users/unlock", name = ServiceMethod.CMS_UNLOCK_USER)
    Void unlockUser(Long id);

    @FwInternalApi(path = "/cms/roles/search", name = ServiceMethod.CMS_SEARCH_ROLES)
    SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> search);

    @FwInternalApi(path = "/cms/permissions/search", name = ServiceMethod.CMS_SEARCH_PERMISSIONS)
    SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> search);

    @FwInternalApi(path = "/cms/tenants/search", name = ServiceMethod.CMS_SEARCH_TENANTS)
    SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> search);

    @FwInternalApi(path = "/cms/tenants/get", name = ServiceMethod.CMS_GET_TENANT)
    TenantDTO getTenant(Long id);

    @FwInternalApi(path = "/cms/tenants/create", name = ServiceMethod.CMS_CREATE_TENANT)
    TenantDTO createTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/update", name = ServiceMethod.CMS_UPDATE_TENANT)
    TenantDTO updateTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/lock-unlock", name = ServiceMethod.CMS_LOCK_UNLOCK_TENANT)
    Void lockUnlockTenant(TenantDTO tenant);

    @FwInternalApi(path = "/cms/tenants/export", name = ServiceMethod.CMS_EXPORT_TENANT)
    byte[] exportTenants(SearchRequest<TenantDTO> request);

    @FwInternalApi(path = "/cms/plans/search", name = ServiceMethod.CMS_SEARCH_PLANS)
    SearchResponse<PlanDTO> searchPlans(SearchRequest<PlanDTO> search);

    @FwInternalApi(path = "/cms/plans/get", name = ServiceMethod.CMS_GET_PLAN)
    PlanDTO getPlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/all", name = ServiceMethod.CMS_GET_ALL_PLANS)
    List<PlanDTO> getAllPlans();

    @FwInternalApi(path = "/cms/plans/create", name = ServiceMethod.CMS_CREATE_PLAN)
    PlanDTO createPlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/update", name = ServiceMethod.CMS_UPDATE_PLAN)
    PlanDTO updatePlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/lock-unlock", name = ServiceMethod.CMS_LOCK_UNLOCK_PLAN)
    Void lockUnlockPlan(PlanDTO plan);

    @FwInternalApi(path = "/cms/plans/export", name = ServiceMethod.CMS_EXPORT_PLAN)
    byte[] exportPlan(SearchRequest<PlanDTO> request);
}

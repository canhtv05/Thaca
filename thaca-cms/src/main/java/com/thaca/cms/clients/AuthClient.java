package com.thaca.cms.clients;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.*;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.blocking.starter.services.InternalApiClient;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.enums.ModeType;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthClient {

    private final InternalApiClient internalApiClient;
    private final FrameworkProperties frameworkProperties;
    private static String authBaseUrl = null;

    @PostConstruct
    public void init() {
        authBaseUrl = frameworkProperties.getRoutes().authServiceInternalRoute();
    }

    @FwMode(name = ServiceMethod.CMS_AUTHENTICATE, type = ModeType.HANDLE)
    public AuthenticateRes signIn(LoginReq loginReq) {
        return internalApiClient.post(authBaseUrl + "/cms/sign-in", loginReq, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_GET_PROFILE, type = ModeType.HANDLE)
    public AuthUserDTO getProfile() {
        return internalApiClient.post(authBaseUrl + "/cms/profile", null, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_SEARCH_USERS, type = ModeType.HANDLE)
    public SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> search) {
        return internalApiClient.post(authBaseUrl + "/cms/users/search", search, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_SEARCH_ROLES, type = ModeType.HANDLE)
    public SearchResponse<RoleDTO> searchRoles(SearchRequest<RoleDTO> search) {
        return internalApiClient.post(authBaseUrl + "/cms/roles/search", search, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_SEARCH_PERMISSIONS, type = ModeType.HANDLE)
    public SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> search) {
        return internalApiClient.post(
            authBaseUrl + "/cms/permissions/search",
            search,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_SEARCH_TENANTS, type = ModeType.HANDLE)
    public SearchResponse<TenantDTO> searchTenants(SearchRequest<TenantDTO> search) {
        return internalApiClient.post(
            authBaseUrl + "/cms/tenants/search",
            search,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_GET_TENANT, type = ModeType.HANDLE)
    public TenantDTO getTenant(Long id) {
        return internalApiClient.post(authBaseUrl + "/cms/tenants/get", id, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_CREATE_TENANT, type = ModeType.HANDLE)
    public TenantDTO createTenant(TenantDTO tenant) {
        return internalApiClient.post(
            authBaseUrl + "/cms/tenants/create",
            tenant,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_UPDATE_TENANT, type = ModeType.HANDLE)
    public TenantDTO updateTenant(TenantDTO tenant) {
        return internalApiClient.post(
            authBaseUrl + "/cms/tenants/update",
            tenant,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_LOCK_UNLOCK_TENANT, type = ModeType.HANDLE)
    public Void lockUnlockTenant(TenantDTO tenant) {
        return internalApiClient.post(
            authBaseUrl + "/cms/tenants/lock-unlock",
            tenant,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_SEARCH_PLANS, type = ModeType.HANDLE)
    public SearchResponse<PlanDTO> searchPlans(SearchRequest<PlanDTO> search) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/search", search, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_GET_PLAN, type = ModeType.HANDLE)
    public PlanDTO getPlan(PlanDTO plan) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/get", plan, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_GET_ALL_PLANS, type = ModeType.HANDLE)
    public List<PlanDTO> getAllPlans() {
        return internalApiClient.post(authBaseUrl + "/cms/plans/all", null, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_CREATE_PLAN, type = ModeType.HANDLE)
    public PlanDTO createPlan(PlanDTO plan) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/create", plan, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_UPDATE_PLAN, type = ModeType.HANDLE)
    public PlanDTO updatePlan(PlanDTO plan) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/update", plan, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_LOCK_UNLOCK_PLAN, type = ModeType.HANDLE)
    public Void lockUnlockPlan(PlanDTO plan) {
        return internalApiClient.post(
            authBaseUrl + "/cms/plans/lock-unlock",
            plan,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_EXPORT_PLAN, type = ModeType.HANDLE)
    public byte[] exportPlan(SearchRequest<PlanDTO> request) {
        return internalApiClient.post(
            authBaseUrl + "/cms/plans/export",
            request,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_EXPORT_TENANT, type = ModeType.HANDLE)
    public byte[] exportTenants(SearchRequest<TenantDTO> request) {
        return internalApiClient.post(
            authBaseUrl + "/cms/tenants/export",
            request,
            new ParameterizedTypeReference<>() {}
        );
    }

    @FwMode(name = ServiceMethod.CMS_LOCK_USER, type = ModeType.HANDLE)
    public Void lockUser(Long id) {
        return internalApiClient.post(authBaseUrl + "/cms/users/lock", id, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_UNLOCK_USER, type = ModeType.HANDLE)
    public Void unlockUser(Long id) {
        return internalApiClient.post(authBaseUrl + "/cms/users/unlock", id, new ParameterizedTypeReference<>() {});
    }
}

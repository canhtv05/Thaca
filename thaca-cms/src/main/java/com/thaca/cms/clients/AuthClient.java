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

    @FwMode(name = ServiceMethod.CMS_SAVE_TENANT, type = ModeType.HANDLE)
    public TenantDTO saveTenant(TenantDTO tenant) {
        return internalApiClient.post(authBaseUrl + "/cms/tenants/save", tenant, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_DELETE_TENANT, type = ModeType.HANDLE)
    public Void deleteTenant(Long id) {
        return internalApiClient.post(authBaseUrl + "/cms/tenants/delete", id, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_SEARCH_PLANS, type = ModeType.HANDLE)
    public SearchResponse<PlanDTO> searchPlans(SearchRequest<PlanDTO> search) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/search", search, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_GET_PLAN, type = ModeType.HANDLE)
    public PlanDTO getPlan(Long id) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/get", id, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_SAVE_PLAN, type = ModeType.HANDLE)
    public PlanDTO savePlan(PlanDTO plan) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/save", plan, new ParameterizedTypeReference<>() {});
    }

    @FwMode(name = ServiceMethod.CMS_DELETE_PLAN, type = ModeType.HANDLE)
    public Void deletePlan(Long id) {
        return internalApiClient.post(authBaseUrl + "/cms/plans/delete", id, new ParameterizedTypeReference<>() {});
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

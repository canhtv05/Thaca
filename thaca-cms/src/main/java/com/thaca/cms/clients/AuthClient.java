package com.thaca.cms.clients;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.*;
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

    @FwInternalApi(path = "/cms/sign-out", name = ServiceMethod.CMS_LOGOUT)
    Void signOut();

    @FwInternalApi(path = "/cms/profile", name = ServiceMethod.CMS_GET_PROFILE)
    SystemUserDTO getProfile();

    // ==========================================
    // END USER MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/users/search", name = ServiceMethod.CMS_SEARCH_USERS)
    SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> search);

    @FwInternalApi(path = "/cms/users/detail", name = ServiceMethod.CMS_DETAIL_USER)
    UserDTO detailUser(UserDTO request);

    @FwInternalApi(path = "/cms/users/download-template", name = ServiceMethod.CMS_DOWNLOAD_USER_TEMPLATE)
    byte[] downloadUserTemplate();

    @FwInternalApi(path = "/cms/users/export", name = ServiceMethod.CMS_EXPORT_USERS)
    byte[] exportUsers(SearchRequest<UserDTO> request);

    @FwInternalApi(path = "/cms/users/file-error", name = ServiceMethod.CMS_EXPORT_USER_FILE_ERROR)
    byte[] exportUserFileError(ImportResponseDTO importResult);

    @FwInternalApi(path = "/cms/users/lock-unlock", name = ServiceMethod.CMS_LOCK_UNLOCK_USER)
    Void lockUnlockUser(SystemUserDTO user);

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

    @FwInternalApi(path = "/cms/roles/export", name = ServiceMethod.CMS_EXPORT_ROLES)
    byte[] exportRoles(SearchRequest<RoleDTO> request);

    // ==========================================
    // PERMISSION MANAGEMENT
    // ==========================================
    @FwInternalApi(path = "/cms/permissions/search", name = ServiceMethod.CMS_SEARCH_PERMISSIONS)
    SearchResponse<PermissionDTO> searchPermissions(SearchRequest<PermissionDTO> search);

    @FwInternalApi(path = "/cms/permissions/all", name = ServiceMethod.CMS_GET_ALL_PERMISSIONS)
    List<PermissionDTO> getAllPermissions();

    @FwInternalApi(path = "/cms/permissions/by-roles", name = ServiceMethod.CMS_GET_PERMISSIONS_BY_ROLES)
    List<PermissionDTO> getPermissionsByRoles(RoleCodesReq request);

    @FwInternalApi(path = "/cms/permissions/export", name = ServiceMethod.CMS_EXPORT_PERMISSIONS)
    byte[] exportPermissions(SearchRequest<PermissionDTO> request);
}

package com.thaca.auth.internal.controllers;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.*;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/refresh-token")
    @FwRequest(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.INTERNAL)
    public ResponseEntity<RefreshTokenRes> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue
    ) {
        return ResponseEntity.ok(fwApiProcess.process(cookieValue));
    }

    @PostMapping("/cms/sign-in")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_AUTHENTICATE, type = RequestType.INTERNAL)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq) {
        return ResponseEntity.ok(fwApiProcess.process(loginReq));
    }

    @PostMapping("/cms/profile")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_PROFILE, type = RequestType.INTERNAL)
    public ResponseEntity<AuthUserDTO> getProfile() {
        return ResponseEntity.ok(fwApiProcess.process(null));
    }

    @PostMapping("/cms/users/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<UserDTO>> searchUsers(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(fwApiProcess.process(criteria));
    }

    @PostMapping("/cms/roles/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_ROLES, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<RoleDTO>> searchRoles(SearchRequest<RoleDTO> criteria) {
        return ResponseEntity.ok(fwApiProcess.process(criteria));
    }

    @PostMapping("/cms/permissions/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_PERMISSIONS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<PermissionDTO>> searchPermissions(SearchRequest<PermissionDTO> criteria) {
        return ResponseEntity.ok(fwApiProcess.process(criteria));
    }

    // làm sau
    @PostMapping("/cms/users/lock")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_LOCK_USER, type = RequestType.INTERNAL)
    public ResponseEntity<Void> lockUser(Long id) {
        fwApiProcess.process(id);
        return ResponseEntity.ok().build();
    }

    // làm sau
    @PostMapping("/cms/users/unlock")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_UNLOCK_USER, type = RequestType.INTERNAL)
    public ResponseEntity<Void> unlockUser(Long id) {
        fwApiProcess.process(id);
        return ResponseEntity.ok().build();
    }
}

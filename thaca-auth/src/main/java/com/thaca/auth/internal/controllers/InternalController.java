package com.thaca.auth.internal.controllers;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.internal.services.InternalService;
import com.thaca.auth.services.AuthService;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final AuthService authService;
    private final InternalService internalService;

    @PostMapping("/refresh-token")
    @FwRequest(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.INTERNAL)
    public ResponseEntity<RefreshTokenRes> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : null;
        return ResponseEntity.ok(authService.refreshToken(cookieValue, channel, httpServletRequest, response));
    }

    @PostMapping("/cms/sign-in")
    @FwRequest(name = ServiceMethod.CMS_AUTHENTICATE, type = RequestType.INTERNAL)
    public ResponseEntity<AuthenticateRes> signIn(
        LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        return ResponseEntity.ok(authService.authenticateCms(loginReq, httpServletRequest, httpServletResponse));
    }

    @PostMapping("/cms/profile")
    @FwRequest(name = ServiceMethod.CMS_GET_PROFILE, type = RequestType.INTERNAL)
    public ResponseEntity<AuthUserDTO> getProfile() {
        return ResponseEntity.ok(internalService.getSystemProfile(SecurityUtils.getCurrentUsername()));
    }

    @PostMapping("/cms/users/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_USERS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<UserDTO>> search(SearchRequest<UserDTO> criteria) {
        return ResponseEntity.ok(internalService.search(criteria));
    }

    @PostMapping("/cms/users/lock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_USER, type = RequestType.INTERNAL)
    public ResponseEntity<Void> lockUser(Long id) {
        internalService.changeLockUser(id, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cms/users/unlock")
    @FwRequest(name = ServiceMethod.CMS_UNLOCK_USER, type = RequestType.INTERNAL)
    public ResponseEntity<Void> unlockUser(Long id) {
        internalService.changeLockUser(id, false);
        return ResponseEntity.ok().build();
    }
}

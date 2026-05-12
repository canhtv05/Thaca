package com.thaca.auth.controllers.internal;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
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
public class InternalAuthController {

    private final FwApiProcess process;

    @PostMapping("/refresh-token")
    @FwRequest(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.INTERNAL)
    public ResponseEntity<RefreshTokenRes> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue
    ) {
        return ResponseEntity.ok(process.process(cookieValue));
    }

    @PostMapping("/cms/sign-in")
    @FwRequest(name = ServiceMethod.CMS_AUTHENTICATE, type = RequestType.INTERNAL)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq) {
        return ResponseEntity.ok(process.process(loginReq));
    }

    @PostMapping("/cms/sign-out")
    @FwRequest(name = ServiceMethod.CMS_LOGOUT, type = RequestType.INTERNAL)
    public ResponseEntity<Void> signOut() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/cms/profile")
    @FwRequest(name = ServiceMethod.CMS_GET_PROFILE, type = RequestType.INTERNAL)
    public ResponseEntity<SystemUserDTO> getProfile() {
        return ResponseEntity.ok(process.process(null));
    }
}

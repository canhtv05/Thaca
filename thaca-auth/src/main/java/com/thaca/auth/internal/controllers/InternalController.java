package com.thaca.auth.internal.controllers;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.services.AuthService;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.RequestType;
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
}

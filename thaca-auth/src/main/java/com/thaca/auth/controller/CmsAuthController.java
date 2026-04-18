package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.req.LoginReq;
import com.thaca.auth.dtos.res.AuthenticateRes;
import com.thaca.auth.services.AuthService;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class CmsAuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.CMS_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq, HttpServletResponse httpServletResponse) {
        return ResponseEntity.ok(authService.authenticateCms(loginReq, httpServletResponse));
    }
}

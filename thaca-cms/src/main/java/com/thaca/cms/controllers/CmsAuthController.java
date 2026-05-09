package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cms")
@RequiredArgsConstructor
public class CmsAuthController {

    private final FwApiProcess process;

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.CMS_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq) {
        return ResponseEntity.ok(process.process(loginReq));
    }

    @PostMapping("/sign-out")
    @FwRequest(name = ServiceMethod.CMS_LOGOUT, type = RequestType.PROTECTED)
    public ResponseEntity<Void> signOut() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/profile")
    @FwRequest(name = ServiceMethod.CMS_GET_PROFILE, type = RequestType.PROTECTED)
    public ResponseEntity<SystemUserDTO> getProfile() {
        return ResponseEntity.ok(process.process(null));
    }
}

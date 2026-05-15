package com.thaca.auth.controllers.external;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.SystemUserDTO;
import com.thaca.auth.dtos.req.LoginReq;
import com.thaca.auth.dtos.req.SendOtpReq;
import com.thaca.auth.dtos.res.AuthenticateRes;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final FwApiProcess process;

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.ADMIN_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> signIn(LoginReq loginReq) {
        return ResponseEntity.ok(process.process(loginReq));
    }

    @PostMapping("/sign-out")
    @FwRequest(name = ServiceMethod.ADMIN_LOGOUT, type = RequestType.PROTECTED)
    public ResponseEntity<Void> signOut() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/send-authenticate-otp")
    @FwRequest(name = ServiceMethod.ADMIN_SEND_AUTHENTICATE_OTP, type = RequestType.PUBLIC)
    public ResponseEntity<Void> sendAuthenticateOtp(SendOtpReq req) {
        return ResponseEntity.ok(process.process(req));
    }

    @PostMapping("/profile")
    @FwRequest(name = ServiceMethod.ADMIN_GET_PROFILE, type = RequestType.PROTECTED)
    public ResponseEntity<SystemUserDTO> getProfile() {
        return ResponseEntity.ok(process.process(null));
    }
}

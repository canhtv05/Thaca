package com.thaca.auth.controllers.external;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.CaptchaDTO;
import com.thaca.auth.dtos.LoginHistoryDTO;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/generate-captcha")
    @FwRequest(name = ServiceMethod.AUTH_GENERATE_CAPTCHA, type = RequestType.PUBLIC)
    public ResponseEntity<CaptchaDTO> generateCaptcha() {
        return ResponseEntity.ok(fwApiProcess.process(null));
    }

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.AUTH_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> authenticate(LoginReq loginReq) {
        return ResponseEntity.ok(fwApiProcess.process(loginReq));
    }

    @PostMapping("/sign-up")
    @FwRequest(name = ServiceMethod.AUTH_CREATE_USER, type = RequestType.PUBLIC)
    public ResponseEntity<Void> createUser(UserDTO userDTO) {
        fwApiProcess.process(userDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    @FwRequest(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.INTERNAL)
    public ResponseEntity<RefreshTokenRes> refreshToken() {
        return ResponseEntity.ok(fwApiProcess.process(null));
    }

    @PostMapping("/change-password")
    @FwRequest(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = RequestType.PROTECTED)
    public ResponseEntity<Void> changePassword(ChangePasswordReq req) {
        fwApiProcess.process(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @FwRequest(name = ServiceMethod.AUTH_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<Void> forgotPassword(ForgotPasswordReq req) {
        fwApiProcess.process(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp-forgot-password")
    @FwRequest(name = ServiceMethod.AUTH_VERIFY_OTP_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<Void> verifyOTPForgotPassword(VerifyOTPReq req) {
        fwApiProcess.process(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @FwRequest(name = ServiceMethod.AUTH_RESET_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<Void> resetPassword(ResetPasswordReq req) {
        fwApiProcess.process(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @FwRequest(name = ServiceMethod.AUTH_LOGOUT, type = RequestType.PROTECTED)
    public ResponseEntity<Void> logout() {
        fwApiProcess.process(null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all-devices")
    @FwRequest(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = RequestType.PROTECTED)
    public ResponseEntity<Void> logoutAllDevices() {
        fwApiProcess.process(null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search-login-history")
    @FwRequest(name = ServiceMethod.AUTH_SEARCH_LOGIN_HISTORY, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<LoginHistoryDTO>> searchLoginHistory(SearchRequest<LoginHistoryDTO> criteria) {
        return ResponseEntity.ok(fwApiProcess.process(criteria));
    }
}

package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.LoginHistoryDTO;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.LoginReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.dtos.res.AuthenticateRes;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.services.AuthService;
import com.thaca.auth.services.UserService;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.RequestType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/sign-in")
    @FwRequest(name = ServiceMethod.AUTH_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<AuthenticateRes> authenticate(
        LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        return ResponseEntity.ok(authService.authenticate(loginReq, httpServletRequest, httpServletResponse));
    }

    @PostMapping("/sign-up")
    @FwRequest(name = ServiceMethod.AUTH_CREATE_USER, type = RequestType.PUBLIC)
    public ResponseEntity<Void> createUser(UserDTO userDTO) {
        userService.createUser(userDTO, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    @FwRequest(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.PROTECTED)
    public ResponseEntity<RefreshTokenRes> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : null;
        return ResponseEntity.ok(authService.refreshToken(cookieValue, channel, httpServletRequest, response));
    }

    @PostMapping("/change-password")
    @FwRequest(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = RequestType.PROTECTED)
    public ResponseEntity<Void> changePassword(ChangePasswordReq req, HttpServletResponse response) {
        userService.changePassword(req, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @FwRequest(name = ServiceMethod.AUTH_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<Void> forgotPassword(ForgotPasswordReq req) {
        userService.handleForgotPasswordRequest(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp-forgot-password")
    @FwRequest(name = ServiceMethod.AUTH_VERIFY_OTP_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<Void> verifyOTPForgotPassword(VerifyOTPReq req) {
        userService.handleVerifyOTPForgotPassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @FwRequest(name = ServiceMethod.AUTH_RESET_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<Void> resetPassword(ResetPasswordReq req) {
        userService.handleResetPassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @FwRequest(name = ServiceMethod.AUTH_LOGOUT, type = RequestType.PROTECTED)
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all-devices")
    @FwRequest(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = RequestType.PROTECTED)
    public ResponseEntity<Void> logoutAllDevices(HttpServletResponse response) {
        authService.logoutAllDevices(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search-login-history")
    @FwRequest(name = ServiceMethod.AUTH_SEARCH_LOGIN_HISTORY, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<LoginHistoryDTO>> searchLoginHistory(SearchRequest<LoginHistoryDTO> criteria) {
        return ResponseEntity.ok(authService.searchLoginHistory(criteria));
    }
}

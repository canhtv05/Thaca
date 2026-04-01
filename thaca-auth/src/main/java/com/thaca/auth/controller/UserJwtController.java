package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.UserProfileDTO;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.LoginReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.dtos.res.AuthenticateRes;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.services.AuthService;
import com.thaca.auth.services.UserService;
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.enums.RequestType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserJwtController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/sign-in")
    @FwRequestMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = RequestType.PUBLIC)
    public AuthenticateRes authenticate(
        @RequestBody ApiPayload<LoginReq> loginReq,
        HttpServletResponse httpServletResponse
    ) {
        return authService.authenticate(loginReq.getBody().getData(), httpServletResponse);
    }

    @PostMapping("/sign-up")
    @FwRequestMode(name = ServiceMethod.AUTH_CREATE_USER, type = RequestType.PUBLIC)
    public ApiPayload<Void> createUser(@RequestBody ApiPayload<UserDTO> userDTO) {
        userService.createUser(userDTO.getBody().getData(), false);
        return ApiPayload.success();
    }

    @PostMapping("/refresh-token")
    @FwRequestMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.PROTECTED)
    public RefreshTokenRes refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        @RequestBody ApiPayload<?> req,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        return authService.refreshToken(cookieValue, req.getHeader().getChannel(), httpServletRequest, response);
    }

    @PostMapping("/change-password")
    @FwRequestMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = RequestType.PROTECTED)
    public ApiPayload<Void> changePassword(
        @RequestBody ApiPayload<ChangePasswordReq> req,
        HttpServletResponse response
    ) {
        userService.changePassword(req.getBody().getData(), response);
        return ApiPayload.success();
    }

    @PostMapping("/forgot-password")
    @FwRequestMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ApiPayload<Void> forgotPassword(@RequestBody ApiPayload<ForgotPasswordReq> req) {
        userService.handleForgotPasswordRequest(req.getBody().getData());
        return ApiPayload.success();
    }

    @PostMapping("/verify-otp-forgot-password")
    @FwRequestMode(name = ServiceMethod.AUTH_VERIFY_OTP_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<ApiPayload<Boolean>> verifyOTPForgotPassword(@RequestBody ApiPayload<VerifyOTPReq> req) {
        userService.handleVerifyOTPForgotPassword(req.getBody().getData());
        return ResponseEntity.ok(ApiPayload.success());
    }

    @PostMapping("/reset-password")
    @FwRequestMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = RequestType.PUBLIC)
    public ApiPayload<Void> resetPassword(@RequestBody ApiPayload<ResetPasswordReq> req) {
        userService.handleResetPassword(req.getBody().getData());
        return ApiPayload.success();
    }

    @PostMapping("/logout")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT, type = RequestType.PROTECTED)
    public ApiPayload<Void> logout(@RequestBody ApiPayload<Void> req, HttpServletResponse response) {
        authService.logout(req, response);
        return ApiPayload.success();
    }

    @PostMapping("/logout-all-devices")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = RequestType.PROTECTED)
    public ResponseEntity<ApiPayload<?>> logoutAllDevices(HttpServletResponse response) {
        authService.logoutAllDevices(response);
        return ResponseEntity.ok(ApiPayload.success());
    }
}

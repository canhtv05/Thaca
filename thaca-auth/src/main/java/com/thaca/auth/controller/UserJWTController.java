package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.UserProfileDTO;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.LoginReq;
import com.thaca.auth.dtos.req.LogoutReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.dtos.res.AuthenticateRes;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.dtos.res.VerifyTokenRes;
import com.thaca.auth.services.AuthService;
import com.thaca.auth.services.UserService;
import com.thaca.common.dtos.ApiResponse;
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.enums.RequestType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me")
public class UserJWTController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/authenticate")
    @FwRequestMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = RequestType.PUBLIC)
    public ResponseEntity<ApiResponse<AuthenticateRes>> authenticate(
        @RequestBody LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        AuthenticateRes authenticateRes = authService.authenticate(loginReq, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success(authenticateRes));
    }

    @PostMapping("/refresh-token")
    @FwRequestMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.PROTECTED)
    public ResponseEntity<ApiResponse<RefreshTokenRes>> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        @RequestBody LogoutReq req,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(authService.refreshToken(cookieValue, req.getChannel(), httpServletRequest, response))
        );
    }

    @PostMapping("/internal/verify")
    @FwRequestMode(name = ServiceMethod.AUTH_VERIFY_TOKEN, type = RequestType.INTERNAL)
    public ResponseEntity<ApiResponse<VerifyTokenRes>> verifyToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyToken(cookieValue, false)));
    }

    @PostMapping("/create")
    @FwRequestMode(name = ServiceMethod.AUTH_CREATE_USER, type = RequestType.PROTECTED)
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        UserDTO newUserDTO = userService.createUser(userDTO, false);
        return ResponseEntity.ok(ApiResponse.success(newUserDTO));
    }

    @PostMapping("/change-password")
    @FwRequestMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = RequestType.PROTECTED)
    public ResponseEntity<ApiResponse<Boolean>> changePassword(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        @RequestBody ChangePasswordReq req,
        HttpServletResponse response
    ) {
        userService.changePassword(cookieValue, req, response);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/reset-password")
    @FwRequestMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@RequestBody ResetPasswordReq req) {
        userService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/forgot-password")
    @FwRequestMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<ApiResponse<Boolean>> forgotPassword(@RequestBody ForgotPasswordReq req) {
        userService.forgotPasswordRequest(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/verify-forgot-password-otp")
    @FwRequestMode(name = ServiceMethod.AUTH_VERIFY_FORGOT_PASSWORD_OTP, type = RequestType.PUBLIC)
    public ResponseEntity<ApiResponse<Boolean>> verifyForgotPasswordOTP(@RequestBody VerifyOTPReq req) {
        userService.verifyForgotPasswordOTP(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/update")
    @FwRequestMode(name = ServiceMethod.AUTH_UPDATE_USER_PROFILE, type = RequestType.PROTECTED)
    public ResponseEntity<ApiResponse<Boolean>> updateUserProfile(@RequestBody UserProfileDTO req) {
        userService.updateUserProfile(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/logout")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT, type = RequestType.PROTECTED)
    public ResponseEntity<ApiResponse<?>> logout(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        @RequestBody LogoutReq req,
        HttpServletResponse response
    ) {
        authService.logout(cookieValue, req.getChannel(), response);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/logout-all-devices")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = RequestType.PROTECTED)
    public ResponseEntity<ApiResponse<?>> logoutAllDevices(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        HttpServletResponse response
    ) {
        authService.logoutAllDevices(cookieValue, response);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

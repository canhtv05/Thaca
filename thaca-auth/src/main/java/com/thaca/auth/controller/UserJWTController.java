package com.thaca.auth.controller;

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
import com.thaca.framework.core.constants.CommonConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    @PostMapping("/p/authenticate")
    public ResponseEntity<ApiResponse<AuthenticateRes>> authorize(
        @Valid @RequestBody LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        AuthenticateRes authenticateRes = authService.authenticate(loginReq, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success(authenticateRes));
    }

    @PostMapping("/p/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenRes>> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        @Valid @RequestBody LogoutReq req,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(authService.refreshToken(cookieValue, req.getChannel(), httpServletRequest, response))
        );
    }

    @PostMapping("/internal/verify")
    public ResponseEntity<ApiResponse<VerifyTokenRes>> verifyToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyToken(cookieValue, false)));
    }

    @PostMapping("/c/create")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO newUserDTO = userService.createUser(userDTO, false);
        return ResponseEntity.ok(ApiResponse.success(newUserDTO));
    }

    @PostMapping("/p/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        @Valid @RequestBody ChangePasswordReq req,
        HttpServletResponse response
    ) {
        userService.changePassword(cookieValue, req, response);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/p/reset-password")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        userService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/p/forgot-password")
    public ResponseEntity<ApiResponse<Boolean>> forgotPassword(@Valid @RequestBody ForgotPasswordReq req) {
        userService.forgotPasswordRequest(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/p/verify-forgot-password-otp")
    public ResponseEntity<ApiResponse<Boolean>> verifyForgotPasswordOTP(@Valid @RequestBody VerifyOTPReq req) {
        userService.verifyForgotPasswordOTP(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/p/update")
    public ResponseEntity<ApiResponse<Boolean>> updateUserProfile(@RequestBody UserProfileDTO req) {
        userService.updateUserProfile(req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/p/logout")
    public ResponseEntity<ApiResponse<?>> logout(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        @RequestBody LogoutReq req,
        HttpServletResponse response
    ) {
        authService.logout(cookieValue, req.getChannel(), response);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/p/logout-all-devices")
    public ResponseEntity<ApiResponse<?>> logoutAllDevices(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        HttpServletResponse response
    ) {
        authService.logoutAllDevices(cookieValue, response);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

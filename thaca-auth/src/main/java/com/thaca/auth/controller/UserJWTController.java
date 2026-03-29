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
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.dtos.ApiEnvelope;
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
    public AuthenticateRes authenticate(
        @RequestBody ApiEnvelope<LoginReq> loginReq,
        HttpServletResponse httpServletResponse
    ) {
        return authService.authenticate(loginReq.getBody().getData(), httpServletResponse);
    }

    @PostMapping("/refresh-token")
    @FwRequestMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<RefreshTokenRes>> refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        @RequestBody LogoutReq req,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        return ResponseEntity.ok(
            ApiEnvelope.success(authService.refreshToken(cookieValue, req.getChannel(), httpServletRequest, response))
        );
    }

    // đôiv ưới API itnernal thì chauw cần viết @FWMode
    @PostMapping("/internal/verify")
    @FwRequestMode(name = ServiceMethod.AUTH_VERIFY_TOKEN, type = RequestType.INTERNAL)
    public ResponseEntity<ApiEnvelope<VerifyTokenRes>> verifyToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue
    ) {
        return ResponseEntity.ok(ApiEnvelope.success(authService.verifyToken(cookieValue, false)));
    }

    // click vào hàm mà endpoint sử dụng rồi viwwst @FW mode
    @PostMapping("/create")
    @FwRequestMode(name = ServiceMethod.AUTH_CREATE_USER, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
        UserDTO newUserDTO = userService.createUser(userDTO, false);
        return ResponseEntity.ok(ApiEnvelope.success(newUserDTO));
    }

    @PostMapping("/change-password")
    @FwRequestMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<Boolean>> changePassword(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        @RequestBody ChangePasswordReq req,
        HttpServletResponse response
    ) {
        userService.changePassword(cookieValue, req, response);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @PostMapping("/reset-password")
    @FwRequestMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<ApiEnvelope<Boolean>> resetPassword(@RequestBody ResetPasswordReq req) {
        userService.resetPassword(req);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @PostMapping("/forgot-password")
    @FwRequestMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public ResponseEntity<ApiEnvelope<Boolean>> forgotPassword(@RequestBody ForgotPasswordReq req) {
        userService.forgotPasswordRequest(req);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @PostMapping("/verify-forgot-password-otp")
    @FwRequestMode(name = ServiceMethod.AUTH_VERIFY_FORGOT_PASSWORD_OTP, type = RequestType.PUBLIC)
    public ResponseEntity<ApiEnvelope<Boolean>> verifyForgotPasswordOTP(@RequestBody VerifyOTPReq req) {
        userService.verifyForgotPasswordOTP(req);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @PostMapping("/update")
    @FwRequestMode(name = ServiceMethod.AUTH_UPDATE_USER_PROFILE, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<Boolean>> updateUserProfile(@RequestBody UserProfileDTO req) {
        userService.updateUserProfile(req);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @PostMapping("/logout")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<?>> logout(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        @RequestBody LogoutReq req,
        HttpServletResponse response
    ) {
        authService.logout(cookieValue, req.getChannel(), response);
        return ResponseEntity.ok(ApiEnvelope.success());
    }

    @PostMapping("/logout-all-devices")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = RequestType.PROTECTED)
    public ResponseEntity<ApiEnvelope<?>> logoutAllDevices(
        @CookieValue(name = CommonConstants.COOKIE_NAME) String cookieValue,
        HttpServletResponse response
    ) {
        authService.logoutAllDevices(cookieValue, response);
        return ResponseEntity.ok(ApiEnvelope.success());
    }
}

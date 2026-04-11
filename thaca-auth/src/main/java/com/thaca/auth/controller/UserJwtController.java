package com.thaca.auth.controller;

import com.thaca.auth.constants.ServiceMethod;
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
import com.thaca.framework.core.annotations.FwRequestMode;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.context.FwContext;
import com.thaca.framework.core.enums.RequestType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserJwtController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/sign-in")
    @FwRequestMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = RequestType.PUBLIC)
    public AuthenticateRes authenticate(LoginReq loginReq, HttpServletResponse httpServletResponse) {
        return authService.authenticate(loginReq, httpServletResponse);
    }

    @PostMapping("/sign-up")
    @FwRequestMode(name = ServiceMethod.AUTH_CREATE_USER, type = RequestType.PUBLIC)
    public void createUser(UserDTO userDTO) {
        userService.createUser(userDTO, false);
    }

    @PostMapping("/refresh-token")
    @FwRequestMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = RequestType.PROTECTED)
    public RefreshTokenRes refreshToken(
        @CookieValue(name = CommonConstants.COOKIE_NAME, required = false) String cookieValue,
        HttpServletRequest httpServletRequest,
        HttpServletResponse response
    ) {
        String channel = FwContext.get() != null ? FwContext.get().getChannel() : null;
        return authService.refreshToken(cookieValue, channel, httpServletRequest, response);
    }

    @PostMapping("/change-password")
    @FwRequestMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = RequestType.PROTECTED)
    public void changePassword(ChangePasswordReq req, HttpServletResponse response) {
        userService.changePassword(req, response);
    }

    @PostMapping("/forgot-password")
    @FwRequestMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public void forgotPassword(ForgotPasswordReq req) {
        userService.handleForgotPasswordRequest(req);
    }

    @PostMapping("/verify-otp-forgot-password")
    @FwRequestMode(name = ServiceMethod.AUTH_VERIFY_OTP_FORGOT_PASSWORD, type = RequestType.PUBLIC)
    public void verifyOTPForgotPassword(VerifyOTPReq req) {
        userService.handleVerifyOTPForgotPassword(req);
    }

    @PostMapping("/reset-password")
    @FwRequestMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = RequestType.PUBLIC)
    public void resetPassword(ResetPasswordReq req) {
        userService.handleResetPassword(req);
    }

    @PostMapping("/logout")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT, type = RequestType.PROTECTED)
    public void logout(HttpServletResponse response) {
        authService.logout(response);
    }

    @PostMapping("/logout-all-devices")
    @FwRequestMode(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = RequestType.PROTECTED)
    public void logoutAllDevices(HttpServletResponse response) {
        authService.logoutAllDevices(response);
    }
}

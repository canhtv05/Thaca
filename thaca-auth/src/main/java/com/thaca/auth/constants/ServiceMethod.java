package com.thaca.auth.constants;

public class ServiceMethod {

    public static final String INTERNAL_ACTIVE_USER = "internal.activeUserByUserName";
    public static final String CMS_AUTHENTICATE = "internal.cmsAuthenticate";
    public static final String CMS_SEARCH_USERS = "internal.cmsSearchUsers";
    public static final String CMS_GET_USER_BY_ID = "internal.cmsGetUserById";
    public static final String CMS_LOCK_USER = "internal.cmsLockUser";
    public static final String CMS_UNLOCK_USER = "internal.cmsUnlockUser";
    public static final String CMS_GET_PROFILE = "internal.cmsGetProfile";

    public static final String AUTH_FORGOT_PASSWORD_REQUEST = "auth.forgotPasswordRequest";
    public static final String AUTH_AUTHENTICATE = "auth.authenticate";
    public static final String AUTH_REFRESH_TOKEN = "auth.refreshToken";
    public static final String AUTH_VERIFY_TOKEN = "auth.verifyToken";
    public static final String AUTH_CREATE_USER = "auth.createUser";
    public static final String AUTH_UPDATE_USER = "auth.updateUser";
    public static final String AUTH_CHANGE_PASSWORD = "auth.changePassword";
    public static final String AUTH_RESET_PASSWORD = "auth.resetPassword";
    public static final String AUTH_FORGOT_PASSWORD = "auth.forgotPassword";
    public static final String AUTH_VERIFY_OTP_FORGOT_PASSWORD = "auth.verifyOTPForgotPassword";
    public static final String AUTH_LOGOUT = "auth.logout";
    public static final String AUTH_LOGOUT_ALL_DEVICES = "auth.logoutAllDevices";
    public static final String AUTH_SEARCH_LOGIN_HISTORY = "auth.searchLoginHistory";
}

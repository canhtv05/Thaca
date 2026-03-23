package com.thaca.auth.constants;

public class ServiceMethod {

    public static final String INTERNAL_ACTIVE_USER = "internal.activeUserByUserName";

    public static final String AUTH_FORGOT_PASSWORD_REQUEST = "auth.forgotPasswordRequest";
    public static final String AUTH_AUTHENTICATE = "auth.authenticate";
    public static final String AUTH_REFRESH_TOKEN = "auth.refreshToken";
    public static final String AUTH_VERIFY_TOKEN = "auth.verifyToken";
    public static final String AUTH_CREATE_USER = "auth.createUser";
    public static final String AUTH_UPDATE_USER = "auth.updateUser";
    public static final String AUTH_CHANGE_PASSWORD = "auth.changePassword";
    public static final String AUTH_RESET_PASSWORD = "auth.resetPassword";
    public static final String AUTH_FORGOT_PASSWORD = "auth.forgotPassword";
    public static final String AUTH_VERIFY_FORGOT_PASSWORD_OTP = "auth.verifyForgotPasswordOTP";
    public static final String AUTH_UPDATE_USER_PROFILE = "auth.updateUserProfile";
    public static final String AUTH_LOGOUT = "auth.logout";
    public static final String AUTH_LOGOUT_ALL_DEVICES = "auth.logoutAllDevices";

    public static final String ADMIN_SEARCH_USERS = "admin.searchUsers";
    public static final String ADMIN_GET_USER_BY_ID = "admin.getUserById";
    public static final String ADMIN_CREATE_USER = "admin.createUser";
    public static final String ADMIN_UPDATE_USER = "admin.updateUser";
    public static final String ADMIN_LOCK_USER = "admin.lockUser";
    public static final String ADMIN_UNLOCK_USER = "admin.unlockUser";
    public static final String ADMIN_GET_USER_PERMISSION = "admin.getUserPermission";
    public static final String ADMIN_UPDATE_USER_PERMISSION = "admin.updateUserPermission";
}

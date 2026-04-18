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
    public static final String AUTH_VERIFY_OTP_FORGOT_PASSWORD = "auth.verifyOTPForgotPassword";
    public static final String AUTH_LOGOUT = "auth.logout";
    public static final String AUTH_LOGOUT_ALL_DEVICES = "auth.logoutAllDevices";

    public static final String CMS_AUTHENTICATE = "cms.authenticate";
    public static final String CMS_SEARCH_USERS = "cms.searchUsers";
    public static final String CMS_GET_USER_BY_ID = "cms.getUserById";
    public static final String CMS_CREATE_USER = "cms.createUser";
    public static final String CMS_UPDATE_USER = "cms.updateUser";
    public static final String CMS_LOCK_USER = "cms.lockUser";
    public static final String CMS_UNLOCK_USER = "cms.unlockUser";
    public static final String ADMIN_GET_USER_PERMISSION = "cms.getUserPermission";
    public static final String ADMIN_UPDATE_USER_PERMISSION = "cms.updateUserPermission";
    public static final String CMS_GET_PROFILE = "cms.getProfile";
}

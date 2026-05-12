package com.thaca.auth.constants;

public class ServiceMethod {

    // ==========================================
    // AUTH ENDPOINTS
    // ==========================================
    public static final String AUTH_FORGOT_PASSWORD_REQUEST = "auth.forgotPasswordRequest";
    public static final String AUTH_AUTHENTICATE = "auth.authenticate";
    public static final String AUTH_GENERATE_CAPTCHA = "auth.generateCaptcha";
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

    public static final String USER_GET_USER_PROFILE = "user.getUserProfile";

    // ==========================================
    // CMS AUTHENTICATE (internal endpoints in auth, called by CMS frontend)
    // ==========================================
    public static final String CMS_AUTHENTICATE = "cms.authenticate";
    public static final String CMS_LOGOUT = "cms.logout";
    public static final String CMS_GET_PROFILE = "cms.getProfile";

    // ==========================================
    // END USER MANAGEMENT (called by CMS via internal API)
    // ==========================================
    public static final String CMS_SEARCH_USERS = "cms.searchUsers";
    public static final String CMS_DETAIL_USER = "cms.detailUser";
    public static final String CMS_DOWNLOAD_USER_TEMPLATE = "cms.downloadUserTemplate";
    public static final String CMS_IMPORT_USERS = "cms.importUsers";
    public static final String CMS_EXPORT_USERS = "cms.exportUsers";
    public static final String CMS_EXPORT_USER_FILE_ERROR = "cms.exportUserFileError";
    public static final String CMS_LOCK_UNLOCK_USER = "cms.lockUnlockUser";
    public static final String CMS_GET_USER_BY_ID = "cms.getUserById";
    public static final String CMS_SEARCH_USER_LOCK_HISTORY = "cms.searchUserLockHistory";

    // ==========================================
    // SYSTEM USER MANAGEMENT (called by CMS via internal API)
    // ==========================================
    public static final String CMS_SEARCH_SYSTEM_USERS = "cms.searchSystemUsers";
    public static final String CMS_GET_SYSTEM_USER = "cms.getSystemUser";
    public static final String CMS_CREATE_SYSTEM_USER = "cms.createSystemUser";
    public static final String CMS_UPDATE_SYSTEM_USER = "cms.updateSystemUser";
    public static final String CMS_LOCK_UNLOCK_SYSTEM_USER = "cms.lockUnlockSystemUser";
    public static final String CMS_EXPORT_SYSTEM_USER = "cms.exportSystemUser";

    // ==========================================
    // ROLE & PERMISSION (called by CMS via internal API)
    // ==========================================
    public static final String CMS_SEARCH_ROLES = "cms.searchRoles";
    public static final String CMS_GET_ALL_ROLES = "cms.getAllRoles";
    public static final String CMS_EXPORT_ROLES = "cms.exportRoles";
    public static final String CMS_SEARCH_PERMISSIONS = "cms.searchPermissions";
    public static final String CMS_GET_ALL_PERMISSIONS = "cms.getAllPermissions";
    public static final String CMS_GET_PERMISSIONS_BY_ROLES = "internal.cmsGetPermissionsByRoles";
    public static final String CMS_EXPORT_PERMISSIONS = "cms.exportPermissions";

    // ==========================================
    // TENANT & PLAN MANAGEMENT (called by Auth via internal API)
    // ==========================================
    public static final String CMS_SEARCH_TENANTS = "cms.searchTenants";
    public static final String CMS_GET_TENANT = "cms.getTenant";
    public static final String CMS_GET_TENANTS_BY_IDS = "cms.getTenantsByIds";
    public static final String CMS_GET_ALL_TENANTS = "cms.getAllTenants";
    public static final String CMS_CREATE_TENANT = "cms.createTenant";
    public static final String CMS_UPDATE_TENANT = "cms.updateTenant";
    public static final String CMS_LOCK_UNLOCK_TENANT = "cms.lockUnlockTenant";
    public static final String CMS_EXPORT_TENANT = "cms.exportTenant";

    public static final String CMS_SEARCH_PLANS = "cms.searchPlans";
    public static final String CMS_GET_PLAN = "cms.getPlan";
    public static final String CMS_GET_ALL_PLANS = "cms.getAllPlans";
    public static final String CMS_CREATE_PLAN = "cms.createPlan";
    public static final String CMS_UPDATE_PLAN = "cms.updatePlan";
    public static final String CMS_LOCK_UNLOCK_PLAN = "cms.lockUnlockPlan";
    public static final String CMS_EXPORT_PLAN = "cms.exportPlan";
}

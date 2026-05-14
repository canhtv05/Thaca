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
    // admin AUTHENTICATE (internal endpoints in auth, called by admin frontend)
    // ==========================================
    public static final String ADMIN_AUTHENTICATE = "admin.authenticate";
    public static final String ADMIN_LOGOUT = "admin.logout";
    public static final String ADMIN_GET_PROFILE = "admin.getProfile";
    public static final String ADMIN_SEND_AUTHENTICATE_OTP = "admin.sendAuthenticateOtp";

    // ==========================================
    // END USER MANAGEMENT (called by admin via internal API)
    // ==========================================
    public static final String ADMIN_SEARCH_USERS = "admin.searchUsers";
    public static final String ADMIN_DETAIL_USER = "admin.detailUser";
    public static final String ADMIN_DOWNLOAD_USER_TEMPLATE = "admin.downloadUserTemplate";
    public static final String ADMIN_IMPORT_USERS = "admin.importUsers";
    public static final String ADMIN_EXPORT_USERS = "admin.exportUsers";
    public static final String ADMIN_EXPORT_USER_FILE_ERROR = "admin.exportUserFileError";
    public static final String ADMIN_LOCK_UNLOCK_USER = "admin.lockUnlockUser";
    public static final String ADMIN_GET_USER_BY_ID = "admin.getUserById";
    public static final String ADMIN_SEARCH_USER_LOCK_HISTORY = "admin.searchUserLockHistory";

    // ==========================================
    // SYSTEM USER MANAGEMENT (called by admin via internal API)
    // ==========================================
    public static final String ADMIN_SEARCH_SYSTEM_USERS = "admin.searchSystemUsers";
    public static final String ADMIN_GET_SYSTEM_USER = "admin.getSystemUser";
    public static final String ADMIN_CREATE_SYSTEM_USER = "admin.createSystemUser";
    public static final String ADMIN_UPDATE_SYSTEM_USER = "admin.updateSystemUser";
    public static final String ADMIN_LOCK_UNLOCK_SYSTEM_USER = "admin.lockUnlockSystemUser";
    public static final String ADMIN_EXPORT_SYSTEM_USER = "admin.exportSystemUser";

    // ==========================================
    // ROLE & PERMISSION (called by admin via internal API)
    // ==========================================
    public static final String ADMIN_SEARCH_ROLES = "admin.searchRoles";
    public static final String ADMIN_GET_ALL_ROLES = "admin.getAllRoles";
    public static final String ADMIN_EXPORT_ROLES = "admin.exportRoles";
    public static final String ADMIN_SEARCH_PERMISSIONS = "admin.searchPermissions";
    public static final String ADMIN_GET_ALL_PERMISSIONS = "admin.getAllPermissions";
    public static final String ADMIN_GET_PERMISSIONS_BY_ROLES = "internal.adminGetPermissionsByRoles";
    public static final String ADMIN_EXPORT_PERMISSIONS = "admin.exportPermissions";

    // ==========================================
    // TENANT & PLAN MANAGEMENT (called by Auth via internal API)
    // ==========================================
    public static final String ADMIN_SEARCH_TENANTS = "admin.searchTenants";
    public static final String ADMIN_GET_TENANT = "admin.getTenant";
    public static final String ADMIN_GET_TENANTS_BY_IDS = "admin.getTenantsByIds";
    public static final String ADMIN_GET_TENANTS_FULL_BY_IDS = "admin.getTenantsFullByIds";
    public static final String ADMIN_GET_ALL_TENANTS = "admin.getAllTenants";
    public static final String ADMIN_CREATE_TENANT = "admin.createTenant";
    public static final String ADMIN_UPDATE_TENANT = "admin.updateTenant";
    public static final String ADMIN_LOCK_UNLOCK_TENANT = "admin.lockUnlockTenant";
    public static final String ADMIN_EXPORT_TENANT = "admin.exportTenant";

    public static final String ADMIN_SEARCH_PLANS = "admin.searchPlans";
    public static final String ADMIN_GET_PLAN = "admin.getPlan";
    public static final String ADMIN_GET_ALL_PLANS = "admin.getAllPlans";
    public static final String ADMIN_CREATE_PLAN = "admin.createPlan";
    public static final String ADMIN_UPDATE_PLAN = "admin.updatePlan";
    public static final String ADMIN_LOCK_UNLOCK_PLAN = "admin.lockUnlockPlan";
    public static final String ADMIN_EXPORT_PLAN = "admin.exportPlan";
}

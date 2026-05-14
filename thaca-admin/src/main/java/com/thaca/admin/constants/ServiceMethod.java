package com.thaca.admin.constants;

public class ServiceMethod {

    // ==========================================
    // TENANT MANAGEMENT
    // ==========================================
    public static final String ADMIN_SEARCH_TENANTS = "admin.searchTenants";
    public static final String ADMIN_GET_TENANT = "admin.getTenant";
    public static final String ADMIN_GET_TENANTS_BY_IDS = "admin.getTenantsByIds";
    public static final String ADMIN_GET_TENANTS_FULL_BY_IDS = "admin.getTenantsFullByIds";
    public static final String ADMIN_GET_ALL_TENANTS = "admin.getAllTenants";
    public static final String ADMIN_CREATE_TENANT = "admin.createTenant";
    public static final String ADMIN_UPDATE_TENANT = "admin.updateTenant";
    public static final String ADMIN_LOCK_UNLOCK_TENANT = "admin.lockUnlockTenant";
    public static final String ADMIN_DELETE_TENANT = "admin.deleteTenant";
    public static final String ADMIN_RESTORE_TENANT = "admin.restoreTenant";
    public static final String ADMIN_EXPORT_TENANT = "admin.exportTenant";

    // ==========================================
    // PLAN MANAGEMENT
    // ==========================================
    public static final String ADMIN_SEARCH_PLANS = "admin.searchPlans";
    public static final String ADMIN_GET_PLAN = "admin.getPlan";
    public static final String ADMIN_GET_ALL_PLANS = "admin.getAllPlans";
    public static final String ADMIN_CREATE_PLAN = "admin.createPlan";
    public static final String ADMIN_UPDATE_PLAN = "admin.updatePlan";
    public static final String ADMIN_LOCK_UNLOCK_PLAN = "admin.lockUnlockPlan";
    public static final String ADMIN_EXPORT_PLAN = "admin.exportPlan";

    // ==========================================
    // EXCEL ENGINE
    // ==========================================
    public static final String ADMIN_EXCEL_GENERATE_TEMPLATE = "admin.excel.generateTemplate";
    public static final String ADMIN_EXCEL_IMPORT = "admin.excel.import";
    public static final String ADMIN_EXCEL_EXPORT = "admin.excel.export";
}

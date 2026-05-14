package com.thaca.admin.enums;

import com.thaca.common.validations.ErrorMessageRule;
import lombok.Getter;

@Getter
public enum AdminErrorMessage implements ErrorMessageRule {
    PLAN_INACTIVE_CANNOT_UPDATE(
        "PLAN.INACTIVE",
        "Plan đã bị khóa",
        "Plan đã bị khóa không thể cập nhật. Vui lòng mở khóa plan để cập nhật",
        "Plan is inactive",
        "Plan is inactive cannot update. Please unlock plan to update"
    ),
    PLAN_INACTIVE_CANNOT_SAVE(
        "PLAN.INACTIVE.CANNOT.SAVE",
        "Plan đã bị khóa",
        "Plan đã bị khóa không thể lưu. Vui lòng mở khóa plan để lưu",
        "Plan is inactive",
        "Plan is inactive cannot save. Please unlock plan to save"
    ),
    PLAN_NOT_FOUND(
        "PLAN.NOT.FOUND",
        "Plan không tồn tại",
        "Plan không tồn tại trong hệ thống",
        "Plan not found",
        "Plan not found in system"
    ),
    PLAN_ALREADY_EXISTS(
        "PLAN.ALREADY.EXISTS",
        "Plan đã tồn tại",
        "Plan đã tồn tại trong hệ thống",
        "Plan already exists",
        "Plan already exists in system"
    ),
    TENANT_NOT_FOUND(
        "TENANT.NOT.FOUND",
        "Tổ chức không tồn tại",
        "Tổ chức không tồn tại trong hệ thống",
        "Tenant not found",
        "Tenant not found in system"
    ),
    TENANT_CODE_ALREADY_EXISTS(
        "TENANT.CODE.ALREADY.EXISTS",
        "Mã tổ chức đã tồn tại",
        "Mã tổ chức đã tồn tại trong hệ thống",
        "Tenant code already exists",
        "Tenant code already exists in system"
    ),
    TENANT_INACTIVE_CANNOT_UPDATE(
        "PLAN.INACTIVE",
        "Tổ chức đã bị khóa",
        "Tổ chức đã bị khóa không thể cập nhật. Vui lòng mở khóa tổ chức để cập nhật",
        "Tenant is inactive",
        "Tenant is inactive cannot update. Please unlock tenant to update"
    );

    private final String code;
    private final String titleVi;
    private final String messageVi;
    private final String titleEn;
    private final String messageEn;

    AdminErrorMessage(String code, String titleVi, String messageVi, String titleEn, String messageEn) {
        this.code = code;
        this.titleVi = titleVi;
        this.messageVi = messageVi;
        this.titleEn = titleEn;
        this.messageEn = messageEn;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String titleVi() {
        return titleVi;
    }

    @Override
    public String messageVi() {
        return messageVi;
    }

    @Override
    public String titleEn() {
        return titleEn;
    }

    @Override
    public String messageEn() {
        return messageEn;
    }
}

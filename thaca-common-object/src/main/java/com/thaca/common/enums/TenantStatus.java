package com.thaca.common.enums;

import com.thaca.common.validations.EnumRule;
import lombok.Getter;

@Getter
public enum TenantStatus implements EnumRule {
    ACTIVE("ACTIVE", "Hoạt động", "Active"),
    INACTIVE("INACTIVE", "Không hoạt động", "Inactive"),
    SUSPENDED("SUSPENDED", "Tạm khóa", "Suspended");

    private final String value;
    private final String nameVi;
    private final String nameEn;

    TenantStatus(String value, String nameVi, String nameEn) {
        this.value = value;
        this.nameVi = nameVi;
        this.nameEn = nameEn;
    }

    @Override
    public String getNameVi() {
        return this.nameVi;
    }

    @Override
    public String getNameEn() {
        return this.nameEn;
    }

    public String getLabel(boolean isVietnamese) {
        return isVietnamese ? this.nameVi : this.nameEn;
    }
}

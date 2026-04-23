package com.thaca.common.enums;

import com.thaca.common.validations.EnumRule;
import lombok.Getter;

@Getter
public enum CommonStatus implements EnumRule {
    ACTIVE("Hoạt động", "Active"),
    INACTIVE("Ngừng hoạt động", "Inactive");

    private final String nameVi;
    private final String nameEn;

    CommonStatus(String nameVi, String nameEn) {
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
}

package com.thaca.auth.enums;

import com.thaca.common.validations.EnumRule;
import lombok.Getter;

@Getter
public enum LoginStatus implements EnumRule {
    SUCCESS("Thành công", "Success"),
    FAILED("Thất bại", "Failed");

    private final String nameVi;
    private final String nameEn;

    LoginStatus(String nameVi, String nameEn) {
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

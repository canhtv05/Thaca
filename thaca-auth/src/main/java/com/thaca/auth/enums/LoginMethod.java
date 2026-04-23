package com.thaca.auth.enums;

import com.thaca.common.validations.EnumRule;
import lombok.Getter;

@Getter
public enum LoginMethod implements EnumRule {
    PASSWORD("Mật khẩu", "Password"),
    OTP("OTP", "One-time password"),
    SSO("SSO", "Single Sign-On");

    private final String nameVi;
    private final String nameEn;

    LoginMethod(String nameVi, String nameEn) {
        this.nameVi = nameVi;
        this.nameEn = nameEn;
    }

    @Override
    public String getNameVi() {
        return nameVi;
    }

    @Override
    public String getNameEn() {
        return nameEn;
    }
}

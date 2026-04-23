package com.thaca.auth.enums;

import com.thaca.common.validations.EnumRule;
import lombok.Getter;

@Getter
public enum DeviceType implements EnumRule {
    MOBILE("Thiết bị di động", "Mobile"),
    DESKTOP("Máy tính", "Desktop"),
    TABLET("Máy tính bảng", "Tablet"),
    UNKNOWN("Không xác định", "Unknown");

    private final String nameVi;
    private final String nameEn;

    DeviceType(String nameVi, String nameEn) {
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

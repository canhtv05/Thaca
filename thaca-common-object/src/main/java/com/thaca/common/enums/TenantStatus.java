package com.thaca.common.enums;

import lombok.Getter;

@Getter
public enum TenantStatus {
    ACTIVE("ACTIVE", "Hoạt động"),
    INACTIVE("INACTIVE", "Không hoạt động"),
    SUSPENDED("SUSPENDED", "Tạm khóa");

    private final String value;
    private final String description;

    TenantStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TenantStatus fromValue(String value) {
        if (value == null) return null;
        for (TenantStatus status : values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}

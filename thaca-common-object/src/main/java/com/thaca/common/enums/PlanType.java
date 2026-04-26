package com.thaca.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanType {
    FREE("FREE"),
    BASIC("BASIC"),
    PRO("PRO"),
    ENTERPRISE("ENTERPRISE");

    private final String value;
}

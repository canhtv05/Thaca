package com.thaca.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationChannel {
    EMAIL("EMAIL"),
    SMS("SMS"),
    PUSH("PUSH"),
    TELEGRAM("TELEGRAM");

    private final String value;
}

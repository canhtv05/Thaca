package com.thaca.common.constants;

import java.util.Map;

public class EventConstants {

    // Topics chuẩn Outbox (thaca_db.<schema>.<aggregate>)
    public static final String AUTH_USERS_TOPIC = "thaca_db.auth.users";
    public static final String AUTH_TENANTS_TOPIC = "thaca_db.auth.tenants";

    // Group IDs cho Consumer
    public static final String VERIFICATION_EMAIL_GROUP_ID = "verification-email-group-id";
    public static final String FORGOT_PASSWORD_GROUP_ID = "forgot-password-group-id";
    public static final String SEND_OTP_GROUP_ID = "send-otp-group-id";
    public static final String USER_CREATED_GROUP_ID = "user-created-group-id";
    public static final String TENANT_DELETED_GROUP_ID = "tenant-deleted-group-id";

    // Mapping DLT cho các topic chính
    public static final Map<String, String> DLT_TOPIC_MAP = Map.of(
        AUTH_USERS_TOPIC,
        AUTH_USERS_TOPIC + ".DLT",
        AUTH_TENANTS_TOPIC,
        AUTH_TENANTS_TOPIC + ".DLT"
    );
}

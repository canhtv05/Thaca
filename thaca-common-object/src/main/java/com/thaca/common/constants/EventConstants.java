package com.thaca.common.constants;

import java.util.Map;

public class EventConstants {

    public static final String VERIFICATION_EMAIL_TOPIC = "verification-email-topic";
    public static final String VERIFICATION_EMAIL_GROUP_ID = "verification-email-group-id";

    public static final String FORGOT_PASSWORD_TOPIC = "forgot-password-topic";
    public static final String FORGOT_PASSWORD_GROUP_ID = "forgot-password-group-id";

    public static final String USER_CREATED_TOPIC = "user-created-topic";
    public static final String USER_CREATED_GROUP_ID = "user-created-group-id";

    public static final String TENANT_DELETED_TOPIC = "tenant-deleted-topic";
    public static final String TENANT_DELETED_GROUP_ID = "tenant-deleted-group-id";

    public static final Map<String, String> DLT_TOPIC_MAP = Map.of(
        VERIFICATION_EMAIL_TOPIC,
        VERIFICATION_EMAIL_TOPIC + ".DLT",
        FORGOT_PASSWORD_TOPIC,
        FORGOT_PASSWORD_TOPIC + ".DLT",
        USER_CREATED_TOPIC,
        USER_CREATED_TOPIC + ".DLT",
        TENANT_DELETED_TOPIC,
        TENANT_DELETED_TOPIC + ".DLT"
    );
}

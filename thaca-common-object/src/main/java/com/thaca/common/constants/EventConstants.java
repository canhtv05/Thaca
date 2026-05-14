package com.thaca.common.constants;

public class EventConstants {

    public static final String AUTH_USERS_TOPIC = "thaca_db.auth.users";
    public static final String AUTH_TENANTS_TOPIC = "thaca_db.auth.tenants";

    public static String getDltTopic(String mainTopic) {
        return mainTopic + ".DLT";
    }
}

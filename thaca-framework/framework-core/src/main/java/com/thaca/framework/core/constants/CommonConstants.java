package com.thaca.framework.core.constants;

import java.util.Arrays;
import java.util.stream.Stream;

public class CommonConstants {

    private CommonConstants() {}

    public static final String[] AUTH_PUBLIC_ENDPOINTS = {
        "/login",
        "/create",
        "/forgot-password",
        "/reset-password",
        "/verify-forgot-password-otp"
    };
    public static final String[] NOTIFICATION_PUBLIC_ENDPOINTS = { "/verify-email", "/resend-verify-email" };
    public static final String[] PROFILE_PUBLIC_ENDPOINTS = { "/profile/**" };
    public static final String[] GRAPHQL_PUBLIC_ENDPOINTS = { "/graphiql/**", "/graphiql", "" };

    public static final String[] CMS_PUBLIC_ENDPOINTS = { "/login" };

    public static final String[] PREFIX_AUTH_PUBLIC_ENDPOINTS = Arrays.stream(AUTH_PUBLIC_ENDPOINTS)
        .map(res -> "/auth" + res)
        .toArray(String[]::new);

    public static final String[] PREFIX_CMS_PUBLIC_ENDPOINTS = Arrays.stream(CMS_PUBLIC_ENDPOINTS)
        .map(res -> "/cms" + res)
        .toArray(String[]::new);

    public static final String[] PREFIX_NOTIFICATION_PUBLIC_ENDPOINTS = Arrays.stream(NOTIFICATION_PUBLIC_ENDPOINTS)
        .map(res -> "/notifications" + res)
        .toArray(String[]::new);

    public static final String[] PREFIX_PROFILE_PUBLIC_ENDPOINTS = Arrays.stream(PROFILE_PUBLIC_ENDPOINTS)
        .map(res -> "/user-profile" + res)
        .toArray(String[]::new);

    public static final String[] PREFIX_GRAPHQL_PUBLIC_ENDPOINTS = Arrays.stream(GRAPHQL_PUBLIC_ENDPOINTS)
        .map(res -> "/graphql" + res)
        .toArray(String[]::new);

    public static final String[] PREFIX_PUBLIC_ENDPOINTS = Stream.of(
        PREFIX_AUTH_PUBLIC_ENDPOINTS,
        PREFIX_CMS_PUBLIC_ENDPOINTS,
        PREFIX_NOTIFICATION_PUBLIC_ENDPOINTS,
        PREFIX_PROFILE_PUBLIC_ENDPOINTS
    )
        .flatMap(Arrays::stream)
        .toArray(String[]::new);

    public static final String COOKIE_NAME = "THACA_COOKIE";
    public static final String CHANNEL_KEY = "channel";
    public static final String REDIS_ROLE_PERM_PREFIX = "auth:role-permissions:";
}

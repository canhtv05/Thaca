package com.thaca.framework.core.context;

public class FwServiceContext {

    private static final ThreadLocal<String> CURRENT_SERVICE_NAME = new ThreadLocal<>();

    public static void set(String name) {
        CURRENT_SERVICE_NAME.set(name);
    }

    public static String get() {
        return CURRENT_SERVICE_NAME.get();
    }

    public static void clear() {
        CURRENT_SERVICE_NAME.remove();
    }
}

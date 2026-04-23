package com.thaca.framework.core.context;

import com.thaca.framework.core.dtos.ApiBody;

public class FwContextBody {

    private static final ThreadLocal<ApiBody<?>> CONTEXT = new ThreadLocal<>();

    public static void set(ApiBody<?> header) {
        CONTEXT.set(header);
    }

    public static ApiBody<?> get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

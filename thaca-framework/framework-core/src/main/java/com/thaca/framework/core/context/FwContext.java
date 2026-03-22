package com.thaca.framework.core.context;

import com.thaca.common.dtos.ApiHeader;

public class FwContext {

    private static final ThreadLocal<ApiHeader> CONTEXT = new ThreadLocal<>();

    public static void set(ApiHeader header) {
        CONTEXT.set(header);
    }

    public static ApiHeader get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

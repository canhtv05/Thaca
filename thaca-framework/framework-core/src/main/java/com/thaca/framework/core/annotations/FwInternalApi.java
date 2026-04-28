package com.thaca.framework.core.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FwInternalApi {
    String path();

    String name();
}

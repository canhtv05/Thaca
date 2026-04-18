package com.thaca.framework.core.annotations;

import com.thaca.framework.core.enums.RequestType;
import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FwRequest {
    String name() default "";

    RequestType type() default RequestType.PUBLIC;
}

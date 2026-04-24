package com.thaca.framework.core.annotations;

import com.thaca.framework.core.enums.ModeType;
import java.lang.annotation.*;

/**
 * Check mode
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FwMode {
    String name() default "";

    ModeType type() default ModeType.HANDLE;
}

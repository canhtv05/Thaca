package com.thaca.framework.core.annotations;

import java.lang.annotation.*;

/**
 * Chỉ sử dụng trong Web Servlet
 * Used to exclude components from being scanned in Reactive (WebFlux)
 * applications.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServletOnly {}

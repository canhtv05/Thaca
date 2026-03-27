package com.thaca.framework.core.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@FunctionalInterface
public interface SecurityCustomizer {
    void customize(HttpSecurity httpSecurity) throws Exception;
}

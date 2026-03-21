package com.thaca.auth.security.jwt;

import com.thaca.framework.blocking.starter.filter.JwtFilter;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public record JWTConfigurer(JwtUtils jwtUtils) implements SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> {
    @Override
    public void init(HttpSecurity builder) {}

    @Override
    public void configure(HttpSecurity builder) {
        JwtFilter customFilter = new JwtFilter(jwtUtils);
        builder.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}

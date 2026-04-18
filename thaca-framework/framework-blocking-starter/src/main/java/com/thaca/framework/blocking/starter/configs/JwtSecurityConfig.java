package com.thaca.framework.blocking.starter.configs;

import com.thaca.framework.blocking.starter.filter.JwtFilter;
import com.thaca.framework.core.filter.FwFilter;
import com.thaca.framework.core.security.SecurityCustomizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig implements SecurityCustomizer {

    private final JwtFilter jwtFilter;
    private final FwFilter fwFilter;

    @Override
    public void customize(HttpSecurity http) {
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(fwFilter, JwtFilter.class);
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    }
}

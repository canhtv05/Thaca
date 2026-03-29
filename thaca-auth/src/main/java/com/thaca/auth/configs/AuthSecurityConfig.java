package com.thaca.auth.configs;

import com.thaca.auth.security.CustomAuthenticationProvider;
import com.thaca.auth.security.jwt.JWTConfigurer;
import com.thaca.auth.security.jwt.PermissionAuthorizationFilter;
import com.thaca.auth.services.PublicApiService;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.dtos.ApiEnvelope;
import com.thaca.framework.core.utils.JsonF;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class AuthSecurityConfig {

    private final CorsFilter corsFilter;
    private final JwtUtils jwtUtil;
    private final PublicApiService publicApiService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, PermissionAuthorizationFilter permissionAuthorizationFilter) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            )
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers(CommonConstants.AUTH_PUBLIC_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .apply(securityConfigurerAdapter());
        http.addFilterAfter(permissionAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(jwtUtil);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    PermissionAuthorizationFilter permissionAuthorizationFilter() {
        return new PermissionAuthorizationFilter(publicApiService);
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http, CustomAuthenticationProvider customProvider) {
        return http.getSharedObject(AuthenticationManagerBuilder.class).authenticationProvider(customProvider).build();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response
                .getWriter()
                .write(Objects.requireNonNull(JsonF.toJson(ApiEnvelope.error(CommonErrorMessage.UNAUTHORIZED))));
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response
                .getWriter()
                .write(Objects.requireNonNull(JsonF.toJson(ApiEnvelope.error(CommonErrorMessage.FORBIDDEN))));
        };
    }
}

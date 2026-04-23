package com.thaca.auth.configs;

import com.maxmind.geoip2.DatabaseReader;
import com.thaca.auth.security.CustomAuthenticationProvider;
import com.thaca.auth.security.jwt.JWTConfigurer;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import com.thaca.framework.core.dtos.ApiPayload;
import com.thaca.framework.core.utils.JsonF;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
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
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll())
            .apply(securityConfigurerAdapter());
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
    AuthenticationManager authenticationManager(HttpSecurity http, CustomAuthenticationProvider customProvider) {
        return http.getSharedObject(AuthenticationManagerBuilder.class).authenticationProvider(customProvider).build();
    }

    @Bean
    DatabaseReader cityDatabaseReader() {
        try (InputStream is = getClass().getResourceAsStream("/geoip/GeoLite2-City.mmdb")) {
            if (is == null) return null;
            return new DatabaseReader.Builder(is).build();
        } catch (Exception e) {
            return null;
        }
    }

    @Bean
    DatabaseReader anonymousIpDatabaseReader() {
        try (InputStream is = getClass().getResourceAsStream("/geoip/GeoIP2-Anonymous-IP.mmdb")) {
            if (is == null) return null;
            return new DatabaseReader.Builder(is).build();
        } catch (Exception e) {
            return null;
        }
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response
                .getWriter()
                .write(Objects.requireNonNull(JsonF.toJson(ApiPayload.error(CommonErrorMessage.UNAUTHORIZED))));
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response
                .getWriter()
                .write(Objects.requireNonNull(JsonF.toJson(ApiPayload.error(CommonErrorMessage.FORBIDDEN))));
        };
    }
}

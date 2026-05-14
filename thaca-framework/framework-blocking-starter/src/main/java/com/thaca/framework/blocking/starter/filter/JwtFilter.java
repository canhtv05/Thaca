package com.thaca.framework.blocking.starter.filter;

import com.thaca.common.enums.TokenStatus;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import com.thaca.framework.core.context.TenantContext;
import com.thaca.framework.core.utils.FwUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = resolveToken(request);
        if (StringUtils.hasText(jwt)) {
            try {
                TokenStatus status = jwtUtils.validateToken(jwt);
                if (TokenStatus.VALID.equals(status)) {
                    Authentication authentication = jwtUtils.getBasicAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    Claims claims = jwtUtils.parseToken(jwt);
                    Object tenantIdsObj = claims.get("tenantIds");
                    List<Long> tenantIds = FwUtils.extractTenantIds(tenantIdsObj);
                    if (!tenantIds.isEmpty()) {
                        TenantContext.set(tenantIds);
                    }
                } else {
                    log.debug("[JwtFilter] token validation failed: {}", status);
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                log.debug("[JwtFilter] authentication failed", e);
                SecurityContextHolder.clearContext();
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

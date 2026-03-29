package com.thaca.auth.security.jwt;

import com.thaca.auth.dtos.PermissionSelect;
import com.thaca.auth.security.CustomUserDetails;
import com.thaca.auth.services.PublicApiService;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@RequiredArgsConstructor
public class PermissionAuthorizationFilter extends GenericFilterBean {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final PublicApiService service;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new FwException(CommonErrorMessage.UNAUTHORIZED);
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        validatePermission(method, path, userDetails);

        filterChain.doFilter(request, response);
    }

    private void validatePermission(String method, String path, CustomUserDetails userDetails) {
        if (SecurityUtils.isGlobalAdmin()) {
            return;
        }

        if (path.contains("/auth/me/p/logout")) {
            return;
        }

        List<PermissionSelect> permissions = service.getPermissionSelect();
        AtomicBoolean hasPermission = new AtomicBoolean(false);

        userDetails
            .getAuthorities()
            .forEach(authority -> {
                PermissionSelect permission = permissions
                    .stream()
                    .filter(p -> p.code().equalsIgnoreCase(authority.getAuthority()))
                    .findFirst()
                    .orElse(null);

                if (permission != null && match(permission.method(), permission.pathPattern(), method, path)) {
                    hasPermission.set(true);
                }
            });

        if (!hasPermission.get()) {
            log.error("User {} does not have permission to access {} on {}", userDetails.getUsername(), method, path);
            throw new FwException(CommonErrorMessage.FORBIDDEN);
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String path = requestPath.contains("?") ? requestPath.substring(0, requestPath.indexOf("?")) : requestPath;

        return (
            Arrays.stream(CommonConstants.PREFIX_AUTH_PUBLIC_ENDPOINTS).anyMatch(
                endpoint -> path.equals(endpoint) || pathMatcher.match(endpoint, path)
            ) ||
            path.startsWith("/ws")
        );
    }

    public static boolean match(
        String permissionMethod,
        String permissionPath,
        String requestMethod,
        String requestPath
    ) {
        boolean methodMatch =
            permissionMethod == null || permissionMethod.isEmpty() || permissionMethod.equalsIgnoreCase(requestMethod);

        boolean pathMatch = pathMatcher.match(permissionPath, requestPath);

        return methodMatch && pathMatch;
    }
}

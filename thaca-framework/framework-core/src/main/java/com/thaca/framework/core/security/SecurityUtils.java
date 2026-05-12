package com.thaca.framework.core.security;

import com.thaca.framework.core.constants.AuthoritiesConstants;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtils {

    private SecurityUtils() {}

    public static String getCurrentUsername() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .map(SecurityUtils::extractPrincipal)
            .orElse("ANONYMOUS");
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return "ANONYMOUS";
        } else if (authentication.getPrincipal() instanceof UserPrincipal springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        } else if (Objects.nonNull(authentication.getName())) {
            return authentication.getName();
        }
        return "ANONYMOUS";
    }

    public static boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.isSuperAdmin();
        }
        return authentication
            .getAuthorities()
            .stream()
            .anyMatch(a -> Objects.equals(a.getAuthority(), AuthoritiesConstants.SUPER_ADMIN));
    }

    public static List<Long> getCurrentTenantIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyList();
        }
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getTenantIds();
        }
        return Collections.emptyList();
    }

    public static Long getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            if (userPrincipal.getTenantId() == null && !userPrincipal.getTenantIds().isEmpty()) {
                return userPrincipal.getTenantIds().getFirst();
            } else {
                return userPrincipal.getTenantId();
            }
        }
        return null;
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}

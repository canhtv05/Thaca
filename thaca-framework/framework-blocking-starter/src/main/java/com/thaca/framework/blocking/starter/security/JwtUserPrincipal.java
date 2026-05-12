package com.thaca.framework.blocking.starter.security;

import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

public final class JwtUserPrincipal implements UserPrincipal {

    private final String username;
    private final String role;
    private final String channel;
    private final boolean superAdmin;
    private final boolean cmsUser;
    private final List<Long> tenantIds;

    private JwtUserPrincipal(
        String username,
        String role,
        String channel,
        boolean superAdmin,
        boolean cmsUser,
        List<Long> tenantIds
    ) {
        this.username = username;
        this.role = role;
        this.channel = channel;
        this.superAdmin = superAdmin;
        this.cmsUser = cmsUser;
        this.tenantIds = tenantIds;
    }

    public static JwtUserPrincipal fromClaims(Claims claims, Collection<? extends GrantedAuthority> authorities) {
        String username = claims.getSubject();
        Object roleObj = claims.get(AuthoritiesConstants.ROLE_KEY);
        String role = roleObj != null ? roleObj.toString() : "";
        String channel = claims.get(CommonConstants.CHANNEL_KEY, String.class);
        Integer c = claims.get("c", Integer.class);
        List<Long> tenantIds = new ArrayList<>();
        Object tenantObjects = claims.get("tenantIds");
        if (tenantObjects instanceof Collection<?> col) {
            for (Object item : col) {
                if (item instanceof Number n) {
                    tenantIds.add(n.longValue());
                }
            }
        } else if (tenantObjects instanceof Number n) {
            tenantIds.add(n.longValue());
        }
        boolean superAdmin = authorities
            .stream()
            .anyMatch(a -> AuthoritiesConstants.SUPER_ADMIN.equals(a.getAuthority()));
        boolean cmsUser = c != null && c == 1;
        return new JwtUserPrincipal(username, role, channel, superAdmin, cmsUser, tenantIds);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public List<Long> getTenantIds() {
        return tenantIds;
    }

    @Override
    public boolean isSuperAdmin() {
        return superAdmin;
    }

    @Override
    public boolean isCmsUser() {
        return cmsUser;
    }
}

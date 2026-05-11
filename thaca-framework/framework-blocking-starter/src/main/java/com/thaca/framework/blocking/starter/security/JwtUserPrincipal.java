package com.thaca.framework.blocking.starter.security;

import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public final class JwtUserPrincipal implements UserPrincipal {

    private final String username;
    private final String role;
    private final String channel;
    private final boolean superAdmin;
    private final boolean cmsUser;
    private final Long tenantId;

    private JwtUserPrincipal(
        String username,
        String role,
        String channel,
        boolean superAdmin,
        boolean cmsUser,
        Long tenantId
    ) {
        this.username = username;
        this.role = role;
        this.channel = channel;
        this.superAdmin = superAdmin;
        this.cmsUser = cmsUser;
        this.tenantId = tenantId;
    }

    public static JwtUserPrincipal fromClaims(Claims claims, Collection<? extends GrantedAuthority> authorities) {
        String username = claims.getSubject();
        Object roleObj = claims.get(AuthoritiesConstants.ROLE_KEY);
        String role = roleObj != null ? roleObj.toString() : "";
        String channel = claims.get(CommonConstants.CHANNEL_KEY, String.class);
        Integer c = claims.get("c", Integer.class);
        Long tenantId = null;
        Object tid = claims.get("tenantId");
        if (tid instanceof Number n) {
            tenantId = n.longValue();
        }
        boolean superAdmin = authorities
            .stream()
            .anyMatch(a -> AuthoritiesConstants.SUPER_ADMIN.equals(a.getAuthority()));
        boolean cmsUser = c != null && c == 1;
        return new JwtUserPrincipal(username, role, channel, superAdmin, cmsUser, tenantId);
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
    public Long getTenantId() {
        return tenantId;
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

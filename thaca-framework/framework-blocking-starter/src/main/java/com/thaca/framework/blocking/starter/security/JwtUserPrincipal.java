package com.thaca.framework.blocking.starter.security;

import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.security.UserPrincipal;
import com.thaca.framework.core.utils.FwUtils;
import io.jsonwebtoken.Claims;
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
    private final Long tenantId;

    private JwtUserPrincipal(
        String username,
        String role,
        String channel,
        boolean superAdmin,
        boolean cmsUser,
        List<Long> tenantIds,
        Long tenantId
    ) {
        this.username = username;
        this.role = role;
        this.channel = channel;
        this.superAdmin = superAdmin;
        this.cmsUser = cmsUser;
        this.tenantIds = tenantIds;
        this.tenantId = tenantId;
    }

    public static JwtUserPrincipal fromClaims(Claims claims, Collection<? extends GrantedAuthority> authorities) {
        String username = claims.getSubject();
        Object roleObj = claims.get(AuthoritiesConstants.ROLE_KEY);
        String role = roleObj != null ? roleObj.toString() : "";
        String channel = claims.get(CommonConstants.CHANNEL_KEY, String.class);
        Integer c = claims.get("c", Integer.class);
        Object tenantObjects = claims.get("tenantIds");
        List<Long> tenantIds = FwUtils.extractTenantIds(tenantObjects);
        Long tenantId = claims.get("tid", Long.class);
        if (tenantId == null && !tenantIds.isEmpty()) {
            tenantId = tenantIds.getFirst();
        }
        boolean superAdmin = authorities
            .stream()
            .anyMatch(a -> AuthoritiesConstants.SUPER_ADMIN.equals(a.getAuthority()));
        boolean cmsUser = c != null && c == 1;
        return new JwtUserPrincipal(username, role, channel, superAdmin, cmsUser, tenantIds, tenantId);
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

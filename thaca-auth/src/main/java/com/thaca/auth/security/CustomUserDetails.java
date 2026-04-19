package com.thaca.auth.security;

import com.thaca.framework.core.security.UserPrincipal;
import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUserDetails extends User implements UserPrincipal {

    private final String role;
    private final String channel;
    private final boolean isSuperAdmin;
    private final boolean isCmsUser;

    public CustomUserDetails(
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        String roles,
        String channel,
        boolean isSuperAdmin,
        boolean isCmsUser
    ) {
        super(username, password, authorities);
        this.role = roles;
        this.channel = channel;
        this.isSuperAdmin = isSuperAdmin;
        this.isCmsUser = isCmsUser;
    }

    @Override
    public @NonNull String getUsername() {
        return super.getUsername();
    }

    @Override
    public String getRole() {
        return this.role;
    }

    @Override
    public String getChannel() {
        return this.channel;
    }

    @Override
    public boolean isSuperAdmin() {
        return this.isSuperAdmin;
    }

    @Override
    public boolean isCmsUser() {
        return this.isCmsUser;
    }
}

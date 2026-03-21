package com.thaca.auth.security;

import com.thaca.framework.core.security.UserPrincipal;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUserDetails extends User implements UserPrincipal {

    private final String role;
    private final boolean isAdmin;
    private final String channel;

    public CustomUserDetails(
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        String roles,
        boolean isAdmin,
        String channel
    ) {
        super(username, password, authorities);
        this.role = roles;
        this.isAdmin = isAdmin;
        this.channel = channel;
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public boolean isGlobal() {
        return this.isAdmin;
    }

    @Override
    public String getRole() {
        return this.role;
    }

    @Override
    public String getChannel() {
        return this.channel;
    }
}

package com.thaca.auth.security;

import com.thaca.framework.core.security.UserPrincipal;
import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUserDetails extends User implements UserPrincipal {

    private final String role;
    private final String channel;

    public CustomUserDetails(
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        String roles,
        String channel
    ) {
        super(username, password, authorities);
        this.role = roles;
        this.channel = channel;
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
}

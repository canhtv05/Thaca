package com.thaca.auth.security;

import com.thaca.auth.domains.Permission;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.SystemCredential;
import com.thaca.auth.domains.SystemCredentialPermission;
import com.thaca.auth.domains.SystemUser;
import com.thaca.auth.domains.User;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.services.AuthService;
import com.thaca.common.enums.PermissionEffect;
// import com.thaca.auth.services.KafkaProducerService;
// import com.thaca.common.dtos.events.VerificationEmailEvent;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.exceptions.FwException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {

    private final AuthService authService;

    // private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(final String login) {
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        Object userObj = authService.findOneByUsername(lowercaseLogin);
        if (userObj == null) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        return createSpringSecurityUser(userObj);
    }

    private CustomUserDetails createSpringSecurityUser(Object userObj) {
        String username;
        String password;
        boolean isLocked;
        boolean isActivated;
        boolean isSuperAdmin = false;
        List<Long> tenantIds = null;
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        String rolesString = AuthoritiesConstants.USER;

        if (userObj instanceof User user) {
            username = user.getUsername();
            password = user.getPassword();
            isLocked = user.getIsLocked();
            isActivated = user.getIsActivated();
            tenantIds = new java.util.ArrayList<>(user.getTenantIds());
            grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        } else if (userObj instanceof SystemCredential sc) {
            SystemUser su = sc.getSystemUser();
            username = sc.getUsername();
            password = sc.getPassword();
            isLocked = su.getIsLocked();
            isActivated = su.getIsActivated();
            tenantIds = new java.util.ArrayList<>(su.getTenantIds());
            isSuperAdmin = su.getIsSuperAdmin();

            Set<Role> roles = sc.getRoles();
            Set<SystemCredentialPermission> credentialPermissions = sc.getCredentialPermissions();

            Set<String> deniedPermissions = credentialPermissions
                .stream()
                .filter(cp -> cp.getEffect() == PermissionEffect.DENY)
                .map(cp -> cp.getPermission().getCode())
                .collect(Collectors.toSet());

            if (isSuperAdmin) {
                rolesString = AuthoritiesConstants.SUPER_ADMIN;
                grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.SUPER_ADMIN));
            } else {
                rolesString = roles.stream().map(Role::getCode).collect(Collectors.joining(","));
                for (Role role : roles) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(role.getCode()));
                    for (Permission perm : role.getPermissions()) {
                        if (!deniedPermissions.contains(perm.getCode())) {
                            grantedAuthorities.add(new SimpleGrantedAuthority(perm.getCode()));
                        }
                    }
                }
            }

            for (SystemCredentialPermission cp : credentialPermissions) {
                if (
                    cp.getEffect() == PermissionEffect.GRANT &&
                    !deniedPermissions.contains(cp.getPermission().getCode())
                ) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(cp.getPermission().getCode()));
                } else if (cp.getEffect() == PermissionEffect.DENY) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("DENY_" + cp.getPermission().getCode()));
                }
            }
        } else {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }

        if (isLocked) {
            throw new FwException(ErrorMessage.USER_LOCKED);
        }

        if (!isActivated) {
            throw new FwException(ErrorMessage.USER_NOT_ACTIVATED);
        }

        boolean adminUser = userObj instanceof SystemCredential;

        return new CustomUserDetails(
            username,
            password,
            grantedAuthorities,
            rolesString,
            StringUtils.defaultIfBlank(FwContextHeader.get().getChannel(), ChannelType.WEB.name()),
            isSuperAdmin,
            adminUser,
            tenantIds,
            (tenantIds != null && !tenantIds.isEmpty()) ? tenantIds.getFirst() : null
        );
    }
}

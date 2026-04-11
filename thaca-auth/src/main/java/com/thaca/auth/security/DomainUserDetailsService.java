package com.thaca.auth.security;

import com.thaca.auth.domains.User;
import com.thaca.auth.dtos.UserInfoDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.services.AuthService;
// import com.thaca.auth.services.KafkaProducerService;
// import com.thaca.common.dtos.events.VerificationEmailEvent;
import com.thaca.framework.core.context.FwContext;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.exceptions.FwException;
import java.util.List;
import java.util.Locale;
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
        return authService
            .findOneWithAuthoritiesByUsername(lowercaseLogin)
            .map(user -> createSpringSecurityUser(lowercaseLogin, user))
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    private CustomUserDetails createSpringSecurityUser(String lowercaseLogin, User user) {
        if (user.isLocked()) {
            throw new FwException(ErrorMessage.USER_LOCKED);
        }

        if (!user.getIsActivated()) {
            // VerificationEmailEvent event = new VerificationEmailEvent(user.getEmail(),
            // lowercaseLogin, null);
            throw new FwException(ErrorMessage.USER_NOT_ACTIVATED);
        }

        UserInfoDTO userInfoDTO = UserInfoDTO.fromEntity(user);

        List<GrantedAuthority> grantedAuthorities =
            userInfoDTO.getRoles() != null
                ? userInfoDTO
                      .getRoles()
                      .stream()
                      .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                      .collect(Collectors.toList())
                : List.of();

        return new CustomUserDetails(
            user.getUsername(),
            user.getPassword(),
            grantedAuthorities,
            String.join(",", userInfoDTO.getRoles()),
            StringUtils.defaultIfBlank(FwContext.get().getChannel(), ChannelType.WEB.name())
        );
    }
}

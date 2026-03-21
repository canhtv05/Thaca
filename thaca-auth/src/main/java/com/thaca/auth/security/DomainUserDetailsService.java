package com.thaca.auth.security;

import com.thaca.auth.context.AuthenticationContext;
import com.thaca.auth.domains.User;
import com.thaca.auth.dtos.UserProfileDTO;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.services.AuthService;
import com.thaca.auth.services.KafkaProducerService;
import com.thaca.common.constants.EventConstants;
import com.thaca.common.dtos.events.VerificationEmailEvent;
import com.thaca.framework.core.exceptions.FwException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String login) {
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

        if (!user.isActivated()) {
            VerificationEmailEvent event = new VerificationEmailEvent(user.getEmail(), lowercaseLogin, null);
            kafkaProducerService.send(EventConstants.VERIFICATION_EMAIL_TOPIC, event);
            throw new FwException(ErrorMessage.USER_NOT_ACTIVATED);
        }

        UserProfileDTO userProfileDTO = UserProfileDTO.fromEntity(user);
        authService.mappingUserPermissions(userProfileDTO, user);
        List<GrantedAuthority> grantedAuthorities = userProfileDTO
            .getPermissions()
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        return new CustomUserDetails(
            user.getUsername(),
            user.getPassword(),
            grantedAuthorities,
            String.join(",", userProfileDTO.getRoles()),
            user.getIsGlobal(),
            AuthenticationContext.getChannel()
        );
    }
}

package com.thaca.framework.reactive.starter.utils;

import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final FrameworkProperties frameworkProperties;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(frameworkProperties.getSecurity().getBase64Secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Mono<Claims> parseToken(String token) {
        return Mono.fromCallable(() ->
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload()
        );
    }

    public Mono<Authentication> getBasicAuthentication(String token) {
        return this.parseToken(token).map(claims -> {
            Collection<? extends GrantedAuthority> authorities = Arrays.stream(
                claims.get(AuthoritiesConstants.AUTHORITIES_KEY).toString().split(",")
            )
                .filter(auth -> !auth.trim().isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
        });
    }
}

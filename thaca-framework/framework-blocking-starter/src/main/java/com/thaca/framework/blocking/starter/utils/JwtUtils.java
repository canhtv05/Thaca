package com.thaca.framework.blocking.starter.utils;

import com.thaca.common.enums.TokenStatus;
import com.thaca.framework.blocking.starter.services.UserSessionService;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.utils.FwUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final FrameworkProperties frameworkProperties;
    private final UserSessionService userSessionService;

    public SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(frameworkProperties.getSecurity().getBase64Secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public Authentication getBasicAuthentication(String token) {
        Claims claims = this.parseToken(token);
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(
            claims.get(AuthoritiesConstants.AUTHORITIES_KEY).toString().split(",")
        )
            .filter(auth -> !auth.trim().isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
    }

    public TokenStatus validateToken(String authToken) {
        try {
            Claims claims = parseToken(authToken);
            String username = claims.getSubject();
            String channel = claims.get(CommonConstants.CHANNEL_KEY, String.class);
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return TokenStatus.EXPIRED;
            }
            String tokenExisting = userSessionService.getOldToken(username, ChannelType.valueOf(channel));
            if (Objects.equals(tokenExisting, FwUtils.hexString(authToken))) {
                return TokenStatus.VALID;
            } else {
                return TokenStatus.INVALID;
            }
        } catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            return TokenStatus.INVALID;
        }
    }
}

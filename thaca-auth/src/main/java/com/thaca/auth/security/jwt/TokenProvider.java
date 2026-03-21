package com.thaca.auth.security.jwt;

import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.User;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.security.CustomUserDetails;
import com.thaca.common.dtos.UserSession;
import com.thaca.common.enums.AuthKey;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.WsType;
import com.thaca.common.socket.WsMessage;
import com.thaca.common.socket.WsSessionRevokedMessage;
import com.thaca.framework.blocking.starter.services.RedisPubService;
import com.thaca.framework.blocking.starter.services.UserSessionService;
import com.thaca.framework.core.config.FrameworkProperties;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.CookieUtils;
import com.thaca.framework.core.utils.FwUtils;
import com.thaca.framework.core.utils.JsonF;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String ROLES_KEY = "role";
    private static final String USER_GLOBAL_KEY = "isGlobal";
    private final SecretKey key;
    private final JwtParser jwtParser;
    private final UserRepository userRepository;
    private final Long tokenValidityDuration;
    private final Long refreshTokenValidityDuration;
    private final RedisPubService redisPubService;
    private final UserSessionService userSessionService;
    private final CookieUtils cookieUtils;

    public TokenProvider(
        UserRepository userRepository,
        FrameworkProperties frameworkProperties,
        RedisPubService redisPubService,
        UserSessionService userSessionService,
        CookieUtils cookieUtils
    ) {
        this.userRepository = userRepository;
        String secret = frameworkProperties.getSecurity().getBase64Secret();
        this.redisPubService = redisPubService;
        this.userSessionService = userSessionService;
        this.cookieUtils = cookieUtils;
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parser().verifyWith(key).build();
        this.tokenValidityDuration = frameworkProperties.getSecurity().getValidDurationInSeconds();
        this.refreshTokenValidityDuration = frameworkProperties.getSecurity().getRefreshDurationInSeconds();
    }

    public String createToken(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response,
        String channel
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String name = userDetails.getUsername();
        String oldToken = userSessionService.isUserOnline(name, channel);
        String sessionId = UUID.randomUUID().toString();

        String token = this.generateToken(authentication, this.tokenValidityDuration, channel, sessionId);
        if (StringUtils.isNotBlank(oldToken)) {
            Thread.startVirtualThread(() -> {
                this.handleKickOldSessionAsync(name, token, oldToken, channel);
            });
        }
        String refreshToken = this.generateToken(authentication, this.refreshTokenValidityDuration, channel, sessionId);
        this.cacheUserToken(name, channel, sessionId, token);

        Cookie cookie = cookieUtils.setTokenCookie(token, refreshToken);
        response.addCookie(cookie);

        User user = userRepository.findByUsername(name).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return token;
    }

    public RefreshTokenRes refreshToken(
        String cookieValue,
        HttpServletRequest request,
        HttpServletResponse response,
        String channel
    ) {
        @SuppressWarnings("unchecked")
        Map<String, String> tokenData = JsonF.jsonToObject(cookieValue, Map.class);
        String refreshToken = "";

        if (Objects.nonNull(tokenData)) {
            refreshToken = tokenData.get(AuthKey.REFRESH_TOKEN.getKey());
        }

        if (StringUtils.isBlank(refreshToken)) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
        }

        if (StringUtils.isBlank(refreshToken)) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }

        Claims claims = jwtParser.parseSignedClaims(refreshToken).getPayload();
        String username = claims.getSubject();
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));

        if (!Objects.equals(user.getRefreshToken(), refreshToken) || StringUtils.isBlank(user.getRefreshToken())) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }

        Collection<? extends GrantedAuthority> authorities = user
            .getRoles()
            .stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());

        String rolesStr = user.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));

        CustomUserDetails userDetails = new CustomUserDetails(
            user.getUsername(),
            user.getPassword(),
            authorities,
            rolesStr,
            user.getIsGlobal(),
            channel
        );

        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        String sessionId = UUID.randomUUID().toString();
        String newToken = this.generateToken(newAuthentication, this.tokenValidityDuration, channel, sessionId);
        String newRefreshToken = this.generateToken(
            newAuthentication,
            this.refreshTokenValidityDuration,
            channel,
            sessionId
        );
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
        this.cacheUserToken(username, channel, sessionId, newToken);

        Cookie cookie = cookieUtils.setTokenCookie(newToken, newRefreshToken);
        response.addCookie(cookie);

        return RefreshTokenRes.builder().accessToken(newToken).refreshToken(newRefreshToken).build();
    }

    @Transactional(readOnly = true)
    public RefreshTokenRes processRefreshInternal(String refreshToken, String channel) {
        if (CommonUtils.isEmpty(refreshToken)) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
        if (CommonUtils.isEmpty(channel)) {
            throw new FwException(ErrorMessage.CHANNEL_INVALID);
        }

        try {
            Claims claims = jwtParser.parseSignedClaims(refreshToken).getPayload();
            String username = claims.getSubject();
            String id = claims.getId();

            User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));

            if (!Objects.equals(user.getRefreshToken(), refreshToken) || StringUtils.isBlank(user.getRefreshToken())) {
                throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
            }

            Collection<? extends GrantedAuthority> authorities = user
                .getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

            String rolesStr = user.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));

            CustomUserDetails userDetails = new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                authorities,
                rolesStr,
                user.getIsGlobal(),
                channel
            );

            Authentication newAuthentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            String newToken = this.generateToken(newAuthentication, this.tokenValidityDuration, channel, id);
            String newRefreshToken = this.generateToken(
                newAuthentication,
                this.refreshTokenValidityDuration,
                channel,
                id
            );
            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);
            this.cacheUserToken(username, channel, id, newToken);

            return RefreshTokenRes.builder().accessToken(newToken).refreshToken(newRefreshToken).build();
        } catch (JwtException e) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        } catch (FwException e) {
            throw e;
        } catch (Exception e) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(
            claims.get(AUTHORITIES_KEY).toString().split(",")
        )
            .filter(auth -> !auth.trim().isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        CustomUserDetails principal = new CustomUserDetails(
            claims.getSubject(),
            "",
            authorities,
            claims.get(ROLES_KEY, String.class),
            claims.get(USER_GLOBAL_KEY, Boolean.class),
            claims.get(CommonConstants.CHANNEL_KEY, String.class)
        );
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public void revokeToken(String token, String channel) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow(() ->
            new FwException(CommonErrorMessage.UNAUTHORIZED)
        );
        if (StringUtils.isNotBlank(username)) {
            User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
            user.setRefreshToken(null);
            userRepository.save(user);
            Thread.startVirtualThread(() -> {
                try {
                    userSessionService.removeOldSessionByChanelType(username, channel);
                } catch (Exception e) {
                    log.error("Async evict redis failed", e);
                }
            });
        }
    }

    public void revokeAllTokens(String token) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow(() ->
            new FwException(CommonErrorMessage.UNAUTHORIZED)
        );
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setRefreshToken(null);
        userRepository.save(user);
        // to sent notification to all devices
        Thread.startVirtualThread(() -> {
            userSessionService.removeOldSessionByChanelType(username, "web");
            userSessionService.removeOldSessionByChanelType(username, "mobile");
        });
    }

    private String generateToken(
        Authentication authentication,
        long validityTimeInSeconds,
        String channel,
        String sessionId
    ) {
        String authorities = authentication
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        long now = System.currentTimeMillis();
        long validityMillis = validityTimeInSeconds * 1000L;
        Date validity = new Date(now + validityMillis);

        return Jwts.builder()
            .id(sessionId)
            .subject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .claim(ROLES_KEY, String.join(",", userDetails.getRole()))
            .claim(USER_GLOBAL_KEY, userDetails.isGlobal())
            .claim(CommonConstants.CHANNEL_KEY, channel)
            .issuedAt(new Date(now))
            .expiration(validity)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    private void cacheUserToken(String username, String channel, String sessionId, String token) {
        UserSession userSessionDTO = UserSession.builder()
            .username(username)
            .sessionId(sessionId)
            .channel(channel)
            .secretKey(FwUtils.generateSecretKey())
            .build();
        userSessionService.cacheUserSession(userSessionDTO);
        userSessionService.cacheToken(username, channel, token);
    }

    public void handleKickOldSessionAsync(String userId, String newToken, String oldToken, String channelType) {
        WsSessionRevokedMessage payload = WsSessionRevokedMessage.builder()
            .tokenSessionValid(newToken)
            .tokenSessionCurrent(oldToken)
            .channelType(channelType)
            .build();
        WsMessage wsMessage = WsMessage.builder().type(WsType.KICK).userId(userId).data(JsonF.toJson(payload)).build();
        redisPubService.publish(wsMessage);
    }
}

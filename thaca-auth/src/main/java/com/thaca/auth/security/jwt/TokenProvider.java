package com.thaca.auth.security.jwt;

import static com.thaca.framework.core.constants.AuthoritiesConstants.ROLE_KEY;

import com.thaca.auth.domains.SystemCredential;
import com.thaca.auth.domains.User;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.security.CustomUserDetails;
import com.thaca.auth.services.AuthService;
import com.thaca.common.dtos.UserSession;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.PermissionEffect;
import com.thaca.common.enums.WsType;
import com.thaca.common.socket.WsMessage;
import com.thaca.common.socket.WsSessionRevokedMessage;
import com.thaca.framework.blocking.starter.services.RedisPubService;
import com.thaca.framework.blocking.starter.services.UserSessionService;
import com.thaca.framework.core.configs.FrameworkProperties;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.constants.CommonConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.FwUtils;
import com.thaca.framework.core.utils.JsonF;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class TokenProvider {

    private final SecretKey key;
    private final JwtParser jwtParser;
    private final Long tokenValidityDuration;
    private final Long adminTokenValidityDuration;
    private final Long refreshTokenValidityDuration;
    private final RedisPubService redisPubService;
    private final UserSessionService userSessionService;
    private final AuthService authService;
    private final String cookieDomain;

    public TokenProvider(
        FrameworkProperties frameworkProperties,
        RedisPubService redisPubService,
        UserSessionService userSessionService,
        @Lazy AuthService authService
    ) {
        String secret = frameworkProperties.getSecurity().getBase64Secret();
        this.redisPubService = redisPubService;
        this.userSessionService = userSessionService;
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parser().verifyWith(key).build();
        this.authService = authService;
        this.tokenValidityDuration = frameworkProperties.getSecurity().getValidDurationInSeconds();
        this.adminTokenValidityDuration = frameworkProperties.getSecurity().getAdminValidDurationInSeconds();
        this.refreshTokenValidityDuration = frameworkProperties.getSecurity().getRefreshDurationInSeconds();
        this.cookieDomain = frameworkProperties.getSecurity().getCookieDomain();
    }

    public String createToken(Authentication authentication, HttpServletResponse response) {
        ChannelType channel =
            FwContextHeader.get() != null ? ChannelType.valueOf(FwContextHeader.get().getChannel()) : null;
        if (channel == null) {
            throw new FwException(CommonErrorMessage.CHANNEL_INVALID);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (userDetails == null) {
            throw new FwException(CommonErrorMessage.UNAUTHORIZED);
        }
        String name = userDetails.getUsername();

        // c = 1 là admin, c = 0 là User
        int c = userDetails.isAdminUser() ? 1 : 0;

        String oldToken = userSessionService.isUserOnline(name, channel);
        String sessionId = UUID.randomUUID().toString();

        long validity = (c == 1) ? this.adminTokenValidityDuration : this.tokenValidityDuration;
        String token = this.generateAccessToken(authentication, validity, channel, sessionId);

        if (StringUtils.isNotBlank(oldToken)) {
            Thread.startVirtualThread(() -> this.handleKickOldSessionAsync(name, token, oldToken, channel));
        }
        if (c == 0) {
            String refreshToken = this.generateRefreshToken(name, userDetails.getTenantId(), sessionId);
            setRefreshCookie(response, refreshToken);
        }
        this.cacheUserToken(name, channel, sessionId, token);
        return token;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenRes refreshToken(HttpServletRequest request, HttpServletResponse response, String channel) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        RefreshTokenRes res = this.doRefresh(refreshToken, ChannelType.valueOf(channel));
        setRefreshCookie(response, res.getRefreshToken());
        return RefreshTokenRes.builder().accessToken(res.getAccessToken()).build();
    }

    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenRes processRefreshInternal(String refreshToken, ChannelType channel) {
        return this.doRefresh(refreshToken, channel);
    }

    public void revokeToken(ChannelType channel, HttpServletResponse response) {
        String username = SecurityUtils.getCurrentUsername();
        if (StringUtils.isNotBlank(username)) {
            deleteRefreshCookie(response);
            Thread.startVirtualThread(() -> {
                try {
                    userSessionService.removeOldSessionByChanelType(username, channel);
                } catch (Exception e) {
                    log.error("Async evict redis failed", e);
                }
            });
        }
    }

    public void revokeAllTokens(HttpServletResponse response) {
        String username = SecurityUtils.getCurrentUsername();
        deleteRefreshCookie(response);
        Thread.startVirtualThread(() -> {
            userSessionService.removeOldSessionByChanelType(username, ChannelType.WEB);
            userSessionService.removeOldSessionByChanelType(username, ChannelType.MOBILE);
        });
    }

    private String generateAccessToken(
        Authentication authentication,
        long validityTimeInSeconds,
        ChannelType channel,
        String sessionId
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        long now = System.currentTimeMillis();
        long validityMillis = validityTimeInSeconds * 1000L;
        Date validity = new Date(now + validityMillis);
        if (userDetails == null) throw new FwException(CommonErrorMessage.UNAUTHORIZED);
        return Jwts.builder()
            .id(sessionId)
            .subject(authentication.getName())
            .claim(
                ROLE_KEY,
                authentication
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","))
            )
            .claim(CommonConstants.CHANNEL_KEY, channel)
            .claim("c", userDetails.isAdminUser() ? 1 : 0)
            .claim("tenantIds", userDetails.getTenantIds())
            .claim("tid", userDetails.getTenantId())
            .issuedAt(new Date(now))
            .expiration(validity)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    private String generateRefreshToken(String username, Long tenantId, String sessionId) {
        long now = System.currentTimeMillis();
        long validityMillis = this.refreshTokenValidityDuration * 1000L;
        Date validity = new Date(now + validityMillis);
        return Jwts.builder()
            .id(sessionId)
            .subject(username)
            .claim("tid", tenantId)
            .claim("type", "refresh")
            .issuedAt(new Date(now))
            .expiration(validity)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(CommonConstants.REFRESH_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // false for localhost, true for production
        cookie.setPath("/");
        cookie.setMaxAge(this.refreshTokenValidityDuration.intValue());
        if (org.springframework.util.StringUtils.hasText(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    public void deleteRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(CommonConstants.REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if (org.springframework.util.StringUtils.hasText(cookieDomain)) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (CommonConstants.REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
    }

    private void cacheUserToken(String username, ChannelType channel, String sessionId, String token) {
        UserSession userSessionDTO = UserSession.builder()
            .username(username)
            .sessionId(sessionId)
            .channel(channel.name())
            .secretKey(FwUtils.generateSecretKey())
            .build();
        userSessionService.cacheUserSession(userSessionDTO);
        userSessionService.cacheToken(username, channel, token);
    }

    public void handleKickOldSessionAsync(String userId, String newToken, String oldToken, ChannelType channelType) {
        WsSessionRevokedMessage payload = WsSessionRevokedMessage.builder()
            .tokenSessionValid(newToken)
            .tokenSessionCurrent(oldToken)
            .channelType(channelType.name())
            .build();
        WsMessage wsMessage = WsMessage.builder().type(WsType.KICK).userId(userId).data(JsonF.toJson(payload)).build();
        redisPubService.publish(wsMessage);
    }

    private RefreshTokenRes doRefresh(String refreshToken, ChannelType channel) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
        try {
            Claims claims = jwtParser.parseSignedClaims(refreshToken).getPayload();
            String username = claims.getSubject();
            String sessionId = UUID.randomUUID().toString();

            // Refresh token must have type=refresh
            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
            }
            Long tenantId = claims.get("tid", Long.class);
            Object userObj = authService.findOneByUsername(username);
            if (userObj == null) {
                throw new FwException(ErrorMessage.USER_NOT_FOUND);
            }
            // Admin users cannot refresh
            if (userObj instanceof SystemCredential) {
                throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
            }
            Authentication authentication = getAuthentication(channel, userObj);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails == null) throw new FwException(CommonErrorMessage.UNAUTHORIZED);

            String newAccessToken = generateAccessToken(authentication, tokenValidityDuration, channel, sessionId);
            String newRefreshToken = generateRefreshToken(username, tenantId, sessionId);

            cacheUserToken(username, channel, sessionId, newAccessToken);
            return RefreshTokenRes.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
        } catch (SignatureException e) {
            throw new FwException(CommonErrorMessage.UNAUTHORIZED);
        } catch (JwtException e) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
    }

    private static Authentication getAuthentication(ChannelType channel, Object userObj) {
        String username;
        String password;
        String rolesString = AuthoritiesConstants.USER;
        boolean isSuperAdmin = false;
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<Long> tenantIds = null;
        Long tenantId = null;

        if (userObj instanceof User u) {
            username = u.getUsername();
            password = u.getPassword();
            if (u.getTenantIds() != null && !u.getTenantIds().isEmpty()) {
                tenantIds = new ArrayList<>(u.getTenantIds());
                tenantId = tenantIds.get(0);
            }
            authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        } else if (userObj instanceof SystemCredential sc) {
            username = sc.getUsername();
            password = sc.getPassword();
            isSuperAdmin = sc.getSystemUser().getIsSuperAdmin();
            if (sc.getSystemUser().getTenantIds() != null && !sc.getSystemUser().getTenantIds().isEmpty()) {
                tenantIds = new ArrayList<>(sc.getSystemUser().getTenantIds());
                tenantId = tenantIds.get(0);
            }
            if (isSuperAdmin) {
                rolesString = AuthoritiesConstants.SUPER_ADMIN;
                authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.SUPER_ADMIN));
            } else {
                rolesString = AuthService.getRoleString(sc, authorities);
                sc
                    .getCredentialPermissions()
                    .stream()
                    .filter(cp -> PermissionEffect.DENY.equals(cp.getEffect()))
                    .map(cp -> cp.getPermission().getCode())
                    .forEach(code -> authorities.add(new SimpleGrantedAuthority("DENY_" + code)));
            }
        } else {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }

        boolean adminUser = userObj instanceof SystemCredential;
        CustomUserDetails userDetails = new CustomUserDetails(
            username,
            password,
            authorities,
            rolesString,
            channel.name(),
            isSuperAdmin,
            adminUser,
            tenantIds,
            tenantId
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}

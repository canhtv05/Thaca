package com.thaca.auth.security.jwt;

import static com.thaca.framework.core.constants.AuthoritiesConstants.ROLE_KEY;

import com.thaca.auth.domains.SystemCredential;
import com.thaca.auth.domains.User;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.security.CustomUserDetails;
import com.thaca.auth.services.AuthService;
import com.thaca.common.dtos.UserSession;
import com.thaca.common.enums.AuthKey;
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
import com.thaca.framework.core.utils.CookieUtils;
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

    private final SecretKey key;
    private final JwtParser jwtParser;
    private final UserRepository userRepository;
    private final Long tokenValidityDuration;
    private final Long cmsTokenValidityDuration;
    private final Long refreshTokenValidityDuration;
    private final RedisPubService redisPubService;
    private final UserSessionService userSessionService;
    private final CookieUtils cookieUtils;
    private final AuthService authService;

    public TokenProvider(
        UserRepository userRepository,
        FrameworkProperties frameworkProperties,
        RedisPubService redisPubService,
        UserSessionService userSessionService,
        @Lazy AuthService authService,
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
        this.authService = authService;
        this.tokenValidityDuration = frameworkProperties.getSecurity().getValidDurationInSeconds();
        this.cmsTokenValidityDuration = frameworkProperties.getSecurity().getCmsValidDurationInSeconds();
        this.refreshTokenValidityDuration = frameworkProperties.getSecurity().getRefreshDurationInSeconds();
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

        // c = 1 là CMS, c = 0 là User
        int c = userDetails.isCmsUser() ? 1 : 0;

        String oldToken = userSessionService.isUserOnline(name, channel);
        String sessionId = UUID.randomUUID().toString();

        long validity = (c == 1) ? this.cmsTokenValidityDuration : this.tokenValidityDuration;
        String token = this.generateToken(authentication, validity, channel, sessionId);

        if (StringUtils.isNotBlank(oldToken)) {
            Thread.startVirtualThread(() -> this.handleKickOldSessionAsync(name, token, oldToken, channel));
        }

        // For CMS/Admin (c=1), refreshToken is null. For others (c=0), check channel.
        String refreshToken = null;
        if (c == 0 && channel != ChannelType.WEB) {
            refreshToken = this.generateToken(authentication, this.refreshTokenValidityDuration, channel, sessionId);
        }

        this.cacheUserToken(name, channel, sessionId, token);

        Cookie cookie = cookieUtils.setTokenCookie(token, refreshToken);
        response.addCookie(cookie);

        if (refreshToken != null) {
            Optional<User> user = userRepository.findByUsername(name);
            if (user.isPresent()) {
                user.get().setRefreshToken(FwUtils.hexString(refreshToken));
                userRepository.save(user.get());
            }
        }
        return token;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenRes refreshToken(
        String cookieValue,
        HttpServletRequest request,
        HttpServletResponse response,
        String channel
    ) {
        String refreshToken = this.extractRefreshToken(cookieValue, request);
        RefreshTokenRes res = this.doRefresh(refreshToken, ChannelType.valueOf(channel), true);
        Cookie cookie = cookieUtils.setTokenCookie(res.getAccessToken(), res.getRefreshToken());
        response.addCookie(cookie);
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenRes processRefreshInternal(String refreshToken, ChannelType channel) {
        return this.doRefresh(refreshToken, channel, false);
    }

    public void revokeToken(ChannelType channel) {
        String username = SecurityUtils.getCurrentUsername();
        if (StringUtils.isNotBlank(username)) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                user.get().setRefreshToken(null);
                userRepository.save(user.get());
            }
            Thread.startVirtualThread(() -> {
                try {
                    userSessionService.removeOldSessionByChanelType(username, channel);
                } catch (Exception e) {
                    log.error("Async evict redis failed", e);
                }
            });
        }
    }

    public void revokeAllTokens() {
        String username = SecurityUtils.getCurrentUsername();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            user.get().setRefreshToken(null);
            userRepository.save(user.get());
        }
        // to sent notification to all devices
        Thread.startVirtualThread(() -> {
            userSessionService.removeOldSessionByChanelType(username, ChannelType.WEB);
            userSessionService.removeOldSessionByChanelType(username, ChannelType.MOBILE);
        });
    }

    private String generateToken(
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
            .claim("c", userDetails.isCmsUser() ? 1 : 0)
            .claim("tenantIds", userDetails.getTenantIds())
            .issuedAt(new Date(now))
            .expiration(validity)
            .signWith(key, Jwts.SIG.HS512)
            .compact();
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

    private RefreshTokenRes doRefresh(String refreshToken, ChannelType channel, boolean isGenerateNewSession) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
        try {
            Claims claims = jwtParser.parseSignedClaims(refreshToken).getPayload();
            String username = claims.getSubject();
            String sessionId = isGenerateNewSession ? UUID.randomUUID().toString() : claims.getId();

            // Read claim c (1: CMS, 0: User)
            Integer cClaim = claims.get("c", Integer.class);
            if (cClaim != null && cClaim == 1) {
                throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
            }

            Object userObj = authService.findOneByUsername(username);
            if (userObj == null) {
                throw new FwException(ErrorMessage.USER_NOT_FOUND);
            }

            String userRefreshToken = null;
            if (userObj instanceof User u) {
                userRefreshToken = u.getRefreshToken();
            }

            if (
                StringUtils.isBlank(userRefreshToken) ||
                !Objects.equals(userRefreshToken, FwUtils.hexString(refreshToken))
            ) {
                throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
            }

            Authentication authentication = getAuthentication(channel, userObj);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails == null) throw new FwException(CommonErrorMessage.UNAUTHORIZED);

            String newAccessToken = generateToken(authentication, tokenValidityDuration, channel, sessionId);
            String newRefreshToken = generateToken(authentication, refreshTokenValidityDuration, channel, sessionId);

            User u = (User) userObj;
            u.setRefreshToken(FwUtils.hexString(newRefreshToken));
            userRepository.save(u);

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
        if (userObj instanceof User u) {
            username = u.getUsername();
            password = u.getPassword();
            tenantIds = u.getTenants().stream().map(com.thaca.auth.domains.Tenant::getId).toList();
            authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        } else if (userObj instanceof SystemCredential sc) {
            username = sc.getUsername();
            password = sc.getPassword();
            isSuperAdmin = sc.getSystemUser().getIsSuperAdmin();
            tenantIds = sc.getSystemUser().getTenants().stream().map(com.thaca.auth.domains.Tenant::getId).toList();
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

        boolean cmsUser = userObj instanceof SystemCredential;
        CustomUserDetails userDetails = new CustomUserDetails(
            username,
            password,
            authorities,
            rolesString,
            channel.name(),
            isSuperAdmin,
            cmsUser,
            tenantIds
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    private String extractRefreshToken(String cookieValue, HttpServletRequest request) {
        String refreshToken = null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> tokenData = JsonF.jsonToObject(cookieValue, Map.class);
            if (Objects.nonNull(tokenData)) {
                refreshToken = tokenData.get(AuthKey.REFRESH_TOKEN.getKey());
            }
        } catch (Exception ignored) {}
        if (StringUtils.isBlank(refreshToken)) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
        }
        if (StringUtils.isBlank(refreshToken)) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
        return refreshToken;
    }
}

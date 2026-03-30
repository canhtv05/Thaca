package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.Permission;
import com.thaca.auth.domains.User;
import com.thaca.auth.domains.UserPermission;
import com.thaca.auth.dtos.UserProfileDTO;
import com.thaca.auth.dtos.req.LoginReq;
import com.thaca.auth.dtos.res.AuthenticateRes;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.dtos.res.VerifyTokenRes;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.enums.PermissionAction;
import com.thaca.auth.repositories.UserPermissionRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.security.jwt.TokenProvider;
import com.thaca.common.dtos.TokenPair;
import com.thaca.common.enums.AuthKey;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.TokenStatus;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.CookieUtils;
import com.thaca.framework.core.utils.JsonF;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.json.JsonParseException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AuthService {

    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final JwtUtils jwtUtils;

    @FwMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = ModeType.VALIDATE)
    public void validateAuthenticate(LoginReq loginReq, HttpServletResponse httpServletResponse) {
        if (StringUtils.isBlank(loginReq.getUsername()) || StringUtils.isBlank(loginReq.getPassword())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = ModeType.HANDLE)
    public AuthenticateRes authenticate(LoginReq loginReq, HttpServletResponse httpServletResponse) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginReq.getUsername(),
            loginReq.getPassword()
        );

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.createToken(authentication, httpServletResponse);
        if (StringUtils.isBlank(token)) {
            throw new FwException(ErrorMessage.ACCESS_TOKEN_INVALID);
        }
        return new AuthenticateRes(true);
    }

    @FwMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = ModeType.VALIDATE)
    public void validateRefreshToken(
        String cookieValue,
        String channel,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (StringUtils.isBlank(cookieValue) || StringUtils.isBlank(channel)) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (!ChannelType.MOBILE.name().equals(channel) && !ChannelType.WEB.name().equals(channel)) {
            throw new FwException(CommonErrorMessage.CHANNEL_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = ModeType.HANDLE)
    public RefreshTokenRes refreshToken(
        String cookieValue,
        String channel,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        return tokenProvider.refreshToken(cookieValue, httpServletRequest, httpServletResponse, channel);
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.AUTH_VERIFY_TOKEN, type = ModeType.HANDLE)
    public VerifyTokenRes verifyTokenInternal(String accessToken, String refreshToken, ChannelType channel) {
        TokenStatus valid = jwtUtils.validateToken(accessToken);
        if (TokenStatus.VALID.equals(valid)) {
            return VerifyTokenRes.builder().valid(true).accessToken(accessToken).refreshToken(refreshToken).build();
        }

        if (StringUtils.isBlank(refreshToken)) {
            throw new FwException(ErrorMessage.REFRESH_TOKEN_INVALID);
        }
        RefreshTokenRes refreshTokenRes = tokenProvider.processRefreshInternal(refreshToken, channel);
        if (Objects.isNull(refreshTokenRes)) {
            throw new FwException(ErrorMessage.TOKEN_PAIR_INVALID);
        }
        return VerifyTokenRes.builder()
            .valid(true)
            .accessToken(refreshTokenRes.getAccessToken())
            .refreshToken(refreshTokenRes.getRefreshToken())
            .build();
    }

    @Transactional
    @FwMode(name = ServiceMethod.AUTH_LOGOUT, type = ModeType.HANDLE)
    public void logout(String channel, HttpServletResponse response) {
        tokenProvider.revokeToken(ChannelType.valueOf(channel));
        cookieUtils.deleteCookie(response);
        SecurityUtils.clear();
    }

    @Transactional(readOnly = true)
    public Optional<User> findOneWithAuthoritiesByUsername(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String username) {
        Optional<User> user = userRepository.findOneWithAuthoritiesByUsername(username);
        if (user.isEmpty()) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        UserProfileDTO userProfileDTO = UserProfileDTO.fromEntity(user.get());
        this.mappingUserPermissions(userProfileDTO, user.get());
        return userProfileDTO;
    }

    @Transactional(readOnly = true)
    public void mappingUserPermissions(UserProfileDTO userProfileDTO, User user) {
        Set<String> permissions = user
            .getRoles()
            .stream()
            .filter(role -> !ObjectUtils.isEmpty(role.getPermissions()))
            .flatMap(role -> role.getPermissions().stream())
            .filter(Objects::nonNull)
            .map(Permission::getCode)
            .collect(Collectors.toSet());
        List<UserPermission> userPermissions = userPermissionRepository.findAllByUserId(user.getId());
        if (!userPermissions.isEmpty()) {
            permissions.addAll(
                userPermissions
                    .stream()
                    .filter(pm -> PermissionAction.GRANT.equals(pm.getAction()))
                    .map(UserPermission::getPermissionCode)
                    .collect(Collectors.toSet())
            );
            permissions.removeAll(
                userPermissions
                    .stream()
                    .filter(pm -> PermissionAction.DENY.equals(pm.getAction()))
                    .map(UserPermission::getPermissionCode)
                    .collect(Collectors.toSet())
            );
        }
        userProfileDTO.setPermissions(new ArrayList<>(permissions));
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public TokenPair getTokenPair(String cookieValueOrTokenString, boolean isInternal) {
        if (StringUtils.isBlank(cookieValueOrTokenString) || isInternal) {
            return new TokenPair(cookieValueOrTokenString, null);
        }
        try {
            Map<String, String> tokenData = JsonF.jsonToObject(cookieValueOrTokenString, Map.class);
            if (CollectionUtils.isEmpty(tokenData)) {
                return new TokenPair(cookieValueOrTokenString, null);
            }
            String accessToken = tokenData.get(AuthKey.ACCESS_TOKEN.getKey());
            String refreshToken = tokenData.get(AuthKey.REFRESH_TOKEN.getKey());
            if (CommonUtils.isEmpty(accessToken, refreshToken)) {
                throw new FwException(ErrorMessage.TOKEN_PAIR_INVALID);
            }
            return new TokenPair(accessToken, refreshToken);
        } catch (JsonParseException e) {
            return new TokenPair(cookieValueOrTokenString, null);
        }
    }

    @Transactional
    @FwMode(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = ModeType.HANDLE)
    public void logoutAllDevices(HttpServletResponse response) {
        tokenProvider.revokeAllTokens();
        cookieUtils.deleteCookie(response);
        SecurityUtils.clear();
    }
}

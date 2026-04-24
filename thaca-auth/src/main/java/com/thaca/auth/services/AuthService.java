package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.*;
import com.thaca.auth.dtos.DeviceInfo;
import com.thaca.auth.dtos.GeoInfo;
import com.thaca.auth.dtos.LoginHistoryDTO;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.dtos.res.VerifyTokenRes;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.enums.LoginStatus;
import com.thaca.auth.internal.services.InternalService;
import com.thaca.auth.repositories.LoginHistoryRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.auth.repositories.SystemUserRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.security.CustomUserDetails;
import com.thaca.auth.security.jwt.TokenProvider;
import com.thaca.auth.validators.core.Validator;
import com.thaca.auth.validators.rules.PasswordRule;
import com.thaca.common.dtos.TokenPair;
import com.thaca.common.dtos.internal.AuthUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.PaginationRequest;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.AuthKey;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.TokenStatus;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.context.FwContextBody;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.CookieUtils;
import com.thaca.framework.core.utils.JsonF;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.json.JsonParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AuthService {

    private final CookieUtils cookieUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SystemUserRepository systemUserRepository;
    private final SystemCredentialRepository systemCredentialRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final JwtUtils jwtUtils;
    private final CommonService commonService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final InternalService internalService;

    @FwMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = ModeType.VALIDATE)
    public void validateAuthenticate(
        LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        if (StringUtils.isEmpty(loginReq.getUsername())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        Validator<UserDTO> validator = new Validator<>(List.of(new PasswordRule<>()));
        validator.validate(UserDTO.builder().password(loginReq.getPassword()).build());
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = ModeType.HANDLE)
    public AuthenticateRes authenticate(LoginReq loginReq) {
        HttpServletRequest httpServletRequest = (
            (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
        ).getRequest();
        HttpServletResponse httpServletResponse = (
            (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
        ).getResponse();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginReq.getUsername(),
            loginReq.getPassword()
        );

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            return getAuthenticateRes(loginReq, httpServletRequest, httpServletResponse, authentication, false);
        } catch (Exception e) {
            userRepository
                .findByUsername(loginReq.getUsername())
                .ifPresent(user ->
                    saveLoginHistory(
                        AuthUserDTO.builder().id(user.getId()).build(),
                        httpServletRequest,
                        LoginStatus.FAILED,
                        e.getMessage(),
                        false
                    )
                );
            throw e;
        }
    }

    @FwMode(name = ServiceMethod.CMS_AUTHENTICATE, type = ModeType.VALIDATE)
    public void validateAuthenticateCms(
        LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        validateAuthenticate(loginReq, httpServletRequest, httpServletResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.CMS_AUTHENTICATE, type = ModeType.HANDLE)
    public AuthenticateRes authenticateCms(LoginReq loginReq) {
        HttpServletRequest httpServletRequest = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest();
        HttpServletResponse httpServletResponse = (
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        ).getResponse();
        try {
            SystemCredential sc = systemCredentialRepository
                .findByUsername(loginReq.getUsername())
                .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
            if (!passwordEncoder.matches(loginReq.getPassword(), sc.getPassword())) {
                throw new FwException(ErrorMessage.PASSWORD_INVALID);
            }
            SystemUser su = sc.getSystemUser();
            if (su.isLocked()) {
                throw new FwException(ErrorMessage.USER_LOCKED);
            }
            if (!su.getIsActivated()) {
                throw new FwException(ErrorMessage.USER_NOT_ACTIVATED);
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            String rolesString;
            if (su.isSuperAdmin()) {
                rolesString = AuthoritiesConstants.SUPER_ADMIN;
                authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.SUPER_ADMIN));
            } else {
                rolesString = getRoleString(sc, authorities);
            }
            CustomUserDetails userDetails = new CustomUserDetails(
                sc.getUsername(),
                sc.getPassword(),
                authorities,
                rolesString,
                StringUtils.defaultIfBlank(FwContextHeader.get().getChannel(), ChannelType.WEB.name()),
                su.isSuperAdmin(),
                true
            );
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return getAuthenticateRes(loginReq, httpServletRequest, httpServletResponse, authentication, true);
        } catch (Exception e) {
            systemCredentialRepository
                .findByUsername(loginReq.getUsername())
                .ifPresent(sc ->
                    saveLoginHistory(
                        AuthUserDTO.builder().id(sc.getSystemUser().getId()).build(),
                        httpServletRequest,
                        LoginStatus.FAILED,
                        e.getMessage(),
                        true
                    )
                );
            throw e;
        }
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
    public RefreshTokenRes refreshToken(String cookieValue) {
        HttpServletRequest httpServletRequest = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest();
        HttpServletResponse httpServletResponse = (
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        ).getResponse();
        if (httpServletResponse == null) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : null;
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

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_LOGOUT, type = ModeType.HANDLE)
    public void logout() {
        HttpServletResponse response = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getResponse();
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : ChannelType.WEB.name();
        tokenProvider.revokeToken(ChannelType.valueOf(channel));
        cookieUtils.deleteCookie(response);
        SecurityUtils.clear();
    }

    @Transactional(readOnly = true)
    public Object findOneByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        }
        return systemCredentialRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public AuthUserDTO getUserProfile(String username) {
        return userRepository
            .findByUsername(username)
            .map(u -> AuthUserDTO.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).build())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
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
    public void logoutAllDevices() {
        HttpServletResponse response = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getResponse();
        tokenProvider.revokeAllTokens();
        cookieUtils.deleteCookie(response);
        SecurityUtils.clear();
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.AUTH_SEARCH_LOGIN_HISTORY, type = ModeType.HANDLE)
    public SearchResponse<LoginHistoryDTO> searchLoginHistory(SearchRequest<LoginHistoryDTO> request) {
        Specification<LoginHistory> spec = createLoginHistorySpecification(request);
        PaginationRequest paginationRequest = request.getPage();
        Page<LoginHistory> page = loginHistoryRepository.findAll(
            spec,
            paginationRequest.toPageable(Sort.Direction.DESC, "loginTime")
        );
        return new SearchResponse<>(
            page.getContent().stream().map(LoginHistoryDTO::fromEntity).collect(Collectors.toList()),
            PaginationResponse.of(page)
        );
    }

    private AuthenticateRes getAuthenticateRes(
        LoginReq loginReq,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        Authentication authentication,
        boolean isCms
    ) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.createToken(authentication, httpServletResponse);
        if (StringUtils.isBlank(token)) {
            throw new FwException(ErrorMessage.ACCESS_TOKEN_INVALID);
        }

        AuthUserDTO userInfoDTO = isCms ? internalService.getSystemProfile() : getUserProfile(loginReq.getUsername());

        saveLoginHistory(userInfoDTO, httpServletRequest, LoginStatus.SUCCESS, null, isCms);

        return new AuthenticateRes(true, userInfoDTO);
    }

    private void saveLoginHistory(
        AuthUserDTO userDTO,
        HttpServletRequest request,
        LoginStatus status,
        String failureReason,
        boolean isCms
    ) {
        try {
            String ip = commonService.extractIpAddress(request);
            String ua = request.getHeader("User-Agent");
            String deviceId = Optional.ofNullable(FwContextHeader.get())
                .map(h -> StringUtils.trimToNull(h.getDeviceId()))
                .orElse(null);

            GeoInfo geo = commonService.lookup(ip);
            DeviceInfo device = commonService.parse(ua);
            boolean isNewDevice = isNewDevice(userDTO.getId(), isCms, deviceId, status);

            LoginHistory history = LoginHistory.builder()
                .username(userDTO.getUsername())
                .user(isCms ? null : userRepository.getReferenceById(userDTO.getId()))
                .systemUser(isCms ? systemUserRepository.getReferenceById(userDTO.getId()) : null)
                .ipAddress(ip)
                .userAgent(ua)
                .browser(device.getBrowser())
                .os(device.getOs())
                .deviceType(device.getDeviceType())
                .country(geo.getCountry())
                .countryIsoCode(geo.getCountryIsoCode())
                .city(geo.getCity())
                .latitude(geo.getLatitude())
                .longitude(geo.getLongitude())
                .approxLocation(geo.getApproxLocation())
                .isVpn(geo.getIsVpn())
                .riskScore(geo.getRiskScore())
                .channel(
                    ChannelType.valueOf(
                        StringUtils.defaultIfBlank(FwContextHeader.get().getChannel(), ChannelType.WEB.name())
                    )
                )
                .status(status)
                .failureReason(failureReason)
                .loginTime(Instant.now())
                .requestId(FwContextBody.get().getTransId())
                .deviceId(deviceId)
                .isNewDevice(isNewDevice)
                .build();

            loginHistoryRepository.save(history);
        } catch (Exception ignored) {}
    }

    private boolean isNewDevice(Long userId, boolean isCms, String deviceId, LoginStatus status) {
        if (userId == null || StringUtils.isBlank(deviceId) || !LoginStatus.SUCCESS.equals(status)) {
            return false;
        }
        if (isCms) {
            return !loginHistoryRepository.existsBySystemUser_IdAndDeviceIdAndStatus(
                userId,
                deviceId,
                LoginStatus.SUCCESS
            );
        }
        return !loginHistoryRepository.existsByUser_IdAndDeviceIdAndStatus(userId, deviceId, LoginStatus.SUCCESS);
    }

    public static String getRoleString(SystemCredential sc, List<GrantedAuthority> authorities) {
        String rolesString = sc.getRoles().stream().map(Role::getCode).collect(Collectors.joining(","));
        for (Role role : sc.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getCode()));
        }
        return rolesString;
    }

    private Specification<LoginHistory> createLoginHistorySpecification(SearchRequest<LoginHistoryDTO> req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                LoginHistoryDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getUsername())) {
                    predicates.add(
                        cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%")
                    );
                }
                if (filter.getIsCms() != null) {
                    if (filter.getIsCms()) {
                        predicates.add(cb.isNotNull(root.get("systemUser")));
                    } else {
                        predicates.add(cb.isNotNull(root.get("user")));
                    }
                }
                if (StringUtils.isNotBlank(filter.getIpAddress())) {
                    predicates.add(cb.like(root.get("ipAddress"), "%" + filter.getIpAddress() + "%"));
                }
                if (filter.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }
                if (filter.getDeviceType() != null) {
                    predicates.add(cb.equal(root.get("deviceType"), filter.getDeviceType().name()));
                }
                if (filter.getChannel() != null) {
                    predicates.add(cb.equal(root.get("channel"), filter.getChannel()));
                }
                if (StringUtils.isNotBlank(filter.getBrowser())) {
                    predicates.add(
                        cb.like(cb.lower(root.get("browser")), "%" + filter.getBrowser().toLowerCase() + "%")
                    );
                }
                if (filter.getFromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("loginTime"), filter.getFromDate()));
                }
                if (filter.getToDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("loginTime"), filter.getToDate()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

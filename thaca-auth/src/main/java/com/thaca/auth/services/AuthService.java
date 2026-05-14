package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.*;
import com.thaca.auth.dtos.CaptchaDTO;
import com.thaca.auth.dtos.LoginHistoryDTO;
import com.thaca.auth.dtos.res.RefreshTokenRes;
import com.thaca.auth.dtos.res.VerifyTokenRes;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.enums.LoginStatus;
import com.thaca.auth.mappers.UserMapper;
import com.thaca.auth.repositories.LoginHistoryRepository;
import com.thaca.auth.repositories.SystemCredentialRepository;
import com.thaca.auth.repositories.SystemUserRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.security.CustomUserDetails;
import com.thaca.auth.security.jwt.TokenProvider;
import com.thaca.auth.utils.CaptchaUtils;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.req.LoginReq;
import com.thaca.common.dtos.internal.req.SendOtpReq;
import com.thaca.common.dtos.internal.res.AuthenticateRes;
import com.thaca.common.dtos.search.PaginationRequest;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.TokenStatus;
import com.thaca.common.events.SendOtpEvent;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.blocking.starter.services.SessionStore;
import com.thaca.framework.blocking.starter.utils.JwtUtils;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.enums.ChannelType;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.FwUtils;
import com.thaca.framework.core.validations.Validator;
import com.thaca.framework.core.validations.rules.PasswordRule;
import com.thaca.framework.core.validations.rules.UsernameRule;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SystemCredentialRepository systemCredentialRepository;
    private final SystemUserRepository systemUserRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final JwtUtils jwtUtils;
    private final LoginHistoryRepository loginHistoryRepository;
    private final SystemUserService systemUserService;
    private final LoginHistoryService loginHistoryService;
    private final RedisCacheService redisCacheService;
    private final SessionStore sessionStore;
    private final OtpService otpService;

    @FwMode(name = ServiceMethod.ADMIN_SEND_AUTHENTICATE_OTP, type = ModeType.HANDLE)
    public void sendOtp(SendOtpReq req) {
        var systemUser = systemUserRepository
            .findByEmail(req.getEmail())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        SendOtpEvent event = SendOtpEvent.builder()
            .objectId(systemUser.getId().toString())
            .email(systemUser.getEmail())
            .build();
        otpService.sendOtp(event);
    }

    @FwMode(name = ServiceMethod.AUTH_GENERATE_CAPTCHA, type = ModeType.HANDLE)
    public CaptchaDTO generateCaptcha() {
        try {
            CaptchaDTO captcha = CaptchaUtils.generate(CaptchaDTO.Mode.AUTO);
            String captchaId = UUID.randomUUID().toString().replace("-", "");
            String key = sessionStore.getKeyCaptcha(captchaId);
            String hashedAnswer = FwUtils.hexString(captcha.getAnswer().trim().toLowerCase());
            redisCacheService.put(key, hashedAnswer, 1, TimeUnit.MINUTES);
            return CaptchaDTO.builder().captchaId(captchaId).image(captcha.getImage()).build();
        } catch (Exception e) {
            throw new FwException(ErrorMessage.CAPTCHA_GENERATION_FAILED);
        }
    }

    @FwMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = ModeType.VALIDATE)
    public void validateAuthenticate(LoginReq loginReq) {
        Validator<UserDTO> validator = new Validator<>(List.of(new UsernameRule<>(), new PasswordRule<>()));
        validator.validate(UserDTO.builder().username(loginReq.getUsername()).password(loginReq.getPassword()).build());
        if (StringUtils.isAnyBlank(loginReq.getCaptchaId(), loginReq.getCaptcha())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        verifyCaptcha(loginReq);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_AUTHENTICATE, type = ModeType.HANDLE)
    public AuthenticateRes authenticate(LoginReq loginReq) {
        HttpServletRequest httpServletRequest = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest();
        HttpServletResponse httpServletResponse = (
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
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
                    loginHistoryService.saveLoginHistory(
                        SystemUserDTO.builder().id(user.getId()).build(),
                        httpServletRequest,
                        LoginStatus.FAILED,
                        e.getMessage(),
                        false,
                        loginReq.getTenantId()
                    )
                );
            if (e instanceof FwException) {
                throw (FwException) e;
            }
            throw e;
        }
    }

    @FwMode(name = ServiceMethod.ADMIN_AUTHENTICATE, type = ModeType.VALIDATE)
    public void validateAuthenticateAdmin(LoginReq loginReq) {
        validateAuthenticate(loginReq);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_AUTHENTICATE, type = ModeType.HANDLE)
    public AuthenticateRes authenticateAdmin(LoginReq loginReq) {
        HttpServletRequest httpServletRequest = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest();
        HttpServletResponse httpServletResponse = (
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()
        ).getResponse();
        try {
            Optional<SystemCredential> scOpt =
                loginReq.getTenantId() != null
                    ? systemCredentialRepository.findByUsernameAndTenantId(
                          loginReq.getUsername(),
                          loginReq.getTenantId()
                      )
                    : systemCredentialRepository.findByUsername(loginReq.getUsername());
            SystemCredential sc = scOpt.orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
            SystemUser su = sc.getSystemUser();
            if (loginReq.getTenantId() == null && !su.getIsSuperAdmin()) {
                throw new FwException(CommonErrorMessage.FORBIDDEN);
            }
            if (su.getLockedUntil() != null && su.getLockedUntil().isAfter(Instant.now())) {
                throw new FwException(ErrorMessage.USER_TEMPORARILY_LOCKED);
            }
            if (Boolean.TRUE.equals(su.getIsLocked())) {
                throw new FwException(ErrorMessage.USER_LOCKED);
            }
            if (!Boolean.TRUE.equals(su.getIsActivated())) {
                throw new FwException(ErrorMessage.USER_NOT_ACTIVATED);
            }

            if (!passwordEncoder.matches(loginReq.getPassword(), sc.getPassword())) {
                int attempts = loginHistoryService.recordFailedLoginAttempt(su.getId());
                if (attempts >= 5) {
                    throw new FwException(ErrorMessage.USER_TEMPORARILY_LOCKED);
                }
                int remaining = 5 - attempts;
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("remainingAttempts", remaining);

                throw new FwException(ErrorMessage.PASSWORD_INVALID_WITH_RETRY, errorData);
            }

            if (su.getFailedLoginAttempts() != null && su.getFailedLoginAttempts() > 0) {
                su.setFailedLoginAttempts(0);
                su.setLockedUntil(null);
                systemUserRepository.save(su);
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            String rolesString;
            if (Boolean.TRUE.equals(su.getIsSuperAdmin())) {
                rolesString = AuthoritiesConstants.SUPER_ADMIN;
                authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.SUPER_ADMIN));
            } else {
                rolesString = getRoleString(sc, authorities);
                List<String> deniedPermissions = systemCredentialRepository.findDeniedPermissionCodes(su.getId());
                deniedPermissions.forEach(p -> authorities.add(new SimpleGrantedAuthority("DENY_" + p)));
            }
            CustomUserDetails userDetails = new CustomUserDetails(
                sc.getUsername(),
                sc.getPassword(),
                authorities,
                rolesString,
                StringUtils.defaultIfBlank(FwContextHeader.get().getChannel(), ChannelType.WEB.name()),
                su.getIsSuperAdmin(),
                true,
                new ArrayList<>(su.getTenantIds()),
                loginReq.getTenantId()
            );
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return getAuthenticateRes(loginReq, httpServletRequest, httpServletResponse, authentication, true);
        } catch (Exception e) {
            systemCredentialRepository
                .findByUsername(loginReq.getUsername())
                .ifPresent(sc ->
                    loginHistoryService.saveLoginHistory(
                        SystemUserDTO.builder().id(sc.getSystemUser().getId()).build(),
                        httpServletRequest,
                        LoginStatus.FAILED,
                        e.getMessage(),
                        true,
                        loginReq.getTenantId()
                    )
                );
            if (e instanceof FwException) {
                throw (FwException) e;
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.USER_GET_USER_PROFILE, type = ModeType.HANDLE)
    public UserDTO findById(Long id) {
        return userRepository
            .findById(id)
            .map(UserMapper::fromEntity)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @FwMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = ModeType.VALIDATE)
    public void validateRefreshToken() {
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : null;
        if (StringUtils.isBlank(channel)) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (!ChannelType.MOBILE.name().equals(channel) && !ChannelType.WEB.name().equals(channel)) {
            throw new FwException(CommonErrorMessage.CHANNEL_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_REFRESH_TOKEN, type = ModeType.HANDLE)
    public RefreshTokenRes refreshToken() {
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
        return tokenProvider.refreshToken(httpServletRequest, httpServletResponse, channel);
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
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : ChannelType.WEB.name();
        logoutWithChannel(channel);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_LOGOUT, type = ModeType.HANDLE)
    public void logoutadmin() {
        String channel = FwContextHeader.get() != null ? FwContextHeader.get().getChannel() : ChannelType.ADMIN.name();
        logoutWithChannel(channel);
    }

    @Transactional(readOnly = true)
    public Object findOneByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        }
        return systemCredentialRepository.findByUsername(username).orElse(null);
    }

    public SystemUserDTO getUserProfile(String username) {
        return userRepository
            .findByUsername(username)
            .map(u ->
                SystemUserDTO.builder()
                    .id(u.getId())
                    .tenantIds(u.getTenantIds() != null ? new ArrayList<>(u.getTenantIds()) : null)
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .build()
            )
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional
    @FwMode(name = ServiceMethod.AUTH_LOGOUT_ALL_DEVICES, type = ModeType.HANDLE)
    public void logoutAllDevices() {
        HttpServletResponse response = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getResponse();
        tokenProvider.revokeAllTokens(response);
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
        boolean isAdmin
    ) {
        String token = tokenProvider.createToken(authentication, httpServletResponse);
        if (StringUtils.isBlank(token)) {
            throw new FwException(ErrorMessage.ACCESS_TOKEN_INVALID);
        }

        SystemUserDTO userInfoDTO = isAdmin
            ? systemUserService.getSystemProfile()
            : getUserProfile(loginReq.getUsername());
        loginHistoryService.saveLoginHistory(
            userInfoDTO,
            httpServletRequest,
            LoginStatus.SUCCESS,
            null,
            isAdmin,
            loginReq.getTenantId()
        );
        return AuthenticateRes.builder().isAuthenticate(true).info(userInfoDTO).accessToken(token).build();
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
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (StringUtils.isNotBlank(req.getFilter().getUsername())) {
                currentUsername = req.getFilter().getUsername();
            }
            LoginHistoryDTO filter = req.getFilter();
            boolean hasExplicitUserFilter =
                filter != null && (filter.getUserId() != null || filter.getSystemUserId() != null);
            if (!hasExplicitUserFilter) {
                if ("ANONYMOUS".equals(currentUsername)) {
                    predicates.add(cb.disjunction());
                } else {
                    Join<LoginHistory, User> userJoin = root.join("user", JoinType.LEFT);
                    Predicate isUser = cb.equal(userJoin.get("username"), currentUsername);
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<SystemCredential> scRoot = subquery.from(SystemCredential.class);
                    subquery
                        .select(scRoot.get("systemUser").get("id"))
                        .where(cb.equal(scRoot.get("username"), currentUsername));

                    Predicate isSystemUser = root.get("systemUser").get("id").in(subquery);
                    predicates.add(cb.or(isUser, isSystemUser));
                }
            }

            if (filter != null) {
                if (filter.getIsAdmin() != null) {
                    if (filter.getIsAdmin()) {
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
                if (filter.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
                }
                if (filter.getSystemUserId() != null) {
                    predicates.add(cb.equal(root.get("systemUser").get("id"), filter.getSystemUserId()));
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

    private void verifyCaptcha(LoginReq loginReq) {
        if (StringUtils.isBlank(loginReq.getCaptcha()) || StringUtils.isBlank(loginReq.getCaptchaId())) {
            throw new FwException(ErrorMessage.CAPTCHA_INVALID);
        }
        String key = sessionStore.getKeyCaptcha(loginReq.getCaptchaId());
        String expectedHash = redisCacheService.get(key, String.class);
        if (expectedHash == null) {
            throw new FwException(ErrorMessage.CAPTCHA_EXPIRED);
        }
        redisCacheService.evict(key);
        String inputHash = FwUtils.hexString(loginReq.getCaptcha().trim().toLowerCase());
        if (!expectedHash.equals(inputHash)) {
            throw new FwException(ErrorMessage.CAPTCHA_INVALID);
        }
    }

    private void logoutWithChannel(String channel) {
        HttpServletResponse response = (
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getResponse();
        tokenProvider.revokeToken(ChannelType.valueOf(channel), response);
        SecurityUtils.clear();
    }
}

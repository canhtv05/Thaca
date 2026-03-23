package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.User;
import com.thaca.auth.domains.UserPermission;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.UserPermissionDTO;
import com.thaca.auth.dtos.UserProfileDTO;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.UserSearchReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.enums.PermissionAction;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.UserPermissionRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.common.constants.EventConstants;
import com.thaca.common.dtos.events.ForgotPasswordEvent;
import com.thaca.common.dtos.events.UserCreationEvent;
import com.thaca.common.dtos.events.VerificationEmailEvent;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.blocking.starter.services.CommonService;
import com.thaca.framework.blocking.starter.services.SessionStore;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.CommonUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final AuthService authService;
    private final KafkaProducerService kafkaProducerService;
    private final RedisCacheService redisService;
    private final SessionStore sessionStore;

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.ADMIN_GET_USER_BY_ID, type = ModeType.HANDLE)
    public UserDTO findById(Long id) {
        return userRepository
                .findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @FwMode(name = ServiceMethod.AUTH_CREATE_USER, type = ModeType.VALIDATE)
    public void validateCreateUser(UserDTO request, boolean isAdmin) {
        if (CommonUtils.isEmpty(request.getUsername(), request.getEmail(), request.getPassword())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (userRepository.existsUserByUsername(request.getUsername())) {
            throw new FwException(ErrorMessage.USERNAME_ALREADY_EXITS);
        }

        if (request.getEmail().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new FwException(ErrorMessage.EMAIL_ALREADY_EXITS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_CREATE_USER, type = ModeType.HANDLE)
    public UserDTO createUser(UserDTO request, boolean isAdmin) {

        User user = User.builder()
                .username(request.getUsername())
                .activated(isAdmin && request.isActivated())
                .isGlobal(isAdmin ? request.getIsGlobal() : false)
                .isLocked(false)
                .email(request.getEmail())
                .build();

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encryptedPassword);
        if (ObjectUtils.isNotEmpty(request.getRoles()) && isAdmin) {
            user.setRoles(new HashSet<>(roleRepository.findAllByCodeIn(request.getRoles())));
        } else {
            user.setRoles(new HashSet<>(roleRepository.findAllByCodeIn(List.of("ROLE_USER"))));
        }

        User res = userRepository.save(user);
        try {
            if (!isAdmin || !request.isActivated()) {
                kafkaProducerService.send(
                        EventConstants.VERIFICATION_EMAIL_TOPIC,
                        new VerificationEmailEvent(request.getEmail(), request.getUsername(), request.getFullname()));
            } else {
                kafkaProducerService.send(
                        EventConstants.USER_CREATED_TOPIC,
                        new UserCreationEvent(user.getUsername(), request.getFullname()));
            }
        } catch (Exception e) {
            throw new FwException(CommonErrorMessage.INTERNAL_SERVER_ERROR);
        }
        return UserDTO.fromEntity(res);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_UPDATE_USER, type = ModeType.VALIDATE)
    public UserDTO updateUser(UserDTO request) {
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        if (optionalUser.isEmpty()) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        User user = optionalUser.get();
        user.setActivated(request.isActivated());
        if (SecurityUtils.isGlobalUser()) {
            user.setIsGlobal(request.getIsGlobal());
        }
        user.setRoles(new HashSet<>());
        if (ObjectUtils.isNotEmpty(request.getRoles())) {
            user.setRoles(new HashSet<>(roleRepository.findAllByCodeIn(request.getRoles())));
        }
        userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_LOCK_USER, type = ModeType.HANDLE)
    public void changeLockUser(Long id, boolean isLocked) {
        User user = userRepository.findById(id).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setLocked(isLocked);
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = ModeType.VALIDATE)
    public void resetPassword(ResetPasswordReq request) {
        if (CommonUtils.isEmpty(request.getEmail(), request.getNewPassword(), request.getOTP())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }

        if (request.getEmail().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new FwException(ErrorMessage.EMAIL_NOT_FOUND));

        boolean hasOTP = hasOTP(user.getUsername());
        if (!hasOTP) {
            throw new FwException(ErrorMessage.FORGET_PASSWORD_OTP_NOT_SENT_OR_EXPIRED);
        }
        String keyForgotPassword = sessionStore.getKeyForgotPassword(user.getUsername());
        if (!request.getOTP().equals(redisService.get(keyForgotPassword, String.class))) {
            throw new FwException(ErrorMessage.FORGET_PASSWORD_OTP_INVALID);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisService.evict(keyForgotPassword);
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.AUTH_VERIFY_FORGOT_PASSWORD_OTP, type = ModeType.VALIDATE)
    public void verifyForgotPasswordOTP(VerifyOTPReq request) {
        if (CommonUtils.isEmpty(request.getEmail(), request.getOTP())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }

        if (request.getEmail().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new FwException(ErrorMessage.EMAIL_NOT_FOUND));

        String keyForgotPassword = sessionStore.getKeyForgotPassword(user.getUsername());
        String otp = redisService.get(keyForgotPassword, String.class);
        if (CommonUtils.isEmpty(otp)) {
            throw new FwException(ErrorMessage.FORGET_PASSWORD_OTP_NOT_SENT_OR_EXPIRED);
        }

        if (!otp.equals(request.getOTP())) {
            throw new FwException(ErrorMessage.FORGET_PASSWORD_OTP_INVALID);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = ModeType.VALIDATE)
    public void changePassword(String cookieValue, ChangePasswordReq req, HttpServletResponse response) {
        if (CommonUtils.isEmpty(req.getCurrentPassword(), req.getNewPassword())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        String userLogin = CommonService.getCurrentUserLogin();
        Optional<User> optionalUser = userRepository.findByUsername(userLogin);
        if (optionalUser.isEmpty()) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new FwException(ErrorMessage.CURRENT_PASSWORD_INVALID);
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new FwException(ErrorMessage.PASSWORD_NEW_CANNOT_BE_SAME_AS_OLD);
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        authService.logout(cookieValue, req.getChannel(), response);
    }

    @FwMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD_REQUEST, type = ModeType.VALIDATE)
    public void validateForgotPasswordRequest(ForgotPasswordReq request) {
        if (request.getEmail().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD_REQUEST, type = ModeType.HANDLE)
    public void forgotPasswordRequest(ForgotPasswordReq request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new FwException(ErrorMessage.EMAIL_NOT_FOUND));

        boolean hasOTP = hasOTP(user.getUsername());
        if (hasOTP) {
            throw new FwException(ErrorMessage.FORGET_PASSWORD_OTP_ALREADY_SENT);
        }

        kafkaProducerService.send(
                EventConstants.FORGOT_PASSWORD_TOPIC,
                new ForgotPasswordEvent(request.getEmail(), user.getUsername()));
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_UPDATE_USER_PROFILE, type = ModeType.VALIDATE)
    public void updateUserProfile(UserProfileDTO request) {
        String userLogin = CommonService.getCurrentUserLogin();
        Optional<User> optionalUser = userRepository.findByUsername(userLogin);
        if (optionalUser.isEmpty()) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        userRepository.save(optionalUser.get());
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.ADMIN_SEARCH_USERS, type = ModeType.VALIDATE)
    public SearchResponse<UserDTO> searchDatatable(SearchRequest<UserSearchReq> request) {
        Specification<User> spec = createSpecification(request);
        Page<User> tenants = userRepository.findAll(spec, request.page().toPageable());
        return new SearchResponse<>(
                tenants.getContent().stream().map(UserDTO::fromEntity).collect(Collectors.toList()),
                PaginationResponse.of(
                        tenants.getNumber(),
                        tenants.getTotalPages(),
                        tenants.getSize(),
                        tenants.getNumberOfElements(),
                        (int) tenants.getTotalElements()));
    }

    private Specification<User> createSpecification(SearchRequest<UserSearchReq> criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(criteria.filter().getUsername())) {
                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(root.get("username")),
                                        "%" + criteria.filter().getUsername().toLowerCase() + "%"),
                                cb.like(
                                        cb.lower(root.get("username")),
                                        "%" + criteria.filter().getUsername().toLowerCase() + "%")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.ADMIN_GET_USER_PERMISSION, type = ModeType.VALIDATE)
    public Map<String, PermissionAction> getUserPermissions(Long userId) {
        List<UserPermission> data = userPermissionRepository.findAllByUserId(userId);
        return data.stream().collect(Collectors.toMap(UserPermission::getPermissionCode, UserPermission::getAction));
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.ADMIN_UPDATE_USER_PERMISSION, type = ModeType.VALIDATE)
    public void updateUserPermission(Long userId, List<UserPermissionDTO> request) {
        userPermissionRepository.deleteAllByUserId(userId);
        userPermissionRepository.saveAll(
                request
                        .stream()
                        .map(item -> item.fromEntity(userId))
                        .collect(Collectors.toSet()));
    }

    private boolean hasOTP(String username) {
        String keyForgotPassword = sessionStore.getKeyForgotPassword(username);
        return StringUtils.isNotEmpty(redisService.get(keyForgotPassword, String.class));
    }
}

package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.User;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.UserMapper;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.validators.core.Validator;
import com.thaca.auth.validators.rules.EmailRule;
import com.thaca.auth.validators.rules.FullnameRule;
import com.thaca.auth.validators.rules.PasswordRule;
import com.thaca.auth.validators.rules.UsernameRule;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.VerifyEmailTokenDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
    private final AuthService authService;
    // private final KafkaProducerService kafkaProducerService;
    private final RedisCacheService redisService;
    private final SessionStore sessionStore;

    @Transactional(readOnly = true)
    public SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> request) {
        Specification<User> spec = createUserSpecification(request);
        Page<User> users = userRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt"));
        return new SearchResponse<>(
            users
                .getContent()
                .stream()
                .map(u -> UserMapper.fromEntityWithCms(u, true))
                .collect(Collectors.toList()),
            PaginationResponse.of(users)
        );
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return userRepository
            .findById(id)
            .map(u -> UserMapper.fromEntityWithCms(u, true))
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(rollbackFor = Exception.class)
    public void lockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsLocked(true);
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unlockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsLocked(false);
        userRepository.save(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public VerifyEmailTokenDTO activeUserByUserName(VerifyEmailTokenDTO request) {
        User user = userRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setIsActivated(true);
        userRepository.save(user);

        // kafkaProducerService.sendAndWait(
        // EventConstants.USER_CREATED_TOPIC,
        // user.getUsername(),
        // new UserCreationEvent(user.getUsername(), request.fullname())
        // );

        return new VerifyEmailTokenDTO(
            user.getUsername(),
            request.fullname(),
            request.email(),
            request.expiredAt(),
            request.jti()
        );
    }

    @FwMode(name = ServiceMethod.AUTH_CREATE_USER, type = ModeType.VALIDATE)
    public void validateCreateUser(UserDTO request) {
        Validator<UserDTO> validator = new Validator<>(
            List.of(new EmailRule<>(), new FullnameRule<>(), new PasswordRule<>(), new UsernameRule<>())
        );
        validator.validate(request);
        if (userRepository.existsUserByUsername(request.getUsername())) {
            throw new FwException(ErrorMessage.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new FwException(ErrorMessage.EMAIL_ALREADY_EXITS);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_CREATE_USER, type = ModeType.HANDLE)
    public void createUser(UserDTO request) {
        boolean isAdmin = SecurityUtils.isSuperAdmin();
        User user = User.builder()
            .username(request.getUsername())
            .isActivated(isAdmin && request.getIsActivated())
            .isLocked(false)
            .email(request.getEmail())
            .build();

        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encryptedPassword);
        // if (ObjectUtils.isNotEmpty(request.getRoles()) && isAdmin) {
        // user.setRoles(new
        // HashSet<>(roleRepository.findAllByCodeIn(request.getRoles())));
        // } else {
        // user.setRoles(new
        // HashSet<>(roleRepository.findAllByCodeIn(List.of(AuthoritiesConstants.USER))));
        // }

        userRepository.save(user);
        // this.publishUserEvent(res, saved, isAdmin);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_UPDATE_USER, type = ModeType.VALIDATE)
    public UserDTO updateUser(UserDTO request) {
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        if (optionalUser.isEmpty()) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        User user = optionalUser.get();
        user.setIsActivated(request.getIsActivated());
        // user.setRoles(new HashSet<>());
        // if (ObjectUtils.isNotEmpty(request.getRoles())) {
        // user.setRoles(new
        // HashSet<>(roleRepository.findAllByCodeIn(request.getRoles())));
        // }
        userRepository.save(user);
        return UserMapper.fromEntity(user);
    }

    @FwMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = ModeType.VALIDATE)
    public void validateResetPassword(ResetPasswordReq request) {
        if (StringUtils.isBlank(request.getOTP())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        Validator<UserDTO> validator = new Validator<>(List.of(new EmailRule<>(), new PasswordRule<>()));
        validator.validate(UserDTO.builder().email(request.getEmail()).password(request.getNewPassword()).build());
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_RESET_PASSWORD, type = ModeType.HANDLE)
    public void handleResetPassword(ResetPasswordReq request) {
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
    @FwMode(name = ServiceMethod.AUTH_VERIFY_OTP_FORGOT_PASSWORD, type = ModeType.VALIDATE)
    public void validateVerifyOTPForgotPassword(VerifyOTPReq request) {
        if (StringUtils.isBlank(request.getOTP())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        Validator<UserDTO> validator = new Validator<>(List.of(new EmailRule<>()));
        validator.validate(UserDTO.builder().email(request.getEmail()).build());
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.AUTH_VERIFY_OTP_FORGOT_PASSWORD, type = ModeType.HANDLE)
    public void handleVerifyOTPForgotPassword(VerifyOTPReq request) {
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

    @FwMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = ModeType.VALIDATE)
    public void validateChangePassword(ChangePasswordReq request) {
        if (CommonUtils.isEmpty(request.getCurrentPassword().trim(), request.getNewPassword().trim())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        String newPwd = request.getNewPassword().trim();
        Validator<UserDTO> validator = new Validator<>(List.of(new PasswordRule<>()));
        validator.validate(UserDTO.builder().password(newPwd).build());
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.AUTH_CHANGE_PASSWORD, type = ModeType.HANDLE)
    public void changePassword(ChangePasswordReq req) {
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
        authService.logoutAllDevices();
    }

    @FwMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD_REQUEST, type = ModeType.VALIDATE)
    public void validateForgotPasswordRequest(ForgotPasswordReq request) {
        Validator<UserDTO> validator = new Validator<>(List.of(new EmailRule<>()));
        validator.validate(UserDTO.builder().email(request.getEmail()).build());
    }

    @Transactional(readOnly = true)
    @FwMode(name = ServiceMethod.AUTH_FORGOT_PASSWORD_REQUEST, type = ModeType.HANDLE)
    public void handleForgotPasswordRequest(ForgotPasswordReq request) {
        User user = userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new FwException(ErrorMessage.EMAIL_NOT_FOUND));

        boolean hasOTP = hasOTP(user.getUsername());
        if (hasOTP) {
            throw new FwException(ErrorMessage.FORGET_PASSWORD_OTP_ALREADY_SENT);
        }

        // kafkaProducerService.sendAndWait(
        // EventConstants.FORGOT_PASSWORD_TOPIC,
        // user.getUsername(),
        // new ForgotPasswordEvent(request.getEmail(), user.getUsername())
        // );

        // fake send otp
        String keyForgotPassword = sessionStore.getKeyForgotPassword(user.getUsername());
        String otp = "123456";
        redisService.put(keyForgotPassword, otp, 5, TimeUnit.MINUTES);
    }

    private Specification<User> createUserSpecification(SearchRequest<UserDTO> req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getFilter() != null) {
                UserDTO filter = req.getFilter();
                if (StringUtils.isNotBlank(filter.getUsername())) {
                    predicates.add(
                        cb.like(cb.lower(root.get("username")), "%" + filter.getUsername().toLowerCase() + "%")
                    );
                }
                if (StringUtils.isNotBlank(filter.getEmail())) {
                    predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
                }
                if (filter.getIsActivated() != null) {
                    predicates.add(cb.equal(root.get("isActivated"), filter.getIsActivated()));
                }
                if (filter.getIsLocked() != null) {
                    predicates.add(cb.equal(root.get("isLocked"), filter.getIsLocked()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasOTP(String username) {
        String keyForgotPassword = sessionStore.getKeyForgotPassword(username);
        return StringUtils.isNotEmpty(redisService.get(keyForgotPassword, String.class));
    }

    // private void publishUserEvent(UserDTO request, User user, boolean isAdmin) {
    // String topic;
    // Object payload;
    // if (!isAdmin || !Boolean.TRUE.equals(request.getIsActivated())) {
    // topic = EventConstants.VERIFICATION_EMAIL_TOPIC;
    // payload = new VerificationEmailEvent(request.getEmail(),
    // request.getUsername(), request.getFullname());
    // } else {
    // topic = EventConstants.USER_CREATED_TOPIC;
    // payload = new UserCreationEvent(user.getUsername(), request.getFullname());
    // }
    // kafkaProducerService.sendAndWait(topic, user.getUsername(), payload);
    // }
}

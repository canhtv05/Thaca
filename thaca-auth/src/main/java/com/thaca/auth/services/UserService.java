package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.User;
import com.thaca.auth.domains.UserLockHistory;
import com.thaca.auth.domains.projections.TenantInfoProjection;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.TenantMapper;
import com.thaca.auth.mappers.UserMapper;
import com.thaca.auth.repositories.TenantRepository;
import com.thaca.auth.repositories.UserLockHistoryRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.validators.core.Validator;
import com.thaca.auth.validators.rules.EmailRule;
import com.thaca.auth.validators.rules.FullnameRule;
import com.thaca.auth.validators.rules.PasswordRule;
import com.thaca.auth.validators.rules.UsernameRule;
import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.ImportResponseDTO;
import com.thaca.common.dtos.internal.SystemUserDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.VerifyEmailTokenDTO;
import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.AccountStatus;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.excel.ExcelEngine;
import com.thaca.common.excel.ImportErrorExcelExport;
import com.thaca.common.excel.result.ImportResult;
import com.thaca.common.excel.result.RowError;
import com.thaca.common.excel.schema.ExcelColumn;
import com.thaca.common.excel.schema.ExcelDataType;
import com.thaca.common.excel.schema.ExcelSchema;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.blocking.starter.services.CommonService;
import com.thaca.framework.blocking.starter.services.SessionStore;
import com.thaca.framework.core.annotations.CheckPermission;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.CommonUtils;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.*;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final TenantRepository tenantRepository;
    private final UserLockHistoryRepository userLockHistoryRepository;

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_SEARCH_USERS, type = ModeType.HANDLE)
    public SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> request) {
        Specification<User> spec = createUserSpecification(request);
        Page<User> users = userRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt"));
        Map<Long, TenantInfoProjection> tenantMap = tenantRepository
            .findAllTenants()
            .stream()
            .collect(Collectors.toMap(TenantInfoProjection::getId, (t -> t)));
        var response = users
            .getContent()
            .stream()
            .map(u -> {
                var res = UserMapper.fromEntityWithCms(u, true);
                res.setTenant(TenantMapper.fromInfoProj(tenantMap.getOrDefault(u.getTenantId(), null)));
                return res;
            })
            .toList();

        return new SearchResponse<>(response, PaginationResponse.of(users));
    }

    @FwMode(name = InternalMethod.INTERNAL_CMS_DETAIL_USER, type = ModeType.VALIDATE)
    public void validateDetailUser(UserDTO request) {
        if (StringUtils.isBlank(request.getUsername())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_DETAIL_USER, type = ModeType.HANDLE)
    public UserDTO detailUser(UserDTO request) {
        Map<Long, TenantInfoProjection> tenantMap = tenantRepository
            .findAllTenants()
            .stream()
            .collect(Collectors.toMap(TenantInfoProjection::getId, (t -> t)));
        User user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        var res = UserMapper.fromEntityWithCms(user, true);
        res.setTenant(TenantMapper.fromInfoProj(tenantMap.getOrDefault(user.getTenantId(), null)));
        return res;
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_DOWNLOAD_USER_TEMPLATE, type = ModeType.HANDLE)
    public byte[] downloadTemplate() throws IOException {
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        return ExcelEngine.generateTemplate(this.buildImportSchema(isVietnamese, isSuperAdmin));
    }

    @Transactional(rollbackFor = Exception.class)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_IMPORT_USERS, type = ModeType.HANDLE)
    public ImportResponseDTO importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        ExcelSchema schema = this.buildImportSchema(isVietnamese, isSuperAdmin);

        ImportResult<Map<String, Object>> result;
        try {
            result = ExcelEngine.importFile(file, schema);
        } catch (Exception e) {
            throw new FwException(CommonErrorMessage.EXCEL_INVALID);
        }
        if (result.hasErrors()) {
            return toImportResponse(result);
        }
        List<Map<String, Object>> successRows = result.getSuccessRows();
        Set<String> existingUsernames = new HashSet<>();
        Set<String> existingEmails = new HashSet<>();

        Map<String, TenantInfoProjection> tenantCodeToId = isSuperAdmin
            ? tenantRepository
                  .findAllActiveTenants()
                  .stream()
                  .collect(Collectors.toMap(TenantInfoProjection::getCode, a -> a))
            : new HashMap<>();

        List<User> usersToSave = new ArrayList<>();
        List<RowError> businessErrors = new ArrayList<>();
        Map<Long, List<User>> usersByTenant = new HashMap<>();

        for (int i = 0; i < successRows.size(); i++) {
            Map<String, Object> row = successRows.get(i);
            int excelRowIndex = result.getTotalRows() > 0 ? i + 1 : i;
            String username = String.valueOf(row.get("username")).trim().toLowerCase();
            String email = String.valueOf(row.get("email")).trim().toLowerCase();
            String password = row.get("password") != null ? String.valueOf(row.get("password")) : null;
            boolean hasError = false;
            if (existingUsernames.contains(username)) {
                businessErrors.add(
                    new RowError(
                        excelRowIndex,
                        "username",
                        isVietnamese ? "Tên đăng nhập" : "Username",
                        isVietnamese ? "Tên đăng nhập đã tồn tại trong hệ thống" : "Username already exists",
                        username
                    )
                );
                hasError = true;
            }
            if (existingEmails.contains(email)) {
                businessErrors.add(
                    new RowError(
                        excelRowIndex,
                        "email",
                        "Email",
                        isVietnamese ? "Email đã tồn tại trong hệ thống" : "Email already exists",
                        email
                    )
                );
                hasError = true;
            }
            Long tenantId = null;
            if (SecurityUtils.isSuperAdmin()) {
                Object tenantRaw = row.get("tenantId");
                if (tenantRaw != null) {
                    String tenantStr = String.valueOf(tenantRaw);
                    String tenantCode = tenantStr.contains(" - ") ? tenantStr.split(" - ")[0].trim() : tenantStr.trim();
                    tenantId = tenantCodeToId.get(tenantCode).getId();
                    if (tenantId == null) {
                        businessErrors.add(
                            new RowError(
                                excelRowIndex,
                                "tenantId",
                                isVietnamese ? "Mã Tenant" : "Tenant ID",
                                isVietnamese ? "Tenant không tồn tại" : "Tenant does not exist",
                                tenantStr
                            )
                        );
                        hasError = true;
                    }
                }
            } else {
                tenantId = SecurityUtils.getCurrentTenantId();
            }
            if (hasError) {
                continue;
            }
            Boolean isActivated =
                row.get("isActivated") != null && Boolean.parseBoolean(String.valueOf(row.get("isActivated")));

            User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .isActivated(isActivated)
                .isLocked(false)
                .tenantId(tenantId)
                .build();
            usersToSave.add(user);
            existingUsernames.add(username);
            existingEmails.add(email);
        }

        if (!usersToSave.isEmpty()) {
            usersByTenant = usersToSave
                .stream()
                .filter(u -> u.getTenantId() != null)
                .collect(Collectors.groupingBy(User::getTenantId));
            Set<Long> tenantIds = usersByTenant.keySet();
            Map<Long, Long> existingCounts = new HashMap<>();
            for (Map<String, Object> row : userRepository.countByTenantIds(tenantIds)) {
                Long id = ((Number) row.get("tenantId")).longValue();
                Long cnt = ((Number) row.get("count")).longValue();
                existingCounts.put(id, cnt);
            }
            Map<Long, Tenant> tenantMap = new HashMap<>();
            for (Tenant t : tenantRepository.findAllByIdIn(tenantIds)) {
                tenantMap.put(t.getId(), t);
            }
            for (Map.Entry<Long, List<User>> entry : usersByTenant.entrySet()) {
                Long tenantId = entry.getKey();
                int newCount = entry.getValue().size();
                long existing = existingCounts.getOrDefault(tenantId, 0L);
                Tenant tenant = tenantMap.get(tenantId);
                if (tenant != null && tenant.getPlan() != null && tenant.getPlan().getMaxUsers() > 0) {
                    int max = tenant.getPlan().getMaxUsers();
                    if (existing + newCount > max) {
                        String detailVi = String.format(
                            "Số người dùng hiện tại: %d, mới: %d, giới hạn: %d, gói: %s",
                            existing,
                            newCount,
                            max,
                            tenant.getPlan().getName()
                        );
                        String detailEn = String.format(
                            "Current users: %d, new: %d, limit: %d, plan: %s",
                            existing,
                            newCount,
                            max,
                            tenant.getPlan().getName()
                        );
                        businessErrors.add(
                            new RowError(
                                0,
                                "plan",
                                isVietnamese ? "Giới hạn gói" : "Plan limit",
                                isVietnamese ? detailVi : detailEn,
                                null
                            )
                        );
                        entry.getValue().clear();
                    }
                }
            }
        }
        if (!businessErrors.isEmpty()) {
            return getImportResponseDTO(businessErrors, result);
        }
        List<User> finalUsersToSave = usersByTenant
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        userRepository.saveAll(finalUsersToSave);
        ImportResponseDTO response = ImportResponseDTO.builder()
            .totalRows(result.getTotalRows())
            .successCount(usersToSave.size())
            .errorCount(0)
            .hasErrors(false)
            .build();
        if (!usersToSave.isEmpty()) {
            List<Map<String, Object>> preview = usersToSave
                .stream()
                .limit(5)
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("username", u.getUsername());
                    map.put("email", u.getEmail());
                    map.put("tenantId", u.getTenantId());
                    map.put("isActivated", u.getIsActivated());
                    return map;
                })
                .toList();
            response.setPreview(preview);
        }
        return response;
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_EXPORT_USER_IMPORT_ERROR, type = ModeType.HANDLE)
    public byte[] exportUserImportError(ImportResponseDTO importResult) throws IOException {
        if (importResult == null || importResult.getErrors() == null || importResult.getErrors().isEmpty()) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        return ImportErrorExcelExport.export(importResult, isVietnamese);
    }

    private ImportResponseDTO toImportResponse(ImportResult<Map<String, Object>> result) {
        List<ImportResponseDTO.ImportErrorDTO> errorDTOs = result
            .getErrors()
            .stream()
            .map(e ->
                ImportResponseDTO.ImportErrorDTO.builder()
                    .row(e.getRowIndex() + 1)
                    .column(e.getColumnHeader())
                    .columnKey(e.getColumnKey())
                    .message(e.getErrorMessage())
                    .value(e.getValue() != null ? e.getValue().toString() : null)
                    .build()
            )
            .toList();

        return ImportResponseDTO.builder()
            .totalRows(result.getTotalRows())
            .successCount(result.getSuccessCount())
            .errorCount(result.getErrorCount())
            .hasErrors(result.hasErrors())
            .errors(errorDTOs)
            .build();
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_GET_USER_BY_ID, type = ModeType.HANDLE)
    public UserDTO findById(Long id) {
        return userRepository
            .findById(id)
            .map(u -> UserMapper.fromEntityWithCms(u, true))
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
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
        Long tenantId = SecurityUtils.getCurrentTenantId();
        if (tenantId != null && userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
            throw new FwException(ErrorMessage.USERNAME_ALREADY_EXISTS);
        }
        if (tenantId != null && userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
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

    @Transactional(rollbackFor = Exception.class)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_USER, type = ModeType.HANDLE)
    public void lockUnlock(SystemUserDTO request) {
        if (request.getId() == null || StringUtils.isBlank(request.getLockReason())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        User su = userRepository
            .findById(request.getId())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        boolean newStatus = !Boolean.TRUE.equals(su.getIsLocked());
        su.setIsLocked(newStatus);
        userRepository.save(su);
        userLockHistoryRepository.save(
            UserLockHistory.builder()
                .targetUserId(request.getId())
                .action(newStatus ? AccountStatus.LOCK : AccountStatus.UNLOCK)
                .reason(request.getLockReason())
                .build()
        );
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
            boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
            Long currentTenantId = SecurityUtils.getCurrentTenantId();
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
                if (StringUtils.isNotBlank(filter.getTenantId()) && isSuperAdmin) {
                    predicates.add(cb.equal(root.get("tenantId"), filter.getTenantId()));
                } else if (!isSuperAdmin) {
                    predicates.add(cb.equal(root.get("tenantId"), currentTenantId));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasOTP(String username) {
        String keyForgotPassword = sessionStore.getKeyForgotPassword(username);
        return StringUtils.isNotEmpty(redisService.get(keyForgotPassword, String.class));
    }

    private ExcelSchema buildImportSchema(boolean isVietnamese, boolean isSuperAdmin) {
        List<String> tenantIds = isSuperAdmin
            ? tenantRepository
                  .findAllActiveTenants()
                  .stream()
                  .map(t -> t.getCode() + " - " + t.getName())
                  .toList()
            : List.of();

        var schema = ExcelSchema.builder()
            .sheetName(isVietnamese ? "Nhập người dùng" : "Import Users")
            .headerRowIndex(0)
            .dataStartRowIndex(1)
            .maxRows(500)
            .strictHeader(true)
            .failFast(false)
            .addColumn(
                ExcelColumn.builder("username", isVietnamese ? "Tên đăng nhập" : "Username")
                    .required()
                    .maxLength(50)
                    .dataType(ExcelDataType.STRING)
                    .comment(
                        isVietnamese
                            ? "Chỉ cho phép chữ thường, số, dấu chấm (.), gạch dưới (_), gạch ngang (-)"
                            : "Only lowercase letters, numbers, dot (.), underscore (_), hyphen (-)"
                    )
                    .customValidator((val, ctx) -> {
                        Validator<UserDTO> validator = new Validator<>(List.of(new UsernameRule<>()));
                        validator.validate(UserDTO.builder().username(String.valueOf(val)).build());
                        return null;
                    })
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("email", "Email")
                    .required()
                    .maxLength(100)
                    .dataType(ExcelDataType.STRING)
                    .comment(isVietnamese ? "Địa chỉ email hợp lệ" : "Valid email address")
                    .customValidator((val, ctx) -> {
                        Validator<UserDTO> validator = new Validator<>(List.of(new EmailRule<>()));
                        validator.validate(UserDTO.builder().email(String.valueOf(val)).build());
                        return null;
                    })
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("password", isVietnamese ? "Mật khẩu" : "Password")
                    .required()
                    .maxLength(100)
                    .dataType(ExcelDataType.STRING)
                    .comment(
                        isVietnamese
                            ? "Tối thiểu 6 ký tự, gồm ít nhất 1 chữ cái, 1 số và 1 ký tự đặc biệt (@$!%*#?&._-)"
                            : "Min 6 chars, at least 1 letter, 1 number, and 1 special char (@$!%*#?&._-)"
                    )
                    .customValidator((val, ctx) -> {
                        Validator<UserDTO> validator = new Validator<>(List.of(new PasswordRule<>()));
                        validator.validate(UserDTO.builder().password(String.valueOf(val)).build());
                        return null;
                    })
                    .build()
            )
            .addColumnCondition(
                isSuperAdmin,
                3,
                ExcelColumn.builder("tenantId", isVietnamese ? "Mã Tenant" : "Tenant ID")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .allowedValues(tenantIds)
                    .comment(
                        isVietnamese
                            ? "Chọn Tenant từ danh sách thả xuống. Hệ thống sẽ tự động ánh xạ từ mã (CODE)."
                            : "Select Tenant from dropdown. System will auto-map from code (CODE)."
                    )
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("isActivated", isVietnamese ? "Kích hoạt" : "Activated")
                    .dataType(ExcelDataType.BOOLEAN)
                    .comment(
                        isVietnamese
                            ? "TRUE = đã kích hoạt, FALSE = chưa kích hoạt. Mặc định: FALSE"
                            : "TRUE = activated, FALSE = not activated. Default: FALSE"
                    )
                    .build()
            )
            .build();
        return schema;
    }

    private ImportResponseDTO getImportResponseDTO(
        List<RowError> businessErrors,
        ImportResult<Map<String, Object>> result
    ) {
        List<RowError> allErrors = new ArrayList<>(businessErrors);
        ImportResult<Map<String, Object>> errorResult = ImportResult.<Map<String, Object>>builder()
            .addErrors(allErrors)
            .totalRows(result.getTotalRows())
            .build();
        return toImportResponse(errorResult);
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

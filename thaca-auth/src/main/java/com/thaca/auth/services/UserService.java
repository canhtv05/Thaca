package com.thaca.auth.services;

import com.thaca.auth.clients.AdminClient;
import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.User;
import com.thaca.auth.domains.UserLockHistory;
import com.thaca.auth.dtos.SystemUserDTO;
import com.thaca.auth.dtos.VerifyEmailTokenDTO;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.mappers.UserMapper;
import com.thaca.auth.repositories.UserLockHistoryRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.utils.TenantEnrichmentHelper;
import com.thaca.common.dtos.internal.ImportResponseDTO;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.UserDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
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
import com.thaca.framework.core.validations.Validator;
import com.thaca.framework.core.validations.rules.EmailRule;
import com.thaca.framework.core.validations.rules.FullnameRule;
import com.thaca.framework.core.validations.rules.PasswordRule;
import com.thaca.framework.core.validations.rules.UsernameRule;
import jakarta.persistence.criteria.Join;
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
    private final UserLockHistoryRepository userLockHistoryRepository;
    private final AdminClient adminClient;
    private final TenantEnrichmentHelper tenantHelper;

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = ServiceMethod.ADMIN_SEARCH_USERS, type = ModeType.HANDLE)
    public SearchResponse<UserDTO> searchUsers(SearchRequest<UserDTO> request) {
        Specification<User> spec = createUserSpecification(request);
        Page<User> users = userRepository.findAll(spec, request.getPage().toPageable(Sort.Direction.DESC, "updatedAt"));
        List<UserDTO> data = users.getContent().stream().map(UserMapper::fromEntity).toList();

        Map<Long, TenantInfoPrj> tenantMap = tenantHelper.fetchTenantMap(tenantHelper.collectTenantIds(data));
        if (!tenantMap.isEmpty()) {
            data.forEach(d -> tenantHelper.enrichTenantInfo(d, tenantMap));
        }

        return new SearchResponse<>(data, PaginationResponse.of(users));
    }

    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = ServiceMethod.ADMIN_EXPORT_USERS, type = ModeType.HANDLE)
    public byte[] exportUsers(SearchRequest<UserDTO> request) throws IOException {
        Specification<User> spec = createUserSpecification(request);
        List<User> userPage = userRepository.findAll(spec);
        List<UserDTO> data = userPage.stream().map(UserMapper::fromEntity).toList();

        Map<Long, TenantInfoPrj> tenantMap = tenantHelper.fetchTenantMap(tenantHelper.collectTenantIds(data));
        if (!tenantMap.isEmpty()) {
            data.forEach(d -> tenantHelper.enrichTenantInfo(d, tenantMap));
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        for (UserDTO user : data) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", user.getUsername());
            row.put("email", user.getEmail());
            String tenantNames = "";
            if (user.getTenantInfos() != null) {
                tenantNames =
                    user.getTenantInfos() != null
                        ? user
                              .getTenantInfos()
                              .stream()
                              .map(t -> t.getCode() + " - " + t.getName())
                              .collect(Collectors.joining(", "))
                        : "";
            }
            row.put("tenantName", tenantNames);
            row.put(
                "isActivated",
                Boolean.TRUE.equals(user.getIsActivated())
                    ? (isVietnamese ? "Đã kích hoạt" : "Activated")
                    : (isVietnamese ? "Chưa kích hoạt" : "Inactive")
            );
            row.put(
                "isLocked",
                Boolean.TRUE.equals(user.getIsLocked())
                    ? (isVietnamese ? "Bị khóa" : "Locked")
                    : (isVietnamese ? "Bình thường" : "Normal")
            );
            row.put("createdAt", user.getCreatedAt());
            row.put("createdBy", user.getCreatedBy());
            row.put("updatedAt", user.getUpdatedAt());
            row.put("updatedBy", user.getUpdatedBy());
            rows.add(row);
        }
        return ExcelEngine.exportData(buildExportSchema(isVietnamese), rows);
    }

    @FwMode(name = ServiceMethod.ADMIN_DETAIL_USER, type = ModeType.VALIDATE)
    public void validateDetailUser(UserDTO request) {
        if (StringUtils.isBlank(request.getUsername())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = ServiceMethod.ADMIN_DETAIL_USER, type = ModeType.HANDLE)
    public UserDTO detailUser(UserDTO request) {
        User user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));

        UserDTO dto = UserMapper.fromEntity(user);
        return tenantHelper.enrichTenantFull(dto);
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = ServiceMethod.ADMIN_DOWNLOAD_USER_TEMPLATE, type = ModeType.HANDLE)
    public byte[] downloadTemplate() throws IOException {
        ApiHeader header = FwContextHeader.get();
        boolean isVietnamese = "vi".equals(header.getLanguage());
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
        return ExcelEngine.generateTemplate(this.buildImportSchema(isVietnamese, isSuperAdmin));
    }

    @Transactional(rollbackFor = Exception.class)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = ServiceMethod.ADMIN_IMPORT_USERS, type = ModeType.HANDLE)
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
        Set<String> existingUsernamesInImport = new HashSet<>();
        Set<String> existingEmailsInImport = new HashSet<>();
        Set<String> usernamesInFile = successRows
            .stream()
            .map(row -> String.valueOf(row.get("username")).trim().toLowerCase())
            .collect(Collectors.toSet());
        Set<String> emailsInFile = successRows
            .stream()
            .map(row -> String.valueOf(row.get("email")).trim().toLowerCase())
            .collect(Collectors.toSet());

        List<Long> currentTenantIds = SecurityUtils.getCurrentTenantIds();
        Set<Long> allRelevantTenantIds = new HashSet<>(currentTenantIds);
        Map<Long, Set<String>> existingEmailsInTenants = new HashMap<>();
        Map<Long, Set<String>> existingUsernamesInTenants = new HashMap<>();

        if (!allRelevantTenantIds.isEmpty()) {
            if (!emailsInFile.isEmpty()) {
                userRepository
                    .findAllByEmailIn(emailsInFile)
                    .forEach(u -> {
                        for (Long tId : u.getTenantIds()) {
                            existingEmailsInTenants
                                .computeIfAbsent(tId, k -> new HashSet<>())
                                .add(u.getEmail().toLowerCase());
                        }
                    });
            }
            if (!usernamesInFile.isEmpty()) {
                userRepository
                    .findByUsernamesAndTenantIds(usernamesInFile, allRelevantTenantIds)
                    .forEach(u -> {
                        for (Long tId : u.getTenantIds()) {
                            existingUsernamesInTenants
                                .computeIfAbsent(tId, k -> new HashSet<>())
                                .add(u.getUsername().toLowerCase());
                        }
                    });
            }
        }

        List<TenantInfoPrj> allTenantsPrj = adminClient.getAllTenants();
        Map<String, Long> tenantCodeToId =
            allTenantsPrj != null
                ? allTenantsPrj.stream().collect(Collectors.toMap(TenantInfoPrj::getCode, TenantInfoPrj::getId))
                : Collections.emptyMap();
        Map<Long, String> tenantIdToLabel =
            allTenantsPrj != null
                ? allTenantsPrj
                      .stream()
                      .collect(Collectors.toMap(TenantInfoPrj::getId, t -> t.getCode() + " - " + t.getName()))
                : Collections.emptyMap();

        List<User> usersToSave = new ArrayList<>();
        List<RowError> businessErrors = new ArrayList<>();
        for (int i = 0; i < successRows.size(); i++) {
            Map<String, Object> row = successRows.get(i);
            int excelRowIndex = result.getTotalRows() > 0 ? i + 1 : i;
            String username = String.valueOf(row.get("username")).trim().toLowerCase();
            String email = String.valueOf(row.get("email")).trim().toLowerCase();
            String password = row.get("password") != null ? String.valueOf(row.get("password")) : null;
            boolean hasError = false;

            if (existingUsernamesInImport.contains(username)) {
                businessErrors.add(
                    new RowError(
                        excelRowIndex,
                        "username",
                        isVietnamese ? "Tên đăng nhập" : "Username",
                        isVietnamese
                            ? "Tên đăng nhập đã tồn tại trong danh sách import"
                            : "Username already exists in import list",
                        username
                    )
                );
                hasError = true;
            }
            if (existingEmailsInImport.contains(email)) {
                businessErrors.add(
                    new RowError(
                        excelRowIndex,
                        "email",
                        "Email",
                        isVietnamese
                            ? "Email đã tồn tại trong danh sách import"
                            : "Email already exists in import list",
                        email
                    )
                );
                hasError = true;
            }

            List<Long> targetTenantIds = new ArrayList<>();
            if (isSuperAdmin) {
                Object tenantRaw = row.get("tenantId");
                if (tenantRaw != null) {
                    String tenantStr = String.valueOf(tenantRaw);
                    String tenantCode = tenantStr.contains(" - ") ? tenantStr.split(" - ")[0].trim() : tenantStr.trim();
                    Long tId = tenantCodeToId.get(tenantCode);
                    if (tId == null) {
                        businessErrors.add(
                            new RowError(
                                excelRowIndex,
                                "tenantId",
                                isVietnamese ? "Mã Tổ chức" : "Tenant ID",
                                isVietnamese ? "Tổ chức không tồn tại" : "Tenant does not exist",
                                tenantStr
                            )
                        );
                        hasError = true;
                    } else {
                        targetTenantIds.add(tId);
                    }
                }
            } else {
                targetTenantIds.addAll(SecurityUtils.getCurrentTenantIds());
            }

            for (Long tId : targetTenantIds) {
                if (existingUsernamesInTenants.getOrDefault(tId, Collections.emptySet()).contains(username)) {
                    String label = tenantIdToLabel.getOrDefault(tId, String.valueOf(tId));
                    businessErrors.add(
                        new RowError(
                            excelRowIndex,
                            "username",
                            isVietnamese ? "Tên đăng nhập" : "Username",
                            isVietnamese
                                ? "Tên đăng nhập đã tồn tại trong tổ chức: " + label
                                : "Username already exists in tenant: " + label,
                            username
                        )
                    );
                    hasError = true;
                    break;
                }
                if (existingEmailsInTenants.getOrDefault(tId, Collections.emptySet()).contains(email)) {
                    String label = tenantIdToLabel.getOrDefault(tId, String.valueOf(tId));
                    businessErrors.add(
                        new RowError(
                            excelRowIndex,
                            "email",
                            "Email",
                            isVietnamese
                                ? "Email đã tồn tại trong tổ chức: " + label
                                : "Email already exists in tenant: " + label,
                            email
                        )
                    );
                    hasError = true;
                    break;
                }
            }

            if (hasError) continue;

            Boolean isActivated =
                row.get("isActivated") != null && Boolean.parseBoolean(String.valueOf(row.get("isActivated")));
            User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .isActivated(isActivated)
                .isLocked(false)
                .tenantIds(new HashSet<>(targetTenantIds))
                .build();
            usersToSave.add(user);
            existingUsernamesInImport.add(username);
            existingEmailsInImport.add(email);
        }

        if (!usersToSave.isEmpty()) {
            Map<Long, List<User>> usersPerTenant = new HashMap<>();
            for (User u : usersToSave) {
                for (Long tId : u.getTenantIds()) {
                    usersPerTenant.computeIfAbsent(tId, k -> new ArrayList<>()).add(u);
                }
            }
            Set<Long> tenantIdsToBatch = usersPerTenant.keySet();
            Map<Long, Long> existingCounts = new HashMap<>();
            for (Map<String, Object> countRow : userRepository.countByTenantIds(tenantIdsToBatch)) {
                existingCounts.put(
                    ((Number) countRow.get("tenantId")).longValue(),
                    ((Number) countRow.get("count")).longValue()
                );
            }

            List<TenantDTO> tenants = adminClient.getTenantsFullByIds(
                TenantDTO.builder().tenantIds(new ArrayList<>(tenantIdsToBatch)).build()
            );
            if (tenants == null) {
                tenants = Collections.emptyList();
            }
            Map<Long, TenantDTO> tenantInfoMap = tenants.stream().collect(Collectors.toMap(TenantDTO::getId, t -> t));

            for (Map.Entry<Long, List<User>> entry : usersPerTenant.entrySet()) {
                Long tId = entry.getKey();
                TenantDTO tenant = tenantInfoMap.get(tId);
                if (tenant != null && tenant.getPlanInfo() != null && tenant.getPlanInfo().getMaxUsers() > 0) {
                    long currentCount = existingCounts.getOrDefault(tId, 0L);
                    if (currentCount + entry.getValue().size() > tenant.getPlanInfo().getMaxUsers()) {
                        String detail = isVietnamese
                            ? String.format(
                                  "Tổ chức %s vượt giới hạn (%d/%d)",
                                  tenant.getName(),
                                  currentCount + entry.getValue().size(),
                                  tenant.getPlanInfo().getMaxUsers()
                              )
                            : String.format(
                                  "Tenant %s exceeds limit (%d/%d)",
                                  tenant.getName(),
                                  currentCount + entry.getValue().size(),
                                  tenant.getPlanInfo().getMaxUsers()
                              );
                        businessErrors.add(
                            new RowError(0, "plan", isVietnamese ? "Giới hạn gói" : "Plan limit", detail, null)
                        );
                    }
                }
            }
        }

        if (!businessErrors.isEmpty()) {
            return getImportResponseDTO(businessErrors, result);
        }
        userRepository.saveAll(usersToSave);
        return ImportResponseDTO.builder()
            .totalRows(result.getTotalRows())
            .successCount(usersToSave.size())
            .errorCount(0)
            .hasErrors(false)
            .preview(
                usersToSave
                    .stream()
                    .limit(5)
                    .map(u -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("username", u.getUsername());
                        map.put("email", u.getEmail());
                        map.put("tenantIds", u.getTenantIds());
                        map.put("isActivated", u.getIsActivated());
                        return map;
                    })
                    .toList()
            )
            .build();
    }

    @Transactional(readOnly = true)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = ServiceMethod.ADMIN_EXPORT_USER_FILE_ERROR, type = ModeType.HANDLE)
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
    @FwMode(name = ServiceMethod.ADMIN_GET_USER_BY_ID, type = ModeType.HANDLE)
    public UserDTO findById(Long id) {
        return userRepository
            .findById(id)
            .map(u -> tenantHelper.enrichTenantFull(UserMapper.fromEntityWithadmin(u, true)))
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
        List<Long> targetTenantIds = request.getTenantIds();
        if (targetTenantIds == null || targetTenantIds.isEmpty()) {
            targetTenantIds = SecurityUtils.getCurrentTenantIds();
        }
        if (targetTenantIds != null && !targetTenantIds.isEmpty()) {
            if (userRepository.existsByUsernameAndTenantIds(request.getUsername(), targetTenantIds)) {
                throw new FwException(ErrorMessage.USERNAME_ALREADY_EXISTS);
            }
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

    @Transactional(rollbackFor = Exception.class)
    @CheckPermission(value = { "USER_MAKER" })
    @FwMode(name = ServiceMethod.ADMIN_LOCK_UNLOCK_USER, type = ModeType.HANDLE)
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
            Long currentTenantId = SecurityUtils.getCurrentTenantId();
            boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
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

                if (isSuperAdmin) {
                    if (filter.getTenantIds() != null && !filter.getTenantIds().isEmpty()) {
                        Join<User, Long> tenantJoin = root.join("tenantIds");
                        predicates.add(tenantJoin.in(filter.getTenantIds()));
                    }
                } else {
                    if (currentTenantId != null) {
                        Join<User, Long> tenantJoin = root.join("tenantIds");
                        predicates.add(cb.equal(tenantJoin, currentTenantId));
                    } else {
                        predicates.add(cb.disjunction());
                    }
                }
            } else if (!isSuperAdmin) {
                if (currentTenantId != null) {
                    Join<User, Long> tenantJoin = root.join("tenantIds");
                    predicates.add(cb.equal(tenantJoin, currentTenantId));
                } else {
                    predicates.add(cb.disjunction());
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
            ? adminClient
                  .getAllTenants()
                  .stream()
                  .map(t -> t.getCode() + " - " + t.getName())
                  .toList()
            : List.of();

        return ExcelSchema.builder()
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
            .addColumnCondition(
                isSuperAdmin,
                2,
                ExcelColumn.builder("tenantId", isVietnamese ? "Mã Tổ chức" : "Tenant ID")
                    .required()
                    .dataType(ExcelDataType.STRING)
                    .allowedValues(tenantIds)
                    .comment(
                        isVietnamese
                            ? "Chọn Tổ chức từ danh sách thả xuống. Hệ thống sẽ tự động ánh xạ từ mã (CODE)."
                            : "Select Tenant from dropdown. System will auto-map from code (CODE)."
                    )
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
    }

    private ExcelSchema buildExportSchema(boolean isVietnamese) {
        return ExcelSchema.builder()
            .sheetName(isVietnamese ? "Danh sách người dùng" : "User List")
            .headerRowIndex(0)
            .dataStartRowIndex(1)
            .strictHeader(true)
            .failFast(false)
            .addColumn(
                ExcelColumn.builder("username", isVietnamese ? "Tên người dùng" : "Username")
                    .maxLength(50)
                    .dataType(ExcelDataType.STRING)
                    .comment(isVietnamese ? "Tên người dùng" : "Username")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("email", "Email")
                    .maxLength(100)
                    .dataType(ExcelDataType.STRING)
                    .comment("Email")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("tenantName", isVietnamese ? "Tổ chức" : "Tenant")
                    .comment(isVietnamese ? "Tổ chức" : "Tenant")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("isActivated", isVietnamese ? "Kích hoạt" : "Activated")
                    .comment(isVietnamese ? "Kích hoạt" : "Activated")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("isLocked", isVietnamese ? "Khóa" : "Locked")
                    .comment(isVietnamese ? "Bị khóa" : "Locked")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("createdAt", isVietnamese ? "Ngày tạo" : "Created At")
                    .dataType(ExcelDataType.DATE)
                    .comment(isVietnamese ? "Ngày tạo" : "Created At")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("createdBy", isVietnamese ? "Người tạo" : "Created By")
                    .dataType(ExcelDataType.STRING)
                    .comment(isVietnamese ? "Người tạo" : "Created By")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("updatedAt", isVietnamese ? "Ngày cập nhật" : "Updated At")
                    .dataType(ExcelDataType.DATE)
                    .comment(isVietnamese ? "Ngày cập nhật" : "Updated At")
                    .build()
            )
            .addColumn(
                ExcelColumn.builder("updatedBy", isVietnamese ? "Người cập nhật" : "Updated By")
                    .dataType(ExcelDataType.STRING)
                    .comment(isVietnamese ? "Người cập nhật" : "Updated By")
                    .build()
            )
            .build();
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
}

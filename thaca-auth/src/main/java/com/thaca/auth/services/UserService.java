package com.thaca.auth.services;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.auth.domains.Role;
import com.thaca.auth.domains.User;
import com.thaca.auth.domains.UserPermission;
import com.thaca.auth.dtos.ImportUserDTO;
import com.thaca.auth.dtos.UserDTO;
import com.thaca.auth.dtos.UserPermissionDTO;
import com.thaca.auth.dtos.UserProfileDTO;
import com.thaca.auth.dtos.excel.ExcelTemplateConfig;
import com.thaca.auth.dtos.excel.ImportExcelResult;
import com.thaca.auth.dtos.excel.ReadExcelResult;
import com.thaca.auth.dtos.excel.RowData;
import com.thaca.auth.dtos.excel.RowHeader;
import com.thaca.auth.dtos.req.ChangePasswordReq;
import com.thaca.auth.dtos.req.ForgotPasswordReq;
import com.thaca.auth.dtos.req.ResetPasswordReq;
import com.thaca.auth.dtos.req.VerifyOTPReq;
import com.thaca.auth.dtos.search.SearchRequest;
import com.thaca.auth.dtos.search.SearchResponse;
import com.thaca.auth.enums.ErrorMessage;
import com.thaca.auth.enums.PermissionAction;
import com.thaca.auth.repositories.RoleRepository;
import com.thaca.auth.repositories.UserPermissionRepository;
import com.thaca.auth.repositories.UserRepository;
import com.thaca.auth.utils.ExcelBuilder;
import com.thaca.common.constants.EventConstants;
import com.thaca.common.dtos.events.ForgotPasswordEvent;
import com.thaca.common.dtos.events.UserCreationEvent;
import com.thaca.common.dtos.events.VerificationEmailEvent;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.framework.blocking.starter.configs.cache.RedisCacheService;
import com.thaca.framework.blocking.starter.services.CommonService;
import com.thaca.framework.blocking.starter.services.SessionStore;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.constants.AuthoritiesConstants;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.DateUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    private final ValidatorFactory validator = Validation.buildDefaultValidatorFactory();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final AuthService authService;
    private final KafkaProducerService kafkaProducerService;
    private final RedisCacheService redisService;
    private final SessionStore sessionStore;

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return userRepository
            .findById(id)
            .map(UserDTO::fromEntity)
            .orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional
    public UserDTO createUser(UserDTO request, boolean isAdmin) {
        if (userRepository.existsUserByUsername(request.getUsername())) {
            throw new FwException(ErrorMessage.USERNAME_ALREADY_EXITS);
        }

        if (request.getEmail().contains("+")) {
            throw new FwException(ErrorMessage.EMAIL_INVALID);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new FwException(ErrorMessage.EMAIL_ALREADY_EXITS);
        }

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
                    new VerificationEmailEvent(request.getEmail(), request.getUsername(), request.getFullname())
                );
            } else {
                kafkaProducerService.send(
                    EventConstants.USER_CREATED_TOPIC,
                    new UserCreationEvent(user.getUsername(), request.getFullname())
                );
            }
        } catch (Exception e) {
            throw new FwException(CommonErrorMessage.INTERNAL_SERVER_ERROR);
        }
        return UserDTO.fromEntity(res);
    }

    @Transactional
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

    @Transactional
    public void changeLockUser(Long id, boolean isLocked) {
        User user = userRepository.findById(id).orElseThrow(() -> new FwException(ErrorMessage.USER_NOT_FOUND));
        user.setLocked(isLocked);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(ResetPasswordReq request) {
        if (CommonUtils.isEmpty(request.getEmail(), request.getNewPassword(), request.getOTP())) {
            throw new FwException(CommonErrorMessage.VALIDATION_ERROR);
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
    public void verifyForgotPasswordOTP(VerifyOTPReq request) {
        if (CommonUtils.isEmpty(request.getEmail(), request.getOTP())) {
            throw new FwException(CommonErrorMessage.VALIDATION_ERROR);
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

    @Transactional
    public void changePassword(String cookieValue, ChangePasswordReq req, HttpServletResponse response) {
        if (CommonUtils.isEmpty(req.getCurrentPassword(), req.getNewPassword())) {
            throw new FwException(CommonErrorMessage.VALIDATION_ERROR);
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
            new ForgotPasswordEvent(request.getEmail(), user.getUsername())
        );
    }

    @Transactional
    public void updateUserProfile(UserProfileDTO request) {
        String userLogin = CommonService.getCurrentUserLogin();
        Optional<User> optionalUser = userRepository.findByUsername(userLogin);
        if (optionalUser.isEmpty()) {
            throw new FwException(ErrorMessage.USER_NOT_FOUND);
        }
        userRepository.save(optionalUser.get());
    }

    @Transactional(readOnly = true)
    public SearchResponse<UserDTO> searchDatatable(SearchRequest request) {
        Specification<User> spec = createSpecification(request);
        Page<User> tenants = userRepository.findAll(spec, request.toPageable());
        return new SearchResponse<>(
            tenants.getContent().stream().map(UserDTO::fromEntity).collect(Collectors.toList()),
            tenants.getTotalElements()
        );
    }

    private Specification<User> createSpecification(SearchRequest criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(criteria.searchText())) {
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("username")), "%" + criteria.searchText().toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("username")), "%" + criteria.searchText().toLowerCase() + "%")
                    )
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public byte[] exportUser(SearchRequest request) {
        Specification<User> spec = createSpecification(request);
        List<User> data = userRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"));
        List<String> headers = List.of("#", "Tên đăng nhập", "Email", "Trạng thái", "Ngày tạo");
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Data");
            CellStyle headerStyle = ExcelBuilder.createHeaderStyle(wb);
            ExcelBuilder.createHeaderRow(sheet, headerStyle, headers);
            int r = 1;
            for (User item : data) {
                Row row = sheet.createRow(r);
                Cell stt = row.createCell(0);
                stt.setCellValue(r);
                stt.setCellStyle(headerStyle);

                row.createCell(1).setCellValue(item.getUsername());
                row.createCell(2).setCellValue(item.isActivated() ? "Hoạt động" : "Không hoạt động");
                row.createCell(3).setCellValue(DateUtils.dateToString(item.getCreatedAt()));
                r++;
            }
            IntStream.range(1, headers.size()).forEach(sheet::autoSizeColumn);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new FwException(CommonErrorMessage.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public byte[] downloadTemplate() {
        List<String> roleCodes = roleRepository
            .findAll()
            .stream()
            .map(Role::getCode)
            .filter(t -> !AuthoritiesConstants.ADMIN.equalsIgnoreCase(t))
            .collect(Collectors.toList());
        ExcelTemplateConfig config = ExcelTemplateConfig.builder()
            .headers(List.of("Tên đăng nhập", "Email", "Vai trò"))
            .fieldRequired(List.of(1, 2, 3))
            .autoNumber(true)
            .listValidations(new ArrayList<>())
            .build();
        config
            .getListValidations()
            .add(ExcelTemplateConfig.ExcelValidation.builder().rangeName("_ROLE").rowIndex(3).data(roleCodes).build());
        return ExcelBuilder.buildFileTemplate(config);
    }

    @Transactional
    public ImportExcelResult<ImportUserDTO> importUser(MultipartFile file) {
        ImportExcelResult<ImportUserDTO> result = new ImportExcelResult<>();
        List<RowHeader> rowHeaders = List.of(
            new RowHeader("Tên đăng nhập", "username", 1),
            new RowHeader("Vai trò", "role", 2)
        );
        ReadExcelResult mapData = ExcelBuilder.readFileExcel(file, rowHeaders);
        List<ImportUserDTO> fileData = mapData.getData().stream().map(ImportUserDTO::fromExcelData).toList();
        result.getHeaders().addAll(rowHeaders);
        List<String> usernames = fileData
            .stream()
            .map(ImportUserDTO::getUsername)
            .filter(StringUtils::isNotBlank)
            .toList();
        List<String> existingData = userRepository.findUserExitsUsername(usernames);
        IntStream.range(0, fileData.size()).forEach(i -> {
            ImportUserDTO dto = fileData.get(i);
            RowData<ImportUserDTO> rowError = new RowData<>(dto);
            Set<ConstraintViolation<ImportUserDTO>> violations = validator.getValidator().validate(dto);
            for (var v : violations) {
                rowError.addFieldError(v.getPropertyPath().toString(), v.getMessage());
            }
            if (StringUtils.isNotBlank(dto.getUsername()) && existingData.contains(dto.getUsername())) {
                rowError.addFieldError("username", "Tên đăng nhập đã tồn tại.");
            }
            if (rowError.hasErrors()) {
                result.getRows().add(rowError);
            }
        });
        if (!result.isHasErrors()) {
            List<String> roleCodes = fileData.stream().map(ImportUserDTO::getRole).toList();
            List<Role> roleList = roleRepository.findAllByCodeIn(roleCodes);
            List<User> users = fileData
                .stream()
                .map(dto ->
                    dto.fromEntity(
                        passwordEncoder.encode(dto.getPassword()),
                        roleList
                            .stream()
                            .filter(r -> r.getCode().equals(dto.getRole()))
                            .collect(Collectors.toSet())
                    )
                )
                .toList();
            userRepository.saveAll(users);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, PermissionAction> getUserPermissions(Long userId) {
        List<UserPermission> data = userPermissionRepository.findAllByUserId(userId);
        return data.stream().collect(Collectors.toMap(UserPermission::getPermissionCode, UserPermission::getAction));
    }

    @Transactional
    public void updateUserPermission(Long userId, List<UserPermissionDTO> request) {
        userPermissionRepository.deleteAllByUserId(userId);
        userPermissionRepository.saveAll(
            request
                .stream()
                .map(item -> item.fromEntity(userId))
                .collect(Collectors.toSet())
        );
    }

    private boolean hasOTP(String username) {
        String keyForgotPassword = sessionStore.getKeyForgotPassword(username);
        return StringUtils.isNotEmpty(redisService.get(keyForgotPassword, String.class));
    }
}

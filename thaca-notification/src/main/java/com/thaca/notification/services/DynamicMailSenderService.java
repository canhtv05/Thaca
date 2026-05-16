package com.thaca.notification.services;

import com.thaca.common.dtos.search.PaginationResponse;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.common.enums.CommonErrorMessage;
import com.thaca.common.enums.CommonStatus;
import com.thaca.framework.blocking.starter.services.CommonService;
import com.thaca.framework.core.annotations.FwMode;
import com.thaca.framework.core.enums.ModeType;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.notification.constants.ServiceMethod;
import com.thaca.notification.domains.MailConfig;
import com.thaca.notification.dtos.MailConfigDTO;
import com.thaca.notification.enums.NotificationErrorMessage;
import com.thaca.notification.repositories.MailConfigRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailSenderService {

    private final MailConfigRepository mailConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender defaultMailSender;

    @FwMode(name = ServiceMethod.MAIL_CONFIG_SEARCH, type = ModeType.VALIDATE)
    public void validateSearchMailConfig(SearchRequest<MailConfigDTO> request) {
        CommonService.validateSearchRequest(request);
    }

    @Transactional(readOnly = true)
    //    @CheckPermission(value = { "USER_MAKER", "USER_VIEWER" })
    @FwMode(name = ServiceMethod.MAIL_CONFIG_SEARCH, type = ModeType.HANDLE)
    public SearchResponse<MailConfigDTO> searchMailConfig(SearchRequest<MailConfigDTO> request) {
        Specification<MailConfig> spec = createMailConfigSpecification(request);
        Page<MailConfig> configs = mailConfigRepository.findAll(
            spec,
            request.getPage().toPageable(Sort.Direction.DESC, "updatedAt")
        );
        return new SearchResponse<>(
            configs.getContent().stream().map(MailConfigDTO::fromEntity).toList(),
            PaginationResponse.of(configs)
        );
    }

    @Transactional(readOnly = true)
    public MailConfig getConfigById(Long id) {
        return mailConfigRepository
            .findById(id)
            .orElseThrow(() -> new FwException(NotificationErrorMessage.MAIL_CONFIG_NOT_FOUND));
    }

    @FwMode(name = ServiceMethod.MAIL_CONFIG_CREATE, type = ModeType.VALIDATE)
    public void validateCreate(MailConfigDTO request) {
        this.validate(request, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.MAIL_CONFIG_CREATE)
    public void createConfig(MailConfigDTO request) {
        MailConfig saved = new MailConfig();
        saved.setTenantId(request.getTenantId());
        mappingMailConfig(saved, request);
        mailConfigRepository.save(saved);
        normalizeDefault(saved);
    }

    @FwMode(name = ServiceMethod.MAIL_CONFIG_CREATE, type = ModeType.VALIDATE)
    public void validateUpdate(MailConfigDTO request) {
        this.validate(request, false);
    }

    @Transactional(rollbackFor = Exception.class)
    @FwMode(name = ServiceMethod.MAIL_CONFIG_CREATE)
    public void updateConfig(MailConfigDTO request) {
        MailConfig existing = getConfigById(request.getId());
        mappingMailConfig(existing, request);
        MailConfig saved = mailConfigRepository.save(existing);
        normalizeDefault(saved);
    }

    @Transactional
    public void deleteConfig(Long id) {
        MailConfig existing = getConfigById(id);
        mailConfigRepository.delete(existing);
    }

    public JavaMailSender getMailSender(String configCode) {
        MailConfig config = mailConfigRepository
            .findFirstByConfigCodeAndStatus(configCode, CommonStatus.ACTIVE)
            .orElseThrow(() -> new FwException(NotificationErrorMessage.MAIL_CONFIG_NOT_FOUND));

        return createMailSenderInstance(config);
    }

    public JavaMailSender getMailSender() {
        return getTenantMailSender(String.valueOf(SecurityUtils.getCurrentTenantId()));
    }

    public JavaMailSender getTenantMailSender(String tenantId) {
        MailConfig config = resolveMailConfig(tenantId);
        if (config == null) {
            if (defaultMailSender == null) {
                throw new FwException(NotificationErrorMessage.MAIL_CONFIG_MISSING);
            }
            return defaultMailSender;
        }

        return createMailSenderInstance(config);
    }

    public JavaMailSender getMailSender(String tenantId, String configCode) {
        MailConfig config = resolveMailConfigByCode(tenantId, configCode);
        return createMailSenderInstance(config);
    }

    private JavaMailSender createMailSenderInstance(MailConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", config.getIsAuth() != null ? config.getIsAuth().toString() : "true");
        props.put(
            "mail.smtp.starttls.enable",
            config.getIsStarttls() != null ? config.getIsStarttls().toString() : "true"
        );
        return mailSender;
    }

    public String getSenderAddress() {
        return getTenantSenderAddress(String.valueOf(SecurityUtils.getCurrentTenantId()));
    }

    public String getTenantSenderAddress(String tenantId) {
        MailConfig config = resolveMailConfig(tenantId);
        if (config == null) {
            return getLocalSenderAddress();
        }
        return resolveSenderAddress(config);
    }

    public String getSenderAddress(String configCode) {
        return mailConfigRepository
            .findFirstByConfigCodeAndStatus(configCode, CommonStatus.ACTIVE)
            .map(this::resolveSenderAddress)
            .orElse(getLocalSenderAddress());
    }

    public String getSenderAddress(String tenantId, String configCode) {
        return resolveSenderAddress(resolveMailConfigByCode(tenantId, configCode));
    }

    private MailConfig resolveMailConfig(String tenantId) {
        if (!isNullTenant(tenantId)) {
            var tenantDefault = mailConfigRepository.findFirstByTenantIdAndIsDefaultTrueAndStatus(
                tenantId,
                CommonStatus.ACTIVE
            );
            if (tenantDefault.isPresent()) {
                return tenantDefault.get();
            }
            var tenantConfig = mailConfigRepository.findFirstByTenantIdAndStatusOrderByIsDefaultDescIdAsc(
                tenantId,
                CommonStatus.ACTIVE
            );
            if (tenantConfig.isPresent()) {
                return tenantConfig.get();
            }
        }
        return mailConfigRepository
            .findFirstByTenantIdIsNullAndIsDefaultTrueAndStatus(CommonStatus.ACTIVE)
            .orElseGet(() ->
                mailConfigRepository
                    .findFirstByTenantIdIsNullAndStatusOrderByIsDefaultDescIdAsc(CommonStatus.ACTIVE)
                    .orElse(null)
            );
    }

    private MailConfig resolveMailConfigByCode(String tenantId, String configCode) {
        if (!isNullTenant(tenantId)) {
            var tenantConfig = mailConfigRepository.findFirstByTenantIdAndConfigCodeAndStatus(
                tenantId,
                configCode,
                CommonStatus.ACTIVE
            );
            if (tenantConfig.isPresent()) {
                return tenantConfig.get();
            }
        }
        return mailConfigRepository
            .findFirstByConfigCodeAndStatus(configCode, CommonStatus.ACTIVE)
            .orElseThrow(() -> new FwException(NotificationErrorMessage.MAIL_CONFIG_NOT_FOUND));
    }

    public JavaMailSender getLocalMailSender() {
        if (defaultMailSender == null) {
            throw new FwException(NotificationErrorMessage.MAIL_CONFIG_MISSING);
        }
        return defaultMailSender;
    }

    public String getLocalSenderAddress() {
        if (defaultMailSender instanceof JavaMailSenderImpl impl) {
            String username = impl.getUsername();
            if (username != null && !username.isBlank()) {
                return username;
            }
        }
        return "no-reply@thaca.com";
    }

    private String resolveSenderAddress(MailConfig config) {
        if (config.getFromEmail() != null && !config.getFromEmail().isBlank()) {
            return config.getFromEmail();
        }
        if (config.getUsername() != null && !config.getUsername().isBlank()) {
            return config.getUsername();
        }
        return getLocalSenderAddress();
    }

    private boolean isNullTenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() || "null".equalsIgnoreCase(tenantId);
    }

    private void normalizeDefault(MailConfig config) {
        if (!Boolean.TRUE.equals(config.getIsDefault())) {
            return;
        }
        if (isNullTenant(config.getTenantId())) {
            mailConfigRepository.clearDefaultForSystem(config.getId());
            return;
        }
        mailConfigRepository.clearDefaultForTenant(config.getTenantId(), config.getId());
    }

    private void mappingMailConfig(MailConfig source, MailConfigDTO target) {
        source.setConfigCode(target.getConfigCode());
        source.setDescription(target.getDescription());
        source.setFromName(target.getFromName());
        source.setFromEmail(target.getFromEmail());
        source.setHost(target.getHost());
        source.setPort(target.getPort());
        source.setUsername(target.getUsername());
        source.setIsAuth(target.getIsAuth());
        source.setIsStarttls(target.getIsStarttls());
        source.setStatus(target.getStatus());
        source.setIsDefault(target.getIsDefault());
        if (!passwordEncoder.matches(target.getPassword(), source.getPassword())) {
            source.setPassword(passwordEncoder.encode(target.getPassword()));
        }
    }

    public void validate(MailConfigDTO req, boolean isCreate) {
        if (isCreate && req.getTenantId() == null && !SecurityUtils.isSuperAdmin()) {
            req.setTenantId(String.valueOf(SecurityUtils.getCurrentTenantId()));
        }
        if (req.getStatus() == null) {
            req.setStatus(CommonStatus.ACTIVE);
        }
        if (StringUtils.isAnyBlank(req.getHost(), req.getUsername(), req.getPassword())) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
        if (req.getPort() == null || (req.getPort() != 587 && req.getPort() != 465 && req.getPort() != 25)) {
            throw new FwException(CommonErrorMessage.REQUEST_INVALID_PARAMS);
        }
    }

    private Specification<MailConfig> createMailConfigSpecification(SearchRequest<MailConfigDTO> req) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            Long tenantId = SecurityUtils.getCurrentTenantId();
            boolean isSuperAdmin = SecurityUtils.isSuperAdmin();
            if (!isSuperAdmin) {
                if (tenantId == null) return cb.disjunction();
                p.add(cb.equal(root.get("tenantId"), String.valueOf(tenantId)));
            }
            if (req.getFilter() == null) return cb.and(p.toArray(new Predicate[0]));
            MailConfigDTO f = req.getFilter();
            if (StringUtils.isNotBlank(f.getConfigCode())) p.add(
                cb.like(cb.lower(root.get("configCode")), "%" + f.getConfigCode().toLowerCase() + "%")
            );
            if (StringUtils.isNotBlank(f.getDescription())) p.add(
                cb.like(cb.lower(root.get("description")), "%" + f.getDescription().toLowerCase() + "%")
            );
            if (StringUtils.isNotBlank(f.getFromName())) p.add(
                cb.like(cb.lower(root.get("fromName")), "%" + f.getFromName().toLowerCase() + "%")
            );
            if (StringUtils.isNotBlank(f.getFromEmail())) p.add(
                cb.like(cb.lower(root.get("fromEmail")), "%" + f.getFromEmail().toLowerCase() + "%")
            );
            if (StringUtils.isNotBlank(f.getHost())) p.add(
                cb.like(cb.lower(root.get("host")), "%" + f.getHost().toLowerCase() + "%")
            );
            if (StringUtils.isNotBlank(f.getUsername())) p.add(
                cb.like(cb.lower(root.get("username")), "%" + f.getUsername().toLowerCase() + "%")
            );
            if (f.getStatus() != null) p.add(cb.equal(root.get("status"), f.getStatus()));
            if (f.getIsDefault() != null) p.add(cb.equal(root.get("isDefault"), f.getIsDefault()));
            if (f.getIsAuth() != null) p.add(cb.equal(root.get("isAuth"), f.getIsAuth()));
            if (f.getIsStarttls() != null) p.add(cb.equal(root.get("isStarttls"), f.getIsStarttls()));
            if (f.getPort() != null) p.add(cb.equal(root.get("port"), f.getPort()));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}

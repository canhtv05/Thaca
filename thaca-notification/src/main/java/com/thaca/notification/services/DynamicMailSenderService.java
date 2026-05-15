package com.thaca.notification.services;

import com.thaca.common.enums.CommonStatus;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.security.SecurityUtils;
import com.thaca.notification.domains.MailConfig;
import com.thaca.notification.enums.NotificationErrorMessage;
import com.thaca.notification.repositories.MailConfigRepository;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailSenderService {

    private final MailConfigRepository mailConfigRepository;

    @Autowired(required = false)
    private JavaMailSender defaultMailSender;

    /**
     * Lấy cấu hình mail mặc định từ file .env / application.yml
     */
    public JavaMailSender getLocalMailSender() {
        if (defaultMailSender == null) {
            throw new FwException(
                NotificationErrorMessage.MAIL_CONFIG_MISSING,
                "Local mail configuration (env) is not defined."
            );
        }
        return defaultMailSender;
    }

    @Transactional(readOnly = true)
    public List<MailConfig> getAllConfigs() {
        return mailConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MailConfig getConfigById(Long id) {
        return mailConfigRepository
            .findById(id)
            .orElseThrow(() ->
                new FwException(NotificationErrorMessage.MAIL_CONFIG_NOT_FOUND, "Mail configuration " + "not found.")
            );
    }

    @Transactional
    public MailConfig createConfig(MailConfig config) {
        if (config.getTenantId() == null) {
            config.setTenantId(String.valueOf(SecurityUtils.getCurrentTenantId()));
        }
        if (config.getStatus() == null) {
            config.setStatus(CommonStatus.ACTIVE);
        }
        return mailConfigRepository.save(config);
    }

    @Transactional
    public MailConfig updateConfig(Long id, MailConfig req) {
        MailConfig existing = getConfigById(id);
        existing.setHost(req.getHost());
        existing.setPort(req.getPort());
        existing.setUsername(req.getUsername());
        existing.setPassword(req.getPassword());
        existing.setIsAuth(req.getIsAuth());
        existing.setIsStarttls(req.getIsStarttls());
        existing.setStatus(req.getStatus());
        return mailConfigRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(Long id) {
        MailConfig existing = getConfigById(id);
        mailConfigRepository.delete(existing);
    }

    /**
     * Lấy cấu hình gửi mail dựa trên mã cấu hình cụ thể (configCode). Dùng cho các trường hợp muốn chỉ định đích danh
     * Sender (VD: gửi bằng Gmail cá nhân).
     */
    public JavaMailSender getMailSender(String configCode) {
        MailConfig config = mailConfigRepository
            .findFirstByConfigCodeAndStatus(configCode, CommonStatus.ACTIVE)
            .orElseThrow(() -> new FwException(NotificationErrorMessage.MAIL_CONFIG_NOT_FOUND));

        return createMailSenderInstance(config);
    }

    /**
     * Lấy cấu hình gửi mail cho ngữ cảnh hiện tại. Tự động lấy định danh từ SecurityContext.
     */
    public JavaMailSender getMailSender() {
        String tenantId = String.valueOf(SecurityUtils.getCurrentTenantId());
        MailConfig config = resolveMailConfig(tenantId);

        if (config == null) {
            if (defaultMailSender == null) {
                throw new FwException(NotificationErrorMessage.MAIL_CONFIG_MISSING);
            }
            log.debug(">>>> Using DEFAULT external MailSender configuration");
            return defaultMailSender;
        }

        return createMailSenderInstance(config);
    }

    private JavaMailSender createMailSenderInstance(MailConfig config) {
        log.debug(">>>> Using MailSender configuration: host={}, user={}", config.getHost(), config.getUsername());
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

    /**
     * Hàm dùng để lấy thông tin tài khoản người gửi (from address)
     */
    public String getSenderAddress() {
        String tenantId = String.valueOf(SecurityUtils.getCurrentTenantId());
        MailConfig config = resolveMailConfig(tenantId);
        return (config != null && config.getUsername() != null) ? config.getUsername() : "no-reply@thaca.com";
    }

    public String getSenderAddress(String configCode) {
        return mailConfigRepository
            .findFirstByConfigCodeAndStatus(configCode, CommonStatus.ACTIVE)
            .map(MailConfig::getUsername)
            .orElse("no-reply@thaca.com");
    }

    private MailConfig resolveMailConfig(String tenantId) {
        if (!"null".equals(tenantId)) {
            var tenantConfig = mailConfigRepository.findFirstByTenantIdAndStatus(tenantId, CommonStatus.ACTIVE);
            if (tenantConfig.isPresent()) {
                return tenantConfig.get();
            }
        }
        return mailConfigRepository.findFirstByTenantIdIsNullAndStatus(CommonStatus.ACTIVE).orElse(null);
    }
}

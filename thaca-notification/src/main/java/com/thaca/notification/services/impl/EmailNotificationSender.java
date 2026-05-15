package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.SendOtpEvent;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.notification.services.DynamicMailSenderService;
import com.thaca.notification.services.EmailInlineResourceService;
import com.thaca.notification.services.NotificationSender;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
public class EmailNotificationSender implements NotificationSender {

    private final DynamicMailSenderService dynamicMailSenderService;
    private final EmailInlineResourceService emailInlineResourceService;
    private final SpringTemplateEngine templateEngine;
    private final MessageSource messageSource;

    public EmailNotificationSender(
        DynamicMailSenderService dynamicMailSenderService,
        EmailInlineResourceService emailInlineResourceService,
        @Qualifier("emailTemplateEngine") SpringTemplateEngine templateEngine,
        MessageSource messageSource
    ) {
        this.dynamicMailSenderService = dynamicMailSenderService;
        this.emailInlineResourceService = emailInlineResourceService;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    @Override
    public <T extends DomainEvent> void send(String recipient, String content, T event) {
        try {
            if (recipient == null || recipient.isBlank()) {
                log.warn("[EmailNotificationSender] Recipient is blank, skip sending for event {}", event.eventType());
                return;
            }
            String lang = Optional.ofNullable(FwContextHeader.get()).map(ApiHeader::getLanguage).orElse("vi");
            var metadata = event.metadata();
            String configCode = metadata != null ? (String) metadata.get("configCode") : null;
            boolean isOtpEvent = event instanceof SendOtpEvent;
            boolean useDefaultConfig =
                isOtpEvent || (metadata != null && Boolean.TRUE.equals(metadata.get("useDefaultConfig")));
            JavaMailSender mailSender;
            String senderAddress;

            if (useDefaultConfig) {
                mailSender = dynamicMailSenderService.getLocalMailSender();
                senderAddress = dynamicMailSenderService.getLocalSenderAddress();
            } else if (configCode != null && !configCode.isEmpty()) {
                mailSender = dynamicMailSenderService.getMailSender(configCode);
                senderAddress = dynamicMailSenderService.getSenderAddress(configCode);
            } else {
                mailSender = dynamicMailSenderService.getMailSender();
                senderAddress = dynamicMailSenderService.getSenderAddress();
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Locale locale = Locale.forLanguageTag(lang);
            Context context = new Context(locale);
            context.setVariable("otpCode", content);
            if (content != null && !content.isBlank()) {
                context.setVariable(
                    "otpDigits",
                    content
                        .trim()
                        .chars()
                        .mapToObj(ch -> String.valueOf((char) ch))
                        .collect(Collectors.toList())
                );
            }

            String htmlContent = templateEngine.process("otp-email", context);
            if (htmlContent == null || htmlContent.isBlank()) {
                log.error("[EmailNotificationSender] Rendered HTML is empty, abort send to {}", recipient);
                return;
            }

            String subject = messageSource.getMessage("email.otp.subject", null, locale);
            String plainText = buildPlainText(content, locale);

            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setFrom(senderAddress);
            helper.setText(plainText, htmlContent);
            emailInlineResourceService.attachOtpEmailAssets(helper);

            mailSender.send(message);
            log.info("[EmailNotificationSender] Sent email to: {}", recipient);
        } catch (Exception e) {
            log.error("[EmailNotificationSender] Error sending email: ", e);
        }
    }

    private String buildPlainText(String otpCode, Locale locale) {
        String intro = messageSource.getMessage("email.otp.intro", null, locale);
        String label = messageSource.getMessage("email.otp.label", null, locale);
        String validity = messageSource.getMessage("email.otp.validity", null, locale);
        String warning = messageSource.getMessage("email.otp.warning", null, locale);
        return """
        THACA - Xac thuc OTP

        %s

        %s: %s

        %s

        %s
        """.formatted(intro, label, otpCode != null ? otpCode.trim() : "", validity, warning);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}

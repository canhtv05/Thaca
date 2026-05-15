package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.SendOtpEvent;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.framework.core.context.FwContextHeader;
import com.thaca.framework.core.dtos.ApiHeader;
import com.thaca.notification.services.DynamicMailSenderService;
import com.thaca.notification.services.NotificationSender;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final DynamicMailSenderService dynamicMailSenderService;
    private final SpringTemplateEngine templateEngine;
    private final org.springframework.context.MessageSource messageSource;

    @Override
    public <T extends DomainEvent> void send(String recipient, String content, T event) {
        try {
            String lang = Optional.ofNullable(FwContextHeader.get()).map(ApiHeader::getLanguage).orElse("vi");
            var metadata = event.metadata();
            String configCode = metadata != null ? (String) metadata.get("configCode") : null;
            boolean isOtpEvent = event instanceof SendOtpEvent;
            boolean useLocalConfig =
                isOtpEvent || (metadata != null && Boolean.TRUE.equals(metadata.get("useDefaultConfig")));
            JavaMailSender mailSender;
            String senderAddress;

            if (useLocalConfig) {
                mailSender = dynamicMailSenderService.getLocalMailSender();
                senderAddress = dynamicMailSenderService.getSenderAddress();
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

            String htmlContent = templateEngine.process("otp-email", context);

            String subject = messageSource.getMessage("email.otp.subject", null, locale);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(senderAddress);

            mailSender.send(message);
            log.info("[EmailNotificationSender] Sent email to: {}", recipient);
        } catch (Exception e) {
            log.error("[EmailNotificationSender] Error sending email: ", e);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}

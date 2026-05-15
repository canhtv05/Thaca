package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.notification.services.NotificationSender;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
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

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void send(String recipient, String content, Map<String, Object> metadata) {
        try {
            String lang = Optional.ofNullable(com.thaca.framework.core.context.FwContextHeader.get())
                .map(com.thaca.framework.core.dtos.ApiHeader::getLanguage)
                .orElse("vi");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("otpCode", content);

            // Add translations based on language
            if ("en".equalsIgnoreCase(lang)) {
                context.setVariable("t_subject", "[THACA] Your OTP Verification Code");
                context.setVariable("t_greeting", "Hello");
                context.setVariable(
                    "t_intro",
                    "You just requested an OTP for authentication. Please use the code below:"
                );
                context.setVariable("t_validity", "This code is valid for 5 minutes. Do not share it with anyone.");
                context.setVariable(
                    "t_warning",
                    "If you did not request this, please ignore this email or contact support."
                );
                context.setVariable("t_footer", "All rights reserved.");
                context.setVariable("t_home", "Home");
                context.setVariable("t_support", "Support");
            } else {
                context.setVariable("t_subject", "[THACA] Mã xác thực OTP của bạn");
                context.setVariable("t_greeting", "Xin chào");
                context.setVariable(
                    "t_intro",
                    "Bạn vừa yêu cầu mã xác thực OTP để thực hiện giao dịch hoặc đăng nhập. Vui lòng sử dụng mã dưới đây:"
                );
                context.setVariable(
                    "t_validity",
                    "Mã này có hiệu lực trong 5 phút. Tuyệt đối không chia sẻ mã này với bất kỳ ai."
                );
                context.setVariable(
                    "t_warning",
                    "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này hoặc liên hệ hỗ trợ."
                );
                context.setVariable("t_footer", "Tất cả quyền được bảo lưu.");
                context.setVariable("t_home", "Trang chủ");
                context.setVariable("t_support", "Hỗ trợ");
            }

            String htmlContent = templateEngine.process("otp-email", context);

            helper.setTo(recipient);
            helper.setSubject((String) context.getVariable("t_subject"));
            helper.setText(htmlContent, true);
            helper.setFrom("no-reply@thaca.com");

            mailSender.send(message);
            log.info(">>>> [EMAIL SENDER] HTML Email ({}) sent successfully to [{}]", lang, recipient);
        } catch (Exception e) {
            log.error(">>>> [EMAIL SENDER] Failed to send email to [{}]: {}", recipient, e.getMessage(), e);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}

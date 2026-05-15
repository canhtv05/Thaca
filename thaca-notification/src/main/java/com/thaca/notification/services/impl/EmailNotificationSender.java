package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.notification.services.DynamicMailSenderService;
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

    private final DynamicMailSenderService dynamicMailSenderService;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void send(String recipient, String content, Map<String, Object> metadata) {
        try {
            var apiHeader = com.thaca.framework.core.context.FwContextHeader.get();
            String lang = Optional.ofNullable(apiHeader)
                .map(com.thaca.framework.core.dtos.ApiHeader::getLanguage)
                .orElse("vi");

            // Kiểm tra các tùy chọn gửi mail từ metadata
            String configCode = (String) metadata.get("configCode");
            Boolean useLocalConfig = (Boolean) metadata.getOrDefault("useLocalConfig", false);

            JavaMailSender mailSender;
            String senderAddress;

            if (Boolean.TRUE.equals(useLocalConfig)) {
                // Ép buộc dùng cấu hình local từ .env
                mailSender = dynamicMailSenderService.getLocalMailSender();
                senderAddress = "no-reply@thaca.com"; // Hoặc lấy từ biến môi trường nếu cần
            } else if (configCode != null && !configCode.isEmpty()) {
                // Dùng cấu hình chỉ định từ DB
                mailSender = dynamicMailSenderService.getMailSender(configCode);
                senderAddress = dynamicMailSenderService.getSenderAddress(configCode);
            } else {
                // Tự động tìm cấu hình phù hợp (DB -> Local Fallback)
                mailSender = dynamicMailSenderService.getMailSender();
                senderAddress = dynamicMailSenderService.getSenderAddress();
            }

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
            helper.setFrom(senderAddress);

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

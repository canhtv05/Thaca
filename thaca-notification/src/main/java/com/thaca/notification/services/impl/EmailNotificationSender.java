package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.notification.services.NotificationSender;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationSender implements NotificationSender {

    @Override
    public void send(String recipient, String content, Map<String, Object> metadata) {
        log.info(">>>> [EMAIL SENDER] Sending Email to [{}]. Content: [{}]", recipient, content);
        // Actual email sending logic with JavaMailSender would go here
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}

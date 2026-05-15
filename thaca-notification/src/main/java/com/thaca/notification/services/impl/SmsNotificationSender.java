package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.notification.services.NotificationSender;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationSender implements NotificationSender {

    @Override
    public void send(String recipient, String content, Map<String, Object> metadata) {
        log.info(">>>> [SMS SENDER] Sending SMS to [{}]. Content: [{}]", recipient, content);
        // Actual SMS sending logic (e.g., Twilio, Infobip) would go here
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}

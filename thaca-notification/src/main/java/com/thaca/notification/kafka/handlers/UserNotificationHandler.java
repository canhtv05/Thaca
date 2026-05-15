package com.thaca.notification.kafka.handlers;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.SendOtpEvent;
import com.thaca.framework.core.utils.JsonF;
import com.thaca.notification.services.NotificationSender;
import com.thaca.notification.services.NotificationSenderFactory;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationHandler {

    private final NotificationSenderFactory senderFactory;

    public void handle(String message) {
        Map<String, Object> payload = JsonF.jsonToObject(message, new TypeReference<>() {});
        String eventType = payload != null ? (String) payload.get("eventType") : null;

        if (eventType == null) {
            log.warn("Unknown message format: {}", message);
            return;
        }

        switch (eventType) {
            case "SEND_OTP" -> {
                SendOtpEvent event = JsonF.jsonToObject(message, SendOtpEvent.class);
                this.processOtp(event);
            }
            case "USER_UPDATED" -> log.info("Handle User Updated logic here...");
            default -> log.warn("No handler for eventType: {}", eventType);
        }
    }

    private void processOtp(SendOtpEvent event) {
        NotificationSender sender = senderFactory.getSender(NotificationChannel.EMAIL);
        String content = "Your OTP code is: " + event.getOtpCode();
        sender.send(event.getEmail(), content, Map.of("eventId", event.getObjectId()));
    }
}

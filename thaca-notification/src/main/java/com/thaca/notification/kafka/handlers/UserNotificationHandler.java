package com.thaca.notification.kafka.handlers;

import com.thaca.common.events.SendOtpEvent;
import com.thaca.framework.core.utils.JsonF;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
public class UserNotificationHandler {

    public void handle(String message) {
        Map<String, Object> payload = JsonF.jsonToObject(message, new TypeReference<Map<String, Object>>() {});
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
        log.info(">>>> [NOTIFICATION] Sending OTP [{}] to Email [{}]", event.getOtpCode(), event.getEmail());
    }
}

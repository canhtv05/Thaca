package com.thaca.notification.listeners;

import com.thaca.common.events.SendOtpEvent;
import com.thaca.framework.core.utils.JsonF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OtpNotificationListener {

    @KafkaListener(topics = "thaca_db.auth.users", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOtpEvent(String message) {
        try {
            SendOtpEvent event = JsonF.jsonToObject(message, SendOtpEvent.class);
            if (event != null) {
                log.info("Sending OTP {} to email {}", event.otpCode(), event.email());
            }
        } catch (Exception e) {
            log.error("Error processing OTP event: {}", e.getMessage());
        }
    }
}

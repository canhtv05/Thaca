package com.thaca.notification.kafka.handlers;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.SendOtpEvent;
import com.thaca.framework.core.exceptions.FwException;
import com.thaca.framework.core.utils.JsonF;
import com.thaca.notification.enums.NotificationErrorMessage;
import com.thaca.notification.services.NotificationSender;
import com.thaca.notification.services.NotificationSenderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationHandler {

    private final NotificationSenderFactory senderFactory;

    public void handle(String message) {
        String eventType = JsonF.getFieldValue(message, "eventType");
        if (eventType == null) {
            throw new FwException(NotificationErrorMessage.EVENT_TYPE_CANNOT_BLANK);
        }

        switch (eventType) {
            case "SEND_OTP" -> {
                SendOtpEvent event = JsonF.jsonToObject(message, SendOtpEvent.class);
                this.processOtp(event);
            }
            case "BLA_BLA" -> {
            }
            default -> throw new FwException(NotificationErrorMessage.EVENT_TYPE_NOT_FOUND);
        }
    }

    private void processOtp(SendOtpEvent event) {
        NotificationSender sender = senderFactory.getSender(NotificationChannel.EMAIL);
        String content = "Your OTP code is: " + event.getOtpCode();
        sender.send(event.getEmail(), content, event);
    }
}

package com.thaca.notification.kafka.handlers;

import com.thaca.common.dtos.EventPayload;
import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.SendOtpEvent;
import com.thaca.framework.core.utils.CommonUtils;
import com.thaca.framework.core.utils.JsonF;
import com.thaca.notification.services.NotificationEventDedupeService;
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

    private static final TypeReference<EventPayload<Map<String, Object>>> ENVELOPE_TYPE = new TypeReference<>() {};

    private final NotificationSenderFactory senderFactory;
    private final NotificationEventDedupeService dedupeService;

    public void handle(String message) {
        if (message == null || message.isBlank()) {
            log.warn("[UserNotificationHandler] Empty Kafka message, skipping");
            return;
        }
        EventPayload<Map<String, Object>> envelope = JsonF.jsonToObject(message, ENVELOPE_TYPE);
        if (envelope == null) {
            log.warn("[UserNotificationHandler] Cannot parse message, skipping");
            return;
        }
        if (CommonUtils.isEmpty(envelope.getEventType())) {
            log.warn("[UserNotificationHandler] Missing eventType in old/invalid message, skipping");
            return;
        }
        switch (envelope.getEventType()) {
            case "SEND_OTP" -> handleSendOtp(envelope);
            case "VERIFICATION_EMAIL", "USER_CREATED", "FORGOT_PASSWORD" -> log.debug(
                "[UserNotificationHandler] Event type {} not handled yet, skipping",
                envelope.getEventType()
            );
            default -> log.warn(
                "[UserNotificationHandler] Unsupported event type '{}', skipping",
                envelope.getEventType()
            );
        }
    }

    private void handleSendOtp(EventPayload<Map<String, Object>> envelope) {
        SendOtpEvent event = JsonF.convertObject(envelope.getPayload(), SendOtpEvent.class);
        if (event == null || CommonUtils.isEmpty(event.getEmail(), event.getOtpCode())) {
            log.warn("[UserNotificationHandler] SEND_OTP payload invalid, skipping");
            return;
        }
        if (!dedupeService.tryMarkProcessed(envelope.getEventId())) {
            return;
        }
        NotificationSender sender = senderFactory.getSender(NotificationChannel.EMAIL);
        sender.send(event.getEmail(), event.getOtpCode(), event);
    }
}

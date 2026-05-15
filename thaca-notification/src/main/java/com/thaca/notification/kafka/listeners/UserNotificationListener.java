package com.thaca.notification.kafka.listeners;

import com.thaca.common.constants.EventConstants;
import com.thaca.notification.kafka.handlers.UserNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationListener {

    private final UserNotificationHandler userNotificationHandler;

    @KafkaListener(
        topics = EventConstants.AUTH_USERS_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvents(String message, Acknowledgment ack) {
        userNotificationHandler.handle(message);
        ack.acknowledge();
    }
}

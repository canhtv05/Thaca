package com.thaca.notification.services.impl;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.notification.services.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationSender implements NotificationSender {

    @Override
    public <T extends DomainEvent> void send(String recipient, String content, T event) {}

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}

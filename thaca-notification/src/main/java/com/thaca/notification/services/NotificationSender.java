package com.thaca.notification.services;

import com.thaca.common.enums.NotificationChannel;
import com.thaca.common.events.base.DomainEvent;

public interface NotificationSender {
    <T extends DomainEvent> void send(String recipient, String content, T event);

    NotificationChannel getChannel();
}

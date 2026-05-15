package com.thaca.notification.services;

import com.thaca.common.enums.NotificationChannel;
import java.util.Map;

public interface NotificationSender {
    void send(String recipient, String content, Map<String, Object> metadata);

    NotificationChannel getChannel();
}

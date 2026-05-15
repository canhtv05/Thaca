package com.thaca.notification.services;

import com.thaca.common.enums.NotificationChannel;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senders;

    public NotificationSenderFactory(List<NotificationSender> senderList) {
        this.senders = senderList.stream().collect(Collectors.toMap(NotificationSender::getChannel, sender -> sender));
    }

    public NotificationSender getSender(NotificationChannel channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("No sender found for channel: " + channel);
        }
        return sender;
    }
}

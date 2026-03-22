package com.thaca.auth.dtos;

import com.thaca.auth.enums.NotificationType;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload<T> {

    NotificationType type;
    String sessionId;
    String username;
    String title;
    String message;
    Instant timestamp;
    T data;
}

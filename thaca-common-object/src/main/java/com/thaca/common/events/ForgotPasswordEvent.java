package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ForgotPasswordEvent(
    String objectId,
    String to,
    String username,
    Instant timestamp
) implements ExportableEvent {
    @Override
    public String objectType() {
        return "users";
    }

    @Override
    public String eventType() {
        return "FORGOT_PASSWORD";
    }

    @Override
    public Instant timestamp() {
        return timestamp != null ? timestamp : Instant.now();
    }
}

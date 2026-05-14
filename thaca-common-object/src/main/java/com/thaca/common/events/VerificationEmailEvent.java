package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import lombok.Builder;

@Builder
public record VerificationEmailEvent(
    String objectId,
    String to,
    String username,
    String fullName,
    Instant timestamp
) implements ExportableEvent {
    @Override
    public String objectType() {
        return "users";
    }

    @Override
    public String eventType() {
        return "VERIFICATION_EMAIL";
    }

    @Override
    public Instant timestamp() {
        return timestamp != null ? timestamp : Instant.now();
    }
}

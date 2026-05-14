package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import lombok.Builder;

@Builder
public record SendOtpEvent(
    String objectId,
    String email,
    String otpCode,
    Instant timestamp
) implements ExportableEvent {
    @Override
    public String objectType() {
        return "users";
    }

    @Override
    public String eventType() {
        return "SEND_OTP";
    }

    @Override
    public Instant timestamp() {
        return timestamp != null ? timestamp : Instant.now();
    }
}

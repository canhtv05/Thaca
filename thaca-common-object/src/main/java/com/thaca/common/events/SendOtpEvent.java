package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpEvent implements ExportableEvent {

    private String objectId;
    private String email;
    private String otpCode;

    @Builder.Default
    private String objectType = "users";

    @Builder.Default
    private String eventType = "SEND_OTP";

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Override
    public String objectId() {
        return objectId;
    }

    @Override
    public String objectType() {
        return objectType;
    }

    @Override
    public String eventType() {
        return eventType;
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }
}

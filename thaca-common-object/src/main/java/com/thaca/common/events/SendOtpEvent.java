package com.thaca.common.events;

import com.thaca.common.events.base.EventMetadata;
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
    private EventMetadata metadata;

    @Builder.Default
    private String objectType = "users";

    @Builder.Default
    private String notificationType = "SEND_OTP";

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Override
    public EventMetadata metadata() {
        return this.metadata;
    }

    @Override
    public String objectId() {
        return this.objectId;
    }

    @Override
    public String objectType() {
        return this.objectType;
    }

    @Override
    public String notificationType() {
        return this.notificationType;
    }

    @Override
    public Instant timestamp() {
        return this.timestamp;
    }
}

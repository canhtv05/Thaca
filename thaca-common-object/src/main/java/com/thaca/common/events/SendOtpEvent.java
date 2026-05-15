package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Override
    public Map<String, Object> metadata() {
        return metadata;
    }

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

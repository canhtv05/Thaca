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
public class UserCreationEvent implements ExportableEvent {

    private String userId;
    private String fullName;

    @Builder.Default
    private String objectType = "users";

    @Builder.Default
    private String eventType = "USER_CREATED";

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Override
    public String objectId() {
        return userId;
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

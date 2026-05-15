package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDeletedEvent implements ExportableEvent {

    private String objectId;
    private Long tenantId;

    @Builder.Default
    private String objectType = "tenants";

    @Builder.Default
    private String eventType = "TENANT_DELETED";

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
    public Map<String, Object> metadata() {
        return Map.of();
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }
}

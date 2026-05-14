package com.thaca.common.events;

import com.thaca.common.events.base.ExportableEvent;
import java.time.Instant;
import lombok.Builder;

@Builder
public record TenantDeletedEvent(String objectId, Long tenantId, Instant timestamp) implements ExportableEvent {
    @Override
    public String objectType() {
        return "tenants";
    }

    @Override
    public String eventType() {
        return "TENANT_DELETED";
    }

    @Override
    public Instant timestamp() {
        return timestamp != null ? timestamp : Instant.now();
    }
}

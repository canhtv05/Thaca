package com.thaca.common.events.base;

import java.time.Instant;
import java.util.Map;

public interface DomainEvent {
    Instant timestamp();

    String objectId();

    String objectType();

    String eventType();

    Map<String, Object> metadata();
}

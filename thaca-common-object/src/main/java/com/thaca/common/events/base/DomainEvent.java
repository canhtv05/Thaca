package com.thaca.common.events.base;

import java.time.Instant;

public interface DomainEvent {
    Instant timestamp();

    String objectId();

    String objectType();

    String eventType();
}

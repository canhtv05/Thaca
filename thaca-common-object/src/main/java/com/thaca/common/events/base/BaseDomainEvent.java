package com.thaca.common.events.base;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseDomainEvent implements DomainEvent {

    private String objectId;
    private final Instant timestamp = Instant.now();

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public String objectId() {
        return objectId;
    }
}

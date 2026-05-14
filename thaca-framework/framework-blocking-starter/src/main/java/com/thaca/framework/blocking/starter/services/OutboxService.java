package com.thaca.framework.blocking.starter.services;

import com.thaca.common.events.base.DomainEvent;

@FunctionalInterface
public interface OutboxService {
    void store(DomainEvent event);
}

package com.thaca.framework.blocking.starter.events;

import org.springframework.context.ApplicationEvent;

public class OutboxSavedEvent extends ApplicationEvent {

    private final Long outboxEventId;

    public OutboxSavedEvent(Object source, Long outboxEventId) {
        super(source);
        this.outboxEventId = outboxEventId;
    }

    public Long getOutboxEventId() {
        return outboxEventId;
    }
}

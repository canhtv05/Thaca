package com.thaca.auth.kafka;

import com.thaca.common.events.ExportableEvent;
import com.thaca.framework.blocking.starter.services.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final OutboxService outboxService;

    @EventListener
    public void handleExportableEvent(ExportableEvent event) {
        outboxService.store(event);
    }
}

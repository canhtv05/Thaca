package com.thaca.auth.kafka;

import com.thaca.auth.domains.OutboxEvent;
import com.thaca.auth.repositories.OutboxEventRepository;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.framework.blocking.starter.events.OutboxSavedEvent;
import com.thaca.framework.blocking.starter.services.OutboxService;
import com.thaca.framework.core.utils.JsonF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void store(DomainEvent event) {
        OutboxEvent entity = OutboxEvent.builder()
            .objectType(event.objectType())
            .objectId(event.objectId())
            .eventType(event.eventType())
            .payload(JsonF.toJson(event))
            .status("PENDING")
            .build();
        outboxEventRepository.save(entity);
        eventPublisher.publishEvent(new OutboxSavedEvent(this, entity.getId()));
    }
}

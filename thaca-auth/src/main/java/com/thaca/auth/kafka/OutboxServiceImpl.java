package com.thaca.auth.kafka;

import com.thaca.auth.domains.OutboxEvent;
import com.thaca.auth.repositories.OutboxEventRepository;
import com.thaca.common.dtos.EventPayload;
import com.thaca.common.events.base.DomainEvent;
import com.thaca.framework.blocking.starter.events.OutboxSavedEvent;
import com.thaca.framework.blocking.starter.services.OutboxService;
import com.thaca.framework.core.utils.JsonF;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void store(DomainEvent event) {
        String eventJson = JsonF.toJson(event);
        Map<String, Object> payloadMap = JsonF.jsonToObject(eventJson, new TypeReference<>() {});
        if (payloadMap == null || payloadMap.isEmpty()) {
            throw new IllegalStateException("Cannot serialize domain event payload");
        }
        String kafkaMessage = JsonF.toJson(
            EventPayload.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(event.notificationType())
                .payload(payloadMap)
                .build()
        );
        OutboxEvent entity = OutboxEvent.builder()
            .objectType(event.objectType())
            .objectId(event.objectId())
            .eventType(event.notificationType())
            .payload(kafkaMessage)
            .status("PENDING")
            .build();
        outboxEventRepository.save(entity);
        eventPublisher.publishEvent(new OutboxSavedEvent(this, entity.getId()));
    }
}

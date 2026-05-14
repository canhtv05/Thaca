package com.thaca.auth.kafka;

import com.thaca.auth.domains.OutboxEvent;
import com.thaca.auth.repositories.OutboxEventRepository;
import com.thaca.framework.blocking.starter.events.OutboxSavedEvent;
import com.thaca.framework.core.context.SpringContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxSavedEvent(OutboxSavedEvent event) {
        SpringContext.getBean(OutboxPublisherService.class).publishEventById(event.getOutboxEventId());
    }

    @Scheduled(fixedDelayString = "${outbox.scheduler.delay:5000}")
    public void sweepPendingOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        if (!pendingEvents.isEmpty()) {
            for (OutboxEvent event : pendingEvents) {
                SpringContext.getBean(OutboxPublisherService.class).publishEventById(event.getId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishEventById(Long eventId) {
        outboxEventRepository
            .findById(eventId)
            .ifPresent(event -> {
                if (!"PENDING".equals(event.getStatus())) return;
                try {
                    String topic = "thaca_db.auth." + event.getObjectType();
                    kafkaTemplate.send(topic, event.getObjectId(), event.getPayload());
                    event.setStatus("COMPLETED");
                    outboxEventRepository.save(event);
                } catch (Exception e) {
                    log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                }
            });
    }
}

package com.thaca.auth.kafka;

import com.thaca.auth.domains.OutboxEvent;
import com.thaca.auth.repositories.OutboxEventRepository;
import com.thaca.framework.blocking.starter.events.OutboxSavedEvent;
import com.thaca.framework.core.context.SpringContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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

    private static final int MAX_RETRY = 10;
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxSavedEvent(OutboxSavedEvent event) {
        SpringContext.getBean(OutboxPublisherService.class).publishEventById(event.getOutboxEventId());
    }

    @Scheduled(fixedDelayString = "${outbox.scheduler.delay:5000}")
    public void sweepPendingOutboxEvents() {
        Instant now = Instant.now();
        Instant stuckThreshold = now.minus(5, ChronoUnit.MINUTES);
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingForUpdate(
            now,
            stuckThreshold,
            PageRequest.of(0, BATCH_SIZE)
        );
        if (!pendingEvents.isEmpty()) {
            for (OutboxEvent event : pendingEvents) {
                SpringContext.getBean(OutboxPublisherService.class).publishEventById(event.getId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishEventById(Long eventId) {
        Instant now = Instant.now();
        Instant stuckThreshold = now.minus(5, ChronoUnit.MINUTES);
        int updated = outboxEventRepository.markProcessing(eventId, now, stuckThreshold);
        if (updated == 0) {
            return;
        }
        OutboxEvent event = outboxEventRepository.findById(eventId).orElseThrow();
        try {
            String topic = "thaca_db.auth." + event.getObjectType();
            kafkaTemplate.send(topic, event.getObjectId(), event.getPayload()).get();
            outboxEventRepository.markCompleted(eventId);
        } catch (Exception e) {
            log.error("Failed to publish outbox event {}", eventId, e);
            handleFailure(event, e);
        }
    }

    private void handleFailure(OutboxEvent event, Exception e) {
        int newRetryCount = event.getRetryCount() + 1;
        String status = (newRetryCount >= MAX_RETRY) ? "FAILED" : "PENDING";
        Instant nextRetryAt = null;
        if ("PENDING".equals(status)) {
            long delayMinutes = (long) Math.pow(2, newRetryCount);
            nextRetryAt = Instant.now().plus(delayMinutes, ChronoUnit.MINUTES);
        }
        outboxEventRepository.markFailed(event.getId(), status, nextRetryAt, e.getMessage());
    }
}

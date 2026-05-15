package com.thaca.auth.repositories;

import com.thaca.auth.domains.OutboxEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status);

    @Modifying
    @Query(
        """
        UPDATE OutboxEvent e
        SET e.status = 'PROCESSING',
            e.processingStartedAt = :now
        WHERE e.id = :id
        AND (e.status = 'PENDING' OR (e.status = 'PROCESSING' AND e.processingStartedAt < :stuckThreshold))
        """
    )
    int markProcessing(Long id, Instant now, Instant stuckThreshold);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'COMPLETED', e.lastError = null WHERE e.id = :id")
    void markCompleted(Long id);

    @Modifying
    @Query(
        """
        UPDATE OutboxEvent e
        SET e.status = :status,
            e.retryCount = e.retryCount + 1,
            e.nextRetryAt = :nextRetryAt,
            e.lastError = :lastError
        WHERE e.id = :id
        """
    )
    void markFailed(Long id, String status, Instant nextRetryAt, String lastError);

    @Query(
        value = """
        SELECT * FROM auth.outbox_events
        WHERE (status = 'PENDING' AND (next_retry_at IS NULL OR next_retry_at <= :now))
        OR (status = 'PROCESSING' AND processing_started_at < :stuckThreshold)
        ORDER BY created_at ASC
        FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    List<OutboxEvent> findPendingForUpdate(Instant now, Instant stuckThreshold, Pageable pageable);
}

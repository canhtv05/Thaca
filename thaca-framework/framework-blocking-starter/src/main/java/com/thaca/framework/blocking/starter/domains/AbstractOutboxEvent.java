package com.thaca.framework.blocking.starter.domains;

import com.thaca.framework.core.utils.JsonF;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_events_seq")
    @SequenceGenerator(name = "outbox_events_seq", allocationSize = 1)
    private Long id;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @Column(name = "object_id", nullable = false)
    private String objectId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public JsonNode getPayloadAsJson() {
        return JsonF.readTree(this.payload);
    }
}

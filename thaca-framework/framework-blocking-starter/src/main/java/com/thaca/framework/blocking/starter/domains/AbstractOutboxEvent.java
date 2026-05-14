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

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public JsonNode getPayloadAsJson() {
        return JsonF.readTree(this.payload);
    }
}

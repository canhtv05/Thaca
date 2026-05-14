package com.thaca.auth.domains;

import com.thaca.framework.blocking.starter.domains.AbstractOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "outbox_events", schema = "auth")
public class OutboxEvent extends AbstractOutboxEvent {}

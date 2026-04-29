package com.thaca.auth.domains;

import com.thaca.common.enums.AccountStatus;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "user_lock_history", schema = "auth")
public class UserLockHistory extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_lock_history_seq")
    @SequenceGenerator(name = "user_lock_history_seq", allocationSize = 1)
    private Long id;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AccountStatus action;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;
}

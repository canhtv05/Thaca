package com.thaca.auth.domains;

import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "system_users", schema = "auth")
public class SystemUser extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_user_seq")
    @SequenceGenerator(name = "system_user_seq", allocationSize = 1)
    private Long id;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "is_activated", nullable = false)
    @Builder.Default
    private Boolean isActivated = true;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    @Column(name = "is_super_admin", nullable = false)
    @Builder.Default
    private boolean isSuperAdmin = false;
}

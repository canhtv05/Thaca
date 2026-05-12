package com.thaca.auth.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
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
@Table(name = "users", schema = "auth")
public class User extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_activated", nullable = false)
    @Builder.Default
    private Boolean isActivated = false;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @JsonIgnore
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    String refreshToken;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_tenants",
        schema = "auth",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tenant_id")
    )
    @Builder.Default
    private Set<Tenant> tenants = new HashSet<>();
}

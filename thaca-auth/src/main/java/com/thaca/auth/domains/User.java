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
@Table(name = "USERS")
public class User extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ")
    @SequenceGenerator(name = "USER_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "USERNAME", unique = true, nullable = false)
    private String username;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "ACTIVATED", nullable = false)
    @Builder.Default
    private boolean activated = false;

    @Column(name = "IS_LOCKED", nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    @Column(name = "IS_GLOBAL", nullable = false)
    @Builder.Default
    private Boolean isGlobal = false;

    @JsonIgnore
    @Column(name = "REFRESH_TOKEN", columnDefinition = "TEXT")
    String refreshToken;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "USER_ROLES",
        joinColumns = { @JoinColumn(name = "USER_ID", referencedColumnName = "ID") },
        inverseJoinColumns = { @JoinColumn(name = "ROLE_CODE", referencedColumnName = "CODE") }
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}

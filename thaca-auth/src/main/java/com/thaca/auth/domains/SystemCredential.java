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
@Table(name = "system_credentials", schema = "auth")
public class SystemCredential extends BaseEntityAudit {

    @Id
    @Column(name = "system_user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "system_user_id", referencedColumnName = "id", nullable = false)
    private SystemUser systemUser;

    @Column(name = "username", nullable = false)
    private String username;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
        name = "system_credential_roles",
        joinColumns = { @JoinColumn(name = "system_user_id") },
        inverseJoinColumns = { @JoinColumn(name = "role_code", referencedColumnName = "code") }
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SystemCredentialPermission> credentialPermissions = new HashSet<>();
}

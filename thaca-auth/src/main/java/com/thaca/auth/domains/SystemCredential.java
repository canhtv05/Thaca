package com.thaca.auth.domains;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class SystemCredential extends BaseTenantEntity {

    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @OneToOne
    @JoinColumn(name = "system_user_id", referencedColumnName = "id", nullable = false)
    private SystemUser systemUser;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
        name = "system_credential_roles",
        joinColumns = { @JoinColumn(name = "credential_id", referencedColumnName = "username") },
        inverseJoinColumns = { @JoinColumn(name = "role_code", referencedColumnName = "code") }
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SystemCredentialPermission> credentialPermissions = new HashSet<>();
}

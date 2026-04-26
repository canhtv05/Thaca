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

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "roles", schema = "auth")
public class Role extends BaseTenantEntity {

    @Id
    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = { @JoinColumn(name = "role_code", referencedColumnName = "code") },
        inverseJoinColumns = { @JoinColumn(name = "permission_code", referencedColumnName = "code") }
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}

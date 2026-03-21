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

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "ROLES")
public class Role extends BaseEntityAudit {

    @Id
    @Column(name = "CODE", length = 50, unique = true, nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "ROLE_PERMISSIONS",
        joinColumns = { @JoinColumn(name = "ROLE_CODE", referencedColumnName = "CODE") },
        inverseJoinColumns = { @JoinColumn(name = "PERMISSION_CODE", referencedColumnName = "CODE") }
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<Permission>();
}

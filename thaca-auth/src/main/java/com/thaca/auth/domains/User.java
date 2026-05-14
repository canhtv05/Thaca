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
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "users", schema = "auth")
@FilterDef(name = "tenantFilter", parameters = { @ParamDef(name = "tenantIds", type = Long.class) })
@Filter(
    name = "tenantFilter",
    condition = "exists (select 1 from auth.user_tenants ut where ut.user_id = id and ut.tenant_id in (:tenantIds))"
)
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

    @ElementCollection
    @CollectionTable(name = "user_tenants", schema = "auth", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tenant_id")
    @Builder.Default
    private Set<Long> tenantIds = new HashSet<>();
}

package com.thaca.auth.domains;

import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import java.time.Instant;
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
@Table(name = "system_users", schema = "auth")
@FilterDef(name = "tenantFilter", parameters = { @ParamDef(name = "tenantIds", type = Long.class) })
@Filter(
    name = "tenantFilter",
    condition = "exists (select 1 from auth.system_user_tenants sut where sut.system_user_id = id and sut.tenant_id in (:tenantIds))"
)
public class SystemUser extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_user_seq")
    @SequenceGenerator(name = "system_user_seq", allocationSize = 1)
    private Long id;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "avatar_url", columnDefinition = "text")
    private String avatarUrl;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "is_activated", nullable = false)
    @Builder.Default
    private Boolean isActivated = true;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "is_super_admin", nullable = false)
    @Builder.Default
    private Boolean isSuperAdmin = false;

    @ElementCollection
    @CollectionTable(name = "system_user_tenants", schema = "auth", joinColumns = @JoinColumn(name = "system_user_id"))
    @Column(name = "tenant_id")
    @Builder.Default
    private Set<Long> tenantIds = new HashSet<>();
}

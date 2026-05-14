package com.thaca.admin.domains;

import com.thaca.common.enums.TenantStatus;
import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "tenants", schema = "admin")
@SQLRestriction("deleted_at IS NULL")
public class Tenant extends BaseEntityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tenant_seq")
    @SequenceGenerator(name = "tenant_seq", sequenceName = "admin.tenant_seq", allocationSize = 1)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "domain")
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TenantStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;
}

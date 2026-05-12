package com.thaca.framework.blocking.starter.domains;

import com.thaca.framework.blocking.starter.configs.audit.BaseEntityAudit;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
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
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = { @ParamDef(name = "tenantIds", type = Long.class) })
@Filter(name = "tenantFilter", condition = "tenant_id IN (:tenantIds)")
public abstract class BaseTenantEntity extends BaseEntityAudit {

    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;
}

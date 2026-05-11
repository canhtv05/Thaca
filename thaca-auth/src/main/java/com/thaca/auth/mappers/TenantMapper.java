package com.thaca.auth.mappers;

import com.thaca.auth.domains.Tenant;
import com.thaca.auth.domains.projections.TenantInfoProjection;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.framework.core.utils.DateUtils;

public class TenantMapper {

    private TenantMapper() {}

    public static TenantDTO fromEntity(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return TenantDTO.builder()
            .id(tenant.getId())
            .code(tenant.getCode())
            .name(tenant.getName())
            .domain(tenant.getDomain())
            .status(tenant.getStatus())
            .version(tenant.getVersion())
            .planId(tenant.getPlan() != null ? tenant.getPlan().getId() : null)
            .expiresAt(DateUtils.localDateToString(tenant.getExpiresAt()))
            .contactEmail(tenant.getContactEmail())
            .logoUrl(tenant.getLogoUrl())
            .createdBy(tenant.getCreatedBy())
            .updatedBy(tenant.getUpdatedBy())
            .createdAt(DateUtils.dateToString(tenant.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(tenant.getUpdatedAt()))
            .build();
    }

    public static TenantInfoPrj fromInfoProj(TenantInfoProjection projection) {
        if (projection == null) {
            return null;
        }
        return TenantInfoPrj.builder()
            .id(projection.getId())
            .code(projection.getCode())
            .name(projection.getName())
            .logoUrl(projection.getLogoUrl())
            .build();
    }
}

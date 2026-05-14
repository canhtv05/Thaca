package com.thaca.admin.mappers;

import com.thaca.admin.domains.Tenant;
import com.thaca.admin.domains.projection.TenantInfoProjection;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.PlanInfoPrj;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.common.enums.PlanType;
import com.thaca.framework.core.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

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

    public static TenantInfoPrj fromPrj(TenantInfoProjection tenant) {
        if (tenant == null) {
            return null;
        }
        return TenantInfoPrj.builder()
            .id(tenant.getId())
            .code(tenant.getCode())
            .name(tenant.getName())
            .domain(tenant.getDomain())
            .status(tenant.getStatus())
            .planId(tenant.getPlanId())
            .expiresAt(DateUtils.localDateToString(tenant.getExpiresAt()))
            .contactEmail(tenant.getContactEmail())
            .logoUrl(tenant.getLogoUrl())
            .version(tenant.getVersion())
            .plan(
                PlanInfoPrj.builder()
                    .id(tenant.getPlanId())
                    .name(tenant.getPlanName())
                    .code(tenant.getPlanCode())
                    .type(StringUtils.isNotBlank(tenant.getPlanType()) ? PlanType.valueOf(tenant.getPlanType()) : null)
                    .build()
            )
            .build();
    }
}

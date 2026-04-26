package com.thaca.auth.mappers;

import com.thaca.auth.domains.Tenant;
import com.thaca.common.dtos.internal.TenantDTO;
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
            .createdBy(tenant.getCreatedBy())
            .updatedBy(tenant.getUpdatedBy())
            .createdAt(DateUtils.dateToString(tenant.getCreatedAt()))
            .updatedAt(DateUtils.dateToString(tenant.getUpdatedAt()))
            .build();
    }
}

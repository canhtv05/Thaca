package com.thaca.common.dtos.internal.contracts;

import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import java.util.List;

public interface TenantAwareDTO {
    List<Long> getTenantIds();

    void setTenantIds(List<Long> tenantIds);

    List<TenantDTO> getTenants();

    void setTenants(List<TenantDTO> tenants);

    List<TenantInfoPrj> getTenantInfos();

    void setTenantInfos(List<TenantInfoPrj> tenantInfos);
}

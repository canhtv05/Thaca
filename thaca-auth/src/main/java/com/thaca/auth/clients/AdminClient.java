package com.thaca.auth.clients;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.framework.core.annotations.FwInternalApi;
import java.util.List;

public interface AdminClient {
    @FwInternalApi(path = "/admin/tenants/all", name = ServiceMethod.ADMIN_GET_ALL_TENANTS)
    List<TenantInfoPrj> getAllTenants();

    @FwInternalApi(path = "/admin/tenants/get-by-ids", name = ServiceMethod.ADMIN_GET_TENANTS_BY_IDS)
    List<TenantInfoPrj> getTenantsByIds(TenantDTO request);

    @FwInternalApi(path = "/admin/tenants/get-full-by-ids", name = ServiceMethod.ADMIN_GET_TENANTS_FULL_BY_IDS)
    List<TenantDTO> getTenantsFullByIds(TenantDTO request);

    @FwInternalApi(path = "/admin/tenants/get", name = ServiceMethod.ADMIN_GET_TENANT)
    TenantDTO getTenant(TenantDTO tenant);
}

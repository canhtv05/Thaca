package com.thaca.auth.clients;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.framework.core.annotations.FwInternalApi;
import java.util.List;

public interface CmsClient {
    @FwInternalApi(path = "/cms/tenants/all", name = ServiceMethod.CMS_GET_ALL_TENANTS)
    List<TenantInfoPrj> getAllTenants();

    @FwInternalApi(path = "/cms/tenants/get-by-ids", name = ServiceMethod.CMS_GET_TENANTS_BY_IDS)
    List<TenantInfoPrj> getTenantsByIds(TenantDTO request);

    @FwInternalApi(path = "/cms/tenants/get-full-by-ids", name = ServiceMethod.CMS_GET_TENANTS_FULL_BY_IDS)
    List<TenantDTO> getTenantsFullByIds(TenantDTO request);

    @FwInternalApi(path = "/cms/tenants/get", name = ServiceMethod.CMS_GET_TENANT)
    TenantDTO getTenant(TenantDTO tenant);
}

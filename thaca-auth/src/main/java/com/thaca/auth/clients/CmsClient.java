package com.thaca.auth.clients;

import com.thaca.auth.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.framework.core.annotations.FwInternalApi;
import java.util.List;

public interface CmsClient {
    @FwInternalApi(path = "/cms/tenants/all", name = ServiceMethod.CMS_GET_ALL_TENANTS)
    List<TenantInfoPrj> getAllTenants();

    @FwInternalApi(path = "/cms/tenants/get", name = ServiceMethod.CMS_GET_TENANT)
    TenantDTO getTenant(TenantDTO tenant);
}

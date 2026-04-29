package com.thaca.auth.internal.controllers;

import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.annotations.FwSecurity;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import com.thaca.framework.core.utils.CommonUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalTenantController {

    private final FwApiProcess process;

    @PostMapping("/cms/tenants/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_TENANTS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<TenantDTO>> searchTenants(SearchRequest<TenantDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/tenants/get")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_TENANT, type = RequestType.INTERNAL)
    public ResponseEntity<TenantDTO> getTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/tenants/all")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_ALL_TENANTS, type = RequestType.INTERNAL)
    public ResponseEntity<List<TenantInfoPrj>> getAllTenants() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/cms/tenants/create")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = InternalMethod.INTERNAL_CMS_CREATE_TENANT, type = RequestType.INTERNAL)
    public ResponseEntity<TenantDTO> createTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/tenants/update")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = InternalMethod.INTERNAL_CMS_UPDATE_TENANT, type = RequestType.INTERNAL)
    public ResponseEntity<TenantDTO> updateTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/tenants/lock-unlock")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_TENANT, type = RequestType.INTERNAL)
    public ResponseEntity<Void> lockUnlockTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/tenants/export")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_EXPORT_TENANT, type = RequestType.INTERNAL)
    public void exportTenants(SearchRequest<TenantDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-tenants-export-{{date}}.xlsx");
    }
}

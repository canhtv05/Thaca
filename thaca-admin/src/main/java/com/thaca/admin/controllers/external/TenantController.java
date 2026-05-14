package com.thaca.admin.controllers.external;

import com.thaca.admin.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
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
@RequestMapping("/admin/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwRequest(name = ServiceMethod.ADMIN_SEARCH_TENANTS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<TenantDTO>> searchTenants(SearchRequest<TenantDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    // test public
    @PostMapping("/all")
    @FwRequest(name = ServiceMethod.ADMIN_GET_ALL_TENANTS, type = RequestType.PUBLIC)
    public ResponseEntity<List<TenantInfoPrj>> getAllTenants() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/get")
    @FwRequest(name = ServiceMethod.ADMIN_GET_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> getTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/create")
    @FwRequest(name = ServiceMethod.ADMIN_CREATE_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> createTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/update")
    @FwRequest(name = ServiceMethod.ADMIN_UPDATE_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> updateTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/lock-unlock")
    @FwRequest(name = ServiceMethod.ADMIN_LOCK_UNLOCK_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUnlockTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/export")
    @FwRequest(name = ServiceMethod.ADMIN_EXPORT_TENANT, type = RequestType.PROTECTED)
    public void exportTenants(SearchRequest<TenantDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-tenants-{{date}}.xlsx");
    }
}

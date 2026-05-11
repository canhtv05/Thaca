package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
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
@RequestMapping("/cms/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_TENANTS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<TenantDTO>> search(SearchRequest<TenantDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/get")
    @FwRequest(name = ServiceMethod.CMS_GET_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> get(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/all")
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_TENANTS, type = RequestType.PUBLIC)
    public ResponseEntity<List<TenantInfoPrj>> getAll() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/create")
    @FwRequest(name = ServiceMethod.CMS_CREATE_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> create(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/update")
    @FwRequest(name = ServiceMethod.CMS_UPDATE_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> update(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/lock-unlock")
    @FwRequest(name = ServiceMethod.CMS_LOCK_UNLOCK_TENANT, type = RequestType.PROTECTED, isSuperAdmin = true)
    public ResponseEntity<Void> lockUnlock(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/export")
    @FwRequest(name = ServiceMethod.CMS_EXPORT_TENANT, type = RequestType.PROTECTED)
    public void export(SearchRequest<TenantDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-tenants-export-{{date}}.xlsx");
    }
}

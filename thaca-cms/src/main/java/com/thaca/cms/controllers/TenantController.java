package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.annotations.FwSecurity;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import com.thaca.framework.core.utils.CommonUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/cms/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_SEARCH_TENANTS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<TenantDTO>> search(SearchRequest<TenantDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/get")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_GET_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> get(Long id) {
        return ResponseEntity.ok(process.process(id));
    }

    @PostMapping("/save")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_CREATE_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> create(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/update")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_UPDATE_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<TenantDTO> update(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/lock-unlock")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_LOCK_UNLOCK_TENANT, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUnlock(TenantDTO request) {
        process.process(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/export")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_EXPORT_TENANT, type = RequestType.PROTECTED)
    public void export(SearchRequest<TenantDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-tenants-export-{{date}}.xlsx");
    }
}

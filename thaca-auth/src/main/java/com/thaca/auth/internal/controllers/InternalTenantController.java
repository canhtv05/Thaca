package com.thaca.auth.internal.controllers;

import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalTenantController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/cms/tenants/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_TENANTS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<TenantDTO>> searchTenants(SearchRequest<TenantDTO> criteria) {
        return ResponseEntity.ok(fwApiProcess.process(criteria));
    }
}

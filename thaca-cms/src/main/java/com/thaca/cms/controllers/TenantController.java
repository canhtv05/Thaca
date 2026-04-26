package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.annotations.FwSecurity;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
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
}

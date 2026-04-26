package com.thaca.cms.controllers;

import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
import com.thaca.framework.blocking.starter.services.InternalApiClient;
import com.thaca.framework.core.annotations.FwSecurity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/cms/tenants")
@RequiredArgsConstructor
public class TenantController {
    // @PostMapping("/search")
    // @FwSecurity(isSuperAdmin = true)
    // public SearchResponse<TenantDTO> search(SearchRequest<TenantDTO> request) {
    // return
    // internalApiServiceClient.invoke(InternalMethod.INTERNAL_CMS_SEARCH_TENANTS,
    // request);
    // }
}

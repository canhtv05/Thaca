package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.PlanDTO;
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
@RequestMapping("/cms/plans")
@RequiredArgsConstructor
public class PlanController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_SEARCH_PLANS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<PlanDTO>> search(SearchRequest<PlanDTO> request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/get")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_GET_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> get(Long id) {
        return ResponseEntity.ok(process.process(id));
    }

    @PostMapping("/save")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_SAVE_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> save(PlanDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/delete")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_DELETE_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<Void> delete(Long id) {
        process.process(id);
        return ResponseEntity.ok().build();
    }
}

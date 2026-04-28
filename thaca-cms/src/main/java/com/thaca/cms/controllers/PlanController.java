package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.PlanDTO;
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
    public ResponseEntity<PlanDTO> get(PlanDTO plan) {
        return ResponseEntity.ok(process.process(plan));
    }

    @PostMapping("/all")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_PLANS, type = RequestType.PROTECTED)
    public ResponseEntity<List<PlanDTO>> getAll() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/create")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_CREATE_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> create(PlanDTO plan) {
        return ResponseEntity.ok(process.process(plan));
    }

    @PostMapping("/update")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_UPDATE_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> update(PlanDTO plan) {
        return ResponseEntity.ok(process.process(plan));
    }

    @PostMapping("/lock-unlock")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_LOCK_UNLOCK_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUnlock(PlanDTO plan) {
        return ResponseEntity.ok(process.process(plan));
    }

    @PostMapping("/export")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_EXPORT_PLAN, type = RequestType.PROTECTED)
    public void export(SearchRequest<PlanDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-plans-export-{{date}}.xlsx");
    }
}

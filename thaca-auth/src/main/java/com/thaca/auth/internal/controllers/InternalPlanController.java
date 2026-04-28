package com.thaca.auth.internal.controllers;

import com.thaca.common.constants.InternalMethod;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalPlanController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/cms/plans/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_PLANS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<PlanDTO>> searchPlans(SearchRequest<PlanDTO> request) {
        return ResponseEntity.ok(fwApiProcess.process(request));
    }

    @PostMapping("/cms/plans/get")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_PLAN, type = RequestType.INTERNAL)
    public ResponseEntity<PlanDTO> getPlan(PlanDTO request) {
        return ResponseEntity.ok(fwApiProcess.process(request));
    }

    @PostMapping("/cms/plans/create")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = InternalMethod.INTERNAL_CMS_CREATE_PLAN, type = RequestType.INTERNAL)
    public ResponseEntity<PlanDTO> createPlan(PlanDTO request) {
        return ResponseEntity.ok(fwApiProcess.process(request));
    }

    @PostMapping("/cms/plans/update")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = InternalMethod.INTERNAL_CMS_UPDATE_PLAN, type = RequestType.INTERNAL)
    public ResponseEntity<PlanDTO> updatePlan(PlanDTO request) {
        return ResponseEntity.ok(fwApiProcess.process(request));
    }

    @PostMapping("/cms/plans/lock-unlock")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = InternalMethod.INTERNAL_CMS_LOCK_UNLOCK_PLAN, type = RequestType.INTERNAL)
    public ResponseEntity<Void> lockUnlockPlan(PlanDTO request) {
        return ResponseEntity.ok(fwApiProcess.process(request));
    }

    @PostMapping("/cms/plans/all")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_ALL_PLANS, type = RequestType.INTERNAL)
    public ResponseEntity<List<PlanDTO>> getAllPlans() {
        return ResponseEntity.ok(fwApiProcess.process(null));
    }

    @PostMapping("/cms/plans/export")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_EXPORT_PLAN, type = RequestType.INTERNAL)
    public void exportPlan(SearchRequest<PlanDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, fwApiProcess.process(request), "thaca-plans-export-{{date}}.xlsx");
    }
}

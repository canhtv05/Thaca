package com.thaca.admin.controllers.external;

import com.thaca.admin.constants.ServiceMethod;
import com.thaca.common.dtos.internal.PlanDTO;
import com.thaca.common.dtos.internal.projection.PlanInfoPrj;
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
@RequestMapping("/admin/plans")
@RequiredArgsConstructor
public class PlanController {

    private final FwApiProcess process;

    @PostMapping("/search")
    @FwRequest(name = ServiceMethod.ADMIN_SEARCH_PLANS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<PlanDTO>> searchPlans(SearchRequest<PlanDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    @PostMapping("/all")
    @FwRequest(name = ServiceMethod.ADMIN_GET_ALL_PLANS, type = RequestType.PROTECTED)
    public ResponseEntity<List<PlanInfoPrj>> getAllPlans() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/get")
    @FwRequest(name = ServiceMethod.ADMIN_GET_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> getPlan(PlanDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/create")
    @FwRequest(name = ServiceMethod.ADMIN_CREATE_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> createPlan(PlanDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/update")
    @FwRequest(name = ServiceMethod.ADMIN_UPDATE_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<PlanDTO> updatePlan(PlanDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/lock-unlock")
    @FwRequest(name = ServiceMethod.ADMIN_LOCK_UNLOCK_PLAN, type = RequestType.PROTECTED)
    public ResponseEntity<Void> lockUnlockPlan(PlanDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/export")
    @FwRequest(name = ServiceMethod.ADMIN_EXPORT_PLAN, type = RequestType.PROTECTED)
    public void exportPlans(SearchRequest<PlanDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, process.process(request), "thaca-plans-{{date}}.xlsx");
    }
}

package com.thaca.cms.controllers.internal;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.TenantDTO;
import com.thaca.common.dtos.internal.projection.TenantInfoPrj;
import com.thaca.framework.core.annotations.FwRequest;
import com.thaca.framework.core.enums.RequestType;
import com.thaca.framework.core.services.FwApiProcess;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalTenantController {

    private final FwApiProcess process;

    @PostMapping("/cms/tenants/get")
    @FwRequest(name = ServiceMethod.CMS_GET_TENANT, type = RequestType.INTERNAL)
    public ResponseEntity<TenantDTO> getTenant(TenantDTO request) {
        return ResponseEntity.ok(process.process(request));
    }

    @PostMapping("/cms/tenants/all")
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_TENANTS, type = RequestType.INTERNAL)
    public ResponseEntity<List<TenantInfoPrj>> getAllTenants() {
        return ResponseEntity.ok(process.process(null));
    }
}

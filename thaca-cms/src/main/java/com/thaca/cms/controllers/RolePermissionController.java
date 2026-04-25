package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
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
@RequestMapping("/cms")
@RequiredArgsConstructor
public class RolePermissionController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/roles/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<RoleDTO>> searchRoles(SearchRequest<RoleDTO> search) {
        return ResponseEntity.ok(fwApiProcess.process(search));
    }

    @PostMapping("/permissions/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_PERMISSIONS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<PermissionDTO>> searchPermissions(SearchRequest<PermissionDTO> loginReq) {
        return ResponseEntity.ok(fwApiProcess.process(loginReq));
    }
}

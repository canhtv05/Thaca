package com.thaca.auth.controllers.internal;

import com.thaca.common.constants.InternalMethod;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.search.SearchRequest;
import com.thaca.common.dtos.search.SearchResponse;
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
public class InternalRolePermissionController {

    private final FwApiProcess process;

    @PostMapping("/cms/roles/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_ROLES, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<RoleDTO>> searchRoles(SearchRequest<RoleDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    @PostMapping("/cms/roles/all")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_ALL_ROLES, type = RequestType.INTERNAL)
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(process.process(null));
    }

    @PostMapping("/cms/permissions/search")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_SEARCH_PERMISSIONS, type = RequestType.INTERNAL)
    public ResponseEntity<SearchResponse<PermissionDTO>> searchPermissions(SearchRequest<PermissionDTO> criteria) {
        return ResponseEntity.ok(process.process(criteria));
    }

    @PostMapping("/cms/permissions/all")
    @FwRequest(name = InternalMethod.INTERNAL_CMS_GET_ALL_PERMISSIONS, type = RequestType.INTERNAL)
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(process.process(null));
    }
}

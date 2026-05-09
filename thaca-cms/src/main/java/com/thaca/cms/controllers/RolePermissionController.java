package com.thaca.cms.controllers;

import com.thaca.cms.constants.ServiceMethod;
import com.thaca.common.dtos.internal.PermissionDTO;
import com.thaca.common.dtos.internal.RoleDTO;
import com.thaca.common.dtos.internal.req.RoleCodesReq;
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
@RequestMapping("/cms")
@RequiredArgsConstructor
public class RolePermissionController {

    private final FwApiProcess fwApiProcess;

    @PostMapping("/roles/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<RoleDTO>> searchRoles(SearchRequest<RoleDTO> search) {
        return ResponseEntity.ok(fwApiProcess.process(search));
    }

    @PostMapping("/roles/all")
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(fwApiProcess.process(null));
    }

    @PostMapping("/permissions/search")
    @FwRequest(name = ServiceMethod.CMS_SEARCH_PERMISSIONS, type = RequestType.PROTECTED)
    public ResponseEntity<SearchResponse<PermissionDTO>> searchPermissions(SearchRequest<PermissionDTO> loginReq) {
        return ResponseEntity.ok(fwApiProcess.process(loginReq));
    }

    @PostMapping("/permissions/all")
    @FwRequest(name = ServiceMethod.CMS_GET_ALL_PERMISSIONS, type = RequestType.PROTECTED)
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(fwApiProcess.process(null));
    }

    @PostMapping("/permissions/by-roles")
    @FwRequest(name = ServiceMethod.CMS_GET_PERMISSIONS_BY_ROLES, type = RequestType.PROTECTED)
    public ResponseEntity<List<PermissionDTO>> getPermissionsByRoles(RoleCodesReq request) {
        return ResponseEntity.ok(fwApiProcess.process(request));
    }

    @PostMapping("/roles/export")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_EXPORT_ROLES, type = RequestType.PROTECTED)
    public void exportRoles(SearchRequest<RoleDTO> request, HttpServletResponse response) throws IOException {
        CommonUtils.writeExcelResponse(response, fwApiProcess.process(request), "thaca-roles-export-{{date}}.xlsx");
    }

    @PostMapping("/permissions/export")
    @FwSecurity(isSuperAdmin = true)
    @FwRequest(name = ServiceMethod.CMS_EXPORT_PERMISSIONS, type = RequestType.PROTECTED)
    public void exportPermissions(SearchRequest<PermissionDTO> request, HttpServletResponse response)
        throws IOException {
        CommonUtils.writeExcelResponse(
            response,
            fwApiProcess.process(request),
            "thaca-permissions-export-{{date}}.xlsx"
        );
    }
}
